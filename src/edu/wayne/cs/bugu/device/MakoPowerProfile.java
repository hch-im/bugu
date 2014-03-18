package edu.wayne.cs.bugu.device;

import java.util.Arrays;

import com.android.internal.os.PowerProfile;

import edu.wayne.cs.bugu.proc.component.Display;
import edu.wayne.cs.bugu.proc.component.Radio.FourGState;
import edu.wayne.cs.bugu.proc.component.Radio.ThreeGState;

import android.content.Context;
/**
 * Power profile for Mako. The unit of all the power values are in milliwatt.
 * @author hchen
 */
public class MakoPowerProfile extends BasePowerProfile {
    public static final double IO_READ_POWER_PER_BYTE = 0.092; // mJ/kb
    public static final double IO_WRITE_POWER_PER_BYTE = 0.564; //mJ/kb  
    public static final double AVERAGE_VOLTAGE = 3.800;

    private static final double CPU_ACTIVE_POWER_RANGE = 4162.04;
    private static final double[] CPU_STATIC_POWER= { 271.65, 269.6, 273.65, 279.94, 257.70, 243.46, 
    													255.67, 250.28, 244.32, 248.17, 241.60, 229.85};
    private double cpuStaticParam = 1.0;
    private double cpuDynamicParam = 0.934;
    
    private double screenMinParam = 1.53; //calibrate the range of screen base power
    private double screenMaxParam = 1.21;
    
    private double[] THREE_G_POWER =  {800, 300, 0}; //the power of DCH, PACH and IDLE state in each signal strength
    
	protected MakoPowerProfile(Context context) {
		super(context);
		
		int stepNum = profile.getNumSpeedSteps();
		speedStepPowerRatios = new double[stepNum];
		for(int i = 0; i < stepNum; i++){
			speedStepPowerRatios[i] = CPU_STATIC_POWER[i] * cpuStaticParam;
		}
		Arrays.sort(speedStepPowerRatios);	
		
		cpuIdlePower = profile.getAveragePower(PowerProfile.POWER_CPU_IDLE) * AVERAGE_VOLTAGE;
		cpuFullActivePower = CPU_ACTIVE_POWER_RANGE * cpuDynamicParam;
		//screen
		screenOnPower = profile.getAveragePower(PowerProfile.POWER_SCREEN_ON) * AVERAGE_VOLTAGE * screenMinParam;
		screenFullPower = profile.getAveragePower(PowerProfile.POWER_SCREEN_FULL) * AVERAGE_VOLTAGE * screenMaxParam;
		screenBinPower = new double[Display.BRIGHTNESS_BIN_NUMBER];
		for(int i = 0; i < Display.BRIGHTNESS_BIN_NUMBER; i++){
			screenBinPower[i] = screenOnPower + (screenFullPower - screenOnPower)/Display.BRIGHTNESS_BIN_NUMBER * (i + 1);
		}
	}

	@Override
	public double getCPUSpeedStepPower(int step) {
		return speedStepPowerRatios[step];
	}

	@Override
	public double getScreenOnPower() {
		return screenOnPower;
	}

	@Override
	public double getScreenFullPower() {
		return screenFullPower;
	}
	
	@Override
	public double getScreenBinPower(int bin){
		if(bin < 0) bin = 0;
		else if(bin > 50) bin = 50;
		
		return screenBinPower[bin];
	}

	@Override
	public double getRadioActivePower() {
		double power = 535;
		return power;
	}

	@Override
	public double getRadioBinPower(int bin) {
		double power = profile.getAveragePower(PowerProfile.POWER_RADIO_ON, bin) * AVERAGE_VOLTAGE;		
		return power;
	}

	@Override
	public double getRadioScanningPower() {
		double power = profile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING) * AVERAGE_VOLTAGE;		
		return power;
	}

	@Override
	public int getNumberOfSpeedStep() {
		return profile.getNumSpeedSteps();
	}

	@Override
	public double getCPUIdlePower() {
		return cpuIdlePower;
	}

	@Override
	public double getCPUPowerOfUtilization(double utilize) {
		return cpuFullActivePower / 100.0 * utilize;
	}

	@Override
	public double getPowerOf3GState(ThreeGState state) {
		
		switch(state){
			case DCH:
				return THREE_G_POWER[0];
			case FACH:
				return THREE_G_POWER[1];
			case IDLE:
			case UNKNOWN:
			default:
				return THREE_G_POWER[2];
		}
	}

	@Override
	public double getPowerOf4GState(FourGState state) {
		// TODO Auto-generated method stub
		return 0;
	}
}

/*
<device name="Android">
    <!-- All values are in mAh except as noted -->
    <item name="none">0</item>
    <item name="screen.on">42.4</item>
    <item name="screen.full">211.6</item>
    <item name="bluetooth.active">59.22</item>
    <item name="bluetooth.on">0.7</item>
    <item name="wifi.on">1.38</item>
    <item name="wifi.active">62.09</item>
    <item name="wifi.scan">52.1</item>
    <item name="dsp.audio">0.1</item>
    <item name="dsp.video">0.1</item>
    <item name="gps.on">20.9</item>
    <item name="radio.active">185.6</item>
    <!-- The current consumed by the radio when it is scanning for a signal -->
    <item name="radio.scanning">122.68</item>
    <!-- Current consumed by the radio at different signal strengths, when paging -->
    <array name="radio.on"> <!-- Strength 0 to BINS-1 -->
        <value>1.16</value>
        <value>2.15</value>
    </array>
    <!-- Different CPU speeds as reported in
         /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state -->
    <array name="cpu.speeds">
        <value>384000</value> <!-- 384 MHz CPU speed -->
        <value>486000</value> <!-- 486 MHz CPU speed -->
        <value>594000</value> <!-- 594 MHz CPU speed -->
        <value>702000</value> <!-- 702 MHz CPU speed -->
        <value>810000</value> <!-- 810 MHz CPU speed -->
        <value>918000</value> <!-- 918 MHz CPU speed -->
        <value>1026000</value> <!-- 1026 MHz CPU speed -->
        <value>1134000</value> <!-- 1134 MHz CPU speed -->
        <value>1242000</value> <!-- 1242 MHz CPU speed -->
        <value>1350000</value> <!-- 1350 MHz CPU speed -->
        <value>1458000</value> <!-- 1458 MHz CPU speed -->
        <value>1512000</value> <!-- 1512 MHz CPU speed -->
    </array>
    <!-- Power consumption when CPU is idle -->
    <item name="cpu.idle">3.5</item>
    <item name="cpu.awake">10.43</item>
    <!-- Power consumption at different speeds -->
    <array name="cpu.active">
        <value>92.6</value> <!-- 384 MHz CPU speed -->
        <value>108.6</value> <!-- 486 MHz CPU speed -->
        <value>118.8</value> <!-- 594 MHz CPU speed -->
        <value>121.4</value> <!-- 702 MHz CPU speed -->
        <value>127.3</value> <!-- 810 MHz CPU speed -->
        <value>133.1</value> <!-- 918 MHz CPU speed -->
        <value>173.3</value> <!-- 1026 MHz CPU speed -->
        <value>209.5</value> <!-- 1134 MHz CPU speed -->
        <value>216.5</value> <!-- 1242 MHz CPU speed -->
        <value>228.5</value> <!-- 1350 MHz CPU speed -->
        <value>236.0</value> <!-- 1458 MHz CPU speed -->
        <value>239.7</value> <!-- 1512 MHz CPU speed -->
    </array>
    <!-- This is the battery capacity in mAh -->
    <item name="battery.capacity">2100</item>
</device>
 */

