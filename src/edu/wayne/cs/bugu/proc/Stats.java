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

import android.util.SparseArray;

public class Stats {
	
	public final SystemCPU mSystemCPU = new SystemCPU();
	public final SparseArray<PidStat> mPidStats= new SparseArray<PidStat>();
	
	public PidStat getPidStat(int pid){
		PidStat pStat = mPidStats.get(pid, null);
		if(pStat == null){
			pStat = new PidStat(pid);
			mPidStats.append(pid, pStat);//`append` is better than `put` here
		}
		
		return pStat;
	}
	
	public class SystemCPU{
		public long mBaseUserTime = 0;
		public long mBaseSysTime = 0;
		public long mBaseNiceTime = 0;
		public long mBaseIdleTime = 0;
		public long[] mBaseCpuSpeedTime = new long[32];
		public long[] mBaseCpuSpeedSteps = new long[32];
		//relative time
		public int mRelUserTime;
		public int mRelSysTime;
		public int mRelIdleTime;
		public long[] mRelCpuSpeedTimes = new long[32];
		
		public int mCpuSpeedStepTimes = 0;		
				
		public void update(long[] data){
			if(data == null || data.length < 4)
				return;
			// total user time = user time + nice time
			mRelUserTime = (int)(data[0] - mBaseUserTime) + (int)(data[1] -mBaseNiceTime);
			mRelSysTime = (int)(data[0] - mBaseSysTime);
			mRelIdleTime = (int)(data[0] - mBaseSysTime);
			
			mBaseUserTime = data[0];
			mBaseNiceTime = data[1];
			mBaseSysTime = data[2];
			mBaseIdleTime = data[3];
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
}
