package com.example.rajk.geofiretrial3.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.helper.MarshmallowPermissions;
import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.net.URL;

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.IPanicked;
import static com.example.rajk.geofiretrial3.SaferIndia.Safe;
import static com.example.rajk.geofiretrial3.SaferIndia.emergencyContact;
import static com.example.rajk.geofiretrial3.SaferIndia.name;
import static com.example.rajk.geofiretrial3.SaferIndia.sendNotif;
import static com.example.rajk.geofiretrial3.SaferIndia.users;


public class SendSMSService extends IntentService {

    public SendSMSService() {
        super("SendSMSService");
    }
    private SharedPreference session;
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ///SENDING SMS TO ALL GUARDIANS CALL THIS SERVICE IN CASE OF EMERGENCY
            session = new SharedPreference(this);
            String emmsg;
            if(session.getPanick()) {
                emmsg = "EMERGENCY!!! "+session.getName()+" just panicked and needs your help. Track me here - feelsafe-9b333.firebaseapp.com/?uid=" + session.getUID();
            }
            else
            {
                emmsg  = session.getName()+" has arrived safely now. No need to worry. Thanks for your concern.";
            }
            if(checkPermissionForSendSms())
            {
                final String msg = emmsg;
                DBREF.child(users).child(session.getUID()).child(emergencyContact).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            String uid = ds.getValue(String.class);
                            String phone = ds.getKey();
                            if(!uid.substring(0,4).equals(name))
                            {
                                if(session.getPanick())
                                sendNotif(session.getUID(),uid,IPanicked,"EMERGENCY!!! "+session.getName()+" just panicked and needs your help.");
                                else
                                sendNotif(session.getUID(),uid,Safe,msg);
                            }
                            sendSMS(phone,msg);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }
    public boolean checkPermissionForSendSms(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }
    public void sendSMS(String phoneNo, String msg) {
        if (checkPermissionForSendSms()) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, msg, null, null);

            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }
}
