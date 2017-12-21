package com.example.rajk.geofiretrial3.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import com.example.rajk.geofiretrial3.R;
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

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.userSession;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private ArrayList<String> chatnotifList = new ArrayList<>();
    private static final String TAG1 = "MyFireMesgService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        String type = remoteMessage.getData().get("type");
            String body = remoteMessage.getData().get("body");
            String senderuid = remoteMessage.getData().get("senderuid");
            String taskId = remoteMessage.getData().get("taskId");
            String id = remoteMessage.getData().get("msgid");
            if (body != null && taskId != null && senderuid != null)
                sendGeneralNotification(body, senderuid, taskId, id);

    }

    private void sendGeneralNotification(String body, String senderuid, String taskId, String id) {
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
}
