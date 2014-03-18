package edu.wayne.cs.bugu.proc.component;

import java.util.HashMap;

import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneConstants.DataState;

import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.proc.Stats;

public class Radio extends Component {
	public PhoneConstants.State mPhoneState = PhoneConstants.State.IDLE;
	public int mSignalStrengthBin = -1; //-1: power off, 0: scanning 1-4: real signal strength
	public int mPhoneServiceState = -1;
	public int mPhoneSimState = -1;
	
	public String iface = null;
	public PhoneConstants.DataState mDataState;
	public long mBaseBytesReceived;
	public long mBaseBytesTransmitted;
	public long mRelBytesReceived;
	public long mRelBytesTransmitted;
	public long mBasePacketsReceived;
	public long mBasePacketsTransmitted;
	public long mRelPacketsReceived;
	public long mRelPacketsTransmitted;
	public double throughput;
	public int mNetworkClass = TelephonyManager.NETWORK_CLASS_UNKNOWN; //3g or 4g
	public int mNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
	public String mNetworkOperator = null;
	
	public enum ThreeGState {UNKNOWN, IDLE, DCH, FACH};
	private ThreeGState mThreeGState = ThreeGState.UNKNOWN;
	public enum FourGState {UNKNOWN, IDLE, CON_RECP, SHORT_DRX, LONG_DRX};
	private FourGState mFourGState = FourGState.UNKNOWN;
	private long timePassed = 0;
	private int[] dataParams;
	private HashMap<String, int[]> tailTimeMap;

	@Override
	public void init() {
		tailTimeMap = new HashMap<String, int[]>();
		int[] attTail = {1000, 100, 0};//in ticks, ATT HSPA+
		tailTimeMap.put("AT&T", attTail);
		int[] tmTail = {500, 100, 100};
		tailTimeMap.put("T-Mobile", tmTail);		
	}

	@Override
	public void updateState(long relTime) {
		parseMobileData(relTime);
	}

	@Override
	public void calculatePower(Stats st) {
		double radioPower = 0;
		
		//when makeing a phone call
		if(mPhoneState == PhoneConstants.State.OFFHOOK || mPhoneState == PhoneConstants.State.RINGING)
			radioPower += st.powerProfile.getRadioActivePower();
		
		//when scanning mSignalStrengthBin = 0
		if(mSignalStrengthBin != -1)
			radioPower += st.powerProfile.getRadioBinPower(mSignalStrengthBin);
		
		//scanning is energy hungry
		if(mPhoneServiceState == ServiceState.STATE_OUT_OF_SERVICE)
			radioPower += st.powerProfile.getRadioScanningPower();				

		if(mDataState == DataState.CONNECTED 
				|| mDataState == DataState.SUSPENDED 
				|| mDataState == DataState.CONNECTING){// 3g/4g is on
			if(mNetworkClass == TelephonyManager.NETWORK_CLASS_3_G){
				radioPower += st.powerProfile.getPowerOf3GState(mThreeGState);
			}else if(mNetworkClass == TelephonyManager.NETWORK_CLASS_4_G){
				radioPower += st.powerProfile.getPowerOf4GState(mFourGState);				
			}		
		}
		st.curDevicePower.radioPower = radioPower;
	}

	@Override
	public void dump(StringBuffer buf) {
		if(Constants.DEBUG_RADIO){
			buf.append("\r\nphone state: ").append(mPhoneState.toString())
			.append(" signal strength bin: ").append(mSignalStrengthBin)
			.append(" service state: ").append(mPhoneServiceState)
			.append(" sim state: ").append(mPhoneSimState);
			
			buf.append("\r\niface: ").append(iface)
			.append(" recv: ").append(mRelBytesReceived)
			.append(" trans: ").append(mRelBytesTransmitted)
			.append(" recvpackets: ").append(mRelPacketsReceived)
			.append(" transpackets: ").append(mRelPacketsTransmitted)
			.append(" throughput: ").append(throughput);
			
			buf.append("\r\nnetwork type : " ).append(this.mNetworkType)
			.append(" class: ").append(this.mNetworkClass)
			.append(" operator: ").append(this.mNetworkOperator);
		}
	}
	
