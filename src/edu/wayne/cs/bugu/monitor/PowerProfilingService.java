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
package edu.wayne.cs.bugu.monitor;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.android.internal.os.ProcessStats;
import com.android.internal.app.IBatteryStats;

import edu.wayne.cs.bugu.db.ConfigDAO;
import edu.wayne.cs.bugu.db.model.Config;
import edu.wayne.cs.bugu.display.BuguActivity;
import edu.wayne.cs.bugu.proc.ProcFileParser;
import edu.wayne.cs.bugu.proc.Stats;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.SparseArray;
/**
 * @author hchen
 */
public class PowerProfilingService extends Service{
    private boolean state = false;
	private int period=1000;
	private static BuguActivity mainActivity;
	private Stats stats = null;
	private ProcFileParser procParser = null;
	
    private Handler powerHandler = null;
    private Runnable powerPeriodicTask = null;    
	private FileWriter writer = null;
	private PowerProfile powerProfile = null;
    private PowerModel powerModel = null;    
    private SparseArray<AppPowerInfo> curAppPower = null;
    private DevicePowerInfo curDevicePower = null;    
    private ConfigDAO cdao = null;
    	
    public PowerProfilingService(BuguActivity activity){
    	mainActivity = activity;
    	stats = new Stats();
    	cdao = new ConfigDAO();
    	procParser = new ProcFileParser();
    	powerHandler = new Handler();
    	powerPeriodicTask = new Runnable() {
            public void run() {
                update();
                if(state)
                    powerHandler.postDelayed(powerPeriodicTask, period);
            }
        };    	
    }
    
    public boolean currentState()
    {
        return state;
    }
    
    /**
     * Reset everything before start a new stat.
     * @param fw
     * @param p
     */
    public void startMonitor(FileWriter fw, int p)
    {
        writer = fw;
        period = p;

        if(powerProfile == null)
        {
            powerProfile = new PowerProfile(mainActivity);
            powerModel = new PowerModel(powerProfile);
            powerModel.printBasicPower(writer);
        }	            
        // init system information
		procParser.parseCPUSpeedTimes(stats.mSysStat, false);
		stats.updateTime();
		
        powerHandler.postDelayed(powerPeriodicTask, period);   
        state = true;
    }
    
    /**
     * Stop the power profiling service.
     */
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

