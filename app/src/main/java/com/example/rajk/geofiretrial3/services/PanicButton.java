package com.example.rajk.geofiretrial3.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.model.SharedPreference;

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.showLongToast;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class PanicButton extends Service {

    private SharedPreference session;
    private MediaPlayer mediaPlayer;

    public PanicButton() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        session = new SharedPreference(this);

        DBREF.child(users).child(session.getUID()).child("panic").setValue(true);
        session.setPanick(true);

//        setMediaVolumeMax();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sample);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        startLocationService();
        startService(new Intent(getApplicationContext(), SendSMSService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.reset();

    }

    private void setMediaVolumeMax() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(3);
        audioManager.setStreamVolume(3, maxVolume, 1);
    }

    private void startLocationService() {
        final LocationManager manager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enable location service to proceed")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent1);
                            dialog.dismiss();
                        }

                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(getApplicationContext(), "You have to give this permission to use the app!!", Toast.LENGTH_SHORT).show();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startService(new Intent(getApplicationContext(), LocServ.class));
        } else {
            showLongToast((Activity) getApplicationContext(), "Enable your GPS to see your location");
        }
    }

}
