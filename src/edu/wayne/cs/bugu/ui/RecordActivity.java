package edu.wayne.cs.bugu.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.wayne.cs.bugu.db.RecordDAO;
import edu.wayne.cs.bugu.db.model.Record;
import edu.wayne.cs.bugu.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RecordActivity extends ListActivity{
    private RecordDAO dao = null;
    private ArrayList<Record> records;
    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");
    public static String EXTRA_FILENAME = "edu.wayne.cs.ptop.extra.filename";
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
                pDialog.show();
                break;   
            case 4:
                if(dao.remove(r.getId())){ 
                    File f = new File(Environment.getExternalStorageDirectory(), "bugu/data/" + r.getName());
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
}