	/**
	 * Update system and power information.
	 */
	public void update(){
		//update system information
		procParser.parseProcStat(stats.mSysStat);
		procParser.parseCPUSpeedTimes(stats.mSysStat, true);
		procParser.parseScreenBrightness(stats.mSysStat);
		
		//update process information
		Vector<Integer> pids = procParser.getAllPids();
		Stats.PidStat pidStat = null;
		for(Integer pid : pids){
			pidStat = stats.getPidStat(pid);
			if(pidStat.uid == -1){//new process
				procParser.parseProcPidStatus(pid, pidStat);
			}
			procParser.parseProcPidStat(pid, pidStat);
		}
		
		stats.updateTime();
		//init the new objects for recording power information
		curAppPower = new SparseArray<AppPowerInfo>();
		curDevicePower = new DevicePowerInfo();
		powerModel.calculatePower(stats, curDevicePower, curAppPower);
		
//		stats.dump();
//		curDevicePower.dump();
		//TODO modify the following part of code
		//load new battery stats
//	    loadStatsData();
//		long realTime = SystemClock.elapsedRealtime();
//        long uSecTime = batteryStats.computeBatteryRealtime(realTime * 1000, statsType);  
//      
//        estimateAppPower(uSecTime);
//        estimateDevicePower(uSecTime);
        writePower(stats);
	}
	
//    private void estimateAppPower(long uSecTime) {
//        SensorManager sensorManager = (SensorManager)mainActivity.getSystemService(Context.SENSOR_SERVICE);
//        double wifiPowerPerKB = getWifiAverageDataCost(uSecTime); 
////        log("wifiPowerPerKB: " + wifiPowerPerKB + "\r\n");
//        
//        SparseArray<? extends Uid> uidStats = batteryStats.getUidStats();
//        final int NU = uidStats.size();
//        for (int iu = 0; iu < NU; iu++) {
//            Uid u = uidStats.valueAt(iu);
//            AppPowerInfo appInfo = curAppPower.get(u.getUid());
//            if(appInfo == null)
//            	appInfo = new AppPowerInfo();
//            appInfo.id = u.getUid();
//            
//            powerModel.setAppPowerInfo(appInfo);
//            powerModel.appWakelockPower(u.getWakelockStats(), uSecTime);
//            //did not distinguish 3G and wifi
//            powerModel.appNetworkPower(u.getTcpBytesReceived(statsType), 
//            						   u.getTcpBytesSent(statsType), 
//            						   u.getWifiRunningTime(uSecTime, statsType),
//            						   wifiPowerPerKB);
//            powerModel.appSensorPower(u.getSensorStats(), uSecTime, sensorManager);
//            powerModel.appIOPower(u.getPidStats());
//            powerModel.appMediaPower(u.getAudioTurnedOnTime(uSecTime, statsType), 
//                                     u.getVideoTurnedOnTime(uSecTime, statsType));
//            //total power
//            if (u.getUid() == Process.WIFI_UID) {
//                double appPower = appInfo.totalPower();
//                curDevicePower.wifiPower += appPower;
//            } else if (u.getUid() == Process.BLUETOOTH_GID) {
//                double appPower = appInfo.totalPower();
//                curDevicePower.buletoothPower += appPower;
//            }
//        }
//            
//    }
    
//    private void estimateDevicePower(long uSecTime) {
//        long phoneOnTime = batteryStats.getPhoneOnTime(uSecTime, statsType);
//        powerModel.phonePower(phoneOnTime);
//        powerModel.radioPower(batteryStats, uSecTime);
//        
//        long wifiOnTime = batteryStats.getWifiOnTime(uSecTime, statsType);
//        long wifiRunTime = batteryStats.getGlobalWifiRunningTime(uSecTime, statsType);
//        wifiRunTime -= getAppWifiRunTime();        
//        if (wifiRunTime < 0) wifiRunTime = 0;        
//        powerModel.wifiPower(wifiOnTime, wifiRunTime);
//        
//        long idleTime = (uSecTime - batteryStats.getScreenOnTime(uSecTime, statsType));
//        powerModel.idlePower(idleTime);
//        
//        long btOnTime = batteryStats.getBluetoothOnTime(uSecTime, statsType);
//        int btPingCount = batteryStats.getBluetoothPingCount();
//        powerModel.bluetoothPower(btOnTime, btPingCount);
//    }
    
//    private void loadStatsData() {
//    	if(batteryInfo == null)
//    	    batteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
//	    if(batteryInfo == null)
//	    {
//	    	Log.w("pTopA: ", "load : failed to create batteryInfo service.");
//	    }
//        try {
//            byte[] data = batteryInfo.getStatistics();
//            Parcel parcel = Parcel.obtain();
//            parcel.unmarshall(data, 0, data.length);
//            parcel.setDataPosition(0);
//            batteryStats = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
//            batteryStats.distributeWorkLocked(statsType);
//        } catch (RemoteException e) {}
//    }
    
//    private double getMobileAverageDataCost(long uSecTime) {
//        final double threeGPower = powerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE);
//        final long mobileData = batteryStats.getMobileTcpBytesReceived(statsType) +
//        		batteryStats.getMobileTcpBytesSent(statsType);
//        final long radioDataUptimeMs = batteryStats.getRadioDataUptime() / 1000;
//
//        double powerPerKB = (threeGPower * radioDataUptimeMs * PowerModel.voltage / 1000) / (mobileData / 1024); // mJ/kb
//        return powerPerKB;
//    }

//    private double getWifiAverageDataCost(long uSecTime) {        
//        if(wifiAvgPower == 0){        
//            //load from database
//            Config c1 = cdao.get("wifiTotalData");
//            if(c1 != null) wifiTotalData = Long.valueOf(c1.getValue());
//            Config c2 = cdao.get("wifiAvgPower");
//            if(c2 != null) wifiAvgPower = Double.valueOf(c2.getValue());
//            
//            final double wifiActivePower = powerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE);
//            //data received and sent from last unplug
//            final long mobileData = batteryStats.getMobileTcpBytesReceived(statsType) +
//                    batteryStats.getMobileTcpBytesSent(statsType);
//            final long wifiData = batteryStats.getTotalTcpBytesReceived(statsType) +
//                    batteryStats.getTotalTcpBytesSent(statsType) - mobileData;
//            
//            long wifiRunTime = batteryStats.getGlobalWifiRunningTime(uSecTime, statsType) / 1000; //ms
////            log("DEV--- mobileData:" + mobileData + " wifiData: " + wifiData + " wifiRunTime: " + wifiRunTime);        
//            double powerPerKB;
//            if(wifiData == 0){
//                return 0;
//            }
//            else
//            {
//                powerPerKB = (wifiActivePower * wifiRunTime / 1000) / (wifiData / 1024); //mJ/kb
//                //save data
//                if(wifiData > wifiTotalData)
//                {
//                    if(c1 == null)
//                    {
//                        c1 = new Config();
//                        c1.setName("wifiTotalData");
//                        c1.setValue(wifiData+"");
//                        cdao.insert(c1);
//                    }
//                    else
//                    {
//                        c1.setValue(wifiData+"");
//                        cdao.update(c1);
//                    }
//                    
//                    if(c2 == null)
//                    {
//                        c2 = new Config();
//                        c2.setName("wifiAvgPower");
//                        c2.setValue(powerPerKB+"");
//                        cdao.insert(c2);
//                    }
//                    else
//                    {
//                        c2.setValue(powerPerKB+"");
//                        cdao.update(c2);
//                    }                    
//                }
//                
//                return powerPerKB;
//            }
//            
//        }        
//        else
//        {
//            return wifiAvgPower;
//        }
//    }
    
//    private long getAppWifiRunTime()
//    {
//    	long appWifiRunTime = 0;
//    	for(int i = 0 ; i < curAppPower.size(); i++){
//    		AppPowerInfo pInfo = curAppPower.valueAt(i);
//    		appWifiRunTime += pInfo.wifiRunTime;    		
//    	}
//    	
//    	return appWifiRunTime;
//    }
    
    private void writePower(Stats st)
    {
    	if(writer == null)
    		return;
        try{
            writer.write("TIME: " + st.mBaseTime + "," + st.mRelTime + "\r\n");
        	for(int i = 0 ; i < curAppPower.size(); i++){
        		AppPowerInfo pInfo = curAppPower.valueAt(i);
                pInfo.write(writer);
            }
            curDevicePower.writePower(writer);            
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        
    }
   
}
