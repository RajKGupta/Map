package com.example.rajk.geofiretrial3.services;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
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
        setMediaVolumeMax();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sample);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        startService(new Intent(getApplicationContext(), LocServ.class));
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
//        audioManager.setStreamVolume(3, maxVolume, 1);
    }
}
