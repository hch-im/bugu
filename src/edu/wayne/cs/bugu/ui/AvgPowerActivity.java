package edu.wayne.cs.bugu.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.wayne.cs.bugu.db.model.Record;
import edu.wayne.cs.bugu.R;
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
    
    private HashMap<String, String> putData(String name, String purpose) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("Application", name);
        item.put("Power", purpose);
        return item;
    }
   
}
