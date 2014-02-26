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

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.device.BasePowerProfile;
import edu.wayne.cs.bugu.proc.Stats;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
/**
 * @author hchen
 */
public class PowerProfilingService extends Service{
	private final IBinder mBinder = new LocalBinder();
    private boolean state = false;
	private int period=1000;
	private final Stats stats = new Stats();

    private Handler powerHandler = new Handler();
    private Runnable   	powerPeriodicTask = new Runnable() {
        public void run() {
            if(!state)
            	return;
            
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);            
            update();
            powerHandler.postDelayed(powerPeriodicTask, period);
        }
    };     
	private FileWriter writer = null;
    private ArrayList<DevicePowerInfo> devPowerHistory = null;
    
    public boolean isMonitoring()
    {
        return state;
    }
    
    public DevicePowerInfo currentDevicePower(){
    	return stats.curDevicePower;
    }
    
    public Stats getStats(){
    	return stats;
    }
    
    /**
     * Reset everything before start a new stat.
     * @param fw
     * @param p
     */
    public void startMonitor(FileWriter fw, int p, Activity activity)
    {
        writer = fw;
        period = p;

        if(stats.powerProfile == null)
        {
            stats.powerProfile = BasePowerProfile.getPowerProfileOfDevice(activity);
        }	            

		stats.updateTime();
		devPowerHistory = new ArrayList<DevicePowerInfo>();        
		powerHandler.postDelayed(powerPeriodicTask, period);   
        state = true;
    }
    
    /**
     * Stop the power profiling service.
     */
    public void stopMonitor()
    {        
        state = false;
        for(DevicePowerInfo dpi : devPowerHistory){
        	try{
        		dpi.writePower(writer);
        	}catch(Exception ex){ex.printStackTrace();}
        }
        
        devPowerHistory = null;
    }
    
	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {		
		/*
		 * Register system events of devices. 
		 */
		IntentFilter filter = new IntentFilter();
		//phone
		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
		//wifi
		filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		
		registerReceiver(receiver, filter);

		stats.init();
	}
	
	@Override
	public void onDestroy() {        
        powerHandler.removeCallbacks(powerPeriodicTask);
	    unregisterReceiver(receiver);
	    super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
	/**
	 * Update system and power information.
	 */
	public void update(){
		if(devPowerHistory == null)//stopped
			return;
		
		stats.updateTime();
		stats.updateStates();		
		stats.calculatePower();
		
		stats.dump(null);
//		curDevicePower.dump();
		
		devPowerHistory.add(stats.curDevicePower);
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
   
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
    	   @Override
    	   public void onReceive(Context context, Intent intent) {
    	      String action = intent.getAction();
//call state
//CALL_STATE_IDLE = 0;
//CALL_STATE_RINGING = 1;  Ringing. A new call arrived and is ringing or waiting.
//CALL_STATE_OFFHOOK = 2;  Off-hook. At least one call exists that is dialing, 
//active, or on hold, and no calls are ringing or waiting.   	      
    	      if(action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
    	        //phone call on/off
    	    	  PhoneConstants.State state = getPhoneState(intent);
    	    	  stats.mSysStat.radio.updatePhoneState(state);
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Phone state: " + state);
    	      }
//radio service state
//STATE_IN_SERVICE = 0;  the phone is registered with an operator.
//STATE_OUT_OF_SERVICE = 1; Phone is not registered with any operator, the phone
//can be currently searching a new operator to register to, or not
//searching to registration at all, or registration is denied, or radio
//signal is not available.
//STATE_EMERGENCY_ONLY = 2; The phone is registered and locked.  Only emergency numbers are allowed.
//STATE_POWER_OFF = 3; Radio of telephony is explicitly powered off.
//---------------------------------------------
//SIM_STATE_UNKNOWN = 0;  Signifies that the SIM is in transition between states.
//SIM_STATE_ABSENT = 1;  no SIM card is available in the device
//SIM_STATE_PIN_REQUIRED = 2;
//SIM_STATE_PUK_REQUIRED = 3;
//SIM_STATE_NETWORK_LOCKED = 4;
//SIM_STATE_READY = 5;    	      
    	      else if(action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)){
    	    	  Bundle data = intent.getExtras();
    	    	  ServiceState ss = ServiceState.newFromBundle(data);
    	    	  int state = ss.getState();
    	          int simState = TelephonyManager.getDefault().getSimState();
    	          stats.mSysStat.radio.updatePhoneServiceState(state, simState);
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Phone service state: " + state + " sim state:" + simState);
    	      }else if(action.equals(TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED)){
    	          Bundle data = intent.getExtras();
    	          SignalStrength ss = SignalStrength.newFromBundle(data);
    	          stats.mSysStat.radio.updateSignalStrengthChange(ss.getLevel());    	    	  
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "signal strength: " + ss.getLevel());
    	      }
//3G LTE : defined in TelephoneManager
//DATA_UNKNOWN = -1;
//DATA_DISCONNECTED = 0;  Disconnected. IP traffic not available.
//DATA_CONNECTING = 1;  Currently setting up a data connection.
//DATA_CONNECTED = 2;  Connected. IP traffic should be available.
//DATA_SUSPENDED = 3; Suspended. The connection is up, but IP traffic is temporarily unavailable.
    	      else if(action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)){
                  String iface = intent.getStringExtra(PhoneConstants.DATA_IFACE_NAME_KEY);
                  PhoneConstants.DataState state = getMobileDataState(intent);
                  //TODO update to stats
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "data connection state: " + state + " iface: " + iface);
    	      }
//wifi events
//WIFI_STATE_DISABLING = 0;  Wi-Fi is currently being disabled.
//WIFI_STATE_DISABLED = 1;
//WIFI_STATE_ENABLING = 2; Wi-Fi is currently being enabled.
//WIFI_STATE_ENABLED = 3;
//WIFI_STATE_UNKNOWN = 4;    	      
    	      else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
    	    	  int state = (Integer) intent.getExtra(WifiManager.EXTRA_WIFI_STATE);
    	    	  stats.mSysStat.wifi.updateWifiState(state);
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Wifi state: " + state);
    	      }else if(action.equals(WifiManager.WIFI_AP_STATE_CHANGED_ACTION)){
    	    	  int state = (Integer) intent.getExtra(WifiManager.EXTRA_WIFI_AP_STATE);
    	    	  stats.mSysStat.wifi.updateWifiState(state);    	    	  
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Wifi AP state: " + state);    	    	  
    	      }
    	      
    	      //TODO get wifi running state
    	   }
    };   
   
    
    public class LocalBinder extends Binder {
    	public PowerProfilingService getService() {
          return PowerProfilingService.this;
        }
    }
    
    private PhoneConstants.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(PhoneConstants.STATE_KEY);
        if (str != null) {
        	return Enum.valueOf(PhoneConstants.DataState.class, str);
        }
        
        return PhoneConstants.DataState.DISCONNECTED;
    }
    
    private PhoneConstants.State getPhoneState(Intent intent) {
        String str = intent.getStringExtra(PhoneConstants.STATE_KEY);
        if (str != null) {
        	return Enum.valueOf(PhoneConstants.State.class, str);
        }
        
        return PhoneConstants.State.IDLE;
    }
}
