package edu.wayne.cs.bugu.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BuguReceiver  extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String intType = intent.getAction();
    }
}
