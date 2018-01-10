package com.example.rajk.geofiretrial3.main;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.emergencyContact;
import static com.example.rajk.geofiretrial3.SaferIndia.getTimeStamp;
import static com.example.rajk.geofiretrial3.SaferIndia.guardianNotUser;
import static com.example.rajk.geofiretrial3.SaferIndia.myResponsibility;
import static com.example.rajk.geofiretrial3.SaferIndia.phoneVsId;
import static com.example.rajk.geofiretrial3.SaferIndia.userSession;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class ViewProfile extends AppCompatActivity {

    EditText ename, ephone, eblood, eaddress, eage, ediseases, egender;
    public SharedPreference session;
    String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        session = new SharedPreference(this);

        ename = (EditText) findViewById(R.id.ename);
        ephone = (EditText) findViewById(R.id.ephone);
        eaddress = (EditText) findViewById(R.id.eaddress);
        eblood = (EditText) findViewById(R.id.eblood);
        eage = (EditText) findViewById(R.id.eage);
        egender = (EditText) findViewById(R.id.egender);
        ediseases = (EditText) findViewById(R.id.edisease);

        ename.setText(session.getName());
        ephone.setText(session.getPhone());
        eaddress.setText(session.getAddress());
        eage.setText(session.getAge());
        eblood.setText(session.getBlood());
        egender.setText(session.getGender());
        ediseases.setText(session.getDiseases());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                final AlertDialog customerEditDetails;
                customerEditDetails = new AlertDialog.Builder(this)
                        .setView(R.layout.activity_profile).create();
                customerEditDetails.show();


                final EditText ename, ephone, eblood, eaddress, eage, ediseases;
                final Button submit_profile;
                final RadioGroup egender;
                final RadioButton[] esex = new RadioButton[1];

                ename = (EditText) customerEditDetails.findViewById(R.id.ename);
                ephone = (EditText) customerEditDetails.findViewById(R.id.ephone);
                eaddress = (EditText) customerEditDetails.findViewById(R.id.eaddress);
                eblood = (EditText) customerEditDetails.findViewById(R.id.eblood);
                eage = (EditText) customerEditDetails.findViewById(R.id.eage);
                egender = (RadioGroup) customerEditDetails.findViewById(R.id.egender);
                egender.clearCheck();
                ediseases = (EditText) customerEditDetails.findViewById(R.id.edisease);
                submit_profile = (Button) customerEditDetails.findViewById(R.id.submit_profile);

                ename.setText(session.getName());
                ephone.setText(session.getPhone());
                ephone.setEnabled(false);
                eaddress.setText(session.getAddress());
                eage.setText(session.getAge());
                eblood.setText(session.getBlood());
                ediseases.setText(session.getDiseases());
                gender = session.getGender();
                if (gender.equals("Male")) {
                    egender.check(R.id.male);
                } else {
                    egender.check(R.id.female);
                }

                submit_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String name, blood, address, gender, age, diseases;
                        name = ename.getText().toString().trim();
                        address = eaddress.getText().toString().trim();
                        blood = eblood.getText().toString().trim();
                        age = eage.getText().toString().trim();
                        diseases = ediseases.getText().toString().trim();
                        int sex = egender.getCheckedRadioButtonId();
                        esex[0] = (RadioButton) customerEditDetails.findViewById(sex);
                        gender = esex[0].getText().toString().trim();

                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(age) || TextUtils.isEmpty(blood) || TextUtils.isEmpty(diseases)) {
                            Toast.makeText(ViewProfile.this, "Fill in all the details", Toast.LENGTH_SHORT).show();
                        } else {
                            session.setSharedPreference(name, session.getPhone(), blood, address, gender, age, diseases, session.getImgurl(), session.getEmail(), session.getPin());
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

                                            ename.setText(name);
                                            eaddress.setText(address);
                                            eage.setText(age);
                                            eblood.setText(blood);
                                            ediseases.setText(address);
                                            if (gender.equals("Male")) {
                                                egender.check(R.id.male);
                                            } else {
                                                egender.check(R.id.female);
                                            }

                                            customerEditDetails.dismiss();
                                        }
                                    } else {
                                        customerEditDetails.dismiss();
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
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}