package edu.wayne.cs.bugu.device;

import com.android.internal.os.PowerProfile;

import android.content.Context;

public class DefaultPowerProfile extends BasePowerProfile{

	protected DefaultPowerProfile(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
//		int stepNum = profile.getNumSpeedSteps();
//		speedStepPowerRatios = new double[stepNum];
//		double min = profile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, 0);
//		double max = profile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, stepNum - 1);
//		//for different platform modify alpha and beta to calibrate the power model
//		double alpha = 0.2;
//		double beta = 1.08;
//		
//		for(int i = 0; i < stepNum; i++){
//			double stepPower = profile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, i);
////			speedStepPowerRatios[i] = (min/10 + (stepPower - min)/(max - min) * alpha) * beta;
//			speedStepPowerRatios[i] = stepPower * AVERAGE_VOLTAGE;
//		}
//		
//		Arrays.sort(speedStepPowerRatios);		
	}

	@Override
	public double getCPUSpeedStepPower(int step) {
		double power = profile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, step);		
		return power / 10;
	}

	@Override
	public double getScreenOnPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getScreenFullPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRadioActivePower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRadioBinPower(int bin) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRadioScanningPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfSpeedStep() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getScreenBinPower(int bin) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCPUIdlePower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCPUPowerOfUtilization(double utilize) {
		// TODO Auto-generated method stub
		return 0;
	}

}
