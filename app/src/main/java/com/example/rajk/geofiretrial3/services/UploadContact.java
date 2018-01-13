package com.example.rajk.geofiretrial3.services;

import android.app.IntentService;
import android.content.Intent;

import com.example.rajk.geofiretrial3.model.Notification;
import com.example.rajk.geofiretrial3.model.Online;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wafflecopter.multicontactpicker.ContactResult;

import java.util.ArrayList;

import static com.example.rajk.geofiretrial3.SaferIndia.AppLink;
import static com.example.rajk.geofiretrial3.SaferIndia.AppName;
import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.InviteSMSMessage;
import static com.example.rajk.geofiretrial3.SaferIndia.InviteSMSNumber;
import static com.example.rajk.geofiretrial3.SaferIndia.Notification;
import static com.example.rajk.geofiretrial3.SaferIndia.addedGuardian;
import static com.example.rajk.geofiretrial3.SaferIndia.contactList;
import static com.example.rajk.geofiretrial3.SaferIndia.emergencyContact;
import static com.example.rajk.geofiretrial3.SaferIndia.getReverseTimeStampInMs;
import static com.example.rajk.geofiretrial3.SaferIndia.getTimeStamp;
import static com.example.rajk.geofiretrial3.SaferIndia.getTimeStampInMs;
import static com.example.rajk.geofiretrial3.SaferIndia.guardianNotUser;
import static com.example.rajk.geofiretrial3.SaferIndia.invite;
import static com.example.rajk.geofiretrial3.SaferIndia.myResponsibility;
import static com.example.rajk.geofiretrial3.SaferIndia.name;
import static com.example.rajk.geofiretrial3.SaferIndia.phoneVsId;
import static com.example.rajk.geofiretrial3.SaferIndia.sendNotif;
import static com.example.rajk.geofiretrial3.SaferIndia.userSession;
import static com.example.rajk.geofiretrial3.SaferIndia.users;
import static com.example.rajk.geofiretrial3.main.LoginActivity.session;


public class UploadContact extends IntentService {


    public UploadContact() {
        super("UploadContact");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ArrayList<ContactResult> results=intent.getParcelableArrayListExtra(contactList);
            for (ContactResult y:results) {
                final ContactResult x=y;
                if (x.getPhoneNumbers().size() != 0) {
                    int j = x.getPhoneNumbers().size();
                    for (int i = 0; i < j; i++) {
                        final int finalI = i;
                        String number1 = x.getPhoneNumbers().get(finalI);
                        number1=number1.replaceAll("[^0-9]","");
                        final String number=number1.substring(number1.length()-10);
                        DBREF.child(phoneVsId).child(number).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    DBREF.child(users).child(session.getUID()).child(emergencyContact).child(number).setValue(dataSnapshot.getValue(String.class));
                                    DBREF.child(users).child(dataSnapshot.getValue(String.class)).child(myResponsibility).child(session.getPhone()).setValue(session.getUID());
                                    DBREF.child(userSession).child(dataSnapshot.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Online online = dataSnapshot.getValue(Online.class);
                                            sendNotif(session.getUID(),online.getId(),addedGuardian,session.getName()+" added you as Guardian");
                                            sendNotif(session.getUID(),session.getUID(),addedGuardian,"You added "+online.getName()+"  as Guardian");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    }
                                else
                                {
                                    DBREF.child(guardianNotUser).child(number).child(session.getPhone()).setValue(session.getUID());
                                    DBREF.child(users).child(session.getUID()).child(emergencyContact).child(number).setValue(name+x.getDisplayName());
                                    sendNotif(session.getUID(),session.getUID(),addedGuardian,"You added "+x.getDisplayName()+"  as Guardian.");
                                    sendNotif(AppName,session.getUID(),invite,x.getDisplayName()+" is not a user but still can track you in emergencies. Send app invite and increase your security network.");
                                    Intent intent = new Intent(UploadContact.this,InviteSMS.class);
                                    intent.putExtra(InviteSMSNumber,number);
                                    intent.putExtra(InviteSMSMessage,session.getName()+" added you as Guardian. Download the app from "+AppLink);
                                    startService(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

        }
    }


}
