package edu.wayne.cs.bugu.proc.component;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import edu.wayne.cs.bugu.proc.ProcFileParser;
import edu.wayne.cs.bugu.proc.Stats;
import edu.wayne.cs.bugu.util.NativeLib;

public abstract class Component {
	protected ProcFileParser parser;
	protected NativeLib natLib;	
	protected final NumberFormat decFormatter = new DecimalFormat("#0.00");  
	
	public Component(){
		parser = new ProcFileParser();
		natLib = new NativeLib();
	}
	
	/**
	 * Init the basic information of the component.
	 * Must be called before the states of the the
	 * component was updated.
	 */
	public abstract void init();
	
	/**
	 * Retrieve the latest state and usage informat
	 * -ion of the component. Invoked in each time
	 * interval.
	 */
	public abstract void updateState(long relTime);

	/**
	 * calculate the power of the component.
	 * @param st
	 */
	public abstract void calculatePower(Stats st);
	
	/**
	 * Dump the informatin of the component.
	 */
	public abstract void dump(StringBuffer buf);
	
	protected int readIntValueFromFile(String file){
		String str = parser.readFile(file, 32);
		if(str == null)
			return 0;
		
		int val = Integer.valueOf(str.split("\n")[0]);				
		return val;
	}
	
	protected long readLongValueFromFile(String file){
		String str = parser.readFile(file, 64);
		if(str == null)
			return 0;
		
		long val = Long.valueOf(str.split("\n")[0]);				
		return val;
	}
	
	protected String readStringValueFromFile(String file){
		String str = parser.readFile(file, 128);
		if(str == null)
			return null;
		
		return str.split("\n")[0];				
	}	
}
