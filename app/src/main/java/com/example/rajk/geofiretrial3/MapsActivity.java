package com.example.rajk.geofiretrial3;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.DistanceListUser.DistanceListUser;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    GeoFire geoFire = new GeoFire(ref);
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationManager locationManager;
    HashMap<String,Marker> userMarkers=new HashMap<>();
    private SharedPreferences sharedPreferences;//  =getSharedPreferences(Settings.Setting,MODE_PRIVATE);
    float distancePref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sharedPreferences  =getSharedPreferences(Settings.Setting,MODE_PRIVATE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);
        distancePref= sharedPreferences.getInt(Settings.DistancePreference,500)/sharedPreferences.getFloat(Settings.ConversionFactor,1F);
        distancePref=Math.round(distancePref);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(13.0f);
        mMap.setMaxZoomPreference(20.0f);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    void updateAndRetrieveLoc(final Location location)
    {
        geoFire.setLocation("Miley", new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Toast.makeText(MapsActivity.this,"There was an error saving the location to GeoFire: " + error,Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this,"Location saved on server successfully!",Toast.LENGTH_SHORT).show();
                }
            }

        });

        startActivity(new Intent(MapsActivity.this, DistanceListUser.class));
        geoFire.getLocation("Miley", new com.firebase.geofire.LocationCallback() {


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLocationResult(final String mykey, final GeoLocation mylocation) {
                if (mylocation != null) {
                    Toast.makeText(MapsActivity.this,String.format("The location for key %s is [%f,%f]", mykey, mylocation.latitude, mylocation.longitude),Toast.LENGTH_SHORT).show();
                    LatLng latLng = new LatLng(mylocation.latitude, mylocation.longitude);
                    LatLngBounds Boundary = new LatLngBounds(
                            new LatLng(location.getLatitude()-0.05, location.getLongitude()-0.05), new LatLng(location.getLatitude()+0.05, location.getLongitude()+0.05));
// Constrain the camera target to the Adelaide bounds.
                    mMap.setLatLngBoundsForCameraTarget(Boundary);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(mylocation.latitude, mylocation.longitude))
                            .radius(distancePref).
                                    fillColor(Color.argb(60,51,98,175)).strokeWidth(1).strokeColor(Color.argb(0,1,1,1))
                            .clickable(false); // In meters

// Get back the mutable Circle
                    Circle circle = mMap.addCircle(circleOptions);
                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mylocation.latitude, mylocation.longitude), distancePref/1000);
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            if(!key.equals(mykey)) {
                                Location myloc = new Location("");
                                myloc.setLatitude(mylocation.latitude);
                                myloc.setLongitude(mylocation.longitude);

                                Location userLoc = new Location("");
                                userLoc.setLatitude(location.latitude);
                                userLoc.setLongitude(location.longitude);

                                float distanceInMeters = myloc.distanceTo(userLoc)*sharedPreferences.getFloat(Settings.ConversionFactor,1);
                                distanceInMeters = Math.round(distanceInMeters);
                                LatLng latLng = new LatLng(location.latitude, location.longitude);
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(key+" "+String.valueOf(distanceInMeters)
                                        +" "+sharedPreferences.getString(Settings.Unit,"m"));
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                mCurrLocationMarker = mMap.addMarker(markerOptions);
                                userMarkers.put(key, mCurrLocationMarker);
                            }
                        }

                        @Override
                        public void onKeyExited(String key) {
                            Marker removeMarker = userMarkers.get(key);
                            userMarkers.remove(key);
                            removeMarker.remove();
                            System.out.println(String.format("Key %s is no longer in the search area", key));
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {
                            Marker removeMarker = userMarkers.get(key);
                            userMarkers.remove(key);
                            removeMarker.remove();
                            Location myloc = new Location("");
                            myloc.setLatitude(mylocation.latitude);
                            myloc.setLongitude(mylocation.longitude);
                            Location userLoc = new Location("");
                            userLoc.setLatitude(location.latitude);
                            userLoc.setLongitude(location.longitude);

                            float distanceInMeters = myloc.distanceTo(userLoc)*sharedPreferences.getFloat(Settings.ConversionFactor,1);
                            distanceInMeters = Math.round(distanceInMeters);
                            LatLng latLng = new LatLng(location.latitude, location.longitude);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title(key+" "+String.valueOf(distanceInMeters)+" "+sharedPreferences.getString(Settings.Unit,"m"));
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            mCurrLocationMarker = mMap.addMarker(markerOptions);
                            userMarkers.put(key,mCurrLocationMarker);
                            System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                        }

                        @Override
                        public void onGeoQueryReady() {
                            System.out.println("All initial data has been loaded and events have been fired!");
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {
                            Toast.makeText(MapsActivity.this,"There was an error with this query: " + error,Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(MapsActivity.this,String.format("There is no location for key %s in GeoFire", mykey),Toast.LENGTH_SHORT).show();
                }
            }


        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MapsActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(MapsActivity.this, "Unavailable Settings", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        updateAndRetrieveLoc(location);

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MapsActivity.this,Settings.class));
        finish();
    }

}
// flow of the above activity
// onCreate checks for the permission
//onMap ready callback and build api client
//onConnected called and location request is made
//on LocationChanged called and device location recorded by calling update and retrieve
//
//