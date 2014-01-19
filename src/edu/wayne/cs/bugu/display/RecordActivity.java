package edu.wayne.cs.bugu.display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import edu.wayne.cs.bugu.analyzer.PowerAnalyzer;
import edu.wayne.cs.bugu.db.RecordDAO;
import edu.wayne.cs.bugu.db.model.Record;
import edu.wayne.cs.bugu.rest.BuguService;
import edu.wayne.cs.bugu.rest.BuguServiceImpl;
import edu.wayne.cs.ptop.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RecordActivity extends ListActivity{
    private RecordDAO dao = null;
    private ArrayList<Record> records;
    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");
    public static String EXTRA_FILENAME = "edu.wayne.cs.ptop.extra.filename";
    private BuguService service = new BuguServiceImpl();
    private ProgressDialog pDialog;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        dao = new RecordDAO();
        resetList();
        
        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Uploading...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);              
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Record r = records.get(position);
        showDialog(r);
    }
    
    private void showDialog(final Record r){
        new AlertDialog.Builder(this)
        .setTitle(R.string.resultTypeOption)
        .setItems(R.array.resulttype, 
                new DialogInterface.OnClickListener() {                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showRecord(r, which);
                    }
                })
         .show();
    }
    
    public void showUploadSuccessDialog()
    {
        new AlertDialog.Builder(this)
        .setTitle("Upload Successfully")
        .setMessage("Thanks for your support to Bugu service.")
         .show();        
    }
    
    private void showRecord(Record r, int which)
    {
        Intent i;
        switch(which)
        {
            case 0:
                i = new Intent(this, AvgPowerActivity.class);
                i.putExtra(EXTRA_FILENAME, r.getName());
                startActivity(i);
                break;                
            case 1:
                i = new Intent(this, ResultActivity.class);
                i.putExtra(EXTRA_FILENAME, r.getName());
                startActivity(i);
            break;
            case 2:
                i = new Intent(this, PFigureResultActivity.class);
                i.putExtra(EXTRA_FILENAME, r.getName());
                startActivity(i);
                break;
            case 3:
                i = new Intent(this, EFigureResultActivity.class);
                i.putExtra(EXTRA_FILENAME, r.getName());
                startActivity(i);
                break;
            case 4:
                pDialog.show();
                UploadThread t = new UploadThread(r.getName());
                t.start();
                break;   
            case 5:
                if(dao.remove(r.getId())){ 
                    File f = new File(Environment.getExternalStorageDirectory(), "ptopa/data/" + r.getName());
                    if(f.exists()) f.delete();
                    f = new File(Environment.getExternalStorageDirectory(), "ptopa/data/powerresult_" + r.getName());
                    if(f.exists()) f.delete();
                    f = new File(Environment.getExternalStorageDirectory(), "ptopa/data/eventresult_" + r.getName());
                    if(f.exists()) f.delete();
                    f = new File(Environment.getExternalStorageDirectory(), "ptopa/data/result_" + r.getName());
                    if(f.exists()) f.delete();                    
                    
                    resetList();
                }
                break;
        }
    }
    
    private void resetList()
    {       
        records = dao.getAvailableRecords();
        String[] results = new String[records.size()];
        for(int i = 0; i < records.size(); i++)
        {
            results[i] = formatter.format(records.get(i).getAddtime());
        }
        
        setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, results));
    }
    
    private void uploadData(String filename)
    {
        String model = Build.MODEL;
//        String product = Build.PRODUCT;
        
        File root = Environment.getExternalStorageDirectory();
        File ptopa = new File(root, "ptopa/data/powerresult_" + filename);
        if(ptopa.exists() == false) { 
            PowerAnalyzer.analyze(filename);
            if(ptopa.exists() == false) return;
        }
        String s = null; String last = null; String[] tstrs = null;
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(
              new FileInputStream(ptopa)));
            s = br.readLine(); 
            if(s == null) return;
            tstrs = s.split(",");
            
            while ((s = br.readLine()) != null) {
                //do nothing
                last = s;
            }
        }catch(Exception ex){}
        
        if(last == null) return;
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###0.0000", decimalFormatSymbols);
        String[] strs = last.split(",");
        HashMap<String, String> plist = new HashMap<String, String>();
        for(int i = 1 ; i < strs.length; i++)
        {
            String val = "";
            if(strs[i].indexOf("/") > 0)
            {
                double pv = Double.valueOf(strs[i].substring(0, strs[i].indexOf("/")));
                val = decimalFormat.format(pv);
            }
            else
            {
                val = "0";
            }
            
            plist.put(tstrs[i], val);
        }
        
        service.uploadPower(model, plist);
    } 
    
    class UploadThread extends Thread
    {
        String filename;
        public UploadThread(String filename)
        {
            this.filename = filename;
        }
        @Override
        public void run() {         
            uploadData(filename);
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {                
                pDialog.dismiss();
                showUploadSuccessDialog();
            }
        };        
    }        
}
