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

    protected static final String HTTP = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    protected static final String API_KEY = "&key=AIzaSyDvSzZs2vIJzot6RrRfPwlBWStLLTrkijY";

    EditText street, city, state, zip;
    LatLng userDestination;
    Double destinationLat, destinationLon;
    String URL, user_address;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.destination_address, null);

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
                                user_address = full_address.replace(' ', '+');

                                URL = HTTP + user_address + API_KEY;
                                new getAddressCoordinates().execute(URL);

                                dialog.dismiss();
                            }
                        }
                );

        return b.create();
    }

    private class getAddressCoordinates extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            StringBuilder sb = new StringBuilder();

            HttpURLConnection urlConnection = null;
            try {
                java.net.URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);
                urlConnection.connect();

                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(), "utf-8"));
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    in.close();

                    //System.out.println("" + sb.toString());
                    return sb.toString();

                } else {
                    System.out.println(urlConnection.getResponseMessage());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                JSONObject json = new JSONObject(result);
                JSONArray results = json.getJSONArray("results");
                JSONObject components = results.getJSONObject(0);
                JSONObject geometry = components.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                String lat = location.getString("lat");
                String lon = location.getString("lon");

                destinationLat = Double.parseDouble(lat);
                destinationLon = Double.parseDouble(lon);

                userDestination = new LatLng(destinationLat,destinationLon);

                MainActivity.mapFrag.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userDestination, 17));

                        // Markers identify locations on the map.
                        map.addMarker(new MarkerOptions()
                                .title("Your Destination")
                                .position(userDestination));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
