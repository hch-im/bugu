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
		parseCoreNumber();
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
		parseCurrentCPUFrequency();		
		//TODO C state power seems not useful
//		for(int i = 0; i < coreNumber; i++){
//			cores[i].updateState();
//		}
	}
	
	public long getSpeedStepTotalTime(){
		long total = 0;
		for(long t:mRelCpuSpeedTimes)
			total += t;
		
		return total;
	}
	
	public boolean updateCPUTime(long[] data){
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
	/**
	 * Get the number of cores.
	 * @param cpu
	 */
	public void parseCoreNumber(){
		String str = parser.readFile(MAX_CORE_ID, 8);
		if(str == null)
			return;
		
		int num = Integer.valueOf(str.split("\n")[0]) + 1;
		coreNumber = num;
	}	
	
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
	
    private final String SYS_CPU_FREQUENCY = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    /**
     * Get current CPU frequency.
     */
	private void parseCurrentCPUFrequency(){
		String str = parser.readFile(SYS_CPU_FREQUENCY, 8);
		if(str == null)
			return;
		
		mCurCPUFrequency = Integer.valueOf(str.split("\n")[0]);
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
		double eng = 0;
		for(int i = 0; i < cpuFrequencies.length; i++){
			eng += (mRelCpuSpeedTimes[i] * st.powerProfile.getCPUSpeedStepPower(i));
		}
		
		double power = (eng * cpuUtilization /getSpeedStepTotalTime());
		st.curDevicePower.cpuPower = power;
	}	
}
