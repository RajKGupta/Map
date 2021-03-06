package com.example.rajk.geofiretrial3.main;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.SaferIndia;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.example.rajk.geofiretrial3.model.gaurdians_and_responsibilities;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.rajk.geofiretrial3.SaferIndia.AppLink;
import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.emergencyContact;
import static com.example.rajk.geofiretrial3.SaferIndia.share;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class ViewGaurdians extends AppCompatActivity implements gaundian_adapter.phonebook_adapterListener{

    RecyclerView rec_contact_list;
    ArrayList<gaurdians_and_responsibilities> contact_list = new ArrayList<>();
    gaundian_adapter gaundian_adapter;
    LinearLayoutManager linearLayoutManager;
    public SharedPreference session;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gaurdians);
        intent = getIntent();
        rec_contact_list = (RecyclerView) findViewById(R.id.contact_list);
        session = new SharedPreference(this);

        gaundian_adapter = new gaundian_adapter(contact_list, getApplicationContext(), this);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rec_contact_list.setLayoutManager(linearLayoutManager);
        rec_contact_list.setItemAnimator(new DefaultItemAnimator());
        rec_contact_list.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        rec_contact_list.setAdapter(gaundian_adapter);

        LoadData();
    }

    void LoadData()
    {
        DBREF.child(users).child(session.getUID()).child(emergencyContact).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren() ) {
                        String installed = ds.getValue(String.class);
                        String name = "", id = "";
                        if (installed.substring(0, 4).equals(SaferIndia.name)) {
                            name = installed.substring(4);
                        } else {
                            id = installed;
                        }
                        gaurdians_and_responsibilities gar = new gaurdians_and_responsibilities(ds.getKey(), name, "", id,"");
                        contact_list.add(gar);

                    }
                    gaundian_adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCALLMEclicked(int position) {
        gaurdians_and_responsibilities phonebook = contact_list.get(position);
        String num = phonebook.getPhone();
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + num));
        startActivity(callIntent);
    }

    @Override
    public void onShareButtonClicked(int posititon,String number) {
        Intent smsIntent = new Intent(Intent.ACTION_SEND);
        smsIntent.setData(Uri.parse("smsto:"));
        String content = "Hi "+session.getName()+" added you in their Security Network. Join them by downloading FeelSafe App "+AppLink;
        smsIntent.setType("text/plain");
        smsIntent.putExtra("address"  ,number);
        smsIntent.putExtra("sms_body"  , content);
        smsIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join me on FeelSafe");
        smsIntent.putExtra(android.content.Intent.EXTRA_TEXT,content );

        try {
            startActivity(Intent.createChooser(smsIntent,"Share"));
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ViewGaurdians.this,
                    "Your phone does not support this option. Contact manufacturer for details", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed()
    {
        if(intent.getBooleanExtra(share,false))
        {
            Intent intent = new Intent(this,MapsActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
            finish();
        }
    }

}