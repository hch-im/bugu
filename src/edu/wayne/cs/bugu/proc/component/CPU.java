package edu.wayne.cs.bugu.proc.component;

import edu.wayne.cs.bugu.Constants;
import edu.wayne.cs.bugu.proc.Stats;

public class CPU extends Component {
	public int coreNumber;
	public int[] cpuFrequencies;
	
	public long mBaseUserTime = 0;
	public long mBaseSysTime = 0;
	public long mBaseIOWaitTime = 0;
	public long mBaseIdleTime = 0;
	public long[] mBaseCpuSpeedTimes = null;
	//relative time in 10 milli sconds
	//system cpu usage = (user + sys + irq + softirq)/(user + sys + io + irq + softirq + idle)		
	public long mRelUserTime;
	public long mRelSysTime;
	public long mRelCPUTime;
	public long mRelIOWaitTime;
	public long mRelIdleTime;
	
	public long[] mRelCpuSpeedTimes = null;		
	public int mCurCPUFrequency;
	public double cpuUtilization;
	
	public Core[] cores;
	
	/**
	 * Init CPU stat. This method must be invoked after core number and cpu frequencies were parsed.
	 */
	public void init(){
		coreNumber = this.readIntValueFromFile(MAX_CORE_ID) + 1;
		parseAvailableFrequencies();
		
		mBaseCpuSpeedTimes = new long[cpuFrequencies.length];
		mRelCpuSpeedTimes = new long[cpuFrequencies.length];				
		cores = new Core[coreNumber];
		for(int i = 0; i < coreNumber; i++){
			cores[i] = new Core(i);
			cores[i].init();
		}
	}
	
	@Override
	public void updateState() {
		parseCPUSpeedTimes();
		parseProcStat(); //invoke after parse cpu speed step times		
		mCurCPUFrequency = this.readIntValueFromFile(SYS_CPU_FREQUENCY);	
		//TODO C state power seems not useful
//		for(int i = 0; i < coreNumber; i++){
//			cores[i].updateState();
//		}
	}
	
	private long getSpeedStepTotalTime(){
		long total = 0;
		for(long t:mRelCpuSpeedTimes)
			total += t;
		
		return total;
	}
	
	private boolean updateCPUTime(long[] data){
		if(data == null || data.length < 7)
			return false;
//		Log.i("Bugu", "time " + data[0] + " " + data[1] + " " + data[2] + " " + data[3] + " " + data[4] + " " + data[5] + " " + data[6]);						
		long usr = data[0] + data[1];
		long sys = data[2] + data[5] + data[6];
		
		long deltaIOW = data[4] - mBaseIOWaitTime;
		//this happen when io on going
		if(deltaIOW < 0){
			deltaIOW = mRelIOWaitTime;//use last io wait time
		}
		
		long deltaIdle = data[3] - mBaseIdleTime;
		if(Math.abs(deltaIdle) > getSpeedStepTotalTime() * 10){
			deltaIdle = mRelIdleTime;
		}
//		Log.i("Bugu", "time " + mRelTime + " - " + usr + " " + sys + " " + total + " " + delta);			
		// total user time = user time + nice time
		mRelUserTime = usr - mBaseUserTime;
		mRelSysTime = sys - mBaseSysTime;
		mRelCPUTime = mRelUserTime + mRelSysTime + deltaIOW + deltaIdle;
		mRelIOWaitTime = deltaIOW;			
		mRelIdleTime = deltaIdle;
		
		//user time
		mBaseUserTime = usr;
		mBaseSysTime = sys;
		mBaseIOWaitTime = data[4];
		mBaseIdleTime = data[3];
				
		cpuUtilization = 100.0 * (mRelUserTime + mRelSysTime) / mRelCPUTime;
		return true;
	}
	
	@Deprecated
	public int indexOfFrequency(int frequency){
		int index = -1;
		for(int i = 0; i < cpuFrequencies.length; i++){
			if(cpuFrequencies[i] == frequency){
				index = i;
				break;
			}
		}
		
		return index;
	}	
	
	/*
	 * Functions for parse CPU related information.
	 */
	
