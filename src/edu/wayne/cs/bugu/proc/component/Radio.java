package edu.wayne.cs.bugu.proc.component;

import android.telephony.ServiceState;
import android.telephony.SignalStrength;

import com.android.internal.telephony.PhoneConstants;

import edu.wayne.cs.bugu.proc.Stats;

public class Radio extends Component {
	public boolean mPhoneOn = false;
	public int mSignalStrengthBin = -1;
	public int mPhoneServiceState = -1;
	public int mPhoneSimState = -1;
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calculatePower(Stats st) {
		double radioPower = 0;
		
		//when makeing a phone call
		if(mPhoneOn)
			radioPower += st.powerProfile.getRadioActivePower();
		
		//when scanning mSignalStrengthBin = 0
		if(mSignalStrengthBin != -1)
			radioPower += st.powerProfile.getRadioBinPower(mSignalStrengthBin);
		
		//scanning is energy hungry
		if(mPhoneServiceState == ServiceState.STATE_OUT_OF_SERVICE)
			radioPower += st.powerProfile.getRadioScanningPower();				
		
		st.curDevicePower.radioPower = radioPower;
	}

	@Override
	public void dump(StringBuffer buf) {
		// TODO Auto-generated method stub
		
	}
	
	public void updatePhoneServiceState(int state, int simState){
		mPhoneServiceState = state;
		mPhoneSimState = simState;
//		boolean scanning = false;
		
		if(state == ServiceState.STATE_POWER_OFF) {
            mSignalStrengthBin = -1;
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
		if(state == PhoneConstants.State.IDLE){
			mPhoneOn = false;
    		  //TODO add timer
		}else{
			mPhoneOn = true;
		}
	}

}
