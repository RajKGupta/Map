package com.example.rajk.geofiretrial3;
// TODO 2 when panic button is activated check for
// i) location permissions                        // Location permissions to starting mei hi check kar li hain na
// ii) sms sending permissions
// iii) ask to enable gps
// iv) start location service
// v) start SendSMSService
// TODO 3 Decide what to do If somebody in myResponsibilitylist activates panic mode I have added the check already see the TODO line 455
//TODO 4 We are removing the shareLocationFunctionality

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rajk.geofiretrial3.helper.CircleTransform;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.main.MainActivity;
import com.example.rajk.geofiretrial3.main.PanicMapsActivity;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.example.rajk.geofiretrial3.services.LocServ;
import com.example.rajk.geofiretrial3.services.PanicButton;
import com.example.rajk.geofiretrial3.services.SendSMSService;
import com.example.rajk.geofiretrial3.services.ShakeSensorService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.myPanicResponsibilityId;
import static com.example.rajk.geofiretrial3.SaferIndia.myResponsibility;
import static com.example.rajk.geofiretrial3.SaferIndia.panick;
import static com.example.rajk.geofiretrial3.SaferIndia.userLoction;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class MapsActivity2 extends MainActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;
    private DatabaseReference ref = DBREF.child(userLoction).getRef();
    private GeoFire geoFire = new GeoFire(ref);
    private HashMap<String, Marker> userMarkers = new HashMap<>();
    private Location myloc;
    private GoogleMap mMap;
    private SharedPreference session;
    private AlertDialog alertDialog;
    private ToggleButton toggleButton;
    private MarshmallowPermissions marshmallowPermissions;
    private Intent locServiceIntent;
    private Location GwaliorLocation;
    private ValueEventListener myResponsibilityListListener;
    private ArrayList<String> myResponsibilityList = new ArrayList<>();
    private GeoQuery GwaliorGeoQuery;
    private HashMap<String, PersonalDetails> myResponsibilityDetail = new HashMap<>();
    HashMap<DatabaseReference, ValueEventListener> dbPersonalDetailHashMap = new HashMap<>();
    private AlertDialog viewSelectedImages;
    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                checkLocationOn(context);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
        getLayoutInflater().inflate(R.layout.activity_maps2, frame);
        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        locServiceIntent = new Intent(getApplicationContext(), LocServ.class);

        session = new SharedPreference(this);
        startService(new Intent(getApplicationContext(), ShakeSensorService.class));
        GwaliorLocation = new Location("");
        GwaliorLocation.setLatitude(26.2183);
        GwaliorLocation.setLongitude(78.1828);
        myloc = new Location("");
        final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity2.this);
        builder.setMessage("Enable location service to proceed")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        dialog.dismiss();
                        enableLoc();
                    }
                });
        alertDialog = builder.create();
        marshmallowPermissions = new MarshmallowPermissions(this);
        if (marshmallowPermissions.checkMultiPermission()) {
            //Toast.makeText(MapsActivity2.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();

            callEverything();
        } else {

            marshmallowPermissions.requestMultiPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    }

    private void callEverything() {
        if (alertDialog != null && !alertDialog.isShowing())
            checkLocationOn(MapsActivity2.this);

        //check panic state OR
        if (session.getPin().equals("")) {
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
                    if (TextUtils.isEmpty(pin) || pin.length() != 4) {
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        toggleButton = (ToggleButton) findViewById(R.id.panicBtn);

        if (getIntent().hasExtra("service")) {
            toggleButton.setChecked(true);
            panicbuttonpressed();
        }

        if (session.getPanick()) {
            toggleButton.setChecked(true);
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
                            if (pin.equals("") || !pin.equals(session.getPin())) {
                                EnterPinWrap.setError("Please enter the correct pin!!");
                                EnterPin.setText("");
                            } else {
                                EnterPinWrap.setErrorEnabled(false);
                                DBREF.child(users).child(session.getUID()).child(panick).setValue(false);
                                session.setPanick(false);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    toggleButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_bg_on));
                                }
                                stopService(new Intent(getApplicationContext(), PanicButton.class));
                                startService(new Intent(MapsActivity2.this, SendSMSService.class));
                                viewSelectedImages.dismiss();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0) {
/*
                    boolean sendsms = marshmallowPermissions.checkPermissionForSendSms();
                    boolean accesscourselocation = marshmallowPermissions.checkPermissionForCoarseLocations();
                    boolean accessfinelocation = marshmallowPermissions.checkPermissionForLocations();
*/
                    if (marshmallowPermissions.checkMultiPermission()) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                        callEverything();
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("You need to give this permission to use the app!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, int id) {
                                        marshmallowPermissions.requestMultiPermission();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
                break;
        }
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
                    if (arg0.getTitle().equals(session.getUID())) {
                        personalDetails = new PersonalDetails(session.getName(), session.getPhone(), session.getBlood(), session.getAddress(), session.getGender(), session.getAge(), session.getDiseases(), session.getImgurl(), session.getEmail(), session.getUID());
                        TextView emailText = (TextView) v.findViewById(R.id.emailText);
                        emailText.setText("You are here");
                    } else {
                        personalDetails = myResponsibilityDetail.get(arg0.getTitle());
                        TextView emailText = (TextView) v.findViewById(R.id.emailText);
                        LatLng loc1 = userMarkers.get(arg0.getTitle()).getPosition();
                        Location loc1_ = new Location("");
                        loc1_.setLatitude(loc1.latitude);
                        loc1_.setLongitude(loc1.longitude);

                        Location loc2_ = new Location("");
                        LatLng loc2 = userMarkers.get(session.getUID()).getPosition();
                        loc2_.setLatitude(loc2.latitude);
                        loc2_.setLongitude(loc2.longitude);
                        float distanceInMeters = loc1_.distanceTo(loc2_);
                        if (distanceInMeters < 100)
                            emailText.setText(String.format("%.0f", distanceInMeters) + " m from you");
                        else if (distanceInMeters < 1000) {
                            int distance = (int) (distanceInMeters / 10);
                            distance = distance * 10;
                            emailText.setText(distance + " m from you");
                        } else {
                            distanceInMeters /= 1000;
                            emailText.setText(String.format("%.1f", distanceInMeters) + " km from you");
                        }

                    }
                    // Getting reference to the TextView to set latitude

                    TextView nameText = (TextView) v.findViewById(R.id.nameText);
                    nameText.setText(personalDetails.getName());

                    TextView phoneText = (TextView) v.findViewById(R.id.mobileText);
                    phoneText.setText(personalDetails.getPhone());

                    ImageView profile = (ImageView) v.findViewById(R.id.userPic);
                    Glide.with(MapsActivity2.this).load(personalDetails.getImgurl())
                            .thumbnail(0.5f)
                            .crossFade()
                            .placeholder(R.drawable.ic_account_circle)
                            .transform(new CircleTransform(MapsActivity2.this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            toggleButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_bg_off));
        }
        startService(new Intent(getApplicationContext(), PanicButton.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (marshmallowPermissions.checkMultiPermission()) {
            if (alertDialog != null && !alertDialog.isShowing())
                checkLocationOn(MapsActivity2.this);
        } else {
            marshmallowPermissions.requestMultiPermission();
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
        if (gpsReceiver != null)
            unregisterReceiver(gpsReceiver);

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
//                            toggleButton.setVisibility(View.GONE);
                            Intent intent = new Intent(MapsActivity2.this, PanicMapsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(myPanicResponsibilityId, personalDetails.getId());
                            stopService(locServiceIntent);
                            startActivity(intent);

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

    private void checkLocationOn(Context context) {
        final LocationManager manager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            alertDialog.show();
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startService(locServiceIntent);

        } else {
            Toast.makeText(context, "You need to enable GPS to make this Application work.", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableLoc() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            googleApiClient = null;
        }
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    }).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(15000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        (Activity) MapsActivity2.this, REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startService(locServiceIntent);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        checkLocationOn(MapsActivity2.this);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

}