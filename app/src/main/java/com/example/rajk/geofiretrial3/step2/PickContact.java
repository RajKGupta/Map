package com.example.rajk.geofiretrial3.step2;
// TODO Delay of 2 sec for contacts to load

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.DropBoxManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.services.UploadContact;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import java.util.ArrayList;
import static com.example.rajk.geofiretrial3.SaferIndia.contactList;

public class PickContact extends AppCompatActivity {
    private static final int CONTACT_PICKER_REQUEST = 12;
    private static int SPLASH_TIME_OUT = 3000;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_contact);

        dialog = new ProgressDialog(PickContact.this);
        dialog.setMessage( "Detecting...");
        dialog.show();
        call();

        }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CONTACT_PICKER_REQUEST){
            if(resultCode == RESULT_OK)
            {
                ArrayList<ContactResult> results = MultiContactPicker.obtainResult(data);
                Intent serviceIntent =new Intent(this, UploadContact.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(contactList,results);
                serviceIntent.putExtras(bundle);
                startService(serviceIntent);
                Toast.makeText(PickContact.this,"The selected contacts were added to the Guardians List.",Toast.LENGTH_SHORT).show();
                super.onBackPressed();
                finish();
            } else if(resultCode == RESULT_CANCELED){
                super.onBackPressed();
                finish();
            }
        }
    }
    private  void call()
    {
        new MultiContactPicker.Builder(PickContact.this) //Activity/fragment context
                .theme(R.style.AppTheme_NoActionBar) //Optional - default: MultiContactPicker.Azure
                .hideScrollbar(false) //Optional - default: false
                .showTrack(true) //Optional - default: true
                .searchIconColor(Color.WHITE) //Option - default: White
                .handleColor(ContextCompat.getColor(PickContact.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                .bubbleColor(ContextCompat.getColor(PickContact.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                .bubbleTextColor(Color.WHITE) //Optional - default: White
                .showPickerForResult(CONTACT_PICKER_REQUEST);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, SPLASH_TIME_OUT);
    }
}
