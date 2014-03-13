package edu.wayne.cs.bugu.proc.component;

import static android.os.Process.PROC_OUT_LONG;
import static android.os.Process.PROC_OUT_STRING;
import static android.os.Process.PROC_SPACE_TERM;
import android.os.Process;
import edu.wayne.cs.bugu.proc.Stats;

public class Pid extends Component {
	public int pid;
	public int uid = -1;
	public String state;
	public long mBaseUserTime = 0;
	public long mBaseSysTime = 0;
	public long mBaseMinorFaults = 0;
	public long mBaseMajorFaults = 0;
	//relative
	//process cpu usage = (cpu time) / (system total cpu time)
	public int mRelCPUTime;
	public int mRelPageFaults;
	
	public Pid(int pid){
		this.pid = pid;
	}
	
	public void update(String[] strData, long[] longData){
		if(strData == null || longData == null || strData.length < 5 || longData.length < 5)
			return;
		
		state = strData[0];
		mRelPageFaults = (int)(longData[1] - mBaseMinorFaults) + (int)(longData[2] - mBaseMajorFaults);
		mRelCPUTime = (int)(longData[3] - mBaseUserTime) + (int)(longData[4] - mBaseSysTime);			
		
		mBaseMinorFaults = longData[1];
		mBaseMajorFaults = longData[2];
		mBaseUserTime = longData[3];
		mBaseSysTime = longData[4];
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(long relTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calculatePower(Stats st) {
		// TODO Auto-generated method stub
//		api.cpuPower += dpi.cpuPower * (ps.mRelCPUTime * 1.0/cpu.mRelCPUTime);		
	}

	@Override
	public void dump(StringBuffer buf) {
		// TODO Auto-generated method stub
		
	}

    public static final int[] PROC_PID_STAT_FORMAT = new int[] {
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,    
        PROC_SPACE_TERM|PROC_OUT_STRING,				// 2: state
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 9: minor faults
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 11: major faults
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 13: utime
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 14: stime
    };
    private static final long[] mProcPidStatLong = new long[5];
    private static final String[] mProcPidStatString = new String[5];
    private static final String[] PROC_PID_STATUS_LABELS = {"Uid:"};
    private static final long[] mProcPidStatusLong = new long[1];
    
	public void parseProcPidStat(){
		if(Process.readProcFile("/proc/" + pid + "/stat", 
				PROC_PID_STAT_FORMAT, mProcPidStatString, mProcPidStatLong, null)){
			update(mProcPidStatString, mProcPidStatLong);
		}
	}	
	
	public void parseProcPidStatus(){
		mProcPidStatusLong[0] = -1;
        Process.readProcLines("/proc/" + pid + "/status", PROC_PID_STATUS_LABELS, mProcPidStatusLong);
        if(mProcPidStatusLong[0] != -1)
        	uid = (int) mProcPidStatusLong[0];
	}	
}
