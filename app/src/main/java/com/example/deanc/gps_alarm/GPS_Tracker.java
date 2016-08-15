package com.example.deanc.gps_alarm;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dcsir on 8/14/2016.
 */
public class GPS_Tracker extends Service {

    boolean isConnected;
    LocationManager locationManager;
    Location location;
    public double latitude;
    public double longitude;
    public CounterClass updater;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        checkNetwork();
        getLocation();
        //updater = new CounterClass(600000, 300000);
    }

    protected void checkNetwork() {

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    protected void getLocation() {

        if (isConnected) {

            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT == 23)

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 1000, (float) 10, new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }

            });

            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 1.2 called from Main Activity - startNewService
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.

        // For time consuming an long tasks you can launch a new thread here...

        //Toast.makeText(this, "GPS Updater has started.", Toast.LENGTH_LONG).show();

        updater.start();

        Intent intent1 = new Intent(GPS_Tracker.this, SweetDreams.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        /*
        2.2
        onDestroy Called by the system to notify a Service that it is no longer used and is being removed.
        when stopNewService called from Main Activity,  it calls stopService. So no service is running
        at that moment and so onDestroy event is invoked.
        */

        // Service destroyed.
        updater.cancel();

    }

    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            if (latitude != 0.0) {
                // childLAT = String.valueOf(latitude);
                // childLON = String.valueOf(longitude);
            } else {
                // childLAT = String.valueOf(location.getLatitude());
                // childLON = String.valueOf(location.getLongitude());
            }

        }

        @Override
        public void onFinish() {
            updater.start();
        }
    }
}
