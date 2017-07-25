package com.example.rajk.geofiretrial3;


import com.example.rajk.geofiretrial3.CheckInternetConnectivity.NetWatcher;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by RajK on 11-05-2017.
 */
public class GeofireTrial3 extends android.support.multidex.MultiDexApplication {
    private static GeofireTrial3 mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if(!FirebaseApp.getApps(this).isEmpty()){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

    }
    public static synchronized GeofireTrial3 getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(NetWatcher.ConnectivityReceiverListener listener) {
        NetWatcher.connectivityReceiverListener = listener;
    }
}
