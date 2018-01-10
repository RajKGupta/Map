package com.example.rajk.geofiretrial3.main;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.example.rajk.geofiretrial3.step2.PickContact;

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static Boolean alarm = true;
    private MediaPlayer mediaPlayer;
    private MarshmallowPermissions marshmallowPermissions;
    SharedPreference session;

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
        session = new SharedPreference(getApplicationContext());

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

        if (id == R.id.alarm) {
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

            Intent intent = new Intent(MainActivity.this, ViewProfile.class);
            startActivity(intent);

        } else if (id == R.id.addgaurdians) {
            if (marshmallowPermissions.checkPermissionForContacts())
                startActivity(new Intent(this, PickContact.class));
            else {

                if (marshmallowPermissions.checkPermissionForContacts())
                    startActivity(new Intent(this, PickContact.class));
                else {
                    marshmallowPermissions.requestPermissionForContacts();
                }
            }

        } else if (id == R.id.changepin) {
            final AlertDialog oldpin;
            oldpin = new AlertDialog.Builder(MainActivity.this)
                    .setView(R.layout.activity_change_pin_dialogue).setCancelable(false).create();
            oldpin.show();

            final EditText EnterOldPin = (EditText) oldpin.findViewById(R.id.enterpin);
            Button ok = (Button) oldpin.findViewById(R.id.ok);
            Button cancel = (Button) oldpin.findViewById(R.id.cancel);
            final TextInputLayout EnterOldPinWrap = (TextInputLayout) oldpin.findViewById(R.id.enterpinwrap);
            EnterOldPin.setHint("Enter Old Pin");

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String oldpins = EnterOldPin.getText().toString().trim();
                    hideKeyboard();
                    if (!oldpins.equals(session.getPin())) {
                        EnterOldPinWrap.setError("The pin you entered is wrong");
                        EnterOldPin.setText("");
                    } else {
                        EnterOldPinWrap.setErrorEnabled(false);
                        oldpin.dismiss();
                        final AlertDialog newpin;
                        newpin = new AlertDialog.Builder(MainActivity.this)
                                .setView(R.layout.activity_change_pin_dialogue).setCancelable(false).create();
                        newpin.show();

                        final EditText EnterNewPin = (EditText) newpin.findViewById(R.id.enterpin);
                        Button newok = (Button) newpin.findViewById(R.id.ok);
                        Button newcancel = (Button) newpin.findViewById(R.id.cancel);
                        final TextInputLayout EnterNewPinWrap = (TextInputLayout) newpin.findViewById(R.id.enterpinwrap);
                        EnterNewPin.setHint("Enter New Pin");

                        newok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newpins = EnterNewPin.getText().toString().trim();
                                hideKeyboard();
                                if (TextUtils.isEmpty(newpins) || newpins.length() != 4) {
                                    EnterNewPinWrap.setError("Please Enter the Pin");
                                    EnterNewPin.setText("");
                                } else {
                                    EnterNewPinWrap.setErrorEnabled(false);
                                    DBREF.child(users).child(session.getUID()).child("pin").setValue(newpins);
                                    session.setPin(newpins);
                                    newpin.dismiss();
                                }
                            }
                        });
                        newcancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                newpin.dismiss();
                            }
                        });
                    }
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oldpin.dismiss();
                }
            });
        } else if (id == R.id.shareapp) {

        }
        else if (id == R.id.viewgardians) {

            Intent intent = new Intent(MainActivity.this, ViewGaurdians.class);
            startActivity(intent);

        }
        else if (id == R.id.viewresponsibilities) {

            Intent intent = new Intent(MainActivity.this, ViewResponsibility.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void setMediaVolumeMax() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(3);
        audioManager.setStreamVolume(3, maxVolume, 1);
    }
}