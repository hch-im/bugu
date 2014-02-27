package edu.wayne.cs.bugu.proc.component;

import edu.wayne.cs.bugu.proc.Stats;

public class Display extends Component {
	public long mBaseScreenOffTime = 0;	
	public long mRelScreenOffTime;
	public static final int SCREEN_BRIGHTNESS_BINS = 51; //bin size is 5
	private long[] mScreenBrightnessBinTimes;
	public int mRelScreenBrightness;//0 means screen is off, 1-255 means screen is on
	
	@Override
	public void init() {
		mScreenBrightnessBinTimes = new long[SCREEN_BRIGHTNESS_BINS];
	}

	@Override
	public void updateState() {
		mRelScreenBrightness = this.readIntValueFromFile(SYS_LEDS_BRIGHTNESS);
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
			double screenOnPower = st.powerProfile.getScreenOnPower();
			double screenFullPower = st.powerProfile.getScreenFullPower();
			int bin = (st.sys.display.mRelScreenBrightness - 1) / 5;
			st.curDevicePower.screenPower = (screenFullPower - screenOnPower) * bin
	                			/ (Display.SCREEN_BRIGHTNESS_BINS - 1)
	                			+ screenOnPower;		
		}
	}		
	
	/*
	 * Functions for parse display related information.
	 */
    private final String SYS_LEDS_BRIGHTNESS = "/sys/class/leds/lcd-backlight/brightness";
}
