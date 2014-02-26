package edu.wayne.cs.bugu.device;

import java.util.Arrays;

import com.android.internal.os.PowerProfile;

import android.content.Context;

public class MakoPowerProfile extends BasePowerProfile {
    public static final double IO_READ_POWER_PER_BYTE = 0.092; // mJ/kb
    public static final double IO_WRITE_POWER_PER_BYTE = 0.564; //mJ/kb
    
	protected MakoPowerProfile(Context context) {
		super(context);
		
		int stepNum = profile.getNumSpeedSteps();
		speedStepPowerRatios = new double[stepNum];
		double min = profile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, 0);
		double max = profile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, stepNum - 1);
		//for different platform modify alpha and beta to calibrate the power model
		double alpha = 0.2;
		double beta = 1.08;
		
		for(int i = 0; i < stepNum; i++){
			double stepPower = profile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, i);
			speedStepPowerRatios[i] = (min/10 + (stepPower - min)/(max - min) * alpha) * beta;
		}
		
		Arrays.sort(speedStepPowerRatios);
	}

	@Override
	public double getCPUSpeedStepPower(int step) {
		return speedStepPowerRatios[step];
	}

	@Override
	public double getScreenOnPower() {
		double power = profile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
		return power;
	}

	@Override
	public double getScreenFullPower() {
		double power = profile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
		return power;
	}

	@Override
	public double getRadioActivePower() {
		double power = profile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE);
		return power;
	}

	@Override
	public double getRadioBinPower(int bin) {
		double power = profile.getAveragePower(PowerProfile.POWER_RADIO_ON, bin);		
		return power;
	}

	@Override
	public double getRadioScanningPower() {
		double power = profile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING);		
		return power;
	}

	@Override
	public int getNumberOfSpeedStep() {
		return profile.getNumSpeedSteps();
	}

}
