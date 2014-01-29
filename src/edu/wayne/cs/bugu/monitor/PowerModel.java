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
package edu.wayne.cs.bugu.monitor;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;

import android.hardware.SensorManager;
import android.telephony.SignalStrength;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.os.PowerProfile;

import edu.wayne.cs.bugu.proc.Stats;
import edu.wayne.cs.bugu.util.NativeLib;
// the unit of power is mA
public class PowerModel {
	private PowerProfile powerProfile;
	private NativeLib natLib = new NativeLib();
    private int speedSteps;
    private double[] speedStepAvgPower;
    private AppPowerInfo appPowerInfo;
    private DevicePowerInfo devicePowerInfo;
    public static final double IO_READ_POWER_PER_BYTE = 0.092; // mJ/kb
    public static final double IO_WRITE_POWER_PER_BYTE = 0.564; //mJ/kb
    private long pageSize = 0;
    
	public PowerModel(PowerProfile profile)
	{
		powerProfile = profile;
		//Get CPU speed step info
	    speedSteps = powerProfile.getNumSpeedSteps();
	    speedStepAvgPower = new double[speedSteps];
	    for (int p = 0; p < speedSteps; p++) {
	    	speedStepAvgPower[p] = powerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p);// mA
	    }   
	    //on some platform (such as galaxy s4), this may in reverse order
	    Arrays.sort(speedStepAvgPower);
	    pageSize = natLib.getPageSize();
	}
	
	public void calculatePower(Stats st, DevicePowerInfo devPower, SparseArray<AppPowerInfo> appPower){
		devicePowerInfo = devPower;
		// calculate device power
		cpuPower(st);
		screenPower(st);
		// calculate app power
		AppPowerInfo api;
		Stats.PidStat ps;
		for(int i = 0; i < st.mPidStats.size(); i++){
			ps = st.mPidStats.valueAt(i);
			api = appPower.get(ps.uid);
			if(api == null){
				api = new AppPowerInfo();
				api.id = ps.uid;
				appPower.append(ps.uid, api);
			}
			
			calculateProcessPower(ps, st.mSysStat, devPower, api);
		}
	}
	
	/**
	 * calculate the power of each process and add the power to the related application.
	 * @param ps
	 * @param dpi
	 * @param api
	 */
	private void calculateProcessPower(Stats.PidStat ps, Stats.SystemStat ss, DevicePowerInfo dpi, AppPowerInfo api){
		//cpu
		api.cpuPower += dpi.cpuPower * (ps.mRelCPUTime * 1.0/ss.relCPUTime());
		//TODO how to handle application wakelock power
	}
	
	private void cpuPower(Stats st){
		double eng = 0;
		for(int i = 0; i < speedSteps; i++){
			eng += (st.mSysStat.mRelCpuSpeedTimes[i] * speedStepAvgPower[i]);
		}
		
		devicePowerInfo.cpuPower = (eng/st.mRelTime);
		//TODO base cpu power?
	}
	
	private void screenPower(Stats st){
		if(st.mSysStat.mRelScreenBrightness == 0){
			devicePowerInfo.screenPower = 0;
			return;
		}else{		
			double screenOnPower = powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
			double screenFullPower = powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
			int bin = (st.mSysStat.mRelScreenBrightness - 1) / 5;
			devicePowerInfo.screenPower = (screenFullPower - screenOnPower) * bin
	                			/ (Stats.SystemStat.SCREEN_BRIGHTNESS_BINS - 1)
	                			+ screenOnPower;		
		}
	}
	
//TODO modify the following code.
	
	public void setAppPowerInfo(AppPowerInfo appPowerInfo)
	{
		this.appPowerInfo = appPowerInfo;
	}
	
//	public void appCPUPower(Map<String, ? extends BatteryStats.Uid.Proc> processStats)
//	{
//        //each Uid may includes multiple process
//        if (processStats.size() > 0) {
//            for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent
//                    : processStats.entrySet()) {
// //           	Log.i("pTopA: ", (i++) + " CPU Power -- " + ent.getKey());
//            	if(appPowerInfo.name == null) appPowerInfo.name = ent.getKey();
//                processCPUPower(ent.getValue());
//            }
//        }
//        
//        appPowerInfo.cpuPower /= 1000;	
//        devicePowerInfo.cpuPower += appPowerInfo.cpuPower;
//        devicePowerInfo.cpuTime += appPowerInfo.cpuTime;
//        devicePowerInfo.foregroundTime += appPowerInfo.foregroundTime; 
//        devicePowerInfo.speedStepTime += appPowerInfo.speedStepTime;
//	}

//    private void processCPUPower(Uid.Proc ps)
//    {
//        long userTime = ps.getUserTime(statsType); // in 1/100 sec
//        long systemTime = ps.getSystemTime(statsType);// in 1/100 sec
//        
//        appPowerInfo.foregroundTime += ps.getForegroundTime(statsType) / 1000; // microseconds to milliseconds
//        appPowerInfo.cpuTime += (userTime + systemTime) * 10; // convert to millis
//        int totalTimeAtSpeeds = 0;
//        double processPower = 0;        
//        // Get the total time
//        for (int step = 0; step < speedSteps; step++) {
//            cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(step, statsType);//microseconds
//            totalTimeAtSpeeds += cpuSpeedStepTimes[step];
//            processPower = cpuSpeedStepTimes[step] * speedStepAvgPower[step];
//        }
//
////        // Then compute the ratio of time spent at each speed
////        if(totalTimeAtSpeeds > 0)
////        {
////            for (int step = 0; step < speedSteps; step++) {
////                double ratio = (double) cpuSpeedStepTimes[step] * 1.0 / totalTimeAtSpeeds;
////                appPowerInfo.cpuPower += ratio * appPowerInfo.cpuTime * speedStepAvgPower[step]; // milli joule * 1000
////            }
////        }
//        appPowerInfo.cpuPower += processPower;
//        appPowerInfo.speedStepTime += totalTimeAtSpeeds;
//    }
	
//	public void appIOPower(SparseArray<? extends Uid.Pid> pidStats)
//	{
////		Log.i("pTopA: ", "Pid amount -- " + pidStats.size());
//        //each Uid may includes multiple process
//        if (pidStats.size() > 0) {
//            for (int j=0; j<pidStats.size(); j++) {
//                try{
//                	Log.i("pTopA: ", "entry key -- " + pidStats.keyAt(j));
//                	processIOPower(readProcessIOInfo(Integer.valueOf(pidStats.keyAt(j))));
//                }catch(Exception ex){}
//            }
//        }
//        
//        devicePowerInfo.ioPower += appPowerInfo.ioPower;
//	}
	
//	private void processIOPower(long[] iobytes)
//	{
//	    long write = iobytes[1] - iobytes[2];
//	    if(write < 0) write = 0;
//	    appPowerInfo.ioPower += ((IO_READ_POWER_PER_BYTE * iobytes[0] + IO_WRITE_POWER_PER_BYTE * write)) / 1024; //mJ
//	    appPowerInfo.bytesRead += iobytes[0];
//	    appPowerInfo.bytesWrite += write;
//	}
	
//	public void appWakelockPower(Map<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats, long uSecTime)
//	{
//        // Process wake lock usage
//        for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry
//                : wakelockStats.entrySet()) {
//            Uid.Wakelock wakelock = wakelockEntry.getValue();
//            BatteryStats.Timer timer = wakelock.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
//            if (timer != null) {
//            	appPowerInfo.wakelockTime += timer.getTotalTimeLocked(uSecTime, statsType);
//            }
//        }
//        
//        appPowerInfo.wakelockTime /= 1000; // convert to millis
//        appPowerInfo.wakelockPower = (appPowerInfo.wakelockTime * 
//        			powerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;	//milli joule	
////        devicePowerInfo.cpuPower += appPowerInfo.wakelockPower;
//	}
	
//	public void appNetworkPower(long received, long sent, long wifiRunTime, double wifiPowerPerKB)
//	{
//	    Log.i("PowerModel",  appPowerInfo.name + "Received: " + received + " sent: " + sent+ " wifiRunTime: " + wifiRunTime + " wifiPowerPerKB:" + wifiPowerPerKB);
//		appPowerInfo.tcpBytesSent = sent;
//		appPowerInfo.tcpBytesReceived = received;
//		appPowerInfo.dataTransRecvPower = (received + sent) * wifiPowerPerKB / 1024; 
//
//		appPowerInfo.wifiRunTime = wifiRunTime /1000.0;
//		appPowerInfo.wifiRunPower =  (wifiRunTime * powerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;	
//
//		devicePowerInfo.wifiPower += (appPowerInfo.dataTransRecvPower + appPowerInfo.wifiRunPower);
//	}
	
