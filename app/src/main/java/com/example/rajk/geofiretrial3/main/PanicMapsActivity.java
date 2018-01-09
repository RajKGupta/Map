package com.example.rajk.geofiretrial3.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.SharedPreference;
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
import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.myPanicResponsibilityId;
import static com.example.rajk.geofiretrial3.SaferIndia.myResponsibility;
import static com.example.rajk.geofiretrial3.SaferIndia.panick;
import static com.example.rajk.geofiretrial3.SaferIndia.userLoction;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class PanicMapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private DatabaseReference ref = DBREF.child(userLoction).getRef();
    private GeoFire geoFire = new GeoFire(ref);
    private Marker mCurrLocationMarker;
    private GoogleMap mMap;
    private Location GwaliorLocation,myloc;
    private Intent intent;
    private String myPanicResponsibilityIdString;
    private SharedPreference session;
    private DatabaseReference myResponsibilityPanicStateReference;
    private HashMap<String, Marker> userMarkers = new HashMap<>();
    private  ValueEventListener myResponsibilityPanicStateListener;
    private ArrayList<String> myResponsibilityList = new ArrayList<>();
    private GeoQuery GwaliorGeoQuery;
    private HashMap<String, PersonalDetails> myResponsibilityDetail = new HashMap<>();
    private Boolean showAll = true;
    private Boolean focusingMe=false;
    private Boolean once=true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panic_maps);
//      get the uid of the panicked person
        intent = getIntent();
        session = new SharedPreference(this);
        myPanicResponsibilityIdString = intent.getStringExtra(myPanicResponsibilityId);

//      Going back to previous activity when panic state off
        myResponsibilityPanicStateReference= DBREF.child(users).child(myPanicResponsibilityIdString).child(panick).getRef();
        myResponsibilityPanicStateListener = myResponsibilityPanicStateReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Boolean panicState = dataSnapshot.getValue(Boolean.class);
                    if (!panicState)
                    {
                        Intent intent = new Intent(PanicMapsActivity.this, MapsActivity2.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        myResponsibilityPanicStateReference.removeEventListener(this);
                        startActivity(intent);
                        finish();
                    }
                }
                else
                {
                    Intent intent = new Intent(PanicMapsActivity.this, MapsActivity2.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                if (myResponsibilityList.indexOf(key) != -1 ) {
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    if(key.equals(myPanicResponsibilityIdString)) {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }
                    else
                    {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    userMarkers.put(key, mCurrLocationMarker);
                    if(key.equals(myPanicResponsibilityIdString)) {
                        toggleLocation(latLng,myPanicResponsibilityIdString);
                    }
                    mCurrLocationMarker.showInfoWindow();
                } else if (key.equals(session.getUID())) {
                    LatLng mypos = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mypos);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    userMarkers.put(session.getUID(), mCurrLocationMarker);
                    toggleLocation(mypos,session.getUID());
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
                    if(key.equals(myPanicResponsibilityIdString)) {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }
                    else
                    {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                    mCurrLocationMarker.setTitle(key);
                    userMarkers.put(key, mCurrLocationMarker);
                    if(key.equals(myPanicResponsibilityIdString)) {
                        toggleLocation(latLng,myPanicResponsibilityIdString);
                    }
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
                    userMarkers.put(session.getUID(), mCurrLocationMarker);
                    toggleLocation(mypos,session.getUID());
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
    private void getMyResponsibilityList()
    {
        DBREF.child(users).child(session.getUID()).child(myResponsibility).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (showAll) {
                        myResponsibilityList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            myResponsibilityList.add(ds.getValue(String.class));
                        }
                    }
                    else
                    {
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
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
private void toggleLocation(LatLng latLng,String id)
{
    if(once==true) {
        once=false;
        if (focusingMe == false && id.equals(myPanicResponsibilityIdString)) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else if (focusingMe == true && id.equals(session.getUID())) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(GwaliorGeoQuery!=null)
            GwaliorGeoQuery.removeAllListeners();
        if(myResponsibilityPanicStateListener!=null)
            myResponsibilityPanicStateReference.removeEventListener(myResponsibilityPanicStateListener);
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        PanicMapsActivity.super.onBackPressed();
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
}
