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
import static com.example.rajk.geofiretrial3.SaferIndia.emergencyContact;
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
            final String msg  = "EMERGENCY!!! I need your help. Track me here - feelsafe-9b333.firebaseapp.com/?uid="+session.getUID();
            if(checkPermissionForSendSms())
            {
                DBREF.child(users).child(session.getUID()).child(emergencyContact).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            String uid = ds.getValue(String.class);
                            String phone = ds.getKey();
                            if(uid.length()>10)
                            {
                                //TODO send notifications to this UID
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
                Toast.makeText(getApplicationContext(), "Message Sent",
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }
}
