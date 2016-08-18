package com.example.deanc.gps_alarm;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    public static MapFragment mapFrag;
    Button destinationAddress, destinationCoordinates, start_tracking, cancel, lirr;
    DataHandler DH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DH = DataHandler.getInstance();
        DH.mContext = getBaseContext();

        mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        destinationAddress = (Button) findViewById(R.id.destination);
        destinationCoordinates = (Button) findViewById(R.id.coordinates);
        start_tracking = (Button) findViewById(R.id.start_tracking);
        lirr = (Button) findViewById(R.id.lirr);
        cancel = (Button) findViewById(R.id.cancel);
        start_tracking.setEnabled(false);

        mapFrag.getMapAsync(this);

        DH.getLocation();
        DH.startLIRR_AsyncTask();

        lirr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DestinationLIRRStation DLR = new DestinationLIRRStation();
                DLR.show(getFragmentManager(), "display");
                start_tracking.setEnabled(true);
            }
        });

        destinationAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DestinationAddress destinationAddress = new DestinationAddress();
                destinationAddress.show(getFragmentManager(), "display");
                start_tracking.setEnabled(true);
            }
        });

        destinationCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DestinationCoordinates DC = new DestinationCoordinates();
                DC.show(getFragmentManager(), "display");
                start_tracking.setEnabled(true);
            }
        });

        start_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (DH.userDestination != null) {
                    DH.updater.start();

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(DH.userLocation);
                    builder.include(DH.userDestination);
                    LatLngBounds bounds = builder.build();
                    int padding = 40;
                    final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    mapFrag.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap map) {
                            map.moveCamera(cu);
                        }
                    });
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DH.userDestination = null;
                
                if (DH.updater != null) DH.updater.cancel();

                if (DH.v != null) {
                    DH.v.cancel();
                    DH.r.stop();
                }

                mapFrag.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        map.clear();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DH.userLocation, 17));

                        map.addMarker(new MarkerOptions()
                                .title("You are Here")
                                .position(DH.userLocation));

                    }
                });
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

        Toast.makeText(MainActivity.this, "Your Current Location", Toast.LENGTH_SHORT).show();
    }

}
