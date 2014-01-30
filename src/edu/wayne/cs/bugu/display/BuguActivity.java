/*
 *   Copyright (C) 2012, Mobile and Internet Systems Laboratory.
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
package edu.wayne.cs.bugu.display;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

import edu.wayne.cs.bugu.db.RecordDAO;
import edu.wayne.cs.bugu.db.SqliteHelper;
import edu.wayne.cs.bugu.db.model.Record;
import edu.wayne.cs.bugu.monitor.BuguReceiver;
import edu.wayne.cs.bugu.monitor.PowerProfilingService;
import edu.wayne.cs.bugu.R;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BuguActivity extends Activity implements OnClickListener{
    private int period=1000;
	private PowerProfilingService ptopaService = null;
    private FileWriter writer = null;
    private RecordDAO rdao = new RecordDAO();    
    private Record lastRecord;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        initViewListeners();
        try{
        	ptopaService = new PowerProfilingService(this);
        }catch(Exception ex)
        {
        	//TODO change service states
        }
    }
    
    private void initViewListeners()
    {
        Button exit = (Button)findViewById(R.id.exitButton);
        Button start = (Button)findViewById(R.id.startButton);
        
        exit.setOnClickListener(this);
        start.setOnClickListener(this);
    }
    
    @Override
    protected void onDestroy() {        
        if(writer != null)
        {
            try{
                writer.flush();
                writer.close();
            }catch(Exception ex){}
        }
        
        if(lastRecord != null){
            lastRecord.setState(1);
            rdao.update(lastRecord);
        }        
        //close database connection
        SqliteHelper.getInstance().close();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {    
        switch(view.getId())
        {
            case R.id.exitButton:
                finish();
                System.exit(0);
                break;
            case R.id.startButton:
                Button start = (Button)view;
                if(ptopaService.currentState() == false)//not running
                {
                    if(startMonitor() == true)
                        start.setText(R.string.stopButton);
                    else
                    {
                        //TODO show alert
                    }
                }
                else
                {
                    stopMonitor();
                    start.setText(R.string.startButton);
                }
                break;
            default:
                break;
        }
    }
    
    private boolean startMonitor()
    {
        initWrite();
        if(writer == null) return false;
        ptopaService.startMonitor(writer, period);        
        
        return true;
    }
    
    private void stopMonitor()
    {
        if(writer != null){
            try{writer.close();}catch(Exception ex){}
        }
        
        if(lastRecord != null)
        {
            lastRecord.setState(1);
            rdao.update(lastRecord);
        }
        ptopaService.stopMonitor();
    }
    
    private void initWrite()
    {
        try {
            File root = Environment.getExternalStorageDirectory();
            File ptopa = new File(root, "ptopa/data");
            if(ptopa.exists() == false) { 
                ptopa.mkdir();
            }
            
            String fileName = System.currentTimeMillis()+"ptoppower.txt";
            File ptopfile = new File(ptopa, fileName);
            if(ptopfile.exists())
            {
                ptopfile.delete();
            }            
            ptopfile.createNewFile();
            
            if (root.canWrite()){
                writer = new FileWriter(ptopfile);
                lastRecord = new Record();
                lastRecord.setName(fileName);
                lastRecord.setAddtime(new Timestamp(System.currentTimeMillis()));
                lastRecord.setState(0);
                rdao.insert(lastRecord);
            }
            else
            {
                Log.i("pTopA: ", "Cannot write power data.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }  
}