package com.example.rajk.geofiretrial3;

import com.example.rajk.geofiretrial3.model.SharedPreference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SaferIndia extends android.support.multidex.MultiDexApplication {
    private static SaferIndia mInstance;
    public static DatabaseReference DBREF;
    public static SimpleDateFormat formatterWithMonthNameAndTime = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
    public static SimpleDateFormat simpleDateFormatWithMonthName = new SimpleDateFormat("dd-MMM-yyyy");
    public static String AppName = "FeelSafe";
    public static String userSession = "userSession";
    public static String PersonalDetails = "PersonalDetails";
    public static String FCMToken = "FCMToken";
    public static String name="name";
    public static String email="email";
    public static String phone="phone";
    public static String panick="panick";
    public static String address="address";
    public static String age="name";
    public static String diseases="diseases";
    public static String shareLocation="shareLocation";
    public static String alarmSound="alarmSound";
    public static String gender="gender";
    public static String blood="blood";
    public static String imgurl="imgurl";
    public static String online="online";
    public static String lastSeen="lastSeen";
    public static String userLoction="userLoction";
    public static String myLocation = "myLocation";
    public static String users = "Users";
    public static String loggedIn = "loggedIn";
    public static String UID = "UID";
    public SharedPreference session ;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        DBREF = FirebaseDatabase.getInstance().getReference().child(AppName).getRef();
        session = new SharedPreference(getApplicationContext());
        String UID = "";
        if (session.getUID()!=null)
            UID = session.getUID();
        setOnlineStatus(UID);
    }

    public static synchronized SaferIndia getInstance()
    {
        return mInstance;
    }

    public void setOnlineStatus(String userkey) {
        if (!userkey.equals("")) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myConnectionsRef = DBREF.child(userSession).child(userkey).child(online).getRef();

            // stores the timestamp of my last disconnect (the last time I was seen online)
            final DatabaseReference lastOnlineRef = database.getReference().child(userSession).child(userkey).child(lastSeen).getRef();

            final DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        myConnectionsRef.setValue(Boolean.TRUE);
                        myConnectionsRef.onDisconnect().setValue(Boolean.FALSE);
                        lastOnlineRef.onDisconnect().setValue(getTimeStamp());
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Listener was cancelled at .info/connected");
                }
            });
        }

    }
    public static String getTimeStamp()
    {
        String timestamp = formatterWithMonthNameAndTime.format(Calendar.getInstance().getTime());
        return timestamp;
    }
    public static String getTimeStampInMs()
    {
        String timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
        return timestamp;
    }
    public static String getRevreseTimeStampInMs()
    {
        String timestamp = String.valueOf(99999999999999L-Calendar.getInstance().getTimeInMillis());
        return timestamp;
    }
//Send notification to an individual
/*
    public static void sendNotif(final String senderId, final String receiverId, final String type, final String content, final String taskId) {
        long idLong = Calendar.getInstance().getTimeInMillis();
        idLong = 9999999999999L - idLong;
        final String id = String.valueOf(idLong);
        final String timestamp = formatter.format(Calendar.getInstance().getTime());
        DBREF.child("Fcmtokens").child(receiverId).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String receiverFCMToken = dataSnapshot.getValue(String.class);
                if (receiverFCMToken != null && !receiverFCMToken.equals("")) {
                    Notif newNotif = new Notif(id, timestamp, type, senderId, receiverId, receiverFCMToken, content, taskId);
                    DBREF.child("Notification").child(receiverId).child(id).setValue(newNotif);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
*/
// Send Notification to everybody
/*
    public static void sendNotifToAllCoordinators(final String senderId, final String type, final String content, final String taskId) {
        long idLong = Calendar.getInstance().getTimeInMillis();
        idLong = 9999999999999L - idLong;
        final String id = String.valueOf(idLong);
        final String timestamp = formatter.format(Calendar.getInstance().getTime());
        final DatabaseReference dbCoordinator = DBREF.child("Coordinator").getRef();
        dbCoordinator.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    final String receiverId = ds.getKey();
                    DBREF.child("Fcmtokens").child(receiverId).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String receiverFCMToken = dataSnapshot.getValue(String.class);
                            if (receiverFCMToken != null&&!receiverFCMToken.equals("") ) {
                                Notif newNotif = new Notif(id, timestamp, type, senderId, receiverId, receiverFCMToken, content, taskId);
                                DBREF.child("Notification").child(receiverId).child(id).setValue(newNotif);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
}*/
    }
