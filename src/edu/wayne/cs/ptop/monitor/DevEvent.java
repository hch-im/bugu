package edu.wayne.cs.ptop.monitor;

import java.io.FileWriter;


public class DevEvent extends Event{
    public int brightness;
    public String iface;
    public int netType;
    
    public void write(FileWriter writer)
    {
        try{
            writer.write("DEVEVENT: " + time + "," +
                    name + "," + no);
            if(no == NO_SCREEN_BRIGHTNESS)
                writer.write("," + brightness);
            else if(no == NO_NETWORK_INTERFACE_TYPE)        
                writer.write("," + netType + "," + iface);
            writer.write("\r\n");
        }catch(Exception ex){}
    }
}
