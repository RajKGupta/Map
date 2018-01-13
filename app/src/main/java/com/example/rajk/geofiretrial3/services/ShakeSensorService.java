package com.example.rajk.geofiretrial3.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.helper.ShakeDetector;
import com.example.rajk.geofiretrial3.model.SharedPreference;

public class ShakeSensorService extends Service {

    private final String TAG = this.getClass().getSimpleName();
    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;
    private SharedPreference session;

    public ShakeSensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        session = new SharedPreference(getApplicationContext());
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if (count == 3) {
                    Log.d(TAG, "Shake Count:" + count);
                    Intent in = new Intent(getApplicationContext(), LocServ.class);
                    startService(in);
                    Intent intent = new Intent(getApplicationContext(), MapsActivity2.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                    intent.putExtra("CALLED_FROM", TAG);
                    intent.putExtra("service", "service");
                    startActivity(intent);
                }

            }
        });
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mSensorManager.unregisterListener(mShakeDetector);
        super.onDestroy();
    }
}