package com.launchathon.deanc.gps_alarm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
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
    public static Button destinationAddress, extras, start_tracking, cancel, lirr;
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
        extras = (Button) findViewById(R.id.extras);
        start_tracking = (Button) findViewById(R.id.start_tracking);
        lirr = (Button) findViewById(R.id.lirr);
        cancel = (Button) findViewById(R.id.cancel);
        //start_tracking.setEnabled(false);

        if (DH.userDestination == null) {
            start_tracking.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);
        }
        if (DH.userDestination != null) cancel.setBackground(ContextCompat.getDrawable(DH.mContext, R.drawable.red_rounded_button));

        mapFrag.getMapAsync(this);

        DH.startLIRR_AsyncTask();

        lirr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DestinationLIRRStation DLR = new DestinationLIRRStation();
                DLR.show(getFragmentManager(), "display");
                //start_tracking.setEnabled(true);
            }
        });

        destinationAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DestinationAddress destinationAddress = new DestinationAddress();
                destinationAddress.show(getFragmentManager(), "display");
                //start_tracking.setEnabled(true);
            }
        });

        extras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(MainActivity.this, extras);
                popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.coordinates:
                                DestinationCoordinates DC = new DestinationCoordinates();
                                DC.show(getFragmentManager(), "display");
                                break;
                            case R.id.read_me:
                                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                                adb.setTitle("App Info");
                                adb.setMessage("Thank you for trying my app.\n\n" +
                                        "Default GPS Alarm triggers are as follows:\n\n" +
                                        "LIRR tracking will activate within 1 mile of destination.\n\n" +
                                        "Address and Coordinates tracking will activate within 1/3 of a mile.\n\n" +
                                        "If there are any questions or comments please feel free to e-mail me.");
                                adb.setPositiveButton("OK", new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                adb.show();
                                break;
                            default:
                                return true;
                        }
                        return true;
                    }
                });
                popup.show();
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

                    start_tracking.setBackground(ContextCompat.getDrawable(DH.mContext, R.drawable.black_rounded_button));
                    cancel.setVisibility(View.VISIBLE);
                    cancel.setBackground(ContextCompat.getDrawable(DH.mContext, R.drawable.red_rounded_button));
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

                    if (android.os.Build.VERSION.SDK_INT >= 23){
                        if (DH.DnD == 0){
                            DH.am.setStreamVolume(AudioManager.STREAM_RING, DH.originalVolume, 0);

                        }
                    }else {
                        DH.am.setStreamVolume(AudioManager.STREAM_RING, DH.originalVolume, 0);
                    }
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

                cancel.setBackground(ContextCompat.getDrawable(DH.mContext, R.drawable.black_rounded_button));
                start_tracking.setVisibility(View.INVISIBLE);
                cancel.setVisibility(View.INVISIBLE);
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
