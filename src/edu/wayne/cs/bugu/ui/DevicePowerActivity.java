package edu.wayne.cs.bugu.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import edu.wayne.cs.bugu.R;
import edu.wayne.cs.bugu.monitor.DevicePowerInfo;
import edu.wayne.cs.bugu.monitor.PowerProfilingService;
import edu.wayne.cs.bugu.proc.component.Battery;
import edu.wayne.cs.bugu.proc.component.CPU;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

public class DevicePowerActivity extends Activity {
	private PowerProfilingService buguService = null;
    private boolean mIsBound = false; //indicate whether we have call bind
    private Handler updateHandler = new Handler();
    private int period = 1000;
    
    NumberFormat formatter = new DecimalFormat("#0.00");  
    
    private Runnable   	displayPeriodicTask = new Runnable() {
        public void run(){
            updateDisplay();
            updateHandler.postDelayed(displayPeriodicTask, period);
        }
    };     
    
	private TextView cpu;
	private TextView radio;
	private TextView wifi;
	private TextView media;
	private TextView display;
	private EditText status;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_power);
		
		cpu = (TextView)this.findViewById(R.id.cpuPowerTextView);
		radio = (TextView)this.findViewById(R.id.radioPowerTextView);
		wifi = (TextView)this.findViewById(R.id.wifiPowerTextView);
		media = (TextView)this.findViewById(R.id.mediaPowerTextView);
		display = (TextView)this.findViewById(R.id.displayPowerTextView);	
		status = (EditText)this.findViewById(R.id.statusEditText);
		
		doBindService();
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_power, menu);
		return true;
	}

	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void updateDisplay(){
		if(buguService != null && buguService.isMonitoring()){
			Battery bat = buguService.getStats().sys.battery;		
			DevicePowerInfo dp = buguService.currentDevicePower();
				
			if(dp != null){
				cpu.setText(formatter.format(dp.cpuPower) + " mw");
				wifi.setText(formatter.format(dp.wifiPower * bat.voltage) + " mw");
				radio.setText(formatter.format(dp.radioPower * bat.voltage) + " mw");			
				display.setText(formatter.format(dp.screenPower) + " mw");	
			}
			
			if(bat != null){
				StringBuffer buf = new StringBuffer();
				bat.dump(buf);
				status.setText(buf.toString());
			}
		}
	}
	
    private void doBindService() {
    	getApplicationContext().bindService(new Intent(DevicePowerActivity.this, 
        		PowerProfilingService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnbindService() {
        if (mIsBound) {
        	getApplicationContext().unbindService(mConnection);
        	mIsBound = false;
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
        	buguService = ((PowerProfilingService.LocalBinder)service).getService();
        	mIsBound = true;
    		updateHandler.postDelayed(displayPeriodicTask, period);
        }

        public void onServiceDisconnected(ComponentName className) {
          buguService = null;
          mIsBound = false;
        }
    };
}
