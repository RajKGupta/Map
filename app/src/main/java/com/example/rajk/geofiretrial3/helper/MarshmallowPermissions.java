package com.example.rajk.geofiretrial3.helper;

/**
 * Created by ghanendra on 11/06/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MarshmallowPermissions {

    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    public static final int READSMS_REQUEST_CODE = 5;
    public static final int RECVSMS_REQUEST_CODE = 6;
    public static final int LOCSERV_COARSE_REQUEST_CODE = 7;
    public static final int LOCSERV_REQUEST_CODE = 8;
    public static final int READ_CONTACTS_REQUEST_CODE = 9;
    public static final int SENDSMS_REQUEST_CODE = 4;
    public static final int MULTI_REQUEST_CODE = 10;

    public static final String sendsms = Manifest.permission.SEND_SMS;
    public static final String coarselocation = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String finelocation = Manifest.permission.ACCESS_FINE_LOCATION;

    Activity activity;

    public MarshmallowPermissions(Activity activity) {
        this.activity = activity;
    }

    public boolean checkPermissionForExternalStorage() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForCamera() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForReadSms() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForRecvSms() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForExternalStorage() {
        Toast.makeText(activity, "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    public void requestPermissionForReadsms() {
        Toast.makeText(activity, "Please allow to read sms to automatically detect otp.", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_SMS}, READSMS_REQUEST_CODE);

    }

    public void requestPermissionForRecievesms() {
        Toast.makeText(activity, "Please allow to read sms to automatically detect otp.", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECEIVE_SMS}, RECVSMS_REQUEST_CODE);

    }

    public boolean checkPermissionForLocations() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForLocations() {
        Toast.makeText(activity, "Please allow us to use locations.", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCSERV_REQUEST_CODE);

    }

    public boolean checkPermissionForCoarseLocations() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForCoarseLocations() {
        Toast.makeText(activity, "Please allow us to use locations.", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCSERV_COARSE_REQUEST_CODE);
    }

    public void requestPermissionForCamera() {
        Toast.makeText(activity, "Please allow to be able to take camera images.", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    public boolean checkPermissionForContacts() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForContacts() {
        Toast.makeText(activity, "Allow Reading Contact List to select Guardians .", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
    }

    public boolean checkPermissionForSendSms() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForSendSms() {
        Toast.makeText(activity, "Allow to send sms in case of emergencies to guardians.", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, SENDSMS_REQUEST_CODE);
    }

    public boolean checkMultiPermission() {

        return checkPermissionForSendSms() && checkPermissionForLocations() && checkPermissionForCoarseLocations() && checkPermissionForContacts();
    }

    public void requestMultiPermission() {
        ActivityCompat.requestPermissions(activity, new String[]
                {
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_CONTACTS
                }, MULTI_REQUEST_CODE);
    }

}