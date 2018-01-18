package com.example.rajk.geofiretrial3.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.model.Online;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.example.rajk.geofiretrial3.step2.Step2PickContact;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.addedGuardian;
import static com.example.rajk.geofiretrial3.SaferIndia.emergencyContact;
import static com.example.rajk.geofiretrial3.SaferIndia.getTimeStamp;
import static com.example.rajk.geofiretrial3.SaferIndia.guardianNotUser;
import static com.example.rajk.geofiretrial3.SaferIndia.myResponsibility;
import static com.example.rajk.geofiretrial3.SaferIndia.phoneVsId;
import static com.example.rajk.geofiretrial3.SaferIndia.sendNotif;
import static com.example.rajk.geofiretrial3.SaferIndia.userSession;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class ProfileActivity extends AppCompatActivity {
    String name, phone, blood, address, gender, age, diseases, imgurl;
    EditText ename, ephone, eblood, eaddress, eage, ediseases, eimgurl;
    FloatingActionButton submit_profile;
    public SharedPreference session;
    RadioGroup egender;
    RadioButton esex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SharedPreference(this);

        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkbg)));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        ename = (EditText) findViewById(R.id.ename);
        ephone = (EditText) findViewById(R.id.ephone);
        eaddress = (EditText) findViewById(R.id.eaddress);
        eblood = (EditText) findViewById(R.id.eblood);
        eage = (EditText) findViewById(R.id.eage);
        egender = (RadioGroup) findViewById(R.id.egender);
        egender.clearCheck();
        ediseases = (EditText) findViewById(R.id.edisease);
        submit_profile = (FloatingActionButton) findViewById(R.id.submit_profile);
        ename.setText(session.getName());

        submit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = ename.getText().toString().trim();
                address = eaddress.getText().toString().trim();
                phone = ephone.getText().toString().trim();
                blood = eblood.getText().toString().trim();
                age = eage.getText().toString().trim();
                diseases = ediseases.getText().toString().trim();
                int sex = egender.getCheckedRadioButtonId();
                esex = (RadioButton) findViewById(sex);
                gender = esex.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(age) || TextUtils.isEmpty(blood) || TextUtils.isEmpty(diseases)) {
                    Toast.makeText(ProfileActivity.this, "Fill all the details", Toast.LENGTH_SHORT).show();
                } else {
                    session.setSharedPreference(name, phone, blood, address, gender, age, diseases, session.getImgurl(), session.getEmail(), "");
                    DBREF.child(users).child(session.getUID()).setValue(new PersonalDetails(session.getName(), session.getPhone(), session.getBlood(), session.getAddress(), session.getGender(), session.getAge(), session.getDiseases(), session.getImgurl(), session.getEmail(), session.getUID()));
                    DBREF.child(phoneVsId).child(session.getPhone()).setValue(session.getUID());
                    DBREF.child(userSession).child(session.getUID()).setValue(new Online(true, getTimeStamp(), session.getPhone(), session.getName(), session.getUID()));
                    DBREF.child(guardianNotUser).child(session.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    DBREF.child(users).child(session.getUID()).child(myResponsibility).child(ds.getKey()).setValue(ds.getValue(String.class));
                                    DBREF.child(users).child(ds.getValue(String.class)).child(emergencyContact).child(session.getPhone()).setValue(session.getUID());
                                    DBREF.child(userSession).child(ds.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Online online = dataSnapshot.getValue(Online.class);
                                            sendNotif(online.getId(), session.getUID(), addedGuardian, online.getName() + " added you as Guardian");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                                DBREF.child(guardianNotUser).child(session.getPhone()).removeValue();
                                goToNextActivity();

                            } else {
                                goToNextActivity();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        ProfileActivity.super.onBackPressed();
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

    private void goToNextActivity() {
        Intent intent = new Intent(ProfileActivity.this, Step2PickContact.class);
        startActivity(intent);
        finish();
    }
}