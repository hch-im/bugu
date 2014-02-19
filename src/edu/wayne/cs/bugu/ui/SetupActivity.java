package edu.wayne.cs.bugu.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SetupActivity extends Activity{

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        TextView textview = new TextView(this);
        textview.setText("This is the Artists tab");
        setContentView(textview);
    }

}
