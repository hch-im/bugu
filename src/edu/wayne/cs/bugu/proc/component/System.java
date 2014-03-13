package edu.wayne.cs.bugu.proc.component;

import edu.wayne.cs.bugu.proc.Stats;

public class System extends Component{
	public long pageSize;
	public CPU cpu; 
	public Display display;
	public Radio radio;
	public Wifi wifi;
	public Bluetooth bt;
	public Battery battery;
	
	public System(){
		cpu = new CPU();
		display = new Display();
		radio = new Radio();
		wifi = new Wifi();
		bt = new Bluetooth();
		battery = new Battery();
	}
	
	@Override
	public void init() {
		cpu.init();
		display.init();
		radio.init();
		wifi.init();
		bt.init();
		battery.init();
		
	    pageSize = natLib.getPageSize();	    
	}

	@Override
	public void updateState() {
		cpu.updateState();
		display.updateState();
		radio.updateState();
		wifi.updateState();
		bt.updateState();
		battery.updateState();
	}

	@Override
	public void dump(StringBuffer buf) {
		if(buf == null) return;
		
		cpu.dump(buf);
		display.dump(buf);
		radio.dump(buf);
		wifi.dump(buf);
		bt.dump(buf);
		battery.dump(buf);
	}

	@Override
	public void calculatePower(Stats st) {
		cpu.calculatePower(st);
		display.calculatePower(st);
		radio.calculatePower(st);
		wifi.calculatePower(st);
		bt.calculatePower(st);
		battery.calculatePower(st);
	}

}
