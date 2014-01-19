package edu.wayne.cs.bugu.display;

import edu.wayne.cs.ptop.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class PtopaTabWidget extends TabActivity{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, PtopaActivity.class);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("home").setIndicator("Home",
                          res.getDrawable(R.drawable.ic_tab_home))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, AppPowerActivity.class);
        spec = tabHost.newTabSpec("apppower").setIndicator("Power",
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
