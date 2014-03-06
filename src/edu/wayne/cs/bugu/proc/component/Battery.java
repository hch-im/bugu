package edu.wayne.cs.bugu.proc.component;

import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.proc.Stats;

public class Battery extends Component {
	public enum BatteryStatus{
		Charging, Discharging, Full, NotCharging, Unknown
	};
	public double voltage;
	public double current;
	public double power; //mw
	public int capacity; //0-100
	public BatteryStatus status;
	//last remembered value of energy when battery became full
	//represents real thresholds, not design values
	public double energyFull; //mAh
	
	@Override
	public void init() {
		energyFull = readIntValueFromFile(SYS_BATTERY_ENERGY_FULL)/1e6; //-> mAh
	}

	@Override
	public void updateState() {
		voltage = readIntValueFromFile(SYS_BATTERY_VOLTAGE)/1e6;//uV->V
		current = readIntValueFromFile(SYS_BATTERY_CURRENT)/1e3;//uA -> mA
		capacity = readIntValueFromFile(SYS_BATTERY_CAPACITY);//0-100
		status = getBatteryStatus(readStringValueFromFile(SYS_BATTERY_STATUS));
	}

	@Override
	public void calculatePower(Stats st) {
		power = voltage * current;
		st.curDevicePower.batteryPower = power;
	}

	@Override
	public void dump(StringBuffer buf) {
		if(Constants.DEBUG_BATTERY){
			buf.append("voltage: ").append(decFormatter.format(voltage)).append("V\t");
			buf.append("current: ").append(decFormatter.format(current)).append("mA\r\n");
			
			buf.append("power: ").append(decFormatter.format(power)).append("mw\t");
			buf.append("capacity: ").append(capacity).append("\r\n");
			
			buf.append("status: ").append(status.toString()).append("\t");
			buf.append("Energy: ").append(decFormatter.format(energyFull)).append("mAh\r\n");
		}
	}

	private BatteryStatus  getBatteryStatus(String status)
	{
	    switch (status.charAt(0)) {
	        case 'C': return BatteryStatus.Charging;         // Charging
	        case 'D': return BatteryStatus.Discharging;      // Discharging
	        case 'F': return BatteryStatus.Full;             // Full
	        case 'N': return BatteryStatus.NotCharging;      // Not Charging
	        case 'U': return BatteryStatus.Unknown;          // Unknown	            
	        default: {
	            return BatteryStatus.Unknown;
	        }
	    }
	}
	
	private static final String SYS_BATTERY_VOLTAGE = "/sys/class/power_supply/battery/voltage_now";
	private static final String SYS_BATTERY_CURRENT = "/sys/class/power_supply/battery/current_now";
	private static final String SYS_BATTERY_CAPACITY = "/sys/class/power_supply/battery/capacity";	
	private static final String SYS_BATTERY_STATUS = "/sys/class/power_supply/battery/status";		
	private static final String SYS_BATTERY_ENERGY_FULL = "/sys/class/power_supply/battery/energy_full";	
}
