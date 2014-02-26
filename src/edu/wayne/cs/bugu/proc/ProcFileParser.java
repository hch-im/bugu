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
package edu.wayne.cs.bugu.proc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Vector;

import android.os.StrictMode;

public class ProcFileParser {	
    // proc directory
    private static final String PROC_DIR ="/proc";
    private static final FilenameFilter pidNameFilter = new FilenameFilter(){    	
		@Override
		public boolean accept(File dir, String filename) {
    		try{
    			Integer.parseInt(filename);
    		}catch(NumberFormatException nfe){
    			return false;
    		}
    		
    		return true;
		}
    	
    };
	
	public static Vector<Integer> getAllPids(){
		Vector<Integer> pids = new Vector<Integer>();
		File f = new File(PROC_DIR);
		String[] pidStrs = f.list(pidNameFilter);
		
		for(String pid : pidStrs)
			pids.add(Integer.valueOf(pid));
		
		return pids;
	}
	
    private byte[] mBuffer = new byte[4096];
    
    public String readFile(String file, int length) {
    	if(length > 4096) length = 4096;
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            int len = is.read(mBuffer, 0, length);
            is.close();

            if (len > 0) {
                return new String(mBuffer, 0, len);
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
            StrictMode.setThreadPolicy(savedPolicy);
        }
        
        return null;
    } 
}
