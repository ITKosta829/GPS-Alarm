package com.launchathon.deanc.gps_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by DeanC on 8/18/2016.
 */
public class DestinationLIRRStation extends DialogFragment {

    DataHandler DH;
    Button button;

    String selectedName;
    int position;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.destination_lirr_station, null);

        DH = DataHandler.getInstance();


        ArrayList<String> stations = new ArrayList<>();

        for (int a = 0; a < DH.getAllStations_LIRR().size(); a++) {
            stations.add(DH.getAllStations_LIRR().get(a).getNAME());
        }

        final Spinner spinner = (Spinner) v.findViewById(R.id.lirr_station_list_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(DH.mContext,R.layout.spinner_textview, stations);

        adapter.setDropDownViewResource(R.layout.spinner_textview);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                selectedName = spinner.getSelectedItem().toString();
                position = i;
                Log.d("SPINNER SELECTION", selectedName + " " + position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        AlertDialog.Builder b;
        b = new AlertDialog.Builder(getActivity());
        b.setView(v)

                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                DH.destinationLat = DH.getAllStations_LIRR().get(position).getLAT();
                                DH.destinationLon = DH.getAllStations_LIRR().get(position).getLON();

                                DH.userDestination = new LatLng(DH.destinationLat, DH.destinationLon);

                                DH.setMyDestination();

                                MainActivity.mapFrag.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap map) {
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DH.userDestination, 17));

                                        // Markers identify locations on the map.
                                        map.addMarker(new MarkerOptions()
                                                .title("Your Destination")
                                                .position(DH.userDestination));

                                        Toast.makeText(DH.mContext, "Your Destination Location", Toast.LENGTH_LONG).show();

                                        DH.alarmDistance = 1609;

                                        DH.setUpdater();

                                        MainActivity.start_tracking.setVisibility(View.VISIBLE);
                                        MainActivity.start_tracking.setBackground(ContextCompat.getDrawable(DH.mContext, R.drawable.green_rounded_button));
                                    }
                                });

                                dialog.dismiss();
                            }
                        }
                );

        return b.create();

    }
}
