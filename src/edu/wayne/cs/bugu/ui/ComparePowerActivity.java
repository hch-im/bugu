package edu.wayne.cs.bugu.ui;

import edu.wayne.cs.bugu.R;
import android.app.Activity;
import android.os.Bundle;

public class ComparePowerActivity extends Activity {
    private String appName = null;
    private double appPower = 0;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        appName = (String)getIntent().getExtra(AvgPowerActivity.APP_NAME);
        appPower = (Double)getIntent().getExtra(AvgPowerActivity.APP_POWER);
        setContentView(R.layout.comparepower);
        this.setTitle(appName + " " + appPower);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
    }

}
