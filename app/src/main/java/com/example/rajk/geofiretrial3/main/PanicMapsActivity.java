package com.example.rajk.geofiretrial3.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.helper.CircleTransform;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.example.rajk.geofiretrial3.services.HelpSound;
import com.example.rajk.geofiretrial3.services.LocServ;
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

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.invite;
import static com.example.rajk.geofiretrial3.SaferIndia.myPanicResponsibilityId;
import static com.example.rajk.geofiretrial3.SaferIndia.myResponsibility;
import static com.example.rajk.geofiretrial3.SaferIndia.panick;
import static com.example.rajk.geofiretrial3.SaferIndia.share;
import static com.example.rajk.geofiretrial3.SaferIndia.soundOff;
import static com.example.rajk.geofiretrial3.SaferIndia.userLoction;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class PanicMapsActivity extends MainActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;
    private DatabaseReference ref = DBREF.child(userLoction).getRef();
    private GeoFire geoFire = new GeoFire(ref);
    private GoogleMap mMap;
    private Location GwaliorLocation, myloc;
    private Intent intent;
    private String myPanicResponsibilityIdString;
    private SharedPreference session;
    private DatabaseReference myResponsibilityPanicStateReference;
    private HashMap<String, Marker> userMarkers = new HashMap<>();
    private ValueEventListener myResponsibilityPanicStateListener;
    private ArrayList<String> myResponsibilityList = new ArrayList<>();
    private GeoQuery GwaliorGeoQuery;
    private HashMap<String, PersonalDetails> myResponsibilityDetail = new HashMap<>();
    private Boolean showAll = true;
    private Boolean focusingMe = false;
    private Boolean once = true;
    private MarshmallowPermissions marshmallowPermissions;
    private AlertDialog alertDialog;
    private Intent locServiceIntent;
    private FloatingActionButton hideAll, toggle;
    private TextView panikedperson;
    private LinearLayout parent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
        getLayoutInflater().inflate(R.layout.activity_panic_maps, frame);
        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        parent = (LinearLayout) findViewById(R.id.parentpanicmapsactivity);
        locServiceIntent = new Intent(this, LocServ.class);
        marshmallowPermissions = new MarshmallowPermissions(this);
        stopService(new Intent(getApplicationContext(), ShakeSensorService.class));
        if (marshmallowPermissions.checkMultiPermission()) {
            //Toast.makeText(MapsActivity2.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();
            callEverything();
        }
    }

    private void callEverything() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(PanicMapsActivity.this);
        builder.setMessage("Enable location service to proceed")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        dialog.dismiss();
                        enableLoc();
                    }
                });
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing())
            checkLocationOn(PanicMapsActivity.this);

        intent = getIntent();
        session = new SharedPreference(this);
        myPanicResponsibilityIdString = intent.getStringExtra(myPanicResponsibilityId);
        Boolean ifShare = intent.getBooleanExtra(share, false);
        if (ifShare) {
            Intent smsIntent = new Intent(Intent.ACTION_SEND);
            String content = intent.getStringExtra("body");
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.setType("text/plain");
            smsIntent.putExtra("sms_body", content);
            smsIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Emergency!! I need your help");
            smsIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
            smsIntent.putExtra("sms_body", content);

            try {
                startActivity(Intent.createChooser(smsIntent, "Share"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(PanicMapsActivity.this,
                        "Your phone does not support this option. Contact manufacturer for details", Toast.LENGTH_SHORT).show();
            }

        }
        Boolean ifSoundOff = intent.getBooleanExtra(soundOff, false);
        if (ifSoundOff) {
            stopService(new Intent(getApplicationContext(), HelpSound.class));
        }
//      Going back to previous activity when panic state off
        myResponsibilityPanicStateReference = DBREF.child(users).child(myPanicResponsibilityIdString).child(panick).getRef();
        myResponsibilityPanicStateListener = myResponsibilityPanicStateReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean panicState = dataSnapshot.getValue(Boolean.class);
                    if (!panicState) {
                        Intent intent = new Intent(PanicMapsActivity.this, MapsActivity2.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        myResponsibilityPanicStateReference.removeEventListener(this);
                        stopService(locServiceIntent);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Intent intent = new Intent(PanicMapsActivity.this, MapsActivity2.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    stopService(locServiceIntent);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        GwaliorLocation = new Location("");
        GwaliorLocation.setLatitude(26.2183);
        GwaliorLocation.setLongitude(78.1828);
        myloc = new Location("");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng Gwaliorpos = new LatLng(GwaliorLocation.getLatitude(), GwaliorLocation.getLongitude());
        LatLngBounds Boundary = new LatLngBounds(
                new LatLng(GwaliorLocation.getLatitude() - 20.00, GwaliorLocation.getLongitude() - 20.00), new LatLng(GwaliorLocation.getLatitude() + 20.00, GwaliorLocation.getLongitude() + 20.00));
        // Constrain the camera target to the Gwalior bounds.
        mMap.setLatLngBoundsForCameraTarget(Boundary);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Gwaliorpos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        hideAll = (FloatingActionButton) findViewById(R.id.hideAll);
        hideAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showAll) {
                    Snackbar snackbar = Snackbar
                            .make(parent, "Hiding Others Location!", Snackbar.LENGTH_SHORT);

                    snackbar.show();
                    showAll = false;
                    getMyResponsibilityList();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(parent, "Showing Everyone's Location!", Snackbar.LENGTH_SHORT);

                    snackbar.show();
                    showAll = true;
                    getMyResponsibilityList();
                }
            }
        });
        toggle = (FloatingActionButton) findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLocation();
            }
        });
        panikedperson = (TextView) findViewById(R.id.panikedperson);
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
                    Glide.with(PanicMapsActivity.this).load(personalDetails.getImgurl())
                            .thumbnail(0.5f)
                            .crossFade()
                            .placeholder(R.drawable.ic_account_circle)
                            .transform(new CircleTransform(PanicMapsActivity.this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(profile);

                } catch (Exception ev) {
                    System.out.print(ev.getMessage());
                }

                return v;
            }
        });
        mMap.getUiSettings().setMapToolbarEnabled(false);
        getMyResponsibilityList();
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
                    if (key.equals(myPanicResponsibilityIdString)) {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    } else {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    }
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    userMarkers.put(key, mCurrLocationMarker);
                    if (key.equals(myPanicResponsibilityIdString)) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                    }
                } else if (key.equals(session.getUID())) {
                    LatLng mypos = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mypos);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
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
                    if (key.equals(myPanicResponsibilityIdString)) {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    } else {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
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
                Toast.makeText(PanicMapsActivity.this, "There was an error with this query: " + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    // call getMyResponsibilityList whenever the user clicks on showOthers or hideOthers
    private void getMyResponsibilityList() {

        DBREF.child(users).child(session.getUID()).child(myResponsibility).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (showAll) {
                        myResponsibilityList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            myResponsibilityList.add(ds.getValue(String.class));
                        }
                    } else {
                        myResponsibilityList.clear();
                        myResponsibilityList.add(myPanicResponsibilityIdString);
                    }
                    mMap.clear();
                    userMarkers.clear();
                    myResponsibilityDetail.clear();
                    if (GwaliorGeoQuery != null)
                        GwaliorGeoQuery.removeAllListeners();
                    fetchDetailsOfMyResponsibility();
                    plotMyResponsibility();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchDetailsOfMyResponsibility() {
        for (String id : myResponsibilityList) {
            DBREF.child(users).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        PersonalDetails personalDetails = dataSnapshot.getValue(PersonalDetails.class);
                        if (myResponsibilityDetail.containsKey(personalDetails.getId())) {
                            myResponsibilityDetail.remove(personalDetails.getId());
                        }
                        myResponsibilityDetail.put(personalDetails.getId(), personalDetails);
                        panikedperson.setText(myResponsibilityDetail.get(myPanicResponsibilityIdString).getName() + " needs your help!!");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void toggleLocation() {
        if (focusingMe == false) {
            focusingMe = true;
            Marker marker = userMarkers.get(myPanicResponsibilityIdString);
            LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else if (focusingMe == true) {
            focusingMe = false;
            Marker marker = userMarkers.get(session.getUID());
            LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }


    @Override
    protected void onDestroy() {
        if (gpsReceiver != null)
            unregisterReceiver(gpsReceiver);
        if (GwaliorGeoQuery != null)
            GwaliorGeoQuery.removeAllListeners();
        if (myResponsibilityPanicStateListener != null)
            myResponsibilityPanicStateReference.removeEventListener(myResponsibilityPanicStateListener);
        startService(new Intent(getApplicationContext(), ShakeSensorService.class));
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        moveTaskToBack(true);
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
            locationRequest.setFastestInterval(15000);
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
                                        (Activity) PanicMapsActivity.this, REQUEST_LOCATION);
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
        startService(new Intent(getApplicationContext(), LocServ.class));
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
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        checkLocationOn(PanicMapsActivity.this);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    private void checkLocationOn(Context context) {
        final LocationManager manager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            alertDialog.show();
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startService(new Intent(getApplicationContext(), LocServ.class));

        } else {
            Toast.makeText(context, "Nothing is enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                checkLocationOn(context);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0) {
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
                        alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (marshmallowPermissions.checkMultiPermission()) {
            if (alertDialog != null && !alertDialog.isShowing())
                checkLocationOn(PanicMapsActivity.this);
        } else {
            marshmallowPermissions.requestMultiPermission();
        }
    }
}
