package edu.wayne.cs.bugu.proc.component;

import android.net.wifi.WifiManager;
import android.util.Log;
import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.proc.Stats;

public class Wifi extends Component {
	public boolean mWifiOn = false;
	public boolean mWifiScan = false;
	public long rxp,txp, rxb,txb;
	private long preRxp, preTxp, preRxb, preTxb;
	
	//TODO: mWifiScan most time is false, it seems scan time is too short to record Scan start.
	// the mWifiScan state is not standard changed, defined some constant in PowerProfilingService.

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
		if(mWifiScan){
			if(mWifiOn)
				st.curDevicePower.wifiPower = st.powerProfile.getWiFiScanPower()[0];
			else
				st.curDevicePower.wifiPower = st.powerProfile.getWiFiScanPower()[1];

		}else{
			if(!mWifiOn)
				return;
		
			
		// coefficient values from devscope
		long deltaTbytes = txb-preTxb;
		long deltaRbytes = rxb-preRxb;
		long deltaTPkg = txp-preTxp;
		long deltaRPkg = rxp-preRxp;
		if(deltaTbytes>=deltaRbytes){
			if(deltaTPkg >= st.powerProfile.getWiFiUploadInfo()[0]){
				st.curDevicePower.wifiPower= deltaTPkg*st.powerProfile.getWiFiUploadParam()[0]+st.powerProfile.getWiFiUploadInfo()[1];
			}
			else
				st.curDevicePower.wifiPower= deltaTPkg*st.powerProfile.getWiFiUploadParam()[1]+st.powerProfile.getWiFiUploadInfo()[2];
			
		}
			
		else{
			if(deltaRPkg >= st.powerProfile.getWiFiDownloadInfo()[0]){
				st.curDevicePower.wifiPower= deltaRPkg*st.powerProfile.getWiFiDownloadParam()[0]+st.powerProfile.getWiFiDownloadInfo()[1];
			}
			else
				st.curDevicePower.wifiPower= deltaRPkg*st.powerProfile.getWiFiDownloadParam()[1]+st.powerProfile.getWiFiDownloadInfo()[2];
			
			}
		}//st.powerProfile.
		
	}

	@Override
	public void dump(StringBuffer buf) {
		// TODO Auto-generated method stub
		long time = java.lang.System.currentTimeMillis();
		if(Constants.DEBUG_WIFI){
			buf.append("rxp " + String.valueOf(rxp) +" txp " + String.valueOf(txp)+" rxb " + String.valueOf(rxb) +" txb " + String.valueOf(txb)+ "\r\n");
			buf.append("WiFi on " + mWifiOn + " WiFiScan " + mWifiScan + "\r\n");
		
		}
		
	}

	public void updateWifiState(int wifiState, int scanState){
        if (wifiState == WifiManager.WIFI_STATE_ENABLED ||
        		wifiState == WifiManager.WIFI_AP_STATE_ENABLED) {
        	mWifiOn = true;
        }else if(wifiState == WifiManager.WIFI_STATE_DISABLED || 
        		wifiState == WifiManager.WIFI_AP_STATE_DISABLED){
        	mWifiOn = false;
        }
        
        if (scanState == 2) {
        	mWifiScan = true;
        }else if(wifiState == 3){
        	mWifiScan = false;
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
