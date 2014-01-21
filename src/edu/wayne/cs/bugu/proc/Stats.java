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
	public final SystemCPU mSystemCPU = new SystemCPU();
	public final SparseArray<PidStat> mPidStats= new SparseArray<PidStat>();
	
	public long mRelTime;
	
	public PidStat getPidStat(int pid){
		PidStat pStat = mPidStats.get(pid, null);
		if(pStat == null){
			pStat = new PidStat(pid);
			mPidStats.append(pid, pStat);//`append` is better than `put` here
		}
		
		return pStat;
	}
	
	public void updateTime(){
		long currentTime = SystemClock.elapsedRealtime();
		mRelTime = currentTime - mBaseTime;
		mBaseTime = currentTime;
	}
	
	public class SystemCPU{
		public long mBaseUserTime = 0;
		public long mBaseSysTime = 0;
		public long mBaseNiceTime = 0;
		public long mBaseIdleTime = 0;
		public long mBaseIOWaitTime = 0;
		public long mBaseIRQTime = 0;
		public long mBaseSoftIRQTime = 0;
		
		public long[] mBaseCpuSpeedTime = new long[32];
		public long[] mBaseCpuSpeedSteps = new long[32];
		//relative time
		//system cpu usage = (user + sys + io + irq + softirq)/(user + sys + io + irq + softirq + idle)		
		public int mRelUserTime;
		public int mRelSysTime;
		public int mRelIdleTime;
		public int mRelIOWaitTime;
		public int mRelIRQTime;
		public int mRelSoftIRQTime;
		
		public long[] mRelCpuSpeedTimes = new long[32];
		
		public int mCpuSpeedStepTimes = 0;		
				
		public boolean update(long[] data){
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
			
			return true;
		}
		
		/**
		 * cpu time = user+system+nice+idle+iowait+irq+softirq
		 * @return
		 */
		public long relCPUTime(){
			return mRelUserTime + mRelSysTime + mRelIdleTime + mRelIOWaitTime + mRelIRQTime + mRelSoftIRQTime;
		}
	}
	
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
	
	public void dump(){
		StringBuffer msg = new StringBuffer();
		msg.append("interval: ").append(mRelTime).append(" ms\r\n");
		msg.append("cpu time: ").append(mSystemCPU.relCPUTime()).append(" jiffies(10ms)\r\n");			
		msg.append("(user + sys) time: ").append(mSystemCPU.mRelSysTime + mSystemCPU.mRelUserTime).append(" jiffies(10ms)\r\n");
		msg.append("idle time: ").append(mSystemCPU.mRelIdleTime).append(" base:(").append(mSystemCPU.mBaseIdleTime).append(") jiffies(10ms)\r\n");	
		msg.append("io wait time: ").append(mSystemCPU.mRelIOWaitTime).append(" jiffies(10ms)\r\n");	
		msg.append("irq time: ").append(mSystemCPU.mRelIRQTime).append(" jiffies(10ms)\r\n");	
		msg.append("softirq time: ").append(mSystemCPU.mRelSoftIRQTime).append(" base:(").append(mSystemCPU.mBaseSoftIRQTime).append(") jiffies(10ms)\r\n");	
		
		long speedTime = 0;
		for(int i = 0; i < mSystemCPU.mCpuSpeedStepTimes; i++){
			speedTime += mSystemCPU.mRelCpuSpeedTimes[i];
		}
		msg.append("speed step time: ").append(speedTime).append(" speed 0:(").append(mSystemCPU.mRelCpuSpeedTimes[0]).append(") jiffies(10ms)\r\n");		
		
		long pidtimes = 0;		
		for(int i = 0; i < mPidStats.size(); i++){
			pidtimes += mPidStats.valueAt(i).mRelCPUTime;
		}
		msg.append("pids cpu time: ").append(pidtimes).append(" jiffies(10ms)\r\n");
		//the summation of pids cpu time is a little larger than (user + sys) time.
		//the summation of speed step time is close to interval
		
		msg.append("\r\n");
		Log.i("Bugu", msg.toString());
	}
}
