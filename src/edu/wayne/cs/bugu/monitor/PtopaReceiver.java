package edu.wayne.cs.bugu.monitor;

import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PtopaReceiver  extends BroadcastReceiver{
    private FileWriter writer;
    private Vector<Event> events = new Vector<Event>();
    
    public void reset(FileWriter fw)
    {
        writer = fw;
        events.clear();
    }
    
    public AppEvent addAppEvent(Intent intent, String intType, int intNo, long time)
    {
        AppEvent e = new AppEvent();
        e.name = intType;
        e.no = intNo;
        e.uid = intent.getIntExtra(Event.EXTRA_UID, -1);
        e.time = time;
        events.add(e);            
        return e;
    }
    
    public DevEvent addDevEvent(Intent intent, String intType, int intNo, long time)
    {
        DevEvent e = new DevEvent();
        e.name = intType;
        e.no = intNo;
        e.time = time;
        events.add(e);            
        return e;
    }        
    @Override
    public void onReceive(Context context, Intent intent) {
        String intType = intent.getAction();
        int intNo = intent.getIntExtra(Event.EXTRA_NO, 0);
        long time = intent.getLongExtra(Event.EXTRA_TIME, 0);
        Log.i("PtopReceiver", "received:" + intType);
        switch(intNo)
        {
            case Event.NO_FULL_WIFILOCK_ACQUIRED:
            case Event.NO_FULL_WIFILOCK_RELEASED:
            case Event.NO_SCAN_WIFILOCK_ACQUIRED:
            case Event.NO_SCAN_WIFILOCK_RELEASED:        
            case Event.NO_WIFIMULTICAST_ENABLED:
            case Event.NO_WIFIMULTICAST_DISABLED:   
            case Event.NO_START_GPS:
            case Event.NO_STOP_GPS:   
                addAppEvent(intent, intType, intNo, time);
                break;
            case Event.NO_START_AUDIO:
            case Event.NO_STOP_AUDIO:        
            case Event.NO_START_VIDEO:
            case Event.NO_STOP_VIDEO:           
                Log.i("PtopReceiver", "received:" + intType + "  uid:" + intent.getIntExtra(Event.EXTRA_UID, -1));
                addAppEvent(intent, intType, intNo, time);                
                break;
            case Event.NO_START_SENSOR:
            case Event.NO_STOP_SENSOR: 
                AppEvent ae = addAppEvent(intent, intType, intNo, time);
                ae.sensor = intent.getIntExtra(Event.EXTRA_SENSOR, -1);                    
                break;
            case Event.NO_START_WAKELOCK:
            case Event.NO_STOP_WAKELOCK: 
                ae = addAppEvent(intent, intType, intNo, time);
                ae.pid = intent.getIntExtra(Event.EXTRA_PID, -1);
                ae.wlName = intent.getStringExtra(Event.EXTRA_NAME);
                ae.wlType = intent.getIntExtra(Event.EXTRA_TYPE, 0);
                break;
            //device events handling
            case Event.NO_PHONE_ON:
            case Event.NO_PHONE_OFF: 
            case Event.NO_SCREEN_ON:
            case Event.NO_SCREEN_OFF:         
            case Event.NO_WIFI_ON:
            case Event.NO_WIFI_OFF:
            case Event.NO_BLUETOOTH_ON:
            case Event.NO_BLUETOOTH_OFF:
                addDevEvent(intent, intType, intNo, time);
                break;
            case Event.NO_SCREEN_BRIGHTNESS:
                DevEvent de = addDevEvent(intent, intType, intNo, time);
                de.brightness = intent.getIntExtra(Event.EXTRA_BRIGHTNESS, 0);
                break;                    
            case Event.NO_NETWORK_INTERFACE_TYPE:
                de = addDevEvent(intent, intType, intNo, time);
                de.iface = intent.getStringExtra(Event.EXTRA_IFACE);
                de.netType = intent.getIntExtra(Event.EXTRA_TYPE, -1);
                break;
            default:
                break;
        }
    }
    
    public void writeLog()
    {
        if(writer == null)
            return;
        
        try{
            List<Event> evs = events.subList(0, events.size());
            Log.i("pTopA: ", "events amount : " + evs.size() );
            for (Iterator<Event> it = evs.iterator(); it.hasNext();)
            {
                Event  e = it.next();
                e.write(writer);
            }
            writer.flush();
            evs.clear();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }  
}
