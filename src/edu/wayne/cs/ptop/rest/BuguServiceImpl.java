package edu.wayne.cs.ptop.rest;


import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class BuguServiceImpl implements BuguService {
    private String baseurl = "http://codegreen.cs.wayne.edu:8088/"; 
//    private String baseurl = "http://35.16.44.159:3000/"; 

    public void setBaseurl(String baseurl) {
        this.baseurl = baseurl;
    }

    @Override
    public JSONArray getApplications(int device, int type, String name, int start, int limit){
        StringBuffer URI = new StringBuffer(baseurl + "applications.json?");
        if( device > 0)
            URI.append("d=" + device);
        if(type >= 0) URI.append("t="+type);
        if(name != null)URI.append("n" + name);
        if(start >= 0) URI.append("p="+ ((start / limit) + 1));
        if(limit >= 0) URI.append("l="+limit);        
        // Prepare the request  
        ClientResource resource = new ClientResource(URI.toString());  
          
        // Add the client authentication to the call  
//        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;  
//        ChallengeResponse authentication = new ChallengeResponse(scheme, "scott", "tiger");  
//        resource.setChallengeResponse(authentication);  

        // Indicates the client preferences and let the server handle
        // the best representation with content negotiation.
        try {
            resource.get(MediaType.APPLICATION_JSON);        
            JSONArray objs = null;
            if (resource.getStatus().isSuccess()) {  
                    JsonRepresentation rep = new JsonRepresentation(resource.getResponseEntity());
                    objs = rep.getJsonArray();                
            }else {  
                // Unexpected status  
                System.out.println("An unexpected status was returned: "  
                        + resource.getStatus());  
            }  
            
            return objs;
        } catch (Exception e) {
            e.printStackTrace();
        }                         
        return null;
    }

    @Override
    public JSONArray getDevices() {
        StringBuffer URI = new StringBuffer(baseurl + "devices.json");
        // Prepare the request  
        ClientResource resource = new ClientResource(URI.toString());  
          
        try {
            resource.get(MediaType.APPLICATION_JSON);        
            JSONArray objs = null;
            if (resource.getStatus().isSuccess()) {  
                    JsonRepresentation rep = new JsonRepresentation(resource.getResponseEntity());
                    objs = rep.getJsonArray();                
            }else {  
                // Unexpected status  
                System.out.println("An unexpected status was returned: "  
                        + resource.getStatus());  
            }  
            return objs;
        } catch (Exception e) {
            e.printStackTrace();
        }                         
        return null;
    } 
    
    @Override
    public boolean uploadPower(String brand, HashMap<String, String> data)
    {
        StringBuffer URI = new StringBuffer(baseurl + "submissions.json");
        // Prepare the request  
        Client client = new Client(Protocol.HTTP);
        client.setConnectTimeout(60000);        
        ClientResource resource = new ClientResource(URI.toString());  
        resource.setNext(client);
        
        try {
            StringBuffer value = new StringBuffer();
            for(Entry<String , String> e : data.entrySet())
            {
                value.append("(").append(e.getKey()).append(",").append(e.getValue()).append(")");
            }
            
            Form form = new Form();
            form.add("submission[device]", brand);
            form.add("submission[value]", value.toString());
            
            resource.post(form, MediaType.APPLICATION_JSON);
            if (resource.getStatus().isSuccess()) {  
                    return true;
            }else {  
                // Unexpected status  
                System.out.println("An unexpected status was returned: "  
                        + resource.getStatus());  
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }          
        return false;
    }
    
    class Submission
    {
        String device;
        String value;
        public Submission(String device, String value)
        {
            this.device = device;
            this.value = value;
        }
        
        public String getDevice() {
            return device;
        }
        public void setDevice(String device) {
            this.device = device;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        
        
    }
}
