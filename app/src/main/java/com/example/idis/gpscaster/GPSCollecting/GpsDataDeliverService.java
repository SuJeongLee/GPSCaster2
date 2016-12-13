package com.example.idis.gpscaster.GPSCollecting;


/*
* Last Revision :: 2016/11/30
* Issue
* 1) When GPS is off, stop to insert data in Database
* 2) How to get Place Detail in Service - Is it necessary to create new Class for this function?
* 3) Place GPS orrrrrrrrrrrrr Current GPS ?????  I need to decide
* */

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.idis.gpscaster.MainActivity;
import com.example.idis.gpscaster.RecommendationService.PlaceRecommendationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GpsDataDeliverService extends Service implements Runnable, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "GpsDataDeliverService";
    //DB
    public static final String TABLE_GPSDATA = "GPSDATA";
    private GPSDatabase mGPSDatabase;

    //Immortal Service
    private static final int REBOOT_DELAY_TIMER = 10 * 1000;
    //Collecting Term
    private static final int LOCATION_UPDATE_DELAY = 3 * 1000; // 5 * 60 * 1000
    private static final int LOCATION_PROCESSING_TIMER = 3 * 1000; //현재 placeId 체크 주기
    //PlaceID Processing for Place Recommendation
    private static int COLLECTING_COUNT = LOCATION_PROCESSING_TIMER / LOCATION_UPDATE_DELAY;
    private static int C1 = 0;
    private static PlaceRecommendationService placeRecommendationService;
    //Thread
    private Handler mHandler;
    private boolean mIsRunning;
    private int mStartId = 0;
    //location
    private LocationManager locManager; //
    private LocationListener locationListener;
    //Google Api
    private GoogleApiClient mGoogleApiClient;
    //private PlaceDetail placeDetail;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    //Place Detail Key
    private String key = "AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";
    private String testHttp = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=52.20956489999999,21.0208235&radius=400&types=cafe&key=AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";
    //Place ID
    private String place_Id;
    private String place_type;

    public GpsDataDeliverService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        //unregisterRestartAlarm();
        super.onCreate();


        //DB Open
        mGPSDatabase = mGPSDatabase.getInsance(getApplicationContext());
        mGPSDatabase.open();
        Log.d(TAG, "GPS Db Open");
        mIsRunning = false;
    }

    public void setGoogleApiClient() {
        /*☆★☆★☆★☆★Able to fail☆★☆★☆★☆★☆★*/
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public void onDestroy() {

        //locManager.removeUpdates(locationListener);
        registerRestartAlarm();
        super.onDestroy();
        mIsRunning = false;
        //GoogleApiClient
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mStartId = startId;

        mHandler = new Handler();
        mHandler.postDelayed(this, LOCATION_UPDATE_DELAY);
        mIsRunning = true;

        setGoogleApiClient();
        //GoogleClientApi
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        //placeRecommendationService Start
        placeRecommendationService =
                placeRecommendationService.getInstance(getApplicationContext(), getResources());


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }


    @Override
    public void run() {
        if (!mIsRunning)
            return;
        else {
            Log.d(TAG, "run-getGPSData");
            getGPSData();
            mHandler.postDelayed(this, LOCATION_UPDATE_DELAY);
            mIsRunning = true;
        }
    }

    public void getGPSData() {
        Log.e(TAG, "getGPSData");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                   return;
            }
            Log.d(TAG, "Current GPS :: Lat-" + LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude()
                    + ", Lng-" + LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude());
            insertGPSDb(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude(),
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude());

            C1++;
            if (C1 == COLLECTING_COUNT) {
                placeRecommendationService.insertPlaceId(place_Id);
                C1 = 0;//C1 초기화
            }

        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "insert DB");
                insertGPSDb(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    public void insertGPSDb(double x, double y) {
        ContentValues GPSValues = new ContentValues();
        //1. get current date, time, day
        //2. get place id, type from google api

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat CurTimeFormat = new SimpleDateFormat("HH:mm");
        String strCurDate = CurDateFormat.format(date); //Date
        String strCurTime = CurTimeFormat.format(date); //Time
        int day = calendar.get(calendar.DAY_OF_WEEK); //Day

        String p_Id = getPlaceDetail();
        String p_type = getPlaceType(p_Id);
        Log.d(TAG, "Place id = " + p_Id + ", Place Type =" + p_type);

        GPSValues.put("a_lat", x);
        GPSValues.put("a_lng", y);

        GPSValues.put("a_date", strCurDate);
        GPSValues.put("a_time", strCurTime);
        GPSValues.put("a_day", day);
        GPSValues.put("a_placeid", p_Id);
        GPSValues.put("a_placetype", p_type);

        mGPSDatabase.insertSQL(TABLE_GPSDATA, GPSValues);

    }

    String getPlaceDetail() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
               return "";
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                    URL url = null;
                    HttpURLConnection urlConnection = null;
                    BufferedInputStream buf = null;
                    String line = null;
                    String page = "";
                    //가장유력한 Place의 Id를 얻어온다
                    Place mPlace = placeLikelihoods.get(0).getPlace();
                    Log.d("TAG", "name : " + mPlace.getName());
                    Log.d("TAG", "ID :" + mPlace.getId());
                    place_Id = mPlace.getId();
                }
            });
        return place_Id;
    }

    String getPlaceType(String placeId) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                String placeType = null;
                URL url = null;
                HttpURLConnection urlConnection = null;
                BufferedInputStream buf = null;
                String line = null;
                String page = "";
                String type = "";

                //1)Get Place Detail
                try {
            /* TEST-------------- */
                    url = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place_Id + "&key=" + key);
                    //url = new URL(testHttp);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    buf = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(buf, "UTF-8"));

                    while ((line = bufferedReader.readLine()) != null) {
                        Log.d(TAG, "http:" + line);
                        page += line;
                    }
                } catch (MalformedURLException me) {
                    me.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "jsonparsing");
                Log.d(TAG, "page :::::: " + page);
                //2. Place Type
                try {
                    JSONObject json = new JSONObject(page);
                    JSONObject json1 = json.getJSONObject("result");
                    JSONArray typeObj = json1.getJSONArray("types");
                    String types = typeObj.toString().replaceAll("\"", "");
                    types = types.substring(1, types.length() - 2);
                    String[] typeArray = types.split(",");
                    JSONObject location = json1.getJSONObject("geometry").getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    Log.d(TAG, "location lat" + lat + ",lng=" + lng);
                    Log.d(TAG, "types=" + types);

                    place_type = typeArray[0]+"|"+typeArray[1];
                    //type += typeArray[typeArray.length - 2];
                    //place_type = type;
                    Log.d(TAG, "type = "+place_type);
                    //requestNearByPlace(lat, lng, typeArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return place_type;
    }

    private void registerRestartAlarm() {
        Intent intent = new Intent(GpsDataDeliverService.this, RestartServiceForGpsData.class);
        intent.setAction(RestartServiceForGpsData.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(GpsDataDeliverService.this, 0, intent, 0);

        long startingTime = SystemClock.elapsedRealtime();
        startingTime += REBOOT_DELAY_TIMER;

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startingTime,
                REBOOT_DELAY_TIMER, sender);

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();


    }

    private void unregisterRestartAlarm() {
        Intent intent = new Intent(GpsDataDeliverService.this, RestartServiceForGpsData.class);
        intent.setAction(RestartServiceForGpsData.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(GpsDataDeliverService.this, 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);

        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed !! ");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                   return;
        }
        LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);  // LocationListener
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
