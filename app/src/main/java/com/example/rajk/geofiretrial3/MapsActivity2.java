package com.example.rajk.geofiretrial3;
///TODO 1 combine all panic button functionality in 1 method
// TODO 2 when panic button is activated check for
// i) location permissions
// ii)sms sending permissions
// iii) ask to enable gps
// iv) start location service
// v) start SendSMSService
// TODO 3 Decide what to do If somebody in myResponsibilitylist activates panic mode I have added the check already see the TODO line 455

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.main.MainActivity;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.example.rajk.geofiretrial3.services.LocServ;
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
import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
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
    private HashMap<String,PersonalDetails> myResponsibilityDetail=new HashMap<>();
    HashMap<DatabaseReference, ValueEventListener> dbPersonalDetailHashMap = new HashMap<>();

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
        if(!marshmallowPermissions.checkPermissionForSendSms())
        {
            marshmallowPermissions.requestPermissionForReadsms();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        toggleButton = (ToggleButton) findViewById(R.id.panicBtn);

        if (getIntent().hasExtra("service")) {
            Toast.makeText(this, "Service called auto ", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                toggleButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_bg_off));
            }
            Toast.makeText(getApplicationContext(), R.string.sound_playing_message, Toast.LENGTH_SHORT).show();
            setMediaVolumeMax();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sample);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            prepareDistressAlert();
        }

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        toggleButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_bg_off));
                    }
                    Toast.makeText(getApplicationContext(), R.string.sound_playing_message, Toast.LENGTH_SHORT).show();
                    setMediaVolumeMax();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sample);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                    prepareDistressAlert();

                } else {
                    // The toggle is disabled
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        toggleButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_bg_on));
                    }
                    Toast.makeText(getApplicationContext(), R.string.sound_stopped_message, Toast.LENGTH_SHORT).show();
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    if (!session.getShareLocation()) {
                        stopService(locServiceIntent);
                    }
                }
            }
        });
    }

    private void plotMyResponsibility() {

        GwaliorGeoQuery = geoFire.queryAtLocation(new GeoLocation(GwaliorLocation.getLatitude(),GwaliorLocation.getLongitude()), 3000);
        GwaliorGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (myResponsibilityList.indexOf(key) != -1) {
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(key);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    userMarkers.put(key, mCurrLocationMarker);
                    mCurrLocationMarker.showInfoWindow();
                }
                else if(key.equals(session.getUID()))
                {
                    LatLng mypos = new LatLng(location.latitude,location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mypos);
                    markerOptions.title("Me");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mypos));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    userMarkers.put(session.getUID(),mCurrLocationMarker);
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
                }
                else if(key.equals(session.getUID()))
                {
                    Marker removeMarker = userMarkers.get(key);
                    userMarkers.remove(key);
                    removeMarker.remove();
                    System.out.println(String.format("Key %s is no longer in the search area", key));
                    myloc.setLatitude(0.0);
                    myloc.setLongitude(0.0);
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
                    markerOptions.title(key);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    userMarkers.put(key, mCurrLocationMarker);
                    mCurrLocationMarker.showInfoWindow();
                    System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                }
                else if(key.equals(session.getUID()))
                {
                    Marker removeMarker = userMarkers.get(key);
                    userMarkers.remove(key);
                    removeMarker.remove();
                    LatLng mypos = new LatLng(location.latitude,location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mypos);
                    markerOptions.title("Me");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mypos));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                    userMarkers.put(session.getUID(),mCurrLocationMarker);
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
                            if(GwaliorGeoQuery!=null)
                                GwaliorGeoQuery.removeAllListeners();
                            removePersonalDetailsListeners();
                            plotMyResponsibility();
                            fetchDetailsOfMyResponsibility();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                LatLng Gwaliorpos = new LatLng(GwaliorLocation.getLatitude(),GwaliorLocation.getLongitude());
                LatLngBounds Boundary = new LatLngBounds(
                        new LatLng(GwaliorLocation.getLatitude() - 20.00, GwaliorLocation.getLongitude() - 20.00), new LatLng(GwaliorLocation.getLatitude() + 20.00, GwaliorLocation.getLongitude() + 20.00));
                // Constrain the camera target to the Gwalior bounds.
                mMap.setLatLngBoundsForCameraTarget(Boundary);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(Gwaliorpos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                plotMyResponsibility();

    }

    private void setMediaVolumeMax() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(3);
        audioManager.setStreamVolume(3, maxVolume, 1);
    }

    private void prepareDistressAlert() {

        Toast.makeText(this, "Alert Button Pressed", Toast.LENGTH_SHORT).show();
        //perform actions of panic button click
        // send messages


        /*Log.d(TAG, "prepareDistressAlert");
        final User user;
        SharedPreferences sp = getSharedPreferences("LOGGED_USER", MODE_PRIVATE);
        String currentUser = sp.getString("current_user", null);
        if (currentUser != null) {
            Gson gson = new Gson();
            user = gson.fromJson(currentUser, User.class);  // get user's location
            final boolean internetAvailable = isInternetAvailable();
            Log.d(TAG, "Internet Available: " + internetAvailable);

            // send distress alerts to all emergency contacts
            ArrayList<Contact> emergencyContactList;
            gson = new Gson();
            String jsonArrayList = sp.getString("contact_list", null);
            Type type = new TypeToken<ArrayList<Contact>>() {
            }.getType();
            if (jsonArrayList != null) {
                emergencyContactList = gson.fromJson(jsonArrayList, type);
                for (Contact contact : emergencyContactList) {
                    sendSMS(user, contact);
                }
            }

            // send distress alerts to nearby users of the app
            // only if current user's location is known
            if (!user.getLocation().getLatitude().equals("null") && !user.getLocation().getLongitude().equals("null")) {
                final DatabaseReference dbLocationRef;
                dbLocationRef = FirebaseDatabase.getInstance().getReference().child("location");
                dbLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            User.Location location = postSnapshot.getValue(User.Location.class);
                            // do not consider locations with null values
                            // do not consider user's own location
                            if (location.hasAlertAllowed()
                                    && !location.getLatitude().equals("null") && !location.getLongitude().equals("null")
                                    && !postSnapshot.getKey().equals(user.getPhone())) {
                                // find nearby users and send them texts
                                Contact helperContact = new Contact();
                                helperContact.setPhone(postSnapshot.getKey());
                                if (internetAvailable) {
                                    findDistanceOnline(user, location, helperContact);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Firebase Error: " + databaseError.getMessage());
                    }
                });
            }

        }*/

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocPermissions();

    }
    private void checkLocPermissions()
    {
        coarsePermission=marshmallowPermissions.checkPermissionForCoarseLocations();
        finePermission = marshmallowPermissions.checkPermissionForLocations();
        if(!coarsePermission&&!finePermission)
        {
            if(!coarsePermission)
            {
                marshmallowPermissions.requestPermissionForCoarseLocations();
                coarsePermission=marshmallowPermissions.checkPermissionForCoarseLocations();
            }
            if(!finePermission)
            {
                marshmallowPermissions.requestPermissionForLocations();
                finePermission = marshmallowPermissions.checkPermissionForLocations();
            }
        }
        if(session.getShareLocation()) {
            startLocationService();
        }
    }
    private void startLocationService()
    {
        final LocationManager manager = (LocationManager) getSystemService( this.LOCATION_SERVICE );
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startService(locServiceIntent);
        }
        else
        {
            showLongToast(MapsActivity2.this,"Enable your GPS to see your location");
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
        if(myResponsibilityListListener!=null)
            DBREF.child(users).child(session.getUID()).child(myResponsibility).removeEventListener(myResponsibilityListListener);
        if(GwaliorGeoQuery!=null)
        GwaliorGeoQuery.removeAllListeners();
        removePersonalDetailsListeners();
    }
    private void fetchDetailsOfMyResponsibility()
    {
        for(String id:myResponsibilityList) {
            DatabaseReference personalDetailReference =DBREF.child(users).child(id) ;
            ValueEventListener  personalDetailReferenceVLE= personalDetailReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        PersonalDetails personalDetails = dataSnapshot.getValue(PersonalDetails.class);
                        if(myResponsibilityDetail.containsKey(personalDetails.getId()))
                        {
                            myResponsibilityDetail.remove(personalDetails.getId());
                        }
                        myResponsibilityDetail.put(personalDetails.getId(),personalDetails);
                        if(personalDetails.getPanic())
                        {
                            //TODO Track who is in danger
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            dbPersonalDetailHashMap.put(personalDetailReference,personalDetailReferenceVLE);
        }
    }
    private void removePersonalDetailsListeners()
    {
        Iterator<HashMap.Entry<DatabaseReference, ValueEventListener>> iterator = dbPersonalDetailHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<DatabaseReference, ValueEventListener> entry = (HashMap.Entry<DatabaseReference, ValueEventListener>) iterator.next();
            if (entry.getValue() != null)
                entry.getKey().removeEventListener(entry.getValue());
        }
        dbPersonalDetailHashMap.clear();
    }
}

