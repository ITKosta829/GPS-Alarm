package com.example.deanc.gps_alarm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    public static MapFragment mapFrag;
    Button destination, start_tracking;
    Tracker gpsTracker;

    GoogleMap map;
    Double userLat, userLon;

    DataHandler DH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DH = DataHandler.getInstance();

        mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        destination = (Button) findViewById(R.id.destination);
        start_tracking = (Button) findViewById(R.id.start_tracking);
        start_tracking.setEnabled(false);

        mapFrag.getMapAsync(this);

        // DH.userLat = 40.785227;
        // DH.userLon = -73.673233;
        // userLocation = new LatLng(userLat,userLon);

        gpsTracker = new Tracker(MainActivity.this);

        if (gpsTracker.canGetLocation()) {

            DH.userLat = gpsTracker.getLatitude();
            DH.userLon = gpsTracker.getLongitude();
            DH.userLocation = new LatLng(userLat, userLon);

        } else {
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
                destinationAddress.show(getFragmentManager(), "display");
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DH.userLocation, 17));

        // Markers identify locations on the map.
        map.addMarker(new MarkerOptions()
                .title("You are Here")
                .position(DH.userLocation));

    }

}
