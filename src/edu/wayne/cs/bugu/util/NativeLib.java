/*
 *   Copyright (C) 2014, Mobile and Internet Systems Laboratory.
 *   All rights reserved.
 *
 *   Authors: Hui Chen (hchen229@gmail.com)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.
 */
package edu.wayne.cs.bugu.util;

public class NativeLib {
	
	static {
		try{
			System.loadLibrary("bugu_lib");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * returns the idle time of CPU.
	 * @param cpuNum
	 * @return
	 */
	public native long getCPUIdleTime(int cpuNum);

	/**
	 * returns the io wait time of CPU.
	 * @param cpuNum
	 * @return
	 */
	public native long getCPUIOWaitTime(int cpuNum);	
	
	/**
	 * return the page size.
	 * @return
	 */
	public native long getPageSize();
}
