package com.launchathon.deanc.gps_alarm;

import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dcsir on 8/13/2016.
 */
public class DataHandler {

    protected static final String HTTP = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    protected static final String API_KEY = "&key=AIzaSyDvSzZs2vIJzot6RrRfPwlBWStLLTrkijY";
    protected static final String LIRR_API_SEARCH = "https://traintime.lirr.org/api/StationsAll?api_key=f63af35c0bc02d01bf133806cb469aad";

    private List<TrainStation> LIRR_Station_List;
    private List<TrainStation> Amtrak_Station_List;

    public Double userLat, userLon, destinationLat, destinationLon;
    double distanceToDest;
    public int alarmDistance;
    public String user_address;
    LatLng userLocation, userDestination;
    String URL;
    public Ringtone r;
    public Vibrator v;
    public AudioManager am;
    public int originalVolume, DnD;

    public CounterClass updater;
    public Context mContext;

    Tracker gpsTracker;

    public Location myLocation, myDestination;

    private static DataHandler instance = new DataHandler();

    private DataHandler() {
        LIRR_Station_List = new ArrayList<>();
        Amtrak_Station_List = new ArrayList<>();
    }

    public static DataHandler getInstance() {
        return instance;
    }

    public List<TrainStation> getAllStations_LIRR() {
        return LIRR_Station_List;
    }

    public void addStation_LIRR(String name, Double lat, Double lon) {
        TrainStation station = new TrainStation(name, lat, lon);
        LIRR_Station_List.add(station);
    }

    public List<TrainStation> getAllStations_Amtrak() {
        return Amtrak_Station_List;
    }

    public void addAmtrakStation(String name, String code, Double lat, Double lon) {
        TrainStation station = new TrainStation(name, code, lat, lon);
        Amtrak_Station_List.add(station);
    }

    public void getLocation() {

        gpsTracker = new Tracker(mContext);

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

    public void startAsyncTask() {
        URL = HTTP + user_address + API_KEY;
        new getAddressCoordinates().execute(URL);
    }

    public void startLIRR_AsyncTask() {
        if (LIRR_Station_List.size() == 0) {
            new getStations().execute(LIRR_API_SEARCH);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public double metersToMiles (double value){
        double km = value / 1000;
        double mile = km * 0.621371;

        return round(mile, 2);
    }

    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            getLocation();

            distanceToDest = myLocation.distanceTo(myDestination);

            Toast.makeText(mContext,
                    "Distance to Dest: " + metersToMiles(distanceToDest) + " miles", Toast.LENGTH_SHORT).show();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(userLocation);
            builder.include(userDestination);
            LatLngBounds bounds = builder.build();
            int padding = 40;
            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            MainActivity.mapFrag.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    map.clear();
                    map.moveCamera(cu);

                    map.addMarker(new MarkerOptions()
                            .title("You are Here")
                            .position(userLocation));

                    map.addMarker(new MarkerOptions()
                            .title("Your Destination")
                            .position(userDestination));
                }
            });

            if (distanceToDest <= alarmDistance) {
                updater.cancel();

                am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

                try {
                    DnD = Settings.Global.getInt(mContext.getContentResolver(), "zen_mode");
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }

                if (android.os.Build.VERSION.SDK_INT >= 23){
                    if (DnD == 0){
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        originalVolume = am.getStreamVolume(AudioManager.STREAM_RING);
                        am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                    }
                }else {
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    originalVolume = am.getStreamVolume(AudioManager.STREAM_RING);
                    am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                }

                v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 10000, 1000};

                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                r = RingtoneManager.getRingtone(mContext, alert);
                r.play();
                v.vibrate(pattern, 0);
            }
        }

        @Override
        public void onFinish() {
            updater.start();
        }
    }

    public void setUpdater() {

        updater = new CounterClass(600000, 10000);

    }

    public Location setMyDestination() {

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

                Iterator<String> keys = stations.keys();

                while(keys.hasNext()){
                    JSONObject station = stations.getJSONObject(keys.next());
                    //Log.d("STATION", station.getString("NAME"));

                    String s = station.getString("LATITUDE");

                    if (!s.equals("null")) {

                        String name = station.getString("NAME");
                        String lat = station.getString("LATITUDE");
                        String lon = station.getString("LONGITUDE");
                        Log.d("STATIONS", LIRR_Station_List.size() + name);

                        Double LAT = Double.parseDouble(lat);
                        Double LON = Double.parseDouble(lon);

                        addStation_LIRR(name, LAT, LON);
                    }

                }
                Log.d("STATIONS", LIRR_Station_List.size() + "" );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void populateAmtrakList(){
        // No API currently exists which contains all of the Amtrak Station data - 8/23/2016
        // Following website used for Station Info.
        // http://www.ensingers.com/Bill222E/gpsamtrak.html
        // as well as https://www.amtrak.com/html/stations_A.html
        // for verification.

    }
}
