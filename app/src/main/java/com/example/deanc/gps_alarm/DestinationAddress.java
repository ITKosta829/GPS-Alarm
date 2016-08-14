package com.example.deanc.gps_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.Bind;

/**
 * Created by DeanC on 8/12/2016.
 */
public class DestinationAddress extends DialogFragment{

    DataHandler DH;

    EditText street, city, state, zip;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.destination_address, null);

        DH = DataHandler.getInstance();

        street = (EditText) v.findViewById(R.id.street_entry);
        city = (EditText) v.findViewById(R.id.city_entry);
        state = (EditText) v.findViewById(R.id.state_entry);
        zip = (EditText) v.findViewById(R.id.zip_entry);

        AlertDialog.Builder b;
        b = new AlertDialog.Builder(getActivity());
        b.setView(v)

                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String strt, c, sta, z, full_address;

                                strt = street.getText().toString();
                                c = city.getText().toString();
                                sta = state.getText().toString();
                                z = zip.getText().toString();

                                full_address = strt + ",+" + c + ",+" + sta + ",+" + z;
                                DH.user_address = full_address.replace(' ', '+');

                                DH.startAsyncTask();

                                dialog.dismiss();
                            }
                        }
                );

        return b.create();
    }



}