	public void updatePhoneServiceState(int state, int simState){
		mPhoneServiceState = state;
		mPhoneSimState = simState;
//		boolean scanning = false;
		
		if(state == ServiceState.STATE_POWER_OFF) {
            mSignalStrengthBin = -1;
            mPhoneState = PhoneConstants.State.IDLE;
		}			
		else if (state == ServiceState.STATE_OUT_OF_SERVICE) {
//            scanning = true;
            mSignalStrengthBin = SignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN;	            
		}
	}
	
	public void updateSignalStrengthChange(int bin){
		mSignalStrengthBin = bin;
	}
	
	public void updatePhoneState(PhoneConstants.State state){
		mPhoneState = state;
	}

	public void updateDataConnectionState(PhoneConstants.DataState dataState, String ifs){
		mDataState = dataState;
		//update mobile data when iface change
		if(iface == null || !iface.equals(ifs)){
			iface = ifs;
			parseMobileData(0);
		}
	}
	
	public void updateNetworkInfo(int type, int clas, String opt){
		this.mNetworkType = type;
		this.mNetworkClass = clas;
		this.mNetworkOperator = opt;
		dataParams = tailTimeMap.get(opt);
		if(dataParams == null) 
			dataParams = tailTimeMap.get("T-Mobile");
	}
	
	private synchronized void parseMobileData(long mRelTime){
		if(iface == null) return;
		
		long recv = readLongValueFromFile("/sys/class/net/" + iface + "/statistics/rx_bytes");
		long trans = readLongValueFromFile("/sys/class/net/" + iface + "/statistics/tx_bytes");
		mRelBytesReceived = recv - mBaseBytesReceived;
		mRelBytesTransmitted = trans - mBaseBytesTransmitted;
		mBaseBytesReceived = recv;
		mBaseBytesTransmitted = trans;
		boolean dataActive = (mRelBytesReceived + mRelBytesTransmitted) > 0;
		throughput = (mRelBytesReceived + mRelBytesTransmitted) * 100.0 / mRelTime; // bytes/s
				
		long recvPackets = readLongValueFromFile("/sys/class/net/" + iface + "/statistics/rx_packets");
		long transPackets = readLongValueFromFile("/sys/class/net/" + iface + "/statistics/tx_packets");
		mRelPacketsReceived = recvPackets - mBasePacketsReceived;
		mRelPacketsTransmitted = transPackets - mBasePacketsTransmitted;
		mBasePacketsReceived = recvPackets;
		mBasePacketsTransmitted = transPackets;

		if(mNetworkClass == TelephonyManager.NETWORK_CLASS_3_G){
			// 3g state machine.
			
			switch(mThreeGState){
				case UNKNOWN: //start from UNKNOWN
						mThreeGState = ThreeGState.IDLE; 
					break;
				case IDLE:
					if(dataActive){ // if any data to sent <check queue size?>, goto DCH
						mThreeGState = ThreeGState.DCH;
					}
					break;
				case DCH:
					if(dataActive){
						timePassed = 0;
					}else{
						timePassed += mRelTime;
						if(timePassed >= dataParams[0]){//inactive for tail time
							mThreeGState = ThreeGState.FACH;
							timePassed = 0;
						}
					}
					break;
				case FACH:
					if(dataActive){
						//upload queue > threshold or download queue > threshold
						//could get queue size, here we use packets insted
						if(mRelPacketsReceived > 0 || mRelPacketsTransmitted > 0){
							mThreeGState = ThreeGState.DCH;
						}
						timePassed = 0;
					}else{
						timePassed += mRelTime;
						if(timePassed >= dataParams[1]){//inactive for tail time
							mThreeGState = ThreeGState.IDLE;
						}
					}					
					break;
			}
			if(Constants.DEBUG_RADIO)
				Log.i(Constants.APP_TAG, "3g state: " + mThreeGState.toString() + " active: " + dataActive);
		}else if(mNetworkClass == TelephonyManager.NETWORK_CLASS_4_G){
			
		}
	}
}
