package edu.wayne.cs.ptop.monitor;

import java.io.FileWriter;
import java.io.IOException;

public class AppPowerInfo {
	int id = 0;
	String name = null;
	//milli joule
	double cpuPower = 0;
	double wakelockPower = 0;
	double dataTransRecvPower = 0;
	double wifiRunPower = 0;
	double gpsPower = 0;
	double otherSensorPower = 0;
	double ioPower = 0;
	double audioPower = 0;
	double videoPower = 0;
	//milli second
    double cpuTime = 0;
    double foregroundTime = 0;
    double wakelockTime = 0;
    double gpsTime = 0;	
    double tcpBytesReceived = 0;
    double tcpBytesSent = 0;    
    double wifiRunTime = 0;
    double bytesRead = 0;
    double bytesWrite = 0;        
    double audioOnTime = 0;
    double videoOnTime = 0;
    double speedStepTime = 0;
    
    public double totalPower()
    {
    	return cpuPower + wakelockPower + dataTransRecvPower + wifiRunPower + gpsPower + otherSensorPower + ioPower + audioPower + videoPower;
    }
    
    public void write(FileWriter io) throws IOException
    {
            io.write("APP: " +id + "," +
            		name + "," +
            		totalPower() + "," +
                    cpuPower + "," +
                    wakelockPower + "," +
                    dataTransRecvPower + "," +
                    wifiRunPower + "," +
                    gpsPower + "," +
                    otherSensorPower + "," +
                    ioPower + "," +
                    audioPower + "," +                    
                    videoPower +                                        
                    "\r\n"        
                       );
            io.write("APPINFO: " +id + "," +
            		name + "," +
            		cpuTime + "," +
                    foregroundTime + "," +
                    wakelockTime + "," +
                    gpsTime + "," +
                    tcpBytesReceived + "," +
                    tcpBytesSent + "," +
                    wifiRunTime + "," +
                    bytesRead + "," + 
                    bytesWrite + "," + 
                    audioOnTime + "," +
                    videoOnTime +                     
                    "\r\n"        
                       );            
    }
}
