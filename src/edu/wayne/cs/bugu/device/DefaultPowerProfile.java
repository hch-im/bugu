package edu.wayne.cs.bugu.device;

import com.android.internal.os.PowerProfile;

import android.content.Context;

public class DefaultPowerProfile extends BasePowerProfile{

	protected DefaultPowerProfile(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
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

}
