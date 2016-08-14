package com.example.deanc.gps_alarm;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

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

/**
 * Created by dcsir on 8/13/2016.
 */
public class DataHandler {

    protected static final String HTTP = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    protected static final String API_KEY = "&key=AIzaSyDvSzZs2vIJzot6RrRfPwlBWStLLTrkijY";

    public Double userLat, userLon, destinationLat, destinationLon;
    public String user_address;
    LatLng userLocation, userDestination;
    String URL;

    private static DataHandler instance = new DataHandler();

    private DataHandler() {


    }

    public static DataHandler getInstance() {
        return instance;
    }

    public void startAsyncTask(){
        URL = HTTP + user_address + API_KEY;
        new getAddressCoordinates().execute(URL);
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

                userDestination = new LatLng(destinationLat, destinationLon);

//                mapFrag.getMapAsync(new OnMapReadyCallback() {
//                    @Override
//                    public void onMapReady(GoogleMap map) {
//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userDestination, 17));
//
//                        // Markers identify locations on the map.
//                        map.addMarker(new MarkerOptions()
//                                .title("Your Destination")
//                                .position(userDestination));
//                    }
//                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
