package edu.wayne.cs.bugu.display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Vector;

import com.androidplot.Plot.BorderStyle;
import com.androidplot.series.XYSeries;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XLayoutStyle;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YLayoutStyle;

import edu.wayne.cs.bugu.analyzer.PowerAnalyzer;
import edu.wayne.cs.bugu.R;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class PFigureResultActivity extends Activity implements OnClickListener{
    private XYPlot powerView;
    private String filename;
//    private Color[] colors = {R.color.crimson, };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pfigurerecord);
        filename = (String)getIntent().getExtra(RecordActivity.EXTRA_FILENAME);
        powerView = (XYPlot)findViewById(R.id.powerFigure);
        preparePowerView();
    }
    
    private void preparePowerView()
    {
        boolean title = true;
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###0.0000", decimalFormatSymbols);
        Vector<Number> times = new Vector<Number>();
        Random r = new Random();
        
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
            LinkedHashMap<String, Vector<Number>> powerVals = new LinkedHashMap<String, Vector<Number>>();
            Vector<Number> appPower = null;
            Vector<String> names = new Vector<String>();
            String text = null;
            
            while ((s = br.readLine()) != null) {   
                if(s.startsWith("Avg")) continue;
                
                String[] strs = s.split(",");
                //get time
                if(title == false)
                {
                    times.add(new BigInteger(strs[0]));
                }
                
                for (Integer j = 0; j < strs.length; j++) {
                    if(title && j >0 ){//title and xyseries
                        appPower = new Vector<Number>();
                        powerVals.put(strs[j], appPower);
                        names.add(strs[j]);
                    }
                    
                    if(j > 0)
                    {
                        appPower = powerVals.get(names.get(j-1));
                    }
                    
                    if(title || j == 0){//title ad time column
                    }
                    else
                    {
                        text = decimalFormat.format(Double.valueOf(strs[j]));                        
                        appPower.add(new BigDecimal(text));
                    }
                }//end for
                
                if(title) title = false;
                                
            }//end while
            
            ViewGroup.LayoutParams lp = powerView.getLayoutParams();
            int width1 = names.size() * 45;
            int width2 = times.size() * 20;
            int width = Math.max(width1, width2);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, lp.height);
            powerView.setLayoutParams(layoutParams);
            initPowerView(names.size());
            
            // draw a domain tick for each year:
            powerView.setDomainStep(XYStepMode.SUBDIVIDE, times.size() > 10 ? 5 : 2);
            //add series
            for(int i = 0; i < names.size(); i++)
            {
                if(i < 2 || names.get(i).equals("Device"))//the first data usually have problem
                    continue;
                
                XYSeries series = new SimpleXYSeries(
                        times,
                        powerVals.get(names.get(i)),
                        wrapName(names.get(i)));                             // Set the display title of the series                                        
         
                // setup our line fill paint to be a slightly transparent gradient:             
                LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255)), 
                        Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255)), Color.TRANSPARENT);
                powerView.addSeries(series, LineAndPointRenderer.class, formatter);                     
            }            
        }catch (Exception ex){ex.printStackTrace();}

    }
    
    private String wrapName(String name)
    {
        if(name == null) return "";
        if(name.indexOf(".") > 0) name = name.substring(name.lastIndexOf(".") + 1);
        return name.length() > 6 ? name.substring(0, 6) : name;
    }
    private void initPowerView(int len){
        //setup power figure
        powerView.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        powerView.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);
        powerView.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
        powerView.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        powerView.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
 
        powerView.setBorderStyle(BorderStyle.SQUARE, null, null);
        powerView.getBorderPaint().setStrokeWidth(1);
        powerView.getBorderPaint().setAntiAlias(false);
        powerView.getBorderPaint().setColor(Color.WHITE);
 
        powerView.getGraphWidget().setPadding(10, 10, 10, 10);         
 
        // customize our domain/range labels
        powerView.setDomainLabel("Time");
        powerView.setRangeLabel("Power(mW)");
        //set legend
        powerView.getLegendWidget().setSize(new SizeMetrics(20, SizeLayoutType.ABSOLUTE, 45 * len, SizeLayoutType.ABSOLUTE));
        powerView.position(
                powerView.getLegendWidget(),
                5,
                XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                5,
                YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                AnchorPosition.RIGHT_BOTTOM);
        // get rid of decimal points in our range labels:
        powerView.setRangeValueFormat(new DecimalFormat("0"));         
        powerView.setDomainValueFormat(new DecimalFormat("0"));         
        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        powerView.disableAllMarkup();                
    }
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
        }
    }
    
}
