package com.example.rajk.geofiretrial3.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.SharedPreference;

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.users;

public class ProfileActivity extends AppCompatActivity {
    String name, phone, blood, address, gender, age, diseases, imgurl;
    EditText ename, ephone, eblood, eaddress, eage, ediseases, eimgurl;
    Button submit_profile;
    public SharedPreference session;
    RadioGroup egender;
    RadioButton esex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SharedPreference(this);

        ename = (EditText) findViewById(R.id.ename);
        ephone = (EditText) findViewById(R.id.ephone);
        eaddress = (EditText) findViewById(R.id.eaddress);
        eblood = (EditText) findViewById(R.id.eblood);
        eage = (EditText) findViewById(R.id.eage);
        egender = (RadioGroup) findViewById(R.id.egender);
        egender.clearCheck();
        ediseases = (EditText) findViewById(R.id.edisease);
        submit_profile = (Button) findViewById(R.id.submit_profile);

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
                    session.setSharedPreference(name, phone, blood, address, gender, age, diseases, session.getImgurl(), session.getEmail());
                    DBREF.child(users).child(session.getUID()).setValue(new PersonalDetails(session.getName(), session.getPhone(), session.getBlood(), session.getAddress(), session.getGender(), session.getAge(), session.getDiseases(), session.getImgurl(), session.getEmail()));
                    Intent intent = new Intent(ProfileActivity.this, MapsActivity2.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }
}
