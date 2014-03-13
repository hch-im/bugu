package edu.wayne.cs.bugu.monitor;

import java.io.FileWriter;
import java.io.IOException;

import edu.wayne.cs.bugu.Constants;

import android.util.Log;

public class DevicePowerInfo {
	public long time;
	public double cpuPower = 0;
	public double wifiPower = 0;
	public double gpsPower = 0;
	public double buletoothPower = 0;
	public double sensorPower = 0;
	public double ioPower = 0;
	public double screenPower = 0;
	public double phonePower = 0;
	public double radioPower = 0;
	public double idlePower = 0;
	public double dspPower = 0;
    public double batteryPower = 0;
    
	public DevicePowerInfo(long t){
		time = t;
	}
	
	public void writePower(FileWriter io)throws IOException
	{
	        io.write("DEV: " + time + "," + totalPower() + "," +
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
	                dspPower + "," +
	                batteryPower + "," +
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
		Log.i(Constants.APP_TAG, msg.toString());
	}
}
