package com.example.rajk.geofiretrial3.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.rajk.geofiretrial3.MapsActivity2;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.SaferIndia;
import com.example.rajk.geofiretrial3.main.PanicMapsActivity;
import com.example.rajk.geofiretrial3.model.Online;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.rajk.geofiretrial3.SaferIndia.AppName;
import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.IPanicked;
import static com.example.rajk.geofiretrial3.SaferIndia.Safe;
import static com.example.rajk.geofiretrial3.SaferIndia.invite;
import static com.example.rajk.geofiretrial3.SaferIndia.myPanicResponsibilityId;
import static com.example.rajk.geofiretrial3.SaferIndia.panick;
import static com.example.rajk.geofiretrial3.SaferIndia.share;
import static com.example.rajk.geofiretrial3.SaferIndia.type;
import static com.example.rajk.geofiretrial3.SaferIndia.userSession;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
            String type = remoteMessage.getData().get(SaferIndia.type);
            String body = remoteMessage.getData().get("body");
            String senderuid = remoteMessage.getData().get("senderuid");
            String id = remoteMessage.getData().get("msgid");
            if (body != null && senderuid != null && id!=null) {
                if (type.equals(IPanicked)) {
                    sendPanicNotification(body,senderuid,id);
                } else {
                    sendGeneralNotification(body,senderuid,id,type);
                }
            }
    }

    private void sendGeneralNotification(String body, String senderuid, String id,String type) {
        Intent intent = new Intent(this, MapsActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyFirebaseMessagingService.this)
                            .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                            .setContentTitle(AppName)
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
                            NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String notifid = id.substring(8);
                    if (type.equals(Safe) )
                    {
                        notificationManager.cancelAll();
                    }
                    notificationManager.notify(Integer.parseInt(notifid) /* ID of notification */, notificationBuilder.build());
                }



    private boolean isAppIsInForeground(Context context) {
        boolean isInForeground = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                isInForeground = true;
            }

        }
        return true;
    }
    private void sendPanicNotification(final String body, final String senderuid, final String id) {
        Intent intent = new Intent(this, PanicMapsActivity.class);
        intent.putExtra(myPanicResponsibilityId,senderuid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent shareIntent = new Intent(this, PanicMapsActivity.class);
        shareIntent.putExtra(myPanicResponsibilityId,senderuid);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(share,true);
        shareIntent.putExtra("body",body);

        final String notifId  = id.substring(10);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, Integer.parseInt(notifId), intent,
                PendingIntent.FLAG_ONE_SHOT);
        final PendingIntent pendingIntentShare = PendingIntent.getActivity(this, Integer.parseInt(notifId)+1, intent,
                PendingIntent.FLAG_ONE_SHOT);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyFirebaseMessagingService.this)
                            .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Emergency!! Help your friend")
                            .setContentText(body)
                            .setOngoing(true)
                            .setAutoCancel(false)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent)
                            .addAction(R.drawable.ic_location_grey,"TRACK", pendingIntent)
                            .addAction(R.drawable.ic_share_black_24dp,"SHARE", pendingIntentShare);
                    final NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = notificationBuilder.build();
            notificationManager.notify(Integer.parseInt(notifId), notification);


        final DatabaseReference panicRef = DBREF.child(SaferIndia.users).child(senderuid).child(panick);
        panicRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Boolean panic = dataSnapshot.getValue(Boolean.class);
                    if(!panic)
                    {
                        notificationManager.cancel(Integer.parseInt(notifId));
                        panicRef.removeEventListener(this);
                    }
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
