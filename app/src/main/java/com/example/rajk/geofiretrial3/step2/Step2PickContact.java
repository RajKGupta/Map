package com.example.rajk.geofiretrial3.step2;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;

import static com.example.rajk.geofiretrial3.SaferIndia.showLongToast;

public class Step2PickContact extends AppCompatActivity {
    private Button pickConatct, skip;
    private MarshmallowPermissions marshmallowPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2_pick_contact);
        pickConatct = (Button) findViewById(R.id.selectContact);
        skip = (Button) findViewById(R.id.skip);
        marshmallowPermissions = new MarshmallowPermissions(this);
        if (!marshmallowPermissions.checkPermissionForContacts()) {
            marshmallowPermissions.requestPermissionForContacts();
        }

        pickConatct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (marshmallowPermissions.checkPermissionForContacts())
                    startActivity(new Intent(Step2PickContact.this, PickContact2.class));
                else {
                    marshmallowPermissions.requestPermissionForContacts();
                    if (marshmallowPermissions.checkPermissionForContacts())
                        startActivity(new Intent(Step2PickContact.this, PickContact2.class));
                    else {
                        Toast.makeText(Step2PickContact.this, "You need to provide permission to access contacts.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLongToast(Step2PickContact.this, "You can add contacts by swiping right and click on Add Guardians");
                Intent intent = new Intent(Step2PickContact.this, MapsActivity2.class);
                startActivity(intent);
                finish();
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
                        Step2PickContact.super.onBackPressed();
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