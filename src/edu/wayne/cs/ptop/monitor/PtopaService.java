/*
 *   Copyright (C) 2012, Mobile and Internet Systems Laboratory.
 *   All rights reserved.
 *
 *   Authors: Hui Chen (hchen229@gmail.com)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.
 */
package edu.wayne.cs.ptop.monitor;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.android.internal.app.IBatteryStats;

import edu.wayne.cs.ptop.db.ConfigDAO;
import edu.wayne.cs.ptop.db.model.Config;
import edu.wayne.cs.ptop.display.PtopaActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.BatteryStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.BatteryStats.Uid;
import android.util.Log;
import android.util.SparseArray;
/**
 * @author hchen
 */
public class PtopaService extends Service{
    private boolean state = false;
	private int period=10000;
	private static PtopaActivity mainActivity;

    private Handler powerHandler = new Handler();
    private Runnable powerPeriodicTask = new Runnable() {
        public void run() {
            update();
            if(state)
                powerHandler.postDelayed(powerPeriodicTask, period);
        }
    };    
	private FileWriter writer = null;
    private int statsType = BatteryStats.STATS_SINCE_CHARGED;
	private BatteryStatsImpl batteryStats;
	private PowerProfile powerProfile = null;
    private IBatteryStats batteryInfo;
    private PowerModel powerModel = null;    
    private ArrayList<HashMap<Integer, AppPowerInfo>> appPower= new ArrayList<HashMap<Integer, AppPowerInfo>>();
    private ArrayList<DevicePowerInfo> devicePower = new ArrayList<DevicePowerInfo>();
    private HashMap<Integer, AppPowerInfo> curAppPower = null;
    private DevicePowerInfo curDevicePower = null;
    
    private ConfigDAO cdao = new ConfigDAO();
    private long wifiTotalData = 0;
    private double wifiAvgPower = 0;
    
    public boolean currentState()
    {
        return state;
    }
    
    public void reset(FileWriter fw, int p)
    {
        writer = fw;
        period = p;
        appPower.clear();
        devicePower.clear();
        powerHandler.postDelayed(powerPeriodicTask, period);   
        state = true;
    }
    
    public void stopMonitor()
    {        
        state = false;
    }
    
	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();        		
	}

	@Override
	public void onDestroy() {        
        powerHandler.removeCallbacks(powerPeriodicTask);
	    super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void setMainActivity(PtopaActivity activity) {
		mainActivity = activity;
	}
	
	public void update(){
        if(powerProfile == null)
        {
            powerProfile = new PowerProfile(mainActivity);
            powerModel = new PowerModel(powerProfile, statsType);
            powerModel.printBasicPower(writer);
        }	    
		//load new battery stats
	    loadStatsData();
		long realTime = SystemClock.elapsedRealtime();
        long uSecTime = batteryStats.computeBatteryRealtime(realTime * 1000, statsType);   		
		curAppPower = new HashMap<Integer, AppPowerInfo>();
		curDevicePower = new DevicePowerInfo();
		powerModel.setDevicePowerInfo(curDevicePower);
		appPower.add(0, curAppPower);
		devicePower.add(0, curDevicePower);
        estimateAppPower(uSecTime);
        estimateDevicePower(uSecTime);
        writePower(realTime);
        //only record the last 1000 records
        if(appPower.size() > 1500)
        {
           //remove old  data
           appPower.subList(999, appPower.size()).clear();
        }
	}
	
    private void estimateAppPower(long uSecTime) {
        SensorManager sensorManager = (SensorManager)mainActivity.getSystemService(Context.SENSOR_SERVICE);
        double wifiPowerPerKB = getWifiAverageDataCost(uSecTime); 
//        log("wifiPowerPerKB: " + wifiPowerPerKB + "\r\n");
        
        SparseArray<? extends Uid> uidStats = batteryStats.getUidStats();
        final int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            Uid u = uidStats.valueAt(iu);
            AppPowerInfo appInfo = new AppPowerInfo();
            appInfo.id = u.getUid();
            curAppPower.put(u.getUid(), appInfo);
            powerModel.setAppPowerInfo(appInfo);
            //estimate power
            powerModel.appCPUPower(u.getProcessStats());
            powerModel.appWakelockPower(u.getWakelockStats(), uSecTime);
            //did not distinguish 3G and wifi
            powerModel.appNetworkPower(u.getTcpBytesReceived(statsType), 
            						   u.getTcpBytesSent(statsType), 
            						   u.getWifiRunningTime(uSecTime, statsType),
            						   wifiPowerPerKB);
            powerModel.appSensorPower(u.getSensorStats(), uSecTime, sensorManager);
            powerModel.appIOPower(u.getPidStats());
            powerModel.appMediaPower(u.getAudioTurnedOnTime(uSecTime, statsType), 
                                     u.getVideoTurnedOnTime(uSecTime, statsType));
            //total power
            if (u.getUid() == Process.WIFI_UID) {
                double appPower = appInfo.totalPower();
                curDevicePower.wifiPower += appPower;
            } else if (u.getUid() == Process.BLUETOOTH_GID) {
                double appPower = appInfo.totalPower();
                curDevicePower.buletoothPower += appPower;
            }
        }
            
    }
    
    private void estimateDevicePower(long uSecTime) {
        long phoneOnTime = batteryStats.getPhoneOnTime(uSecTime, statsType);
        powerModel.phonePower(phoneOnTime);
        powerModel.screenPower(batteryStats, uSecTime);
        powerModel.radioPower(batteryStats, uSecTime);
        
        long wifiOnTime = batteryStats.getWifiOnTime(uSecTime, statsType);
        long wifiRunTime = batteryStats.getGlobalWifiRunningTime(uSecTime, statsType);
        wifiRunTime -= getAppWifiRunTime();        
        if (wifiRunTime < 0) wifiRunTime = 0;        
        powerModel.wifiPower(wifiOnTime, wifiRunTime);
        
        long idleTime = (uSecTime - batteryStats.getScreenOnTime(uSecTime, statsType));
        powerModel.idlePower(idleTime);
        
        long btOnTime = batteryStats.getBluetoothOnTime(uSecTime, statsType);
        int btPingCount = batteryStats.getBluetoothPingCount();
        powerModel.bluetoothPower(btOnTime, btPingCount);
    }
    
    private void loadStatsData() {
    	if(batteryInfo == null)
    	    batteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
	    if(batteryInfo == null)
	    {
	    	Log.w("pTopA: ", "load : failed to create batteryInfo service.");
	    }
        try {
            byte[] data = batteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            batteryStats = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
            batteryStats.distributeWorkLocked(statsType);
        } catch (RemoteException e) {}
   
    }
    
