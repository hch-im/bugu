package edu.wayne.cs.bugu.display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import edu.wayne.cs.bugu.analyzer.PowerAnalyzer;
import edu.wayne.cs.bugu.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ResultActivity extends Activity implements OnClickListener{
    private TableLayout tView;
    private String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filename = (String)getIntent().getExtra(RecordActivity.EXTRA_FILENAME);
        setContentView(R.layout.result);                
        tView = (TableLayout)findViewById(R.id.tableView);         
        preparePowerView();
    }
    
    private void preparePowerView()
    {
        TableRow tableRow;
        TextView textView;
        boolean title = true;
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("###0.0000", decimalFormatSymbols);
        
        try {
            File root = Environment.getExternalStorageDirectory();
            File ptopa = new File(root, "ptopa/data/powerresult_" + filename);
            
            if(ptopa.exists() == false) { 
                PowerAnalyzer.analyze(filename);
                if(ptopa.exists() == false) return;
            }
           
            String s = null;  
            BufferedReader br = new BufferedReader(new InputStreamReader(
              new FileInputStream(ptopa)));
            String text = null;
            
            while ((s = br.readLine()) != null) {
                tableRow = new TableRow(getApplicationContext());
                
                String[] strs = s.split(",");
                
                for (Integer j = 0; j < strs.length; j++) {
                    textView = new TextView(getApplicationContext());                   
                    
                    if(title || j == 0){//title ad time column
                        textView.setText(strs[j]);
                    }
                    else if(strs[0].equals("Avg"))
                    {
                        if(strs[j].equals("0"))
                            textView.setText(strs[j]);
                        else
                        {
                            String[] pstr = strs[j].split("/");
                            textView.setText(decimalFormat.format(Double.valueOf(pstr[0])));
                        }
                    }
                    else
                    {
                        text = decimalFormat.format(Double.valueOf(strs[j]));                        
                        textView.setText(text);
                    }
                    textView.setPadding(5, 2, 5, 2);
                    tableRow.addView(textView);
                }//end for
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                params.setMargins(2, 2, 2, 2);
                tableRow.setLayoutParams(params);
                tView.addView(tableRow);
                
                if(title) title = false;
                
            }//end while
        }catch (Exception ex){ex.printStackTrace();}

    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
        }
    }
    
}
