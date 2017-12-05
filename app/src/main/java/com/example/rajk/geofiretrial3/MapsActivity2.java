package com.example.rajk.geofiretrial3;
import android.content.DialogInterface;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.rajk.geofiretrial3.model.GlobalEmployee;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("GlobalEmployee").child("Location").getRef();
    private GeoFire geoFire = new GeoFire(ref);
    private HashMap<String,Marker> userMarkers=new HashMap<>();
    private Location myloc;
    private GoogleMap mMap;
    Marker mCurrLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        myloc = new Location("");
        myloc.setLatitude(26.207613);
        myloc.setLongitude(78.1650822);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng mypos = new LatLng(myloc.getLatitude(),myloc.getLongitude());
        LatLngBounds Boundary = new LatLngBounds(
                new LatLng(myloc.getLatitude()-20.00, myloc.getLongitude()-20.00), new LatLng(myloc.getLatitude()+20.00, myloc.getLongitude()+20.00));
// Constrain the camera target to the Adelaide bounds.
        mMap.setLatLngBoundsForCameraTarget(Boundary);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mypos);
        markerOptions.title("Arvind Nahar Enterprises");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mCurrLocationMarker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(mypos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {

            @Override
            public boolean onMarkerClick(final Marker arg0) {
                if(arg0 != null && !arg0.getTitle().equals("Arvind Nahar Enterprises")) { // if marker  source is clicked
                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MapsActivity2.this);
                    View mView = layoutInflaterAndroid.inflate(R.layout.emp_desc, null);
                    final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MapsActivity2.this);
                    alertDialogBuilderUserInput.setView(mView);

                    final TextView name = (TextView) mView.findViewById(R.id.name1);
                    final TextView phone = (TextView) mView.findViewById(R.id.number1);
                    final TextView address = (TextView) mView.findViewById(R.id.address1);
                    final TextView lastSeen = (TextView) mView.findViewById(R.id.lastseen1);
                    alertDialogBuilderUserInput.setTitle("Employee Details");
                    String key = arg0.getTitle();
                    DatabaseReference dbDetail = FirebaseDatabase.getInstance().getReference().child("GlobalEmployee").child("EmployeeDetail").child(key).getRef();
                    dbDetail.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                GlobalEmployee employee = dataSnapshot.getValue(GlobalEmployee.class);
                                name.setText(employee.getName());
                                phone.setText(employee.getPhone_num());
                                address.setText(employee.getAddress());
                                lastSeen.setText(employee.getLastSeen());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    alertDialogBuilderUserInput.setIcon(R.drawable.ic_face_black_24dp);
                    alertDialogBuilderUserInput
                            .setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.dismiss();
                           //         showWindow();
                                    arg0.showInfoWindow();
                                }
                            });


                    AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                    alertDialogAndroid.show();
                }
                else if(arg0.getTitle().equals("Arvind Nahar Enterprises"))
                {
           //         showWindow();
                    mCurrLocationMarker.showInfoWindow();
                }
                return true;
            }

        });

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(myloc.getLatitude(), myloc.getLongitude()),1500);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                LatLng latLng = new LatLng(location.latitude, location.longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(key);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                userMarkers.put(key, mCurrLocationMarker);
                mCurrLocationMarker.showInfoWindow();

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
                LatLng latLng = new LatLng(location.latitude, location.longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(key);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                Marker mCurrLocationMarker = mMap.addMarker(markerOptions);
                userMarkers.put(key,mCurrLocationMarker);
                mCurrLocationMarker.showInfoWindow();
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(MapsActivity2.this,"There was an error with this query: " + error,Toast.LENGTH_SHORT).show();
            }
        });
        }
        /*private void showWindow() {
            mCurrLocationMarker.showInfoWindow();
            Iterator<HashMap.Entry<String, Marker>> iterator2 = userMarkers.entrySet().iterator();
            while (iterator2.hasNext()) {
                HashMap.Entry<String, Marker> entry = iterator2.next();
                entry.getValue().showInfoWindow();
            }
        }
        */

}
