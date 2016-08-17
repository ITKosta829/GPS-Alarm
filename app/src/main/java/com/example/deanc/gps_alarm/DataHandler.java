package com.example.deanc.gps_alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.List;

/**
 * Created by dcsir on 8/13/2016.
 */
public class DataHandler {

    protected static final String HTTP = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    protected static final String API_KEY = "&key=AIzaSyDvSzZs2vIJzot6RrRfPwlBWStLLTrkijY";
    protected static final String LIRR_API_SEARCH = "https://traintime.lirr.org/api/StationsAll?api_key=f63af35c0bc02d01bf133806cb469aad";

    private List<LIRR_Station> stationList;

    public Double userLat, userLon, destinationLat, destinationLon, distanceFromDest;
    public String user_address;
    LatLng userLocation, userDestination;
    String URL;
    MediaPlayer MP;

    public CounterClass updater;
    public Context mContext;

    Tracker gpsTracker;

    public Location myLocation, myDestination;

    private static DataHandler instance = new DataHandler();

    private DataHandler() {

    }

    public static DataHandler getInstance() {
        return instance;
    }

    public List<LIRR_Station> getAllStations() {
        return stationList;
    }

    public void addStation(String name, String lat, String lon) {
        LIRR_Station station = new LIRR_Station(name, lat, lon);
        stationList.add(station);
    }

    public void deleteStation(int index) {
        stationList.remove(index);
    }

    public void getLocation(){
        // if (gpsTracker == null) {
            gpsTracker = new Tracker(mContext);
        // }

        if (gpsTracker.canGetLocation()) {

            myLocation = gpsTracker.fetchLocation();

            userLat = gpsTracker.getLatitude();
            userLon = gpsTracker.getLongitude();
            Log.d("MyTag", "User Lat: " + userLat);
            Log.d("MyTag", "User Lon: " + userLon);
            userLocation = new LatLng(userLat, userLon);

        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    public void startAsyncTask(){
        URL = HTTP + user_address + API_KEY;
        new getAddressCoordinates().execute(URL);
    }

    public void startLIRR_AsyncTask(){
        if (stationList == null) {
            new getStations().execute(LIRR_API_SEARCH);
        }
    }

    public Double calcDistance(LatLng start, LatLng end) {

        final int R = 6371; // Radius of the earth
        Double latDistance = toRad(end.latitude - start.latitude);
        Double lonDistance = toRad(end.longitude - start.longitude);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(start.latitude)) * Math.cos(toRad(end.latitude)) *
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

    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            getLocation();
            //distanceFromDest = calcDistance(userDestination, userLocation);

            float distanceToDest = myLocation.distanceTo(myDestination);

            Toast.makeText(mContext, "User Coordinates:\n" +
                    "Latitude: "+ userLat + "\n" +
                    "Longitude: " + userLon+ "\n" +
                    "Distance to Dest: " + distanceToDest + " meters", Toast.LENGTH_SHORT).show();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(userLocation);
            builder.include(userDestination);
            LatLngBounds bounds = builder.build();
            int padding = 40;
            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            MainActivity.mapFrag.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    map.moveCamera(cu);
                }
            });

            if (distanceToDest <= 0.1){
                updater.cancel();

                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                MP = MediaPlayer.create(mContext, alert);
                MP.start();
            }
        }

        @Override
        public void onFinish() {
            updater.start();
        }
    }

    public void setUpdater(){

        updater = new CounterClass(600000, 10000);

    }

    public Location setMyDestination(){

        myDestination = new Location("");
        myDestination.setLatitude(destinationLat);
        myDestination.setLongitude(destinationLon);

        return myDestination;
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
                String lon = location.getString("lng");

                destinationLat = Double.parseDouble(lat);
                destinationLon = Double.parseDouble(lon);
                userDestination = new LatLng(destinationLat, destinationLon);

                setMyDestination();

                MainActivity.mapFrag.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userDestination, 17));

                        // Markers identify locations on the map.
                        map.addMarker(new MarkerOptions()
                                .title("Your Destination")
                                .position(userDestination));

                        Toast.makeText(mContext, "Your Destination Location", Toast.LENGTH_LONG).show();
                        setUpdater();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class getStations extends AsyncTask<String, String, String> {

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
                JSONObject stations = json.getJSONObject("Stations");






            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
