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

import static android.os.Process.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Vector;

import android.os.Process;
import android.os.StrictMode;

public class ProcFileParser {
	// /proc/stat
	private static final String PROC_STAT = "/proc/stat";
    private static final int[] PROC_STAT_FORMAT = new int[] {
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 1: user time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 2: nice time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 3: sys time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 4: idle time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 5: iowait time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 6: irq time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 7: softirq time        
    };
    
    private final long[] mProcStatLong = new long[7];
    //TODO should we add cutime cstime 
    // /proc/pid/stat
    private static final int[] PROC_PID_STAT_FORMAT = new int[] {
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,    
        PROC_SPACE_TERM|PROC_OUT_STRING,				// 2: state
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 9: minor faults
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 11: major faults
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 13: utime
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 14: stime
    };
    private final long[] mProcPidStatLong = new long[5];
    private final String[] mProcPidStatString = new String[5];
    // /proc/pid/status
    private final String[] PROC_PID_STATUS_LABELS = {"Uid:"};
    private final long[] mProcPidStatusLong = new long[1];
    // /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state
    private final String SYS_CPU_SPEED_STEPS = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    private final String SYS_CPU_FREQUENCY = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    // proc directory
    private final String PROC_DIR ="/proc";
    private final FilenameFilter pidNameFilter = new FilenameFilter(){    	
		@Override
		public boolean accept(File dir, String filename) {
    		try{
    			Integer.parseInt(filename);
    		}catch(NumberFormatException nfe){
    			return false;
    		}
    		
    		return true;
		}
    	
    };
    // screen 
    private final String SYS_LEDS_BRIGHTNESS = "/sys/class/leds/lcd-backlight/brightness";
//    private NativeLib natLib = new NativeLib();
    
	public boolean parseProcStat(Stats.SystemStat cpuStat){
		//When using tickless kernel, idle/iowait accounting are not doing.
		//So the value might get outdated. 		
		if(Process.readProcFile(PROC_STAT, PROC_STAT_FORMAT, null, mProcStatLong, null)){
			if(cpuStat.updateCPUTime(mProcStatLong))
				return true;
		}
		
		return false;
	}
	
	public void parseProcPidStat(int pid, Stats.PidStat pidStat){
		if(Process.readProcFile("/proc/" + pid + "/stat", 
				PROC_PID_STAT_FORMAT, mProcPidStatString, mProcPidStatLong, null)){
			pidStat.update(mProcPidStatString, mProcPidStatLong);
		}
	}	
	
	public void parseProcPidStatus(int pid, Stats.PidStat pidStat){
		mProcPidStatusLong[0] = -1;
        Process.readProcLines("/proc/" + pid + "/status", PROC_PID_STATUS_LABELS, mProcPidStatusLong);
        if(mProcPidStatusLong[0] != -1)
        	pidStat.uid = (int) mProcPidStatusLong[0];
	}
	
	public void parseCurrentCPUFrequency(Stats.SystemStat st){
		String str = readFile(SYS_CPU_FREQUENCY, 8);
		if(str == null)
			return;
		
		int freq = Integer.valueOf(str.split("\n")[0]);
		st.mCurCPUFrequency = freq;
	}
	
	public void parseCPUSpeedTimes(Stats.SystemStat cpuStat, boolean timeOnly){
		String str = readFile(SYS_CPU_SPEED_STEPS, 512);
		if(str == null)
			return;
		
		String[] vals;
		int index = 0;
		long time;
		long total = 0;
		
		for(String token : str.split("\n")){
			if(str.trim().length() == 0) continue;
			try{
				vals = token.split(" ");
				if(!timeOnly){
					cpuStat.mBaseCpuSpeedSteps[index] = Long.valueOf(vals[0]);
				}
				
				time = Long.valueOf(vals[1]);
				cpuStat.mRelCpuSpeedTimes[index] = time - cpuStat.mBaseCpuSpeedTime[index];
				cpuStat.mBaseCpuSpeedTime[index] = time;
				total += cpuStat.mRelCpuSpeedTimes[index];
			}catch (NumberFormatException nfe){
				nfe.printStackTrace();
			}finally{
				index++;
			}
		}
		cpuStat.mCpuSpeedStepTimes = index;
		cpuStat.mSpeedStepTotalTime = total;
	}
	
	public void parseScreenBrightness(Stats.SystemStat st){
		String str = readFile(SYS_LEDS_BRIGHTNESS, 8);
		if(str == null)
			return;
		
		int brightness = Integer.valueOf(str.split("\n")[0]);
		st.updateScreenBrightness(brightness);
	}
	
	public Vector<Integer> getAllPids(){
		Vector<Integer> pids = new Vector<Integer>();
		File f = new File(PROC_DIR);
		String[] pidStrs = f.list(pidNameFilter);
		
		for(String pid : pidStrs)
			pids.add(Integer.valueOf(pid));
		
		return pids;
	}
	
    private byte[] mBuffer = new byte[4096];
    
    private String readFile(String file, int length) {
    	if(length > 4096) length = 4096;
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            int len = is.read(mBuffer, 0, length);
            is.close();

            if (len > 0) {
                return new String(mBuffer, 0, len);
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
            StrictMode.setThreadPolicy(savedPolicy);
        }
        
        return null;
    } 
}
