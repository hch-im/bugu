package edu.wayne.cs.bugu.display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Vector;

import com.androidplot.Plot.BorderStyle;
import com.androidplot.series.XYSeries;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XLayoutStyle;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YLayoutStyle;

import edu.wayne.cs.bugu.analyzer.EventAnalyzer;
import edu.wayne.cs.bugu.R;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class EFigureResultActivity extends Activity implements OnClickListener{
    private XYPlot eventView;
    private String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.efigurerecord);
        filename = (String)getIntent().getExtra(RecordActivity.EXTRA_FILENAME);
        
        eventView = (XYPlot)findViewById(R.id.eventFigure);        
        prepareEventView();
    }
    
    private void prepareEventView()
    {
        boolean title = true;
        Vector<Number> times = new Vector<Number>();
        
        try {
            File root = Environment.getExternalStorageDirectory();
            File ptopa = new File(root, "ptopa/data/eventresult_" + filename);
            
            if(ptopa.exists() == false) { 
                EventAnalyzer.analyze(filename);
                if(ptopa.exists() == false) return;
            }
           
            String s = null;  
            BufferedReader br = new BufferedReader(new InputStreamReader(
              new FileInputStream(ptopa)));
            LinkedHashMap<String, Vector<Number>> powerVals = new LinkedHashMap<String, Vector<Number>>();
            Vector<Number> appPower = null;
            Vector<String> names = new Vector<String>();
            Random r = new Random();
            
            while ((s = br.readLine()) != null) {                
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
                        if(strs[j].equals("0") == false)
                            appPower.add(new BigInteger(strs[j]));
                        else
                            appPower.add(null);
                    }
                }//end for
                
                if(title) title = false;                               
            }//end while
            
            ViewGroup.LayoutParams lp = eventView.getLayoutParams();
            int width1 = names.size() * 100;
            int width2 = times.size() * 5;
            int width = Math.max(width1, width2);
            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, lp.height);
            eventView.setLayoutParams(layoutParams);
            initEventView(names.size());
            
            eventView.setDomainStep(XYStepMode.SUBDIVIDE, times.size() > 10 ? 5 : 2);
            eventView.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
            eventView.setRangeLowerBoundary(0, BoundaryMode.FIXED);
            eventView.setRangeUpperBoundary(names.size() + 1, BoundaryMode.FIXED);
            //add series
            for(int i = 0; i < names.size(); i++)
            {                    
                XYSeries series = new SimpleXYSeries(
                        times,
                        powerVals.get(names.get(i)),
                        names.get(i));                             // Set the display title of the series                                        
         
                // setup our line fill paint to be a slightly transparent gradient:             
                LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255)), 
                        Color.TRANSPARENT, Color.TRANSPARENT);
                Paint paint = formatter.getLinePaint();
                paint.setStrokeWidth(5);
                formatter.setLinePaint(paint);
                
                eventView.addSeries(series, LineAndPointRenderer.class, formatter);                     
            }        
            
        }catch (Exception ex){ex.printStackTrace();}     
    }
    
    private void initEventView(int len){        
        //setup power figure
        eventView.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        eventView.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);
        eventView.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
        eventView.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        eventView.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
 
        eventView.setBorderStyle(BorderStyle.SQUARE, null, null);
        eventView.getBorderPaint().setStrokeWidth(1);
        eventView.getBorderPaint().setAntiAlias(false);
        eventView.getBorderPaint().setColor(Color.WHITE);
 
        eventView.getGraphWidget().setPadding(10, 10, 10, 10);         
 
        // customize our domain/range labels
        eventView.setDomainLabel("Time");
        eventView.setRangeLabel("Event");
        eventView.getLegendWidget().setSize(new SizeMetrics(20, SizeLayoutType.ABSOLUTE, 100 * len, SizeLayoutType.ABSOLUTE));
        eventView.position(
                eventView.getLegendWidget(),
                5,
                XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                5,
                YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                AnchorPosition.RIGHT_BOTTOM);
        // get rid of decimal points in our range labels:
        eventView.setRangeValueFormat(new DecimalFormat("0"));         
        eventView.setDomainValueFormat(new DecimalFormat("0"));         
        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        eventView.disableAllMarkup();        
    }
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
        }
    }
    
}
