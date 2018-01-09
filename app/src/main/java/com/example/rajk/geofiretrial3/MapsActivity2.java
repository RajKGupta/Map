package com.example.rajk.geofiretrial3;
// TODO 2 when panic button is activated check for
// i) location permissions                        // Location permissions to starting mei hi check kar li hain na
// ii) sms sending permissions
// iii) ask to enable gps
// iv) start location service
// v) start SendSMSService
// TODO 3 Decide what to do If somebody in myResponsibilitylist activates panic mode I have added the check already see the TODO line 455
//TODO 4 We are removing the shareLocationFunctionality
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.main.MainActivity;
import com.example.rajk.geofiretrial3.main.PanicMapsActivity;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.example.rajk.geofiretrial3.services.LocServ;
import com.example.rajk.geofiretrial3.services.SendSMSService;
import com.example.rajk.geofiretrial3.services.ShakeSensorService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.bumptech.glide.Glide.with;
import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.myPanicResponsibilityId;
import static com.example.rajk.geofiretrial3.SaferIndia.myResponsibility;
import static com.example.rajk.geofiretrial3.SaferIndia.showLongToast;
import static com.example.rajk.geofiretrial3.SaferIndia.userLoction;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class MapsActivity2 extends MainActivity implements OnMapReadyCallback {

    private DatabaseReference ref = DBREF.child(userLoction).getRef();
    private GeoFire geoFire = new GeoFire(ref);
    private HashMap<String, Marker> userMarkers = new HashMap<>();
    private Location myloc;
    private GoogleMap mMap;
    private SharedPreference session;
    Marker mCurrLocationMarker;
    private ToggleButton toggleButton;
    private MediaPlayer mediaPlayer;
    private Boolean coarsePermission, finePermission;
    private MarshmallowPermissions marshmallowPermissions;
    private Intent locServiceIntent;
    private Location GwaliorLocation;
    private ValueEventListener myResponsibilityListListener;
    private ArrayList<String> myResponsibilityList = new ArrayList<>();
    private GeoQuery GwaliorGeoQuery;
    private HashMap<String, PersonalDetails> myResponsibilityDetail = new HashMap<>();
    HashMap<DatabaseReference, ValueEventListener> dbPersonalDetailHashMap = new HashMap<>();
    private AlertDialog viewSelectedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
        getLayoutInflater().inflate(R.layout.activity_maps2, frame);

        locServiceIntent = new Intent(MapsActivity2.this, LocServ.class);

        session = new SharedPreference(this);
        startService(new Intent(getApplicationContext(), ShakeSensorService.class));
        GwaliorLocation = new Location("");
        GwaliorLocation.setLatitude(26.2183);
        GwaliorLocation.setLongitude(78.1828);
        myloc = new Location("");

        marshmallowPermissions = new MarshmallowPermissions(this);
        if (!marshmallowPermissions.checkPermissionForSendSms()) {
            marshmallowPermissions.requestPermissionForSendSms();
        }
        if (session.getPin().equals(""))
        {
            viewSelectedImages = new AlertDialog.Builder(MapsActivity2.this)
                    .setView(R.layout.activity_pin_dialogue).setCancelable(false).create();
            viewSelectedImages.show();

            final EditText EnterPin = (EditText) viewSelectedImages.findViewById(R.id.enterpin);
            Button oksave = (Button) viewSelectedImages.findViewById(R.id.oksave);
            final TextInputLayout EnterPinWrap = (TextInputLayout) viewSelectedImages.findViewById(R.id.enterpinwrap);
            EnterPinWrap.setHint("Enter Pin");
            EnterPinWrap.setHintAnimationEnabled(true);

            oksave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pin = EnterPin.getText().toString().trim();
                    hideKeyboard();
                    if (TextUtils.isEmpty(pin)||pin.length()!=4) {
                        EnterPinWrap.setError("Please Enter the Pin");
                    } else {
                        EnterPinWrap.setErrorEnabled(false);
                        DBREF.child(users).child(session.getUID()).child("pin").setValue(pin);
                        session.setPin(pin);
                        viewSelectedImages.dismiss();
                    }
                }
            });
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        toggleButton = (ToggleButton) findViewById(R.id.panicBtn);

        if (getIntent().hasExtra("service")) {
            toggleButton.setChecked(true);

            panicbuttonpressed();
        }

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    panicbuttonpressed();
                } else {
                    // TODO PIN to close panic state
                    // The toggle is disabled
                    viewSelectedImages = new AlertDialog.Builder(MapsActivity2.this)
                            .setView(R.layout.activity_pin_dialogue).create();
                    viewSelectedImages.show();

                    final EditText EnterPin = (EditText) viewSelectedImages.findViewById(R.id.enterpin);
                    Button oksave = (Button) viewSelectedImages.findViewById(R.id.oksave);
                    final TextInputLayout EnterPinWrap = (TextInputLayout) viewSelectedImages.findViewById(R.id.enterpinwrap);
                    EnterPinWrap.setHint("Enter Pin");
                    EnterPinWrap.setHintAnimationEnabled(true);

                    oksave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String pin = EnterPin.getText().toString().trim();
                            hideKeyboard();
                            if (pin.equals("")||!pin.equals(session.getPin()))
                            {
                                EnterPinWrap.setError("Please enter the correct pin!!");
                                EnterPin.setText("");
                            } else {
                                EnterPinWrap.setErrorEnabled(false);
                                DBREF.child(users).child(session.getUID()).child("panic").setValue(false);
                                session.setPanick(false);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    toggleButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_bg_on));
                                }
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                                if (!session.getShareLocation()) {
                                    stopService(locServiceIntent);
                                }
                                viewSelectedImages.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    private void plotMyResponsibility() {

        GwaliorGeoQuery = geoFire.queryAtLocation(new GeoLocation(GwaliorLocation.getLatitude(), GwaliorLocation.getLongitude()), 3000);
        GwaliorGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (myResponsibilityList.indexOf(key) != -1) {
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    userMarkers.put(key, mCurrLocationMarker);
                    mCurrLocationMarker.showInfoWindow();
                } else if (key.equals(session.getUID())) {
                    LatLng mypos = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mypos);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mypos));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    userMarkers.put(session.getUID(), mCurrLocationMarker);
                    myloc.setLatitude(location.latitude);
                    myloc.setLongitude(location.longitude);

                }
            }

            @Override
            public void onKeyExited(String key) {
                if (myResponsibilityList.indexOf(key) != -1) {
                    Marker removeMarker = userMarkers.get(key);
                    userMarkers.remove(key);
                    removeMarker.remove();
                    System.out.println(String.format("Key %s is no longer in the search area", key));
                } else if (key.equals(session.getUID())) {
                    Marker removeMarker = userMarkers.get(key);
                    userMarkers.remove(key);
                    removeMarker.remove();
                    System.out.println(String.format("Key %s is no longer in the search area", key));
                    myloc.setLatitude(GwaliorLocation.getLatitude());
                    myloc.setLongitude(GwaliorLocation.getLongitude());
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                if (myResponsibilityList.indexOf(key) != -1) {
                    Marker removeMarker = userMarkers.get(key);
                    userMarkers.remove(key);
                    removeMarker.remove();
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    userMarkers.put(key, mCurrLocationMarker);
                    mCurrLocationMarker.showInfoWindow();
                    System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                } else if (key.equals(session.getUID())) {
                    Marker removeMarker = userMarkers.get(key);
                    userMarkers.remove(key);
                    removeMarker.remove();
                    LatLng mypos = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mypos);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    mCurrLocationMarker.setTitle(key);
                    userMarkers.put(session.getUID(), mCurrLocationMarker);
                    myloc.setLatitude(location.latitude);
                    myloc.setLongitude(location.longitude);
                }
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(MapsActivity2.this, "There was an error with this query: " + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        myResponsibilityListListener = DBREF.child(users).child(session.getUID()).child(myResponsibility).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myResponsibilityList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        myResponsibilityList.add(ds.getValue(String.class));
                    }
                    mMap.clear();
                    userMarkers.clear();
                    myResponsibilityDetail.clear();
                    if (GwaliorGeoQuery != null)
                        GwaliorGeoQuery.removeAllListeners();
                    removePersonalDetailsListeners();
                    fetchDetailsOfMyResponsibility();
                    }
                plotMyResponsibility();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        LatLng Gwaliorpos = new LatLng(GwaliorLocation.getLatitude(), GwaliorLocation.getLongitude());
        LatLngBounds Boundary = new LatLngBounds(
                new LatLng(GwaliorLocation.getLatitude() - 20.00, GwaliorLocation.getLongitude() - 20.00), new LatLng(GwaliorLocation.getLatitude() + 20.00, GwaliorLocation.getLongitude() + 20.00));
        // Constrain the camera target to the Gwalior bounds.
        mMap.setLatLngBoundsForCameraTarget(Boundary);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Gwaliorpos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {
                View v = null;
                try {

                    // Getting view from the layout file info_window_layout
                    v = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                    PersonalDetails personalDetails;
                    if(arg0.getTitle().equals(session.getUID()))
                    {
                      personalDetails = new PersonalDetails(session.getName(),session.getPhone(),session.getBlood(),session.getAddress(),session.getGender(),session.getAge(),session.getDiseases(),session.getImgurl(),session.getEmail(),session.getUID());
                    }
                    else
                     personalDetails= myResponsibilityDetail.get(arg0.getTitle());
                    // Getting reference to the TextView to set latitude
                    TextView emailText = (TextView) v.findViewById(R.id.emailText);
                    emailText.setText(personalDetails.getEmail());

                    TextView nameText = (TextView) v.findViewById(R.id.nameText);
                    nameText.setText(personalDetails.getName());

                    TextView phoneText = (TextView) v.findViewById(R.id.mobileText);
                    phoneText.setText(personalDetails.getPhone());

                    ImageView profile = (ImageView)v.findViewById(R.id.userPic);
                    Glide.with(MapsActivity2.this)
                            .load(personalDetails.getImgurl())
                            .centerCrop()
                            .placeholder(R.drawable.ic_account_circle)
                            .into(profile);

                } catch (Exception ev) {
                    System.out.print(ev.getMessage());
                }

                return v;
            }
        });
        mMap.getUiSettings().setMapToolbarEnabled(false);

    }

    private void panicbuttonpressed() {
        DBREF.child(users).child(session.getUID()).child("panic").setValue(true);
        session.setPanick(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            toggleButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_bg_off));
        }
        setMediaVolumeMax();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sample);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        if (!marshmallowPermissions.checkPermissionForSendSms()) {
            marshmallowPermissions.requestPermissionForSendSms();
        } else {
            startLocationService();
            startService(new Intent(getApplicationContext(), SendSMSService.class));
        }
    }

    private void setMediaVolumeMax() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(3);
        audioManager.setStreamVolume(3, maxVolume, 1);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkLocPermissions();
    }

    private void checkLocPermissions() {
        coarsePermission = marshmallowPermissions.checkPermissionForCoarseLocations();
        finePermission = marshmallowPermissions.checkPermissionForLocations();
        if (!coarsePermission && !finePermission) {
            if (!coarsePermission) {
                marshmallowPermissions.requestPermissionForCoarseLocations();
                coarsePermission = marshmallowPermissions.checkPermissionForCoarseLocations();
            }
            if (!finePermission) {
                marshmallowPermissions.requestPermissionForLocations();
                finePermission = marshmallowPermissions.checkPermissionForLocations();
            }
        }
        //check panic state OR
        if (session.getShareLocation()) {
            startLocationService();
        }
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
                            Toast.makeText(MapsActivity2.this, "You have to give this permission to use the app!!", Toast.LENGTH_SHORT).show();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startService(locServiceIntent);
        } else {
            showLongToast(MapsActivity2.this, "Enable your GPS to see your location");
        }
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        MapsActivity2.super.onBackPressed();
                    }


                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myResponsibilityListListener != null)
            DBREF.child(users).child(session.getUID()).child(myResponsibility).removeEventListener(myResponsibilityListListener);
        if (GwaliorGeoQuery != null)
            GwaliorGeoQuery.removeAllListeners();
        removePersonalDetailsListeners();
    }

    private void fetchDetailsOfMyResponsibility() {
        for (String id : myResponsibilityList) {
            DatabaseReference personalDetailReference = DBREF.child(users).child(id);
            ValueEventListener personalDetailReferenceVLE = personalDetailReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        PersonalDetails personalDetails = dataSnapshot.getValue(PersonalDetails.class);
                        if (myResponsibilityDetail.containsKey(personalDetails.getId())) {
                            myResponsibilityDetail.remove(personalDetails.getId());
                        }
                        myResponsibilityDetail.put(personalDetails.getId(), personalDetails);
                        if (personalDetails.getPanic()) {
                            Intent intent = new Intent(MapsActivity2.this, PanicMapsActivity.class);
                            intent.putExtra(myPanicResponsibilityId,personalDetails.getId());
                            startActivity(intent);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            dbPersonalDetailHashMap.put(personalDetailReference, personalDetailReferenceVLE);
        }
    }

    private void removePersonalDetailsListeners() {
        Iterator<HashMap.Entry<DatabaseReference, ValueEventListener>> iterator = dbPersonalDetailHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<DatabaseReference, ValueEventListener> entry = (HashMap.Entry<DatabaseReference, ValueEventListener>) iterator.next();
            if (entry.getValue() != null)
                entry.getKey().removeEventListener(entry.getValue());
        }
        dbPersonalDetailHashMap.clear();
    }


    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}