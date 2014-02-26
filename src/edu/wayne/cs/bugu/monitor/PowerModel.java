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
// the unit of power is mA
public class PowerModel {
	
//TODO modify the following code.
	
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
	
//	public void appMediaPower(long audioOnTime, long videoOnTime)
//	{
//	    appPowerInfo.audioOnTime = audioOnTime / 1000.0f;
//	    appPowerInfo.videoOnTime = videoOnTime / 1000.0f;
//	    
//	    appPowerInfo.audioPower = powerProfile.getAveragePower(PowerProfile.POWER_AUDIO) * audioOnTime / 1000.0f/1000;
//        appPowerInfo.videoPower = powerProfile.getAveragePower(PowerProfile.POWER_VIDEO) * videoOnTime / 1000.0f/1000;        
//        
//        devicePowerInfo.dspPower = appPowerInfo.audioPower + appPowerInfo.videoPower;
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
}
