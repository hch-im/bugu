package edu.wayne.cs.bugu.proc.component;

import android.net.wifi.WifiManager;
import edu.wayne.cs.bugu.proc.Stats;

public class Wifi extends Component {
	public boolean mWifiOn = false;

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(long relTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calculatePower(Stats st) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dump(StringBuffer buf) {
		// TODO Auto-generated method stub
		
	}

	public void updateWifiState(int wifiState){
        if (wifiState == WifiManager.WIFI_STATE_ENABLED ||
        		wifiState == WifiManager.WIFI_AP_STATE_ENABLED) {
        	mWifiOn = true;
        }else if(wifiState == WifiManager.WIFI_STATE_DISABLED || 
        		wifiState == WifiManager.WIFI_AP_STATE_DISABLED){
        	mWifiOn = false;
        }
	}
}
