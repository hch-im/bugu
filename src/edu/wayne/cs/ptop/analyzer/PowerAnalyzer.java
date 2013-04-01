package edu.wayne.cs.ptop.analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import android.os.Environment;

public class PowerAnalyzer{

//    [Time[[app,power]],[app, power], ...]
    private static LinkedHashMap<Long, HashMap<String, String>> finalPower = new LinkedHashMap<Long, HashMap<String, String>>();
    private static HashSet<String> uids = new HashSet<String>();
    private static HashMap<String, String> nameUid = new HashMap<String, String>();
    
    public static void analyze(String filename) {
        File root = Environment.getExternalStorageDirectory();
        File ptopa = new File(root, "ptopa/data/" + filename);
        if(ptopa.exists() == false) { 
            return;
        }
        
        uids.add("00");
        nameUid.put("00", "Device");
        try {
            ProcessFile(ptopa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ProcessFile(File f) throws Exception {
       String s = null;  
         BufferedReader br = new BufferedReader(new InputStreamReader(
           new FileInputStream(f)));
         
         File powerDoc = new File(f.getParent()+"/result_"+f.getName());     
         if(!powerDoc.exists())
             if(!powerDoc.createNewFile())
                 throw new Exception("powerDoc File not exist, failed created！");
         BufferedWriter powerDocutput = new BufferedWriter(new FileWriter(powerDoc));
    
         File powerDoc2 = new File(f.getParent()+"/powerresult_"+f.getName());     
         if(!powerDoc2.exists())
             if(!powerDoc2.createNewFile())
                 throw new Exception("powerDoc2 File not exist, failed created！");
         BufferedWriter powerDoc2output = new BufferedWriter(new FileWriter(powerDoc2));
         
         HashMap<String, String> powerUid = null;
         double timebase = 1000.0;
         //Read file
         while ((s = br.readLine()) != null) {
             if(s.startsWith("TIME:"))
             {
                 String time = s.replace("TIME:", "").trim();
                 powerUid = new HashMap<String, String>();
                 finalPower.put(Long.valueOf(time), powerUid);
             }
             else if(s.startsWith("APP:"))
             {
                 if(powerUid == null) continue;
                 String[] vals = s.replace("APP:", "").split(",");
                 if(vals.length > 5)
                 {
                     powerUid.put(vals[0], vals[2]);
                     if(uids.contains(vals[0]) == false)
                         uids.add(vals[0]);
                     
                     if(nameUid.containsKey(vals[0]) == false && vals[1].equals("*wakelock*") == false)
                     {
                         nameUid.put(vals[0], vals[1]);
                     }
                 }
             }
             else if(s.startsWith("APPINFO:"))
             {
                 //TODO
             }
             else if(s.startsWith("DEV:"))
             {
                 if(powerUid == null) continue;
                 String[] vals = s.replace("DEV:", "").split(",");
                 if(vals.length > 5)
                 {
                     powerUid.put("00", vals[0]);
                 }
             }
             else if(s.startsWith("DEVINFO:"))
             {
                 //TODO
             }
            
         }//end while
         
         HashMap<String, Object[]> appTempPower = new HashMap<String, Object[]>();
         HashMap<String, Object[]> avgPower = new HashMap<String, Object[]>();
         
         Iterator<Entry<Long, HashMap<String, String>>> iterPower = finalPower.entrySet().iterator(); 
         boolean header = true;
         HashMap<String, String> lastval = null;
         Long lasttime = null;
         int eqtimes = 3;
         long minTimeInterval = 10000;//minimum time interval
         
         while (iterPower.hasNext()) {
                Entry<Long, HashMap<String, String>> entry = iterPower.next();
                Long time = entry.getKey();
                HashMap<String, String> val = entry.getValue();
                //write header
                if(header)
                {
                    powerDocutput.write("time,Device");
                    powerDoc2output.write("time");
                    for(Iterator<String> it = uids.iterator(); it.hasNext();)
                    {
                        String n = it.next();
                        if(nameUid.containsKey(n)) n = nameUid.get(n);
                        if(it.equals("00") == false)
                            powerDocutput.write(","+ n);
                        
                        powerDoc2output.write("," + n);
                    }
                    powerDocutput.write("\r\n");
                    powerDoc2output.write("\r\n");
                    header = false;
                }
                //wrte data
                powerDocutput.write(time.toString());
                if(val.containsKey("Device")) powerDocutput.write(","+val.get("Device"));
                else powerDocutput.write(","+0);
                for(Iterator<String> it = uids.iterator(); it.hasNext();)
                {
                    String n = it.next();
                    if(it.equals("Device") == false)
                        powerDocutput.write(","+val.get(n));
                    
                    //analyze average power
                    String startkey = n+":start";
                    String endkey = n+":end";
                    double pv = 0;                
                    try{pv = Double.valueOf(val.get(n));}catch(Exception ex){}
                    Object[] oval = {time, pv, 0};
                    if(pv == 0) continue;
                    if(avgPower.containsKey(n)) continue;
    
                    if(appTempPower.containsKey(startkey) == false)
                    {
                        appTempPower.put(startkey, oval);
                        continue;
                    }else{
                        if(appTempPower.containsKey(endkey) == false)
                        {
                            //get last val
                            Object[] lastvals = appTempPower.get(startkey);
                            if(pv == (Double)lastvals[1]) appTempPower.put(startkey, oval);
                            else//not equal, start to find end val
                            {
                                appTempPower.put(endkey, oval);
                            }
                        }else
                        {
                            Object[] lastvals = appTempPower.get(endkey);
                            if(pv != (Double)lastvals[1]) appTempPower.put(endkey, oval);
                            else
                            {
                                Object[] svals = appTempPower.get(startkey);
                                if((Integer)svals[2] + 1 < eqtimes)//not more than three times
                                {
                                    svals[2] = (Integer)svals[2] + 1;
                                    appTempPower.put(endkey, svals);
                                    continue;
                                }
                                //error data
                                if(pv <= (Double)svals[1])
                                {
                                    appTempPower.remove(startkey);
                                    appTempPower.remove(endkey);
                                    appTempPower.put(startkey, oval);
                                    continue;
                                }
                                
                                Object[] pval = {( pv - (Double)svals[1] ) / ( ( time - (Long)svals[0]) /timebase )  + "/"+ ( time - (Long)svals[0]) /timebase,
                                                 Double.valueOf(( time - (Long)svals[0]) /timebase) };
                                if(avgPower.containsKey(n))
                                {
                                    Object[] lpval = avgPower.get(n);
                                    if((Double)lpval[1] < (Double)pval[1])
                                        avgPower.put(n, pval);
                                }else{
                                    avgPower.put(n, pval);
                                }
                                continue;
                            }
                        }
                    }
                }            
                powerDocutput.write("\r\n");
                //compute power
                if(lastval != null)
                {
                      long timeinterval = Long.valueOf(time) - Long.valueOf(lasttime);
                      if(timeinterval < minTimeInterval) continue; //if the time interval is too short, skip this loop
                      powerDoc2output.write(time.toString());
                      for(Iterator<String> it = uids.iterator(); it.hasNext();)
                      {
                          String n = it.next();
                          double np = 0;
                          try{np=Double.valueOf(val.get(n));}catch(Exception ex){}
                          double op = 0;
                          try{op=Double.valueOf(lastval.get(n));}catch(Exception ex){}
                          if(op > 0 && np > 0 && np > op && timeinterval > 0)
                          {
                              double rpower = (np - op) / (timeinterval / timebase );
                              if(rpower < 0){
                                  System.out.println( np + " " + op + "error");
                              }
                               powerDoc2output.write(","+rpower);
                          }else{
                              powerDoc2output.write(","+0);
                          }
                      }
                      powerDoc2output.write("\r\n");
                }//end if
                lastval = val;
                lasttime = time;
         }//end while
         
         powerDoc2output.write("Avg");
         for(Iterator<String> it = uids.iterator(); it.hasNext();)
         {
             String n = it.next();
             String startkey = n+":start";
             String endkey = n+":end";
             if(avgPower.containsKey(n))
             {
                 Object[] vals = avgPower.get(n);
                 powerDoc2output.write(","+(String)vals[0]);
             }
             else if(appTempPower.containsKey(startkey) && appTempPower.containsKey(endkey))
             {
                 Object[] sobjs = appTempPower.get(startkey);
                 Object[] eobjs = appTempPower.get(endkey);
                 if((Double)eobjs[1] <= (Double)sobjs[1])
                 {
                     powerDoc2output.write(","+0);
                     continue;
                 }
                 double interval = ( (Long)eobjs[0] - (Long)sobjs[0]) /timebase;
                 powerDoc2output.write(","+ ( (Double)eobjs[1] - (Double)sobjs[1] ) / interval  + "/"+ interval);
             }
             else
             {
                 powerDoc2output.write(","+0);
             }
         }
         powerDoc2output.write("\r\n");
         powerDocutput.flush();   
         powerDocutput.close();    
         powerDoc2output.flush();
         powerDoc2output.close();
         
        }
    

}

