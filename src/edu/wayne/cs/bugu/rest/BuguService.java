package edu.wayne.cs.bugu.rest;

import java.util.HashMap;

import org.json.JSONArray;

public interface BuguService {
    public JSONArray getApplications(int dev, int type, String name, int start, int limit);
    public JSONArray getDevices();    
    public boolean uploadPower(String brand, HashMap<String, String> data);        
}
