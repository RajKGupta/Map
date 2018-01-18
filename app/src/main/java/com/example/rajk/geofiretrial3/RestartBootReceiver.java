package com.example.rajk.geofiretrial3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.rajk.geofiretrial3.services.MyFirebaseMessagingService;
import com.example.rajk.geofiretrial3.services.ShakeSensorService;

public class RestartBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ShakeSensorService.class));
        context.startService(new Intent(context, MyFirebaseMessagingService.class));
    }
}
