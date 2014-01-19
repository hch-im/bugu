package edu.wayne.cs.bugu.monitor;

import java.io.FileWriter;

public abstract class Event {
    public static final String EXTRA_NO = "notifyhelper.no";
    public static final String EXTRA_UID = "notifyhelper.uid";
    public static final String EXTRA_TIME = "notifyhelper.time";
    public static final String EXTRA_SENSOR = "notifyhelper.sensor";
    public static final String EXTRA_PID = "notifyhelper.pid";
    public static final String EXTRA_NAME = "notifyhelper.name";
    public static final String EXTRA_TYPE = "notifyhelper.type";
    public static final String EXTRA_BRIGHTNESS = "notifyhelper.brightness";
    public static final String EXTRA_IFACE = "notifyhelper.iface";
    
    public static final String INT_FULL_WIFILOCK_ACQUIRED = "NotifyHelper.INT_FULL_WIFILOCK_ACQUIRED";
    public static final String INT_FULL_WIFILOCK_RELEASED = "NotifyHelper.INT_FULL_WIFILOCK_RELEASED";
    public static final String INT_SCAN_WIFILOCK_ACQUIRED = "NotifyHelper.INT_SCAN_WIFILOCK_ACQUIRED";
    public static final String INT_SCAN_WIFILOCK_RELEASED = "NotifyHelper.INT_SCAN_WIFILOCK_RELEASED";        
    public static final String INT_WIFIMULTICAST_ENABLED = "NotifyHelper.INT_WIFIMULTICAST_ENABLED";
    public static final String INT_WIFIMULTICAST_DISABLED = "NotifyHelper.INT_WIFIMULTICAST_DISABLED";
    public static final int NO_FULL_WIFILOCK_ACQUIRED = 1;
    public static final int NO_FULL_WIFILOCK_RELEASED = 2;
    public static final int NO_SCAN_WIFILOCK_ACQUIRED = 3;
    public static final int NO_SCAN_WIFILOCK_RELEASED = 4;        
    public static final int NO_WIFIMULTICAST_ENABLED = 5;
    public static final int NO_WIFIMULTICAST_DISABLED = 6;        
    //audio video
    public static final String INT_START_AUDIO = "NotifyHelper.INT_START_AUDIO";
    public static final String INT_STOP_AUDIO = "NotifyHelper.INT_STOP_AUDIO";        
    public static final String INT_START_VIDEO = "NotifyHelper.INT_START_VIDEO";
    public static final String INT_STOP_VIDEO = "NotifyHelper.INT_STOP_VIDEO";   
    public static final int NO_START_AUDIO = 7;
    public static final int NO_STOP_AUDIO = 8;        
    public static final int NO_START_VIDEO = 9;
    public static final int NO_STOP_VIDEO = 10;           
    //sensor
    public static final String INT_START_SENSOR = "NotifyHelper.INT_START_SENSOR";
    public static final String INT_STOP_SENSOR = "NotifyHelper.INT_STOP_SENSOR";        
    public static final String INT_START_GPS = "NotifyHelper.INT_START_GPS";
    public static final String INT_STOP_GPS = "NotifyHelper.INT_STOP_GPS";   
    public static final int NO_START_SENSOR = 11;
    public static final int NO_STOP_SENSOR = 12;        
    public static final int NO_START_GPS = 13;
    public static final int NO_STOP_GPS = 14;   
    //wake lock
    public static final String INT_START_WAKELOCK = "NotifyHelper.INT_START_WAKELOCK";
    public static final String INT_STOP_WAKELOCK = "NotifyHelper.INT_STOP_WAKELOCK";           
    public static final String INT_PHONE_ON = "NotifyHelper.INT_PHONE_ON";
    public static final String INT_PHONE_OFF = "NotifyHelper.INT_PHONE_OFF"; 
    public static final String INT_SCREEN_ON = "NotifyHelper.INT_SCREEN_ON";
    public static final String INT_SCREEN_OFF = "NotifyHelper.INT_SCREEN_OFF";         
    public static final String INT_SCREEN_BRIGHTNESS = "NotifyHelper.INT_SCREEN_BRIGHTNESS";
    public static final String INT_WIFI_ON = "NotifyHelper.INT_WIFI_ON";
    public static final String INT_WIFI_OFF = "NotifyHelper.INT_WIFI_OFF";
    public static final String INT_BLUETOOTH_ON = "NotifyHelper.INT_BLUETOOTH_ON";
    public static final String INT_BLUETOOTH_OFF = "NotifyHelper.INT_BLUETOOTH_OFF";
    public static final String INT_NETWORK_INTERFACE_TYPE = "NotifyHelper.INT_NETWORK_INTERFACE_TYPE";

    public static final int NO_START_WAKELOCK = 15;
    public static final int NO_STOP_WAKELOCK = 16;           
    public static final int NO_PHONE_ON = 17;
    public static final int NO_PHONE_OFF = 18; 
    public static final int NO_SCREEN_ON = 19;
    public static final int NO_SCREEN_OFF = 20;         
    public static final int NO_SCREEN_BRIGHTNESS = 21;
    public static final int NO_WIFI_ON = 22;
    public static final int NO_WIFI_OFF = 23;
    public static final int NO_BLUETOOTH_ON = 24;
    public static final int NO_BLUETOOTH_OFF = 25;
    public static final int NO_NETWORK_INTERFACE_TYPE = 26;
    public static final String INT_EVENT_LISTION = "BatteryStatsService.eventlisten";
    
    public int no;
    public String name;
    public long time;
    
    public abstract void write(FileWriter writer);
}
