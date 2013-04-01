package edu.wayne.cs.ptop.monitor;

import java.io.FileWriter;
import java.io.IOException;

public class DevicePowerInfo {
	double cpuPower = 0;
	double wifiPower = 0;
	double gpsPower = 0;
	double buletoothPower = 0;
	double sensorPower = 0;
	double ioPower = 0;
	double screenPower = 0;
	double phonePower = 0;
	double radioPower = 0;
	double idlePower = 0;
	double dspPower = 0;
	
	double phoneOnTime = 0;
	double screenOnTime = 0;
	double signalTime = 0;
	double scanTime = 0;
	double wifiOnTime = 0;
	double wifiRunTime = 0;
	double idleTime = 0;
	double bluetoothOnTime = 0;
    double dspOnTime = 0;
    
	public void writePower(FileWriter io)throws IOException
	{
	        io.write("DEV: " + totalPower() + "," +
	                cpuPower + "," +
	                wifiPower + "," +
	                gpsPower + "," +
	                buletoothPower + "," +
	                sensorPower + "," +
	                ioPower + "," +
	                screenPower + "," +
	                phonePower + "," +
	                radioPower + "," +
	                idlePower + "," +
	                dspPower +
	                "\r\n"
	                  );

            io.write("DEVINFO: " +
                    phoneOnTime + "," +
                    screenOnTime + "," +
                    signalTime + "," +
                    scanTime + "," +
                    wifiOnTime + "," +
                    wifiRunTime + "," +
                    idleTime + "," +
                    bluetoothOnTime + "," + 
                    dspOnTime + 
                    "\r\n"
                      );	        
	}
	
	public double totalPower()
	{
	    return 
	     cpuPower +
	     wifiPower +
	     gpsPower +
	     buletoothPower +
	     sensorPower +
	     ioPower +
	     screenPower +
	     phonePower +
	     radioPower +
	     idlePower +
	     dspPower;
	}
}
