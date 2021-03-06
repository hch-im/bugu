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
import android.net.wifi.SupplicantState;
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
	private int period = 500;
	private final Stats stats = new Stats();
	private FileWriter writer = null;
    private ArrayList<DevicePowerInfo> devPowerHistory = null;
    private ArrayList<String> logs = null;
    
    private final Handler powerHandler = new Handler();
    private final Runnable powerPeriodicTask = new Runnable() {
        public void run() {
            if(!state)
            	return;
            
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);            
            update();
            powerHandler.postDelayed(powerPeriodicTask, period);
        }
    };     
    
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
		logs = new ArrayList<String>();
		powerHandler.postDelayed(powerPeriodicTask, period);   
        state = true;
    }
    
    /**
     * Stop the power profiling service.
     */
    public void stopMonitor()
    {        
        state = false;
    	try{
	        for(String str : logs){
        		writer.write(str);
	        }
	        writer.write("\r\n");
	        for(DevicePowerInfo dpi : devPowerHistory){
	        		dpi.writePower(writer);
	        }
    	}catch(Exception ex){ex.printStackTrace();}
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
		//network interface
		
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
//		stats.dump(null);	
		stats.dump(logs);
		devPowerHistory.add(stats.curDevicePower);
	}
       
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
    	    	  stats.sys.radio.updatePhoneState(state);
    	          if(Constants.DEBUG_RADIO)
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
    	          stats.sys.radio.updatePhoneServiceState(state, simState);
    	          if(Constants.DEBUG_RADIO)
    	        	  Log.i(Constants.APP_TAG, "Phone service state: " + state + " sim state:" + simState);
    	      }
// SIGNAL_STRENGTH_GREAT = 4;
// SIGNAL_STRENGTH_GOOD = 3;    	      
// SIGNAL_STRENGTH_MODERATE = 2;
// SIGNAL_STRENGTH_POOR = 1;    	          	      
// SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;   in scanning state
    	      else if(action.equals(TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED)){
    	          Bundle data = intent.getExtras();
    	          SignalStrength ss = SignalStrength.newFromBundle(data);
    	          stats.sys.radio.updateSignalStrengthChange(ss.getLevel());    	    	  
    	          if(Constants.DEBUG_RADIO)
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
                  //determine if this is 3g or 4g network
                  if(state == PhoneConstants.DataState.CONNECTED 
	          				&& stats.sys.radio.mNetworkClass == TelephonyManager.NETWORK_CLASS_UNKNOWN){
                	  TelephonyManager mTelephonyManager = (TelephonyManager)
	          		                                context.getSystemService(Context.TELEPHONY_SERVICE);
                	  int type = mTelephonyManager.getNetworkType();
                	  int clas = TelephonyManager.getNetworkClass(type);
                	  String opt = mTelephonyManager.getNetworkOperatorName();
                	  stats.sys.radio.updateNetworkInfo(type, clas, opt);
                  }
                  stats.sys.radio.updateDataConnectionState(state, iface);

    	          if(Constants.DEBUG_RADIO)
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
    	    	  stats.sys.wifi.updateWifiState(state,-1);
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Wifi state: " + state);
    	      }else if(action.equals(WifiManager.WIFI_AP_STATE_CHANGED_ACTION)){
    	    	  int state = (Integer) intent.getExtra(WifiManager.EXTRA_WIFI_AP_STATE);
    	    	  stats.sys.wifi.updateWifiState(state,-1);    	    	  
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Wifi AP state: " + state);    	    	  
    	      }else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
    	    	  int state = 3; //Constant: scan results available
    	    	  stats.sys.wifi.updateWifiState(-1,state);    	    	  
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Wifi Scan results available " + state);    	    	  
    	      }else if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
    	    	  SupplicantState supState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
    	    	  if (supState.equals(SupplicantState.SCANNING)){
    	    		  int state = 2;//Constant: Scan start
    	    		  stats.sys.wifi.updateWifiState(-1,state); 
    	    	  }
    	          if(Constants.DEBUG_EVENTS)
    	        	  Log.i(Constants.APP_TAG, "Wifi Supplicant state " + supState);    	    	  
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
