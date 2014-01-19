package edu.wayne.cs.bugu.analyzer;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import android.os.Environment;

public class EventAnalyzer{

    private static ArrayList<Long> times = new ArrayList<Long>();
    private static LinkedHashMap<String, LinkedHashMap<Long, String>> finalEvent = new LinkedHashMap<String, LinkedHashMap<Long, String>>();
    public static final int NO_FULL_WIFILOCK_ACQUIRED = 1;
    public static final int NO_FULL_WIFILOCK_RELEASED = 2;
    public static final int NO_SCAN_WIFILOCK_ACQUIRED = 3;
    public static final int NO_SCAN_WIFILOCK_RELEASED = 4;        
    public static final int NO_WIFIMULTICAST_ENABLED = 5;
    public static final int NO_WIFIMULTICAST_DISABLED = 6;        
    //audio video
    public static final int NO_START_AUDIO = 7;
    public static final int NO_STOP_AUDIO = 8;        
    public static final int NO_START_VIDEO = 9;
    public static final int NO_STOP_VIDEO = 10;           
    //sensor
    public static final int NO_START_SENSOR = 11;
    public static final int NO_STOP_SENSOR = 12;        
    public static final int NO_START_GPS = 13;
    public static final int NO_STOP_GPS = 14;   
    //wake lock
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
    /**
   * @param args
   */
    public static void analyze(String filename) {
        File root = Environment.getExternalStorageDirectory();
        File ptopa = new File(root, "ptopa/data/" + filename);
        if(ptopa.exists() == false) { 
            return;
        }
        
        try {
            ProcessFile(ptopa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ProcessFile(File f) throws Exception {
        String s  = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(f)));
//        APPEVENT: 33356949,NotifyHelper.INT_START_WAKELOCK,15,1000,131,NetworkStats,0
//        DEVEVENT: 33356963,NotifyHelper.INT_NETWORK_INTERFACE_TYPE,26,1,wlan0
        // Read file
        while ((s = br.readLine()) != null) {
            if(s.startsWith("APPEVENT:"))
            {
                processAppEvent(s.substring("APPEVENT:".length() + 1));
            }
            else if(s.startsWith("DEVEVENT:"))
            {
                processDevEvent(s.substring("DEVEVENT:".length() + 1));
            }
        }//end while
        
        File powerDoc = new File(f.getParent()+"/eventresult_"+f.getName());     
        if(!powerDoc.exists())
            if(!powerDoc.createNewFile())
                throw new Exception("powerDoc File not exist, failed created???");
        BufferedWriter out = new BufferedWriter(new FileWriter(powerDoc));
        //print out the final result
        //print title
        Iterator<Entry<String, LinkedHashMap<Long, String>>> iterPower = finalEvent.entrySet().iterator(); 
        out.write("Time");
        while(iterPower.hasNext())
        {
            Entry<String, LinkedHashMap<Long, String>> vals= iterPower.next();
            out.write("," + vals.getKey());
        }
        out.write("\r\n");
        //print out content
        int[][] values = new int[times.size()][finalEvent.size()];
        iterPower = finalEvent.entrySet().iterator();
        
        int row = 0;
        int col = 0;
        while(iterPower.hasNext())
        {
            Entry<String, LinkedHashMap<Long, String>> item = iterPower.next();            
            LinkedHashMap<Long, String> vals = item.getValue();
            Iterator<Entry<Long, String>> it = vals.entrySet().iterator();
            while(it.hasNext())
            {
                Entry<Long, String> e = it.next();
                row = times.indexOf(e.getKey());
                int v = Integer.valueOf(e.getValue());
                if(v == 1)
                {
                    values[row][col] = col + 1;
                }
                else if (v == 0)
                {
                    values[row][col] = col + 1;
                    
                    for( int j = row - 1; j >= 0; j--)
                    {
                        if(values[j][col] == 0)
                        {
                            values[j][col] = col + 1;                            
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
            
            col++;
        }
        
        for(row = 0; row < values.length ; row++)
        {
            out.write(times.get(row).toString());
            for (col = 0; col < values[row].length; col++)
            {
                if(values[row][col] == 0)
                    out.write(",0");
                else
                    out.write("," + values[row][col]);
            }
            out.write("\r\n");
        }
        
        out.flush();
        out.close();
        br.close();
    }

    private static void processAppEvent(String s)
    {
        String[] sts = s.split(",");
        if(sts.length < 3) return;
        long time = Long.valueOf(sts[0]);
        int type = Integer.valueOf(sts[2]);
        int uid = Integer.valueOf(sts[3]);
        times.add(time);
        
        LinkedHashMap<Long, String> vals = null;
        String name = null;
        int sensor = -1;
        switch(type)
        {
            case NO_FULL_WIFILOCK_ACQUIRED:
                name = "fullwifilock/" + uid;
                vals = getVals(name);
                vals.put(time, "1");//1 means on, acquired, open
                break;
            case NO_FULL_WIFILOCK_RELEASED:
                name = "fullwifilock/" + uid;
                vals = getVals(name);
                vals.put(time, "0");//0 means off, release , close
                break;
            case NO_SCAN_WIFILOCK_ACQUIRED:
                name = "scanwifilock/" + uid;
                vals = getVals(name);
                vals.put(time, "1");                
                break;
            case NO_SCAN_WIFILOCK_RELEASED:        
                name = "scanwifilock/" + uid;
                vals = getVals(name);
                vals.put(time, "0");                
                break;
            case NO_WIFIMULTICAST_ENABLED:
                name = "wifimulticast/" + uid;
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_WIFIMULTICAST_DISABLED:   
                name = "wifimulticast/" + uid;
                vals = getVals(name);
                vals.put(time, "0");                
                break;
            case NO_START_AUDIO:
                name = "audio/" + uid;
                vals = getVals(name);
                vals.put(time, "1");                
                break;
            case NO_STOP_AUDIO:        
                name = "audio/" + uid;
                vals = getVals(name);
                vals.put(time, "0");                
                break;
            case NO_START_VIDEO:
                name = "video/" + uid;
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_STOP_VIDEO:           
                name = "video/" + uid;
                vals = getVals(name);
                vals.put(time, "0");
                break;
            case NO_START_GPS:
                name = "gps/" + uid;
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_STOP_GPS:   
                name = "gps/" + uid;
                vals = getVals(name);
                vals.put(time, "0");
                break;
            case NO_START_SENSOR:
                sensor = Integer.valueOf(sts[sts.length - 1]);
                name = "sensor/" + uid + "/" + sensor;
                vals = getVals(name);
                vals.put(time, "1");                
                break;
            case NO_STOP_SENSOR: 
                sensor = Integer.valueOf(sts[sts.length - 1]);
                name = "sensor/" + uid + "/" + sensor;
                vals = getVals(name);
                vals.put(time, "0");
                break;
            case NO_START_WAKELOCK:
                name = "wakelock/" + uid;
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_STOP_WAKELOCK:
                name = "wakelock/" + uid;
                vals = getVals(name);
                vals.put(time, "0");
                break;            
            default:
                break;
        }
    }
    
    private static void processDevEvent(String s)
    {
        String[] sts = s.split(",");      
        if(sts.length < 3) return;
        long time = Long.valueOf(sts[0]);
        int type = Integer.valueOf(sts[2]);        
        times.add(time);
        LinkedHashMap<Long, String> vals = null;
        String name = null;
        
        switch(type)
        {
            case NO_PHONE_ON:
                name = "phone";
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_PHONE_OFF:
                name = "phone";
                vals = getVals(name);
                vals.put(time, "0");
                break;
            case NO_SCREEN_ON:
                name = "screen";
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_SCREEN_OFF:         
                name = "screen";
                vals = getVals(name);
                vals.put(time, "0");
                break;
            case NO_WIFI_ON:
                name = "wifi";
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_WIFI_OFF:
                name = "phone";
                vals = getVals(name);
                vals.put(time, "0");
                break;
            case NO_BLUETOOTH_ON:
                name = "bluetooth";
                vals = getVals(name);
                vals.put(time, "1");
                break;
            case NO_BLUETOOTH_OFF:
                name = "bluetooth";
                vals = getVals(name);
                vals.put(time, "0");
                break;
            case NO_SCREEN_BRIGHTNESS:
                break;                    
            case NO_NETWORK_INTERFACE_TYPE:
                break;
            default:
                break;            
        }        
    }
    
    private static LinkedHashMap<Long, String> getVals(String name)
    {
        LinkedHashMap<Long, String> vals = null;
        if(finalEvent.containsKey(name) == false)
        {
            vals = new LinkedHashMap<Long, String>();
            finalEvent.put(name, vals);
        }
        else
        {
            vals = finalEvent.get(name);
        }       
        return vals;
    }
}
    
    



