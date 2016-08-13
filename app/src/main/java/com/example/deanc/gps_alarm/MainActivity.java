package com.example.deanc.gps_alarm;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    protected static final String HTTP = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    protected static final String API_KEY = "&key=AIzaSyDvSzZs2vIJzot6RrRfPwlBWStLLTrkijY";

    public static MapFragment mapFrag;
    Button destination, start_tracking;
    Tracker gpsTracker;

    String URL;
    GoogleMap map;
    Double userLat, userLon;
    LatLng userLocation, userDestination;

    public static String user_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // userLat = 40.785227;
        // userLon = -73.673233;

        mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        destination = (Button) findViewById(R.id.destination);
        start_tracking = (Button) findViewById(R.id.start_tracking);
        start_tracking.setEnabled(false);

        mapFrag.getMapAsync(this);

        // userLocation = new LatLng(userLat,userLon);

        gpsTracker = new Tracker(MainActivity.this);

        if (gpsTracker.canGetLocation()) {

            userLat = gpsTracker.getLatitude();
            userLon = gpsTracker.getLongitude();

            userLocation = new LatLng(userLat,userLon);

        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }

//        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 1000, (float) 10, new LocationListener() {
//
//            @Override
//            public void onLocationChanged(Location location) {
//
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//
//            }
//
//        });
//        Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//        userLat = location.getLatitude();
//        userLon = location.getLongitude();
//
//         userLocation = new LatLng(userLat,userLon);



        destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DestinationAddress destinationAddress = new DestinationAddress();
                destinationAddress.show(getFragmentManager(),"display");
                start_tracking.setEnabled(true);
            }
        });

        start_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });


    }

    @Override
    public void onMapReady(GoogleMap map) {

        // Focus map to particular place.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));

        // Markers identify locations on the map.
        map.addMarker(new MarkerOptions()
                .title("You are Here")
                .position(userLocation));

    }

    public Double calcDistance(Double Start_LAT, Double Start_LON, Double End_LAT, Double End_LON) {

        final int R = 6371; // Radius of the earth
        Double latDistance = toRad(End_LAT - Start_LAT);
        Double lonDistance = toRad(End_LON - Start_LON);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(Start_LAT)) * Math.cos(toRad(End_LAT)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double distance = R * c;

        distance = round(distance, 4);

        return distance;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }


}
