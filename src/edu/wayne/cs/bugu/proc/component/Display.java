package edu.wayne.cs.bugu.proc.component;

import edu.wayne.cs.bugu.proc.Stats;

public class Display extends Component {
	public long mBaseScreenOffTime = 0;	
	public long mRelScreenOffTime;
	public static final int BRIGHTNESS_BIN_NUMBER = 51; //bin size is 5
	private long[] mScreenBrightnessBinTimes;
	public int mRelScreenBrightness;//0 means screen is off, 1-255 means screen is on
	
	@Override
	public void init() {
		mScreenBrightnessBinTimes = new long[BRIGHTNESS_BIN_NUMBER];
	}

	@Override
	public void updateState(long relTime) {
		mRelScreenBrightness = readIntValueFromFile(SYS_LEDS_BRIGHTNESS);
	}
	
	public void postUpdateScreenBrightness(long relTime){
		if(mRelScreenBrightness == 0){//screen off
			mRelScreenOffTime = relTime;
			mBaseScreenOffTime += relTime;
		}else{
			int bin = (mRelScreenBrightness - 1) / 5;
			mScreenBrightnessBinTimes[bin] += relTime;
		}
	}

	@Override
	public void dump(StringBuffer buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calculatePower(Stats st) {
		if(mRelScreenBrightness == 0){
			st.curDevicePower.screenPower = 0;
			return;
		}else{		
			//P1 + (P255-P1)/254*(brightness - 1)
			st.curDevicePower.screenPower = st.powerProfile.getScreenOnPower() +
					(st.powerProfile.getScreenFullPower() - st.powerProfile.getScreenOnPower()) 
					/ 254 * (mRelScreenBrightness - 1);
		}
	}		
	
	/*
	 * Functions for parse display related information.
	 */
    private final String SYS_LEDS_BRIGHTNESS = "/sys/class/leds/lcd-backlight/brightness";
}
