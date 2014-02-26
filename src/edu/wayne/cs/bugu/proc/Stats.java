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
import java.util.Vector;

import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.device.BasePowerProfile;
import edu.wayne.cs.bugu.monitor.AppPowerInfo;
import edu.wayne.cs.bugu.monitor.DevicePowerInfo;
import edu.wayne.cs.bugu.proc.component.Pid;
import edu.wayne.cs.bugu.proc.component.System;
import edu.wayne.cs.bugu.proc.component.Uid;

import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

public class Stats {
	public BasePowerProfile powerProfile = null;
	public long mBaseTime = 0;
	public System mSysStat = null;
	public SparseArray<Pid> mPidStats;
	public SparseArray<Uid> mUidStats;	
	public long mRelTime; //10 ms
    public SparseArray<AppPowerInfo> curAppPower = null;
    public DevicePowerInfo curDevicePower = null;    
    
	public Stats(){
		mSysStat = new System();
		mPidStats= new SparseArray<Pid>();
		mUidStats= new SparseArray<Uid>();
	}
	
	public void init(){
		mSysStat.init();
	}
	
	public void updateStates(){
		mSysStat.updateState();
		
		Vector<Integer> pids = ProcFileParser.getAllPids();
		Pid pidStat = null;
		for(Integer pid : pids){
			pidStat = getPid(pid);
			if(pidStat.uid == -1){//new process
				pidStat.parseProcPidStatus();
			}
			pidStat.parseProcPidStat();
		}
	}
	
	public Pid getPid(int pid){
		Pid pStat = mPidStats.get(pid, null);
		if(pStat == null){
			pStat = new Pid(pid);
			mPidStats.append(pid, pStat);//`append` is better than `put` here
		}
		
		return pStat;
	}
	
	public Uid getUid(int uid){
		Uid uStat = mUidStats.get(uid, null);
		if(uStat == null){
			uStat = new Uid(uid);
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
		mSysStat.display.postUpdateScreenBrightness(mRelTime);
	}
	
	public void calculatePower(){
		curAppPower = new SparseArray<AppPowerInfo>();
		curDevicePower = new DevicePowerInfo(mBaseTime);	
		mSysStat.calculatePower(this);
		//TODO calculate pid and uid power
		
		AppPowerInfo api;
		Pid ps;
		for(int i = 0; i < mPidStats.size(); i++){
			ps = mPidStats.valueAt(i);
			api = curAppPower.get(ps.uid);
			if(api == null){
				api = new AppPowerInfo();
				api.id = ps.uid;
				curAppPower.append(ps.uid, api);
			}
			
			ps.calculatePower(this);
		}		
	}
	
	public void dump(FileWriter fw){
		StringBuffer msg = new StringBuffer();
		mSysStat.dump(msg);
		
		if(fw != null){
			try{
				fw.write(msg.toString());
			}catch(IOException ioe){}
		}else
			Log.i(Constants.APP_TAG, msg.toString());
	}
}