//	public void appSensorPower(Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats, long uSecTime, SensorManager sensorManager)
//	{
//        for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry
//                : sensorStats.entrySet()) {
//            Uid.Sensor sensor = sensorEntry.getValue();
//            int sensorType = sensor.getHandle();
//            BatteryStats.Timer timer = sensor.getSensorTime();
//            double sensorTime = timer.getTotalTimeLocked(uSecTime, statsType) / 1000.0; //ms
//            double multiplier = 0;
//            switch (sensorType) {
//                case Uid.Sensor.GPS:
//                    multiplier = powerProfile.getAveragePower(PowerProfile.POWER_GPS_ON);
//                    appPowerInfo.gpsTime = sensorTime;
//                    appPowerInfo.gpsPower += (multiplier * sensorTime) / 1000;
//                    devicePowerInfo.gpsPower += appPowerInfo.gpsPower;
//                    break;
//                default:
//                    android.hardware.Sensor sensorData =
//                            sensorManager.getDefaultSensor(sensorType);
//                    if (sensorData != null) {
//                        multiplier = sensorData.getPower();
//                        appPowerInfo.otherSensorPower += (multiplier * sensorTime) / 1000;
//                        devicePowerInfo.sensorPower += appPowerInfo.otherSensorPower;
//                    }
//            }
//        }		
//	}
	
	public void appMediaPower(long audioOnTime, long videoOnTime)
	{
	    appPowerInfo.audioOnTime = audioOnTime / 1000.0f;
	    appPowerInfo.videoOnTime = videoOnTime / 1000.0f;
	    
	    appPowerInfo.audioPower = powerProfile.getAveragePower(PowerProfile.POWER_AUDIO) * audioOnTime / 1000.0f/1000;
        appPowerInfo.videoPower = powerProfile.getAveragePower(PowerProfile.POWER_VIDEO) * videoOnTime / 1000.0f/1000;        
        
        devicePowerInfo.dspPower = appPowerInfo.audioPower + appPowerInfo.videoPower;
	}
	
//	public void phonePower(long phoneOnTime)
//	{
//        devicePowerInfo.phonePower = powerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
//                              * phoneOnTime /1000 / 1000; //mJ
//        devicePowerInfo.phoneOnTime = phoneOnTime /1000.0;
//	}
	
//	public void screenPower(BatteryStatsImpl batteryStats, long uSecTime)
//	{
//        double screenOnTimeMs = batteryStats.getScreenOnTime(uSecTime, statsType) / 1000.0;
//        devicePowerInfo.screenPower += screenOnTimeMs * powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON)  / 1000;
//        devicePowerInfo.screenOnTime = screenOnTimeMs;
//        
//        double screenFullPower = powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
//        //screen power model
//        for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
//            double screenBinPower = screenFullPower * (i + 0.5f)
//                    / BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
//            double brightnessTime = batteryStats.getScreenBrightnessTime(i, uSecTime, statsType) / 1000.0;
//            devicePowerInfo.screenPower += (screenBinPower * brightnessTime  / 1000);
//        }
//	}
	
//	public void radioPower(BatteryStatsImpl batteryStats, long uSecTime)
//	{
//	    //android 3.1
////        int BINS = BatteryStats.NUM_SIGNAL_STRENGTH_BINS;
//        //android 4
//	    double signalTime = 0;
//        int BINS = SignalStrength.NUM_SIGNAL_STRENGTH_BINS;
//        for (int i = 0; i < BINS; i++) {
//            double strengthTimeMs = batteryStats.getPhoneSignalStrengthTime(i, uSecTime, statsType) / 1000.0;
//            signalTime += strengthTimeMs;
//            devicePowerInfo.radioPower += (strengthTimeMs / 1000
//                    * powerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i));
//        }
//        
//        double scanningTimeMs = batteryStats.getPhoneSignalScanningTime(uSecTime, statsType) / 1000.0;
//        devicePowerInfo.radioPower += (scanningTimeMs * powerProfile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING) / 1000);	
//        devicePowerInfo.signalTime = signalTime;
//        devicePowerInfo.scanTime = scanningTimeMs;
//	}
	
