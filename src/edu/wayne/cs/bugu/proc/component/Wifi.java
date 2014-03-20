package edu.wayne.cs.bugu.proc.component;

import android.net.wifi.WifiManager;
import android.util.Log;
import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.proc.Stats;

public class Wifi extends Component {
	public boolean mWifiOn = false;
	public long rxp,txp, rxb,txb;
	private long preRxp, preTxp, preRxb, preTxb;

	@Override
	public void init() {
		// TODO Auto-generated method stub
		rxp = readIntValueFromFile(SYS_WIFI_RXPACKETS);
		txp = readIntValueFromFile(SYS_WIFI_TXPACKETS);
		rxb = readIntValueFromFile(SYS_WIFI_RXBYTES);
		txb = readIntValueFromFile(SYS_WIFI_TXBYTES);
		
	}

	@Override
	public void updateState(long relTime) {
		// TODO Auto-generated method stub
		preRxp = rxp;
		preTxp = txp;
		preRxb = rxb;
		preTxb = txb;
		rxp = readIntValueFromFile(SYS_WIFI_RXPACKETS);
		txp = readIntValueFromFile(SYS_WIFI_TXPACKETS);
		rxb = readIntValueFromFile(SYS_WIFI_RXBYTES);
		txb = readIntValueFromFile(SYS_WIFI_TXBYTES);
		//Log.d("package data", txp+","+"rxp");
		
	}

	@Override
	public void calculatePower(Stats st) {
		// TODO Auto-generated method stub
		if(!mWifiOn)
			return;
		// coefficient values from devscope
		long deltaPkg = txp+rxp-preTxp-preRxp;
		if(deltaPkg<20)
			st.curDevicePower.wifiPower= deltaPkg*2+110;
		else{
			st.curDevicePower.wifiPower= deltaPkg*0.7+140.7;
			}//st.powerProfile.
		
	}

	@Override
	public void dump(StringBuffer buf) {
		// TODO Auto-generated method stub
		long time = java.lang.System.currentTimeMillis();
		if(Constants.DEBUG_WIFI){
			buf.append("rxp " + String.valueOf(rxp) +" txp " + String.valueOf(txp)+" rxb " + String.valueOf(rxb) +" txb " + String.valueOf(txb)+ "\r\n");
			//buf.append("delta " + String.valueOf(txp+rxp-preTxp-preRxp) + "\r\n");
		
		}
		
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
	
	/*
	 * Functions for parse system wifi related information.
	 */
    private final String SYS_WIFI_TXPACKETS = "/sys/class/net/wlan0/statistics/tx_packets";
    private final String SYS_WIFI_RXPACKETS = "/sys/class/net/wlan0/statistics/rx_packets";
    private final String SYS_WIFI_TXBYTES = "/sys/class/net/wlan0/statistics/tx_bytes";
    private final String SYS_WIFI_RXBYTES = "/sys/class/net/wlan0/statistics/rx_bytes";
}
