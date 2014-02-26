package edu.wayne.cs.bugu.proc.component;

import edu.wayne.cs.bugu.proc.ProcFileParser;
import edu.wayne.cs.bugu.proc.Stats;
import edu.wayne.cs.bugu.util.NativeLib;

public abstract class Component {
	protected ProcFileParser parser;
	protected NativeLib natLib;	
	
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
	public abstract void updateState();

	/**
	 * calculate the power of the component.
	 * @param st
	 */
	public abstract void calculatePower(Stats st);
	
	/**
	 * Dump the informatin of the component.
	 */
	public abstract void dump(StringBuffer buf);
}