//    private double getMobileAverageDataCost(long uSecTime) {
//        final double threeGPower = powerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE);
//        final long mobileData = batteryStats.getMobileTcpBytesReceived(statsType) +
//        		batteryStats.getMobileTcpBytesSent(statsType);
//        final long radioDataUptimeMs = batteryStats.getRadioDataUptime() / 1000;
//
//        double powerPerKB = (threeGPower * radioDataUptimeMs * PowerModel.voltage / 1000) / (mobileData / 1024); // mJ/kb
//        return powerPerKB;
//    }

    private double getWifiAverageDataCost(long uSecTime) {        
        if(wifiAvgPower == 0){        
            //load from database
            Config c1 = cdao.get("wifiTotalData");
            if(c1 != null) wifiTotalData = Long.valueOf(c1.getValue());
            Config c2 = cdao.get("wifiAvgPower");
            if(c2 != null) wifiAvgPower = Double.valueOf(c2.getValue());
            
            final double wifiActivePower = powerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE);
            //data received and sent from last unplug
            final long mobileData = batteryStats.getMobileTcpBytesReceived(statsType) +
                    batteryStats.getMobileTcpBytesSent(statsType);
            final long wifiData = batteryStats.getTotalTcpBytesReceived(statsType) +
                    batteryStats.getTotalTcpBytesSent(statsType) - mobileData;
            
            long wifiRunTime = batteryStats.getGlobalWifiRunningTime(uSecTime, statsType) / 1000; //ms
//            log("DEV--- mobileData:" + mobileData + " wifiData: " + wifiData + " wifiRunTime: " + wifiRunTime);        
            double powerPerKB;
            if(wifiData == 0){
                return 0;
            }
            else
            {
                powerPerKB = (wifiActivePower * wifiRunTime * PowerModel.voltage / 1000) / (wifiData / 1024); //mJ/kb
                //save data
                if(wifiData > wifiTotalData)
                {
                    if(c1 == null)
                    {
                        c1 = new Config();
                        c1.setName("wifiTotalData");
                        c1.setValue(wifiData+"");
                        cdao.insert(c1);
                    }
                    else
                    {
                        c1.setValue(wifiData+"");
                        cdao.update(c1);
                    }
                    
                    if(c2 == null)
                    {
                        c2 = new Config();
                        c2.setName("wifiAvgPower");
                        c2.setValue(powerPerKB+"");
                        cdao.insert(c2);
                    }
                    else
                    {
                        c2.setValue(powerPerKB+"");
                        cdao.update(c2);
                    }                    
                }
                
                return powerPerKB;
            }
            
        }        
        else
        {
            return wifiAvgPower;
        }
    }
    
    private long getAppWifiRunTime()
    {
    	long appWifiRunTime = 0;
    	Collection<AppPowerInfo> info = curAppPower.values();
    	for (Iterator<AppPowerInfo> it = info.iterator(); it.hasNext();)
    	{
    		AppPowerInfo pInfo = it.next();
    		appWifiRunTime += pInfo.wifiRunTime;
    	}
    	
    	return appWifiRunTime;
    }
    
    private void writePower(long uSec)
    {
    	if(writer == null)
    		return;
        try{
            writer.write("TIME: " + uSec + "\r\n");
            Collection<AppPowerInfo> info = curAppPower.values();
            for (Iterator<AppPowerInfo> it = info.iterator(); it.hasNext();)
            {
                AppPowerInfo pInfo = it.next();
                pInfo.write(writer);
            }
            curDevicePower.writePower(writer);            
            Log.i("pTopA: ", "done write power " + uSec);
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        
    }
   
//    private void log(String info)
//    {
//        if(writer == null)
//            return;
//        try{            
//            writer.write("LOG: " + info);
//            writer.flush();
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }        
//    }
   
}