	private static final String MAX_CORE_ID = "/sys/devices/system/cpu/kernel_max";
    private final String SYS_CPU_FREQUENCY = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
	
	private static final String FREQ_AVAILABLE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	/**
	 * Get the available frequencies of the processor.
	 * @param cpu
	 */
	private void parseAvailableFrequencies(){
		String str = parser.readFile(FREQ_AVAILABLE, 256);
		if(str == null)
			return;
		
		String[] frestrs = str.split(" ");
		int[] freqs = new int[frestrs.length - 1];
		for(int i = 0; i < freqs.length; i++)
			freqs[i] = Integer.valueOf(frestrs[i]);
		
		cpuFrequencies = freqs;
	}
	
    private final String SYS_CPU_SPEED_STEPS = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    /**
     * Get the time that the CPU worked in each frequency.
     */
	private void parseCPUSpeedTimes(){
		String str = parser.readFile(SYS_CPU_SPEED_STEPS, 512);
		if(str == null)
			return;
		
		String[] vals;
		int index = 0;
		long time;
		
//		StringBuffer buf = new StringBuffer("speedstep");
		
		for(String token : str.split("\n")){
			if(str.trim().length() == 0) continue;
			try{
				vals = token.split(" ");
				time = Long.valueOf(vals[1]);
//				buf.append(" ").append(time);
				mRelCpuSpeedTimes[index] = time - mBaseCpuSpeedTimes[index];
				mBaseCpuSpeedTimes[index] = time;
			}catch (NumberFormatException nfe){
				nfe.printStackTrace();
			}finally{
				index++;
			}
		}
		
//		Log.i("Bugu", buf.toString());		
	}	
	
	private static final String PROC_STAT = "/proc/stat";
	/**
	 * Get CPU times.
	 * @return
	 */
	private void parseProcStat(){
		//When using tickless kernel, idle/iowait accounting are not doing.
		//So the value might get outdated. 		
//		if(Process.readProcFile(PROC_STAT, PROC_STAT_FORMAT, null, mProcStatLong, null)){
//			if(cpuStat.updateCPUTime(mProcStatLong))
//				return true;
//		}
		
		String str = parser.readFile(PROC_STAT, 512);
		if(str == null)
			return;		

		int lineNo = 0;
		long[] times = new long[7];
		for(String line : str.split("\n")){
			String[] strs = line.substring(5).split(" ");
//			Log.i("Bugu", line);
			for(int i = 0; i < times.length; i++)
				times[i] = Long.valueOf(strs[i]);
			
			if(lineNo == 0)
				updateCPUTime(times);	
			else if(lineNo <= coreNumber)
				cores[lineNo - 1].updateCoreCPUTime(times);
			
			if(lineNo == coreNumber)
				break;
			
			lineNo++;
		}
	}

	@Override
	public void dump(StringBuffer buf) {
		if(Constants.DEBUG_CPU)
			buf.append("utilization " + cpuUtilization + " " + (mRelUserTime + mRelSysTime) + " " + mRelCPUTime + " " + mCurCPUFrequency/1000);

		for(int i = 0; i < coreNumber; i++)
			cores[i].dump(buf);
	}

	@Override
	public void calculatePower(Stats st) {
		//power model 1
		double eng = 0;
		for(int i = 0; i < cpuFrequencies.length; i++){
			eng += (mRelCpuSpeedTimes[i] * st.powerProfile.getCPUSpeedStepPower(i));
		}
		
		double power = (eng * cpuUtilization /getSpeedStepTotalTime());
		st.curDevicePower.cpuPower = power;
		
		//power model 2
//		long totalTime = getSpeedStepTotalTime();
//		double basePower = st.powerProfile.getCPUSpeedStepPower(0);
//		double eng = totalTime * basePower;
//		for(int i = 1; i < cpuFrequencies.length; i++)
//			eng += (mRelCpuSpeedTimes[i] * (st.powerProfile.getCPUSpeedStepPower(i) - basePower));
//		
//		st.curDevicePower.cpuPower = eng / totalTime;
	}	
}
