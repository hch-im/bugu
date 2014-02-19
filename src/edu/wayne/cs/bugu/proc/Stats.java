/*
 *   Copyright (C) 2014, Mobile and Internet Systems Laboratory.
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
package edu.wayne.cs.bugu.proc;

import java.io.FileWriter;
import java.io.IOException;

import com.android.internal.telephony.PhoneConstants;

import edu.wayne.cs.bugu.Constants;

import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.util.Log;
import android.util.SparseArray;

public class Stats {
	public long mBaseTime = 0;
	public final SystemStat mSysStat = new SystemStat();
	public final SparseArray<PidStat> mPidStats= new SparseArray<PidStat>();
	public final SparseArray<UidStat> mUidStats= new SparseArray<UidStat>();
	
	public long mRelTime; //10 ms
	
	public PidStat getPidStat(int pid){
		PidStat pStat = mPidStats.get(pid, null);
		if(pStat == null){
			pStat = new PidStat(pid);
			mPidStats.append(pid, pStat);//`append` is better than `put` here
		}
		
		return pStat;
	}
	
	public UidStat getUidStat(int uid){
		UidStat uStat = mUidStats.get(uid, null);
		if(uStat == null){
			uStat = new UidStat(uid);
			mUidStats.append(uid, uStat);//`append` is better than `put` here
		}
		
		return uStat;
	}
	/**
	 * Must be invoked after stats system information.
	 */
	public void updateTime(){
		long currentTime = SystemClock.elapsedRealtime() / 10;//convert to 10ms
		mRelTime = currentTime - mBaseTime;
		mBaseTime = currentTime;
		//post update
		mSysStat.postUpdateScreenBrightness(mRelTime);
	}
	
	//system stats
	public class SystemStat{
		public long mBaseUserTime = 0;
		public long mBaseSysTime = 0;
		public long mBaseCPUTime = 0;
		public long mBaseScreenOffTime = 0;
		
		public long[] mBaseCpuSpeedTime = new long[32];
		public long[] mBaseCpuSpeedSteps = new long[32];
		//relative time in 10 milli sconds
		//system cpu usage = (user + sys + irq + softirq)/(user + sys + io + irq + softirq + idle)		
		public long mRelUserTime;
		public long mRelSysTime;
		public long mRelCPUTime;
		
		public long[] mRelCpuSpeedTimes = new long[32];		
		public int mCpuSpeedStepTimes = 0;		
		public int mCurCPUFrequency;
		public double cpuUtilization;
		public long mSpeedStepTotalTime;
		
		public long mRelScreenOffTime;
		public static final int SCREEN_BRIGHTNESS_BINS = 51; //bin size is 5
		private long[] mScreenBrightnessBinTimes = new long[SCREEN_BRIGHTNESS_BINS];
		public int mRelScreenBrightness;//0 means screen is off, 1-255 means screen is on
		//phone
		public boolean mPhoneOn = false;
		public int mSignalStrengthBin = -1;
		public int mPhoneServiceState = -1;
		public int mPhoneSimState = -1;
		//wifi
		public boolean mWifiOn = false;
		
		public boolean updateCPUTime(long[] data){
			if(data == null || data.length < 7)
				return false;
			
			long usr = data[0] + data[1];
			long sys = data[2] + data[5] + data[6];
			long total = usr + sys + data[3] + data[4];
			
//			Log.i("Bugu", "time " + mRelTime + " - " + usr + " " + sys + " " + total);			
			
			// total user time = user time + nice time
			mRelUserTime = usr - mBaseUserTime;
			mRelSysTime = sys - mBaseSysTime;
			if(mRelSysTime < 0) mRelSysTime = 0;
			long delta = total - mBaseCPUTime;
			if(delta < 0 || Math.abs(delta) > mRelTime * 5){
				//keep last one
			}else{
				mRelCPUTime = delta;
			}
			
			//user time
			mBaseUserTime = usr;
			mBaseSysTime = sys;
			mBaseCPUTime = total;

			cpuUtilization = 1.0 * (mRelUserTime + mRelSysTime) / mRelCPUTime;
//			Log.i("Bugu", "utilization " + cpuUtilization * 100 + " " + mCurCPUFrequency/1000 + " " + mRelUserTime + " " + mRelSysTime + " " + mRelCPUTime);
			return true;
		}
		
		public void updateScreenBrightness(int brightness){
			mRelScreenBrightness = brightness;
		}
		
		protected void postUpdateScreenBrightness(long relTime){
			if(mRelScreenBrightness == 0){//screen off
				mRelScreenOffTime = relTime;
				mBaseScreenOffTime += relTime;
			}else{
				int bin = (mRelScreenBrightness - 1) / 5;
				mScreenBrightnessBinTimes[bin] += relTime;
			}
		}
		
		public void updatePhoneServiceState(int state, int simState){
			mPhoneServiceState = state;
			mPhoneSimState = simState;
//			boolean scanning = false;
			
			if(state == ServiceState.STATE_POWER_OFF) {
	            mSignalStrengthBin = -1;
			}			
			else if (state == ServiceState.STATE_OUT_OF_SERVICE) {
//	            scanning = true;
	            mSignalStrengthBin = SignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN;	            
			}
		}
		
		public void updateSignalStrengthChange(int bin){
			mSignalStrengthBin = bin;
		}
		
		public void updatePhoneState(PhoneConstants.State state){
			if(state == PhoneConstants.State.IDLE){
				mPhoneOn = false;
	    		  //TODO add timer
			}else{
				mPhoneOn = true;
			}
		}
		
		public void updateWifiState(int wifiState){
            if (wifiState == WifiManager.WIFI_STATE_ENABLED ||
            		wifiState == WifiManager.WIFI_AP_STATE_ENABLED) {
            	mWifiOn = true;
            }else if(wifiState == WifiManager.WIFI_STATE_DISABLED || 
            		wifiState == WifiManager.WIFI_AP_STATE_DISABLED){
            	mWifiOn = false;
            }
		}
		
		public double cpuUtilization(){
			return cpuUtilization;
		}
		
		public int indexOfFrequency(int frequency){
			int index = -1;
			for(int i = 0; i < mCpuSpeedStepTimes; i++){
				if(mBaseCpuSpeedSteps[i] == frequency){
					index = i;
					break;
				}
			}
			
			return index;
		}
	}
	
	//process stats
	public class PidStat{
		public int pid;
		public int uid = -1;
		public String state;
		public long mBaseUserTime = 0;
		public long mBaseSysTime = 0;
		public long mBaseMinorFaults = 0;
		public long mBaseMajorFaults = 0;
		//relative
		//process cpu usage = (cpu time) / (system total cpu time)
		public int mRelCPUTime;
		public int mRelPageFaults;
		
		public PidStat(int pid){
			this.pid = pid;
		}
		
		public void update(String[] strData, long[] longData){
			if(strData == null || longData == null || strData.length < 5 || longData.length < 5)
				return;
			
			state = strData[0];
			mRelPageFaults = (int)(longData[1] - mBaseMinorFaults) + (int)(longData[2] - mBaseMajorFaults);
			mRelCPUTime = (int)(longData[3] - mBaseUserTime) + (int)(longData[4] - mBaseSysTime);			
			
			mBaseMinorFaults = longData[1];
			mBaseMajorFaults = longData[2];
			mBaseUserTime = longData[3];
			mBaseSysTime = longData[4];
		}
	}

	//application stats
	public class UidStat{
		public int uid;
		public String name = null;
		
		public UidStat(int uid){
			this.uid = uid;
		}
	}
	
	public void dump(FileWriter fw){
		StringBuffer msg = new StringBuffer("-----------------------------------\r\n");
		msg.append("interval: ").append(mRelTime).append(" 10 ms\r\n");
		msg.append("(user + sys) time: ").append(mSysStat.mRelSysTime + mSysStat.mRelUserTime).append(" jiffies(10ms)\r\n");
		msg.append("frequency: ").append(mSysStat.mCurCPUFrequency).append(" mhz\r\n");
//		long speedTime = 0;
//		for(int i = 0; i < mSysStat.mCpuSpeedStepTimes; i++){
//			speedTime += mSysStat.mRelCpuSpeedTimes[i];
//			msg.append("step time").append(i).append(":").append(mSysStat.mRelCpuSpeedTimes[i]).append("\r\n");
//		}
//		msg.append("speed step time: ").append(speedTime).append(" speed 0:(").append(mSysStat.mRelCpuSpeedTimes[0]).append(") jiffies(10ms)\r\n");		
		msg.append("cpu utilization: ").append(mSysStat.cpuUtilization()).append(" \r\n");			
		
//		long pidtimes = 0;		
//		for(int i = 0; i < mPidStats.size(); i++){
//			pidtimes += mPidStats.valueAt(i).mRelCPUTime;
//		}
//		msg.append("pids cpu time: ").append(pidtimes).append(" jiffies(10ms)\r\n");
		//the summation of pids cpu time is a little larger than (user + sys) time.
		//the summation of speed step time is close to interval
		if(fw != null){
			try{
				fw.write(msg.toString());
			}catch(IOException ioe){}
		}else
			Log.i(Constants.APP_TAG, msg.toString());
	}
}
