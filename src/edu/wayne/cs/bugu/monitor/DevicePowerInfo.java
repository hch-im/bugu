package edu.wayne.cs.bugu.monitor;

import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

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
	
	public void dump(){
		StringBuffer msg = new StringBuffer();
		msg.append("cpu power:").append(cpuPower).append("\r\n");
		msg.append("screen power:").append(screenPower).append("\r\n");		
		Log.i("Bugu", msg.toString());
	}
}
