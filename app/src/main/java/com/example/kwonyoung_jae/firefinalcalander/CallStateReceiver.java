package com.example.kwonyoung_jae.firefinalcalander;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.kwonyoung_jae.firefinalcalander.util.Logger;

public class CallStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.debug("Broadcastreceiver : " + intent.getAction());
        Log.d("#과연#","onreceive 부분");
        CallJobService.enqueueWork(context, intent);


    }


}

