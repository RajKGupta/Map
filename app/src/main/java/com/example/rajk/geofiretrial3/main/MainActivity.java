package com.example.rajk.geofiretrial3.main;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.step2.PickContact;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static Boolean alarm = true;
    private MediaPlayer mediaPlayer;
    private MarshmallowPermissions marshmallowPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        marshmallowPermissions = new MarshmallowPermissions(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.location) {
            //TODO open location directly

            //gpsON  = CheckGpsStatus();
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            Toast.makeText(this, provider, Toast.LENGTH_SHORT).show();

            if (!provider.contains("gps")) { //if gps is disabled

            } else { //if gps is enabled

            }
        } else if (id == R.id.alarm) {
            if (alarm) {
                setMediaVolumeMax();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.scream);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                alarm = false;
            } else {
                mediaPlayer.stop();
                mediaPlayer.reset();
                alarm = true;
            }
        } else if (id == R.id.profile) {

        } else if (id == R.id.addgaurdians) {
            if (marshmallowPermissions.checkPermissionForContacts())
                startActivity(new Intent(this, PickContact.class));
            else {
                marshmallowPermissions.requestPermissionForContacts();
                if (marshmallowPermissions.checkPermissionForContacts())
                    startActivity(new Intent(this, PickContact.class));
                else {
                    Toast.makeText(this, "You need to provide permission to access contacts.", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (id == R.id.shareapp) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setMediaVolumeMax() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(3);
        audioManager.setStreamVolume(3, maxVolume, 1);
    }

}