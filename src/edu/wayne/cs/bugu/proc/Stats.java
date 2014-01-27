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

import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

public class Stats {
	public long mBaseTime = 0;
	public final SystemStat mSysStat = new SystemStat();
	public final SparseArray<PidStat> mPidStats= new SparseArray<PidStat>();
	public final SparseArray<UidStat> mUidStats= new SparseArray<UidStat>();
	
	public long mRelTime; //miliseconds
	
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
		public long mBaseNiceTime = 0;
		public long mBaseIdleTime = 0;
		public long mBaseIOWaitTime = 0;
		public long mBaseIRQTime = 0;
		public long mBaseSoftIRQTime = 0;
		public long mBaseScreenOffTime = 0;
		
		public long[] mBaseCpuSpeedTime = new long[32];
		public long[] mBaseCpuSpeedSteps = new long[32];
		//relative time in 10 milli sconds
		//system cpu usage = (user + sys + io + irq + softirq)/(user + sys + io + irq + softirq + idle)		
		public int mRelUserTime;
		public int mRelSysTime;
		public int mRelIdleTime;
		public int mRelIOWaitTime;
		public int mRelIRQTime;
		public int mRelSoftIRQTime;
		public int mRelCPUTime;//the summation of all the previous times
		public long[] mRelCpuSpeedTimes = new long[32];		
		public int mCpuSpeedStepTimes = 0;		
		
		public long mRelScreenOffTime;
		public static final int SCREEN_BRIGHTNESS_BINS = 51; //bin size is 5
		private long[] mScreenBrightnessBinTimes = new long[SCREEN_BRIGHTNESS_BINS];
		public int mRelScreenBrightness;//0 means screen is off, 1-255 means screen is on
				
		public boolean updateCPUTime(long[] data){
			if(data == null || data.length < 4)
				return false;
			if(data[3] < mBaseIdleTime || data[4] < mBaseIOWaitTime)
				return false;
			// total user time = user time + nice time
			mRelUserTime = (int)(data[0] - mBaseUserTime) + (int)(data[1] -mBaseNiceTime);
			mRelSysTime = (int)(data[2] - mBaseSysTime);
			mRelIdleTime = (int)(data[3] - mBaseIdleTime);
			mRelIOWaitTime = (int)(data[4] - mBaseIOWaitTime);
			mRelIRQTime = (int)(data[5] - mBaseIRQTime);
			mRelSoftIRQTime = (int)(data[6] - mBaseSoftIRQTime);
			
			mBaseUserTime = data[0];
			mBaseNiceTime = data[1];
			mBaseSysTime = data[2];
			mBaseIdleTime = data[3];
			mBaseIOWaitTime = data[4];
			mBaseIRQTime = data[5];
			mBaseSoftIRQTime = data[6];			
			mRelCPUTime = mRelUserTime + mRelSysTime + mRelIdleTime + mRelIOWaitTime + mRelIRQTime + mRelSoftIRQTime;

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
		
		/**
		 * cpu time = user+system+nice+idle+iowait+irq+softirq
		 * @return
		 */
		public long relCPUTime(){
			return mRelCPUTime;
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
	
	public void dump(){
		StringBuffer msg = new StringBuffer();
		msg.append("interval: ").append(mRelTime).append(" 10 ms\r\n");
		msg.append("cpu time: ").append(mSysStat.relCPUTime()).append(" jiffies(10ms)\r\n");			
		msg.append("(user + sys) time: ").append(mSysStat.mRelSysTime + mSysStat.mRelUserTime).append(" jiffies(10ms)\r\n");
		msg.append("idle time: ").append(mSysStat.mRelIdleTime).append(" base:(").append(mSysStat.mBaseIdleTime).append(") jiffies(10ms)\r\n");	
		msg.append("io wait time: ").append(mSysStat.mRelIOWaitTime).append(" jiffies(10ms)\r\n");	
		msg.append("irq time: ").append(mSysStat.mRelIRQTime).append(" jiffies(10ms)\r\n");	
		msg.append("softirq time: ").append(mSysStat.mRelSoftIRQTime).append(" base:(").append(mSysStat.mBaseSoftIRQTime).append(") jiffies(10ms)\r\n");	
		
		long speedTime = 0;
		for(int i = 0; i < mSysStat.mCpuSpeedStepTimes; i++){
			speedTime += mSysStat.mRelCpuSpeedTimes[i];
			msg.append("step time").append(i).append(":").append(mSysStat.mRelCpuSpeedTimes[i]).append("\r\n");
		}
		msg.append("speed step time: ").append(speedTime).append(" speed 0:(").append(mSysStat.mRelCpuSpeedTimes[0]).append(") jiffies(10ms)\r\n");		
		
		long pidtimes = 0;		
		for(int i = 0; i < mPidStats.size(); i++){
			pidtimes += mPidStats.valueAt(i).mRelCPUTime;
		}
		msg.append("pids cpu time: ").append(pidtimes).append(" jiffies(10ms)\r\n");
		//the summation of pids cpu time is a little larger than (user + sys) time.
		//the summation of speed step time is close to interval
		Log.i("Bugu", msg.toString());
	}
}
