package edu.wayne.cs.bugu.proc.component;

import edu.wayne.cs.bugu.proc.Stats;

public class System extends Component{
	public long pageSize;
	public CPU cpu; 
	public Display display;
	public Radio radio;
	public Wifi wifi;
	
	public System(){
		cpu = new CPU();
		display = new Display();
		radio = new Radio();
		wifi = new Wifi();
	}
	
	@Override
	public void init() {
		cpu.init();
		display.init();
	    pageSize = natLib.getPageSize();	    
	}

	@Override
	public void updateState() {
		cpu.updateState();
		display.updateState();
	}

	@Override
	public void dump(StringBuffer buf) {
		if(buf == null) return;
		
		cpu.dump(buf);
		display.dump(buf);
	}

	@Override
	public void calculatePower(Stats st) {
		cpu.calculatePower(st);
		display.calculatePower(st);
	}

}
