package edu.wayne.cs.bugu.monitor;

import java.io.FileWriter;


public class AppEvent extends Event{
    public int uid;
    public int sensor;
    public int pid;
    public String wlName;//wake lock
    public int wlType;
    
    public void write(FileWriter writer)
    {
        try{
            writer.write("APPEVENT: " + time + "," +
                    name + "," + no + "," + uid);
            if(no == NO_START_SENSOR ||
                    no == NO_STOP_SENSOR)
                writer.write("," + sensor);
            else if(no == NO_START_WAKELOCK ||
                    no == NO_STOP_WAKELOCK)        
                writer.write("," + pid + "," + wlName + "," + wlType);
            writer.write("\r\n");
        }catch(Exception ex){}
    }
}
