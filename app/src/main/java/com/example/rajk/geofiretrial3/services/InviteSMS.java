package com.example.rajk.geofiretrial3.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.model.SharedPreference;
import static com.example.rajk.geofiretrial3.SaferIndia.InviteSMSMessage;
import static com.example.rajk.geofiretrial3.SaferIndia.InviteSMSNumber;

public class InviteSMS extends IntentService {
    public InviteSMS() {
        super("InviteSMS");
    }

    public SharedPreference session;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            session = new SharedPreference(this);


            if (checkPermissionForSendSms()) {
                final String msg = intent.getStringExtra(InviteSMSMessage);
                final String no = intent.getStringExtra(InviteSMSNumber);
                sendSMS(no, msg);

            }
        }
    }

    public boolean checkPermissionForSendSms() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
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
