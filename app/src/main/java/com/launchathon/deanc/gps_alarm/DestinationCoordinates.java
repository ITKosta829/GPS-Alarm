package com.launchathon.deanc.gps_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by DeanC on 8/16/2016.
 */
public class DestinationCoordinates extends DialogFragment{

    DataHandler DH;

    EditText latitude, longitude;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.destination_coordinates, null);

        DH = DataHandler.getInstance();

        latitude = (EditText) v.findViewById(R.id.latitude_entry);
        longitude = (EditText) v.findViewById(R.id.longitude_entry);

        latitude.requestFocus();

        AlertDialog.Builder b;
        b = new AlertDialog.Builder(getActivity());
        b.setView(v)

                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String lat = latitude.getText().toString();
                                String lon = longitude.getText().toString();

                                if (lat.equals("") || lon.equals("")){

                                    dialog.dismiss();

                                } else {

                                    DH.destinationLat = Double.parseDouble(lat);
                                    DH.destinationLon = Double.parseDouble(lon);

                                    DH.userDestination = new LatLng(DH.destinationLat, DH.destinationLon);

                                    DH.setMyDestination();

                                    MainActivity.mapFrag.getMapAsync(new OnMapReadyCallback() {
                                        @Override
                                        public void onMapReady(GoogleMap map) {
                                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DH.userDestination, 17));
                                            map.addMarker(new MarkerOptions()
                                                    .title("Your Destination")
                                                    .position(DH.userDestination));

                                            Toast.makeText(DH.mContext, "Your Destination Location", Toast.LENGTH_LONG).show();

                                            DH.alarmDistance = 500;

                                            DH.setUpdater();

                                            MainActivity.start_tracking.setVisibility(View.VISIBLE);
                                            MainActivity.start_tracking.setBackground(ContextCompat.getDrawable(DH.mContext, R.drawable.green_rounded_button));
                                        }
                                    });

                                    dialog.dismiss();
                                }
                            }
                        }
                );

        return b.create();
    }

}
