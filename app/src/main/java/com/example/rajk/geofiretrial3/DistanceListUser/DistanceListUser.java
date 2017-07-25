package com.example.rajk.geofiretrial3.DistanceListUser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.MapsActivity;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.Settings;
import com.example.rajk.geofiretrial3.adapter.DistanceUserAdapter;
import com.example.rajk.geofiretrial3.model.Distance;
import com.example.rajk.geofiretrial3.model.DistanceUser;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
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

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;

public class DistanceListUser extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    ArrayList<DistanceUser> list;
    HashMap<String,DistanceUser> hashMap;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    GeoFire geoFire = new GeoFire(ref);
    private SharedPreferences sharedPreferences;//  =getSharedPreferences(Settings.Setting,MODE_PRIVATE);
    float distancePref;

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distance_list_view);
        recyclerView = (RecyclerView)findViewById(R.id.rv_distanceUser);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        hashMap = new HashMap<>();
        sharedPreferences  =getSharedPreferences(Settings.Setting,MODE_PRIVATE);
        distancePref= sharedPreferences.getInt(Settings.DistancePreference,500)/sharedPreferences.getFloat(Settings.ConversionFactor,1F);
        distancePref=Math.round(distancePref);

        loadRecyclerViewData();
        adapter = new DistanceUserAdapter(list,this);
        recyclerView.setAdapter(adapter);
    }

    private void loadRecyclerViewData() {

        geoFire.getLocation("Miley", new com.firebase.geofire.LocationCallback() {

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DistanceListUser.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLocationResult(final String mykey, final GeoLocation mylocation) {
                if (mylocation != null) {
                    Toast.makeText(DistanceListUser.this,String.format("The location for key %s is [%f,%f]", mykey, mylocation.latitude, mylocation.longitude),Toast.LENGTH_SHORT).show();
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
                                DistanceUser distanceUser = new DistanceUser(key,String.valueOf(distanceInMeters)
                                        +" "+sharedPreferences.getString(Settings.Unit,"m"));
                                list.add(distanceUser);
                                hashMap.put(key,distanceUser);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onKeyExited(String key) {
                            DistanceUser distanceUser = hashMap.get(key);
                            hashMap.remove(key);
                            list.remove(distanceUser);
                            adapter.notifyDataSetChanged();
                            }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {
                            DistanceUser distanceUserOld=hashMap.get(key);
                            hashMap.remove(key);
                            list.remove(distanceUserOld);
                            Location myloc = new Location("");
                            myloc.setLatitude(mylocation.latitude);
                            myloc.setLongitude(mylocation.longitude);
                            Location userLoc = new Location("");
                            userLoc.setLatitude(location.latitude);
                            userLoc.setLongitude(location.longitude);

                            float distanceInMeters = myloc.distanceTo(userLoc)*sharedPreferences.getFloat(Settings.ConversionFactor,1);
                            distanceInMeters = Math.round(distanceInMeters);
                            DistanceUser distanceUser = new DistanceUser(key,String.valueOf(distanceInMeters)
                                    +" "+sharedPreferences.getString(Settings.Unit,"m"));
                            list.add(distanceUser);
                            hashMap.put(key,distanceUser);
                            adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onGeoQueryReady() {
                            System.out.println("All initial data has been loaded and events have been fired!");
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {
                            Toast.makeText(DistanceListUser.this,"There was an error with this query: " + error,Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(DistanceListUser.this,String.format("There is no location for key %s in GeoFire", mykey),Toast.LENGTH_SHORT).show();
                }
            }


        });


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DistanceListUser.this,Settings.class));
        finish();
    }
}
