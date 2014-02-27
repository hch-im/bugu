package edu.wayne.cs.bugu.proc.component;

import java.io.File;

import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.proc.Stats;

public class Core extends Component {
	public int coreId;
	public int cStatesNumber;
	public long[] baseCStatesPower = null; //milliwatts
	public long[] baseCStatesTime = null;	
	public long[] relCStatesPower = null; //microseconds
	public long[] relCStatesTime = null;	
	
	public long mBaseCPUTime = 0;
	public long mBaseIdleTime = 0;
	
	public long mRelCPUTime;
	public long mRelIdleTime;
	public double cpuUtilization;
	
	public Core(int id){
		coreId = id;
	}

	@Override
	public void init() {
		parseCStateNumber();
		baseCStatesPower = new long[cStatesNumber];
		baseCStatesTime = new long[cStatesNumber];	
		relCStatesPower = new long[cStatesNumber];
		relCStatesTime = new long[cStatesNumber];			
	}
	
	@Override
	public void updateState() {
		for(int i = 0; i < cStatesNumber; i++)
			parseCStateOfCore(i);
	}
	
	public boolean updateCoreCPUTime(long[] data){
		if(data == null || data.length < 7)
			return false;

		long cpu = data[0] + data[1] + data[2] + data[5] + data[6];
		long idle = data[3] + data[4];
		
		mRelCPUTime = cpu - mBaseCPUTime;
		mRelIdleTime = idle - mBaseIdleTime;
		
		//user time
		mBaseCPUTime = cpu;
		mBaseIdleTime = idle;
				
		cpuUtilization = 100.0 * mRelCPUTime / (mRelCPUTime + mRelIdleTime);
		return true;
	}
	
	@Override
	public void dump(StringBuffer buf) {
		if(Constants.DEBUG_CORE){
			buf.append("core").append(coreId).append("-power ");
			for(int i = 0; i < cStatesNumber; i++){
				buf.append(relCStatesPower[i]).append(" ");
			}
			buf.append("\r\n");
			
			buf.append("core").append(coreId).append("-time ");
			for(int i = 0; i < cStatesNumber; i++){
				buf.append(relCStatesTime[i]).append(" ");
			}			
			buf.append("\r\n");
		}
	}

	@Override
	public void calculatePower(Stats st) {
		// TODO Auto-generated method stub		
	}
	
	/*
	 * Functions to parse core related information.
	 */
	
	/**
	 * Get the number of C states of this core.
	 */
	private void parseCStateNumber(){
		String filename = "/sys/devices/system/cpu/cpu" + coreId + "/cpuidle/";
		File f = new File(filename);
		String[] files = f.list();
		cStatesNumber = files.length;
	}
	
	private void parseCStateOfCore(int stateNum){
		String baseName = "/sys/devices/system/cpu/cpu" + coreId + "/cpuidle/state" + stateNum;		
		
		String str = parser.readFile(baseName + "/power", 32);
		if(str != null){
			long val = Long.valueOf(str.split("\n")[0]);
			relCStatesPower[stateNum] = val - baseCStatesPower[stateNum];
			baseCStatesPower[stateNum] = val;
		}
		
		str = parser.readFile(baseName + "/time", 32);
		if(str != null){
			long val = Long.valueOf(str.split("\n")[0]);
			relCStatesTime[stateNum] = val - baseCStatesTime[stateNum];
			baseCStatesTime[stateNum] = val;
		}		
	}
}
