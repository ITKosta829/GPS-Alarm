package com.example.deanc.gps_alarm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
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
    GoogleMap map;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DH = DataHandler.getInstance();
        DH.mContext = MainActivity.this;

        mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        destinationAddress = (Button) findViewById(R.id.destination);
        destinationCoordinates = (Button) findViewById(R.id.coordinates);
        start_tracking = (Button) findViewById(R.id.start_tracking);
        lirr = (Button) findViewById(R.id.lirr);
        cancel = (Button) findViewById(R.id.cancel);
        start_tracking.setEnabled(false);

        mapFrag.getMapAsync(this);

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

        this.map = map;

        checkPermissions();

     }

    private void setMapLoc(){

        if (DH.userLocation == null) return;

        // Focus map to particular place.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DH.userLocation, 17));

        // Markers identify locations on the map.
        map.addMarker(new MarkerOptions()
                .title("You are Here")
                .position(DH.userLocation));

        Toast.makeText(MainActivity.this, "Your Current Location", Toast.LENGTH_SHORT).show();

    }

    private void checkPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to GPS",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }


                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        setMapLoc();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "GPS permission denied.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                setMapLoc();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DH.getLocation();
        if (DH.userLocation != null && this.map != null) {

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DH.userLocation, 17));

            map.addMarker(new MarkerOptions()
                    .title("You are Here")
                    .position(DH.userLocation));

            Toast.makeText(MainActivity.this, "Your Current Location", Toast.LENGTH_SHORT).show();
        }
    }

}
