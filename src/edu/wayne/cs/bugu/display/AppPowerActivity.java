package edu.wayne.cs.bugu.display;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wayne.cs.bugu.rest.BuguService;
import edu.wayne.cs.bugu.rest.BuguServiceImpl;
import edu.wayne.cs.bugu.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AppPowerActivity extends Activity{
    private BuguService service = new BuguServiceImpl();
    private TableLayout table = null;
    private Spinner spinner = null;
    private Spinner devSpinner = null;    
    private EditText textField = null;
    private JSONArray devs = null;
    private Button nextPage = null;
    private int totaloffset = 0;
    private int numperpage = 10;    
//    private ProgressBar progressBar;
    private ProgressDialog pDialog;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);        
        setContentView(R.layout.apppower);
       
        table = (TableLayout)findViewById(R.id.table_app_power);
        spinner = (Spinner)findViewById(R.id.spinner1);
        devSpinner = (Spinner)findViewById(R.id.spinner2);        
        textField = (EditText)findViewById(R.id.editText1);
        
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, 
                android.R.layout.simple_spinner_item, getAppTypeArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        Button button = (Button)findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textField.getWindowToken(), 0);                                                    
                loadContent(0);
            }
        });

        textField.setOnKeyListener(new View.OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textField.getWindowToken(), 0);                                  
                }
                return false;
            }
        });

        nextPage = (Button)findViewById(R.id.button2);
        nextPage.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textField.getWindowToken(), 0);     
                loadContent(totaloffset);
            }
        });        
        nextPage.setEnabled(false);
        
//        progressBar = (ProgressBar)findViewById(R.id.progressBar1);
//        progressBar.setVisibility(View.INVISIBLE);
        
        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Loading...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);      
        
        this.initDeviceList();
    }
    
    private void loadContent(int offset)
    {
        int dev = devSpinner.getSelectedItemPosition();
        if(dev == 0)
        {
            dev = -1;
        }
        else
        {
            try{
                JSONObject o = devs.getJSONObject(dev - 1);
                dev = o.getInt("id");
            }catch(Exception ex){dev = -1;}
        }
        
        int type = spinner.getSelectedItemPosition();
        pDialog.show();
        Thread t = new SearchThread(dev, type, offset);
        t.start();
    }
    private CharSequence[] getAppTypeArray()
    {
        CharSequence array[] = new CharSequence[6];        
        array[0] = "All";
        array[1] = "Game";
        array[2] = "Social";
        array[3] = "Audio";
        array[4] = "Video";
        array[5] = "Web Browser";
        return array;
    }

    private String getAppTypeName(int type)
    {
        return (String)spinner.getAdapter().getItem(type + 1);
    }
    
    public void initDeviceList()
    {
        pDialog.show();
        UpdateDevicesThread t = new UpdateDevicesThread();
        t.start();
    }
    
    public void updateDeviceList(JSONArray objs)
    {
        if(devs == null)
            devs = objs;
        CharSequence array[] = new CharSequence[devs == null? 1 : devs.length() + 1];
        array[0] = "All";
        if(devs != null)
        {
            for (int i = 0; i < devs.length(); i++)
            {
                try{
                    JSONObject o = devs.getJSONObject(i);
                    array[i + 1] = o.getString("name");
                }catch(Exception ex){}
            }
        }
        
        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(this, 
                android.R.layout.simple_spinner_item, array);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devSpinner.setAdapter(adapter2);
    }
    
    private JSONArray load(int dev, int type, int offset)
    {
        JSONArray objs = service.getApplications(dev, type - 1, textField.getText().toString().trim(), offset, numperpage);
        if(objs == null) return null;
        return objs;
    }
    
    public void displayResult(int offset, JSONArray objs){
        if(objs == null)
            return;
        
        if(offset == 0){
            clear();
            totaloffset = objs.length();
            if(objs.length() == numperpage)
            {
                nextPage.setEnabled(true);
            }            
        }
        else
        {
            totaloffset += objs.length();
            if(objs.length() < numperpage)
            {
                nextPage.setEnabled(false);
            }
        }        
        Log.i("pTopA", "App numbers:" + objs.length());
        DecimalFormat format = new DecimalFormat("#.####");
        for (int i = 0; i < objs.length(); i++)
        {
            try{
            JSONObject o = objs.getJSONObject(i);
            TableRow row = new TableRow(this);
            table.addView(row);
            TableRow.LayoutParams trParams = new TableRow.LayoutParams();
            trParams.gravity = 3;
            
            TextView tv1 = new TextView(this);
            TextView tv2 = new TextView(this);
            TextView tv3 = new TextView(this);
            try{
                String name = o.getString("name");
                if(name != null)
                    tv1.setText(name.length() > 12 ? (name.substring(0, 12) + "...") : name);
            }catch(Exception ex){}
            tv1.setLayoutParams(trParams);

            try{
            tv3.setText(this.getAppTypeName(o.getInt("apptype")));
            }catch(Exception ex){}
            trParams = new TableRow.LayoutParams();
            trParams.gravity = 17;
            tv3.setLayoutParams(trParams);
            
            try{
            tv2.setText(format.format(o.getDouble("power")));
            }catch(Exception ex){}
            trParams = new TableRow.LayoutParams();
            trParams.gravity = 5;
            tv2.setLayoutParams(trParams);
            
            row.addView(tv1);
            row.addView(tv3);            
            row.addView(tv2);          
            }catch(Exception ex){}
          
        }
    }
    
    private void clear()
    {
        int count = table.getChildCount();
        table.removeViews(1, count - 1);
    }
    
    class SearchThread extends Thread
    {
        public SearchThread(int dev, int type, int offset)
        {
            this.offset = offset;
            this.dev = dev;
            this.type = type;
        }
        
        int offset;
        int dev;
        int type;
        JSONArray objs;
        @Override
        public void run() {         
            objs = load(dev, type, offset);
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                displayResult(offset, objs);
                pDialog.dismiss();
            }
        };        
    }
    
    class UpdateDevicesThread extends Thread
    {
        JSONArray objs;
        @Override
        public void run() {         
            objs = service.getDevices();;
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                updateDeviceList(objs);
                pDialog.dismiss();
            }
        };        
    }    
}
