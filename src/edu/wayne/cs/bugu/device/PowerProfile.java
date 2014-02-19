package edu.wayne.cs.bugu.device;

import android.os.Build;

public class PowerProfile {
	public static PowerProfile getPowerProfileOfDevice(){
		String dev = Build.DEVICE;
		
		if(dev.equals("mako"))
			return new MakoPowerProfile();
		else
			return new DefaultPowerProfile();
	}
}
