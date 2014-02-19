package edu.wayne.cs.bugu.ui;

import edu.wayne.cs.bugu.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class BuguTabWidget extends TabActivity{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent; 
        
        intent = new Intent().setClass(this, HomeActivity.class);
        spec = tabHost.newTabSpec("home").setIndicator("Home",
                          res.getDrawable(R.drawable.ic_tab_home))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, DevicePowerActivity.class);
        spec = tabHost.newTabSpec("apppower").setIndicator("Device Power",
                          res.getDrawable(R.drawable.ic_tab_apppower))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, RecordActivity.class);
        spec = tabHost.newTabSpec("event").setIndicator("Record",
                          res.getDrawable(R.drawable.ic_tab_event))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SetupActivity.class);
        spec = tabHost.newTabSpec("setup").setIndicator("Setup",
                          res.getDrawable(R.drawable.ic_tab_setup))
                      .setContent(intent);
        tabHost.addTab(spec);        
        tabHost.setCurrentTab(0);
        
    }
}
