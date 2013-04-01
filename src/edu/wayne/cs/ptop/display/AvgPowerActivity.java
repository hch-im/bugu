package edu.wayne.cs.ptop.display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.wayne.cs.ptop.R;
import edu.wayne.cs.ptop.analyzer.PowerAnalyzer;
import edu.wayne.cs.ptop.db.model.Record;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AvgPowerActivity extends LauncherActivity {
    private String filename = null;
    private ArrayList<Map<String, String>> list;
    public static final String APP_NAME = "edu.wayne.cs.ptop.appname";
    public static final String APP_POWER = "edu.wayne.cs.ptop.apppower";
    
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        filename = (String)getIntent().getExtra(RecordActivity.EXTRA_FILENAME);
        prepareData();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        showDialog(list.get(position));
    }
    
    private void showDialog(final Map<String, String> map){
        new AlertDialog.Builder(this)
        .setTitle(R.string.resultTypeOption)
        .setItems(R.array.apppoweropt, 
                new DialogInterface.OnClickListener() {                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        operateAppPower(map, which);
                    }
                })
         .show();
    }
    
    private void operateAppPower(Map<String, String> map, int which)
    {
        double power = Double.valueOf(map.get("Power").replace(" mw", ""));
        Intent i;
        switch(which)
        {
            case 0:
                i = new Intent(this, ComparePowerActivity.class);
                i.putExtra(APP_NAME, map.get("Application"));
                i.putExtra(APP_POWER, power);
                startActivity(i);
                break;
        }
    }
    
    private void prepareData()
    {
        try {
            File root = Environment.getExternalStorageDirectory();
            File ptopa = new File(root, "ptopa/data/powerresult_" + filename);
            if(ptopa.exists() == false) { 
                PowerAnalyzer.analyze(filename);
                if(ptopa.exists() == false) return;
            }
            String s = null; String last = null; 
            BufferedReader br = new BufferedReader(new InputStreamReader(
              new FileInputStream(ptopa)));
            s = br.readLine(); 
            if(s == null) return;
            String[] tstrs = s.split(",");
            
            while ((s = br.readLine()) != null) {
                //do nothing
                last = s;
            }
            
            if(last == null) return;
            
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("###0.0000", decimalFormatSymbols);
            String[] strs = last.split(",");
            list = new ArrayList<Map<String, String>>();
            for(int i = 1 ; i < strs.length; i++)
            {
                String val = "";
                if(strs[i].indexOf("/") > 0)
                {
                    double pv = Double.valueOf(strs[i].substring(0, strs[i].indexOf("/")));
                    val = decimalFormat.format(pv) + " mw";
                }
                else
                {
                    val = "0 mw";
                }
                
                list.add(putData(tstrs[i], val));
            }
            String[] from = { "Application", "Power" };
            int[] to = { android.R.id.text1, android.R.id.text2 };

            SimpleAdapter adapter = new SimpleAdapter(this, list,
                    android.R.layout.simple_list_item_2, from, to);
            setListAdapter(adapter);
        }catch(Exception ex){}
    }
    
    private HashMap<String, String> putData(String name, String purpose) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("Application", name);
        item.put("Power", purpose);
        return item;
    }
   
}