//	public void wifiPower(long wifiOnTime, long wifiRunTime)
//	{
////        log("DEV--- wifiOnTime: " + wifiOnTime+ " wifiRunTime: " + wifiRunTime);	    
//	    devicePowerInfo.wifiOnTime = wifiOnTime / 1000.0;//ms
//	    devicePowerInfo.wifiRunTime = wifiRunTime / 1000.0;
//        devicePowerInfo.wifiPower += (wifiOnTime /1000.0 * powerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)
//                                    + wifiRunTime /1000.0 * powerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE)) / 1000;//mJ	
//	}
//	
//	public void idlePower(long idleTime)
//	{
////        log("DEV--- idleTime: " + idleTime);	    
//	    devicePowerInfo.idleTime = idleTime / 1000.0;//ms
//        devicePowerInfo.idlePower = (idleTime / 1000.0 * powerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE))
//               / 1000;		
//	}
//	
//	public void bluetoothPower(long btOnTime, int btPingCount)
//	{	    
//	    devicePowerInfo.bluetoothOnTime = btOnTime / 1000.0;//ms
//        devicePowerInfo.buletoothPower += (btOnTime /1000.0 * powerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON) / 1000);
//        devicePowerInfo.buletoothPower += (btPingCount
//                * powerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 1000;		
//	}
	
    private long[] readProcessIOInfo(int pid)
    {
//another method to read a system file
/*
Process proc = Runtime.getRuntime().exec(new String[] {"cat", "/proc/net/netstat"});
BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
String line = null;  

while ((line = reader.readLine()) != null)  
{
  // parse netstat output here
} 
 */
        String filename = "/proc/" + pid + "/io";
        long[] iobytes = {0, 0, 0};
        try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String str, datastr;
            while( (str = br.readLine()) != null)
            {
                if(str.startsWith("read_bytes:"))
                {
                    datastr = str.replace("read_bytes:", "").trim();
                    iobytes[0] = Long.valueOf(datastr);
                }
                else if(str.startsWith("write_bytes:"))
                {
                    datastr = str.replace("write_bytes:", "").trim();
                    iobytes[1] = Long.valueOf(datastr);
                }
                else if(str.startsWith("cancelled_write_bytes:"))
                {
                    datastr = str.replace("cancelled_write_bytes:", "").trim();
                    iobytes[2] = Long.valueOf(datastr);                    
                    break;
                }
            }
            return iobytes;
        }catch(Exception ex)
        {
            Log.i("pTopA", "Failed to read io info of pid:" + pid);
            return iobytes;
        }
    }	
    
    private FileWriter wr;
    public void printBasicPower(FileWriter wr)
    {
        if(wr == null) return; this.wr = wr;
        try{
            //cpu power
            for (int p = 0; p < speedSteps; p++) {
                wr.write("BASEPOWER: STEP-" + p + " " + speedStepAvgPower[p] + "\r\n"); 
            } 
            wr.write("BASEPOWER: WakeLock " + powerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE) + "\r\n");
            wr.write("BASEPOWER: WifiOn " + powerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON) + "\r\n");
            wr.write("BASEPOWER: WifiActive " + powerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) + "\r\n");            
            wr.write("BASEPOWER: GpsOn " + powerProfile.getAveragePower(PowerProfile.POWER_GPS_ON)  + "\r\n");
            wr.write("BASEPOWER: RadioActive " + powerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) + "\r\n");
            wr.write("BASEPOWER: ScreenOn " + powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON)  + "\r\n");
            wr.write("BASEPOWER: ScreenFull " + powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL)  + "\r\n");
            //radio power
//            int BINS = BatteryStats.NUM_SIGNAL_STRENGTH_BINS;            
            int BINS = SignalStrength.NUM_SIGNAL_STRENGTH_BINS;
            for (int i = 0; i < BINS; i++) {
                wr.write("BASEPOWER: RadioOn-" + i + " " + powerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i) + "\r\n");                
            }
            wr.write("BASEPOWER: RadioScan " + powerProfile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING)  + "\r\n");            
            wr.write("BASEPOWER: CPUIdle " + powerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE)  + "\r\n");
            wr.write("BASEPOWER: BluetoothOn " + powerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)  + "\r\n");
            wr.write("BASEPOWER: BluetoothAtCmd " + powerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)  + "\r\n");
        }catch(Exception ex)
        {}
    } 
    
//    private Stats getStatsOfPid(int pid, ProcessStats processStats){
//    	int n = processStats.countStats();
//    	Stats st = null;
//    	for(int i = 0; i < n; i++){
//    		st = processStats.getStats(i);
//    		if(st.pid == pid)
//    		{
//    			return st;
//    		}
//    	}
//    	
//    	return st;
//    }    
}
