package edu.wayne.cs.bugu.device;

import com.android.internal.os.PowerProfile;

import edu.wayne.cs.bugu.proc.component.Radio.FourGState;
import edu.wayne.cs.bugu.proc.component.Radio.ThreeGState;

import android.content.Context;
import android.os.Build;

public abstract class BasePowerProfile {
	protected PowerProfile profile;
	protected double[] speedStepPowerRatios;
	protected double cpuIdlePower;
	protected double cpuMinPower;
	protected double cpuMaxPower;	
    //screen
	protected double[] screenBinPower;
	protected double screenOnPower;
	protected double screenFullPower;
	
	protected BasePowerProfile(Context context){
		profile = new PowerProfile(context);
	}
	
	public static BasePowerProfile getPowerProfileOfDevice(Context context){
		String dev = Build.DEVICE;
		
		if(dev.equals("mako"))
			return new MakoPowerProfile(context);
		else
			return new DefaultPowerProfile(context);
	}
	
	public int getNumSpeedSteps(){
		return profile.getNumSpeedSteps();
	}
	
	public abstract double getCPUSpeedStepPower(int step);
	public abstract double getCPUIdlePower();
	public abstract double getCPUPowerOfUtilization(double utilize);
	public abstract int getNumberOfSpeedStep();
	public abstract double getScreenOnPower();
	public abstract double getScreenFullPower();
	public abstract double getScreenBinPower(int bin);	
	public abstract double getRadioActivePower();
	public abstract double getRadioBinPower(int bin);
	public abstract double getRadioScanningPower();	
	public abstract double getPowerOf3GState(ThreeGState state);
	public abstract double getPowerOf4GState(FourGState state);	
}
