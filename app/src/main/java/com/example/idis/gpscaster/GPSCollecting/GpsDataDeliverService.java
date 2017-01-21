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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.idis.gpscaster.Frag5_Setting.ListData;
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

public class GpsDataDeliverService extends Service implements Runnable,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "GpsDataDeliverService";
    private static final String TAG_DB = "PatternDB";
    //DB
    public static final String TABLE_GPSDATA = "GPSDATA";
    public static final String TABLE_PATTERN = "PATTERNDATA";
    private GPSDatabase mGPSDatabase;
    private PatternDatabase mPatternDatabase;


    //Immortal Service
    private static final int REBOOT_DELAY_TIMER = 10 * 1000;
    //Collecting Term
    private static final int BASIC_UPDATE_DEALY = 2 * 1000; // Bsic setting
    private static final int BASIC_PROCESSING_TIMER = 2 * 1000;

    private static int CURRENT_UPDATE_DELAY = BASIC_UPDATE_DEALY; // 5 * 60 * 1000
    private static int CURRENT_PROCESSING_TIMER = BASIC_PROCESSING_TIMER;//현재 placeId 체크 주기
    //PlaceID Processing for Place Recommendation
    private static int COLLECTING_COUNT = CURRENT_PROCESSING_TIMER / CURRENT_UPDATE_DELAY;
    private static int C1 = 0;

    PlaceRecommendationService placeRecommendationService;
    //Thread
    private Handler mHandler;
    private boolean mIsRunning;
    private int mStartId = 0;
    //location
    private LocationManager locManager; //
    private LocationListener locationListener;
    //Google Api
    private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    //Place Detail Key
    private String key = "AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";
    private String testHttp = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=52.20956489999999,21.0208235&radius=400&types=cafe&key=AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";
    //Place ID
    private String place_Id;
    private String place_type;
    /*
    Compare previous day, time, placeId to make pattern,
    if prv_day = cur_day and son on -> don't need to make new pattern else make new value
    */
    private int p_day = -1;
    private int p_time = -1;
    private String p_pid = null;
    private int p_min = -1;
    private int c_freq = 0;
    private static boolean initial_flag = false;
    //moving check
    private double pX=0, pY=0, cX=0, cY=0;
    boolean isMoving;
    PatternInfo p_PatternInfo = null; // past pattern info
    PatternInfo c_PatternInfo = null; // current pattern info
    ListData listData = null;

    public GpsDataDeliverService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        //unregisterRestartAlarm();
        super.onCreate();


        //DB Open
        mGPSDatabase = mGPSDatabase.getInsance(getApplicationContext());
        mPatternDatabase = mPatternDatabase.getInsance(getApplicationContext());
        mGPSDatabase.open();
        mPatternDatabase.open();
        listData = listData.getInstace(getApplicationContext());

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
        mHandler.postDelayed(this, CURRENT_UPDATE_DELAY);
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
            int battery = getBatteryPercentage(getApplicationContext());
            Log.d(TAG, "battery level:: " + battery);
            switch (battery) {
                case 1:
                    CURRENT_PROCESSING_TIMER = BASIC_PROCESSING_TIMER;
                    CURRENT_UPDATE_DELAY = BASIC_UPDATE_DEALY;
                    break;
                case 2:
                    CURRENT_PROCESSING_TIMER = 2 * BASIC_PROCESSING_TIMER;
                    CURRENT_UPDATE_DELAY = 2 * BASIC_UPDATE_DEALY;
                    break;
                case 3:
                    CURRENT_PROCESSING_TIMER = 4 * BASIC_PROCESSING_TIMER;
                    CURRENT_UPDATE_DELAY = 4 * BASIC_UPDATE_DEALY;
                    break;
            }
            getGPSData(battery);
            mHandler.postDelayed(this, CURRENT_UPDATE_DELAY);


            mIsRunning = true;
        }
    }

    public int getBatteryPercentage(Context context) {
        /*
        * level 1 > 50
        * level 2 > 25
        * level 3 >= 0
        * */
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float) scale;
        Log.d(TAG, "battery :: " + batteryPct);

        if (batteryPct > 50.0)
            return 1;
        else if (batteryPct > 25.0)
            return 2;
        else
            return 3;
    }

    public double getDistancefromGPS(double x1, double y1, double x2, double y2){
        double distance;

        Location locationA = new Location("point A");

        locationA.setLatitude(x1);
        locationA.setLongitude(y1);

        Location locationB = new Location("point B");

        locationB.setLatitude(x2);
        locationB.setLongitude(y2);

        distance = locationA.distanceTo(locationB);

        return distance;
    }

    public boolean checkMovement(double d){
         if( (d/CURRENT_UPDATE_DELAY) > 5/6) {
             isMoving = true;
             listData.setIsMoving(true);
             return true;
         }
        else{
             isMoving = false;
             listData.setIsMoving(false);
             return false;
         }

    }
    public void getGPSData(int battery) {
        Log.e(TAG, "getGPSData");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            double x = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude();
            double y = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude();
            insertGPSDb(x,y, battery);

            //check isMoving
            pX = cX; pY = cY;
            cX = x;  cY = y;
            if(pX*pY*cX*cY != 0 )
                isMoving = checkMovement(getDistancefromGPS(pX, pY, cX, cY));
            else
                isMoving = false;

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

    public void insertGPSDb(double x, double y, int battery) {
        ContentValues GPSValues = new ContentValues();
        int freq_addition = 0;

        //1. get current date, time, day
        //2. get place id, type from google api

        long now = System.currentTimeMillis();
        boolean cmp = false; //prv데이터와 현재 데이터를 비교
        Date date = new Date(now);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat CurTimeFormat = new SimpleDateFormat("HH:mm");
        String strCurDate = CurDateFormat.format(date); //Date
        String strCurTime = CurTimeFormat.format(date); //Time
        int day = calendar.get(calendar.DAY_OF_WEEK); //Day
        int c_min = (int) Integer.parseInt(strCurTime.substring(3, 5));
        String c_pid = getPlaceDetail();
        String c_ptype = getPlaceType(c_pid);
        int c_time = Integer.parseInt(strCurTime.substring(0, 2)) / 2;

        Log.d(TAG, "battery = " + battery);
        Log.d(TAG, "Place id = " + c_pid + ", Place Type =" + c_ptype);

        GPSInfo gpsInfo = new GPSInfo(x, y, strCurDate, strCurTime, day, c_pid, c_ptype);
        GPSValues = gpsInfo.getContentValue();
        c_PatternInfo = new PatternInfo(day, c_time, c_pid, c_ptype, 0);

        if (c_pid != null)
            mGPSDatabase.insertSQL(TABLE_GPSDATA, GPSValues);
        else
            Log.d(TAG_DB, "GPS DATA pid = null");

        Log.d(TAG_DB, "gpsvalue pattern :: d :" + day + ", t :" + strCurTime + ",place" + c_pid);

        //time, day, placeId 비교
        /* PatternDatabase */


        Log.d(TAG_DB, "cur_min = " + c_min);


        if (c_pid != null && p_pid != null) {
            //Log.d(TAG_DB, "(insertDB-IF) Place ID is not null");
            if (initial_flag == false) { //최초이면
                c_freq = getFreq(c_PatternInfo);
                initial_flag = true;
            }
            if (compareData(p_PatternInfo, c_PatternInfo, p_min, c_min) == false) {
                //날짜or시간or분orplaceid가 변했다면
                //저장
                //다음꺼 얻어온다
                saveP_PatternInfo(p_PatternInfo);
                c_freq = updatePattern(c_PatternInfo);
            }

            // 이때 freq를 battery level에 맞게 한다

            if (battery == 1)
                freq_addition = 1;
            else if (battery == 2)
                freq_addition = 2;
            else
                freq_addition = 4;

            c_freq += freq_addition; //어떤 경우에도 ++한다
            Log.d(TAG, "freq_addition = " + freq_addition + ", c_freq = " + c_freq);
            c_PatternInfo.freq = c_freq;
            p_min = c_min;// min은 안쓰는걸로 !
        }
        p_PatternInfo = c_PatternInfo;
        p_pid = c_pid;


    }

    boolean compareData(PatternInfo p, PatternInfo c, int p_min, int c_min) {
        if (p.time >= 0 && p.pid != null && c.time >= 0 && c.pid != null)
            if (p.time == c.time &&
                    p.pid.compareTo(c.pid) == 0 &&
                    p_min == c_min) {
                return true; // 변하지 않았음
            }
        return false; // 변했음
    }

    public int updatePattern(PatternInfo c1) {


        Cursor c = mPatternDatabase.rawQuery("SELECT * from " + TABLE_PATTERN + " where a_day = " +
                c1.day + " AND a_time = " + c1.time + " AND a_placeid = '" + c1.pid + "';");

        if (c.getCount() != 0) {  //찾았는데 없는거면 새로만들고 있는거면 불러오기
            c.moveToNext();
            Log.d(TAG_DB, "(updatePattern) 데이터변했는데 있는거 찾았고 반환한다 " + c.getInt(c.getColumnIndex("a_frequency")));
            return c.getInt(c.getColumnIndex("a_frequency"));

        } else { //데이터가 없다면  새로만들어주고 0 반환
            Log.d(TAG_DB, "(updatePattern) 데이터가변했는데 새로만들어주고 0 반환한다 ");

            c1.freq = 0;
            mPatternDatabase.insertSQL(TABLE_PATTERN, c1.getContentValue());
            Log.d(TAG_DB, "(updateFrequency) 정보가 바뀐경우 d:" + c1.day + ",t:" + c1.time + ",place" + c1.pid + ",placetype:" + c1.ptype);

            return 0;
        }

    }

    public int getFreq(PatternInfo p) {

        Cursor c = mPatternDatabase.rawQuery("SELECT * from " + TABLE_PATTERN + " where a_day = " +
                p.day + " AND a_time = " + p.time + " AND a_placeid = '" + p.pid + "';");
        if (c.getCount() != 0) {  //데이터찾았는데 만약 있다면
            Log.d(TAG_DB, "(getFreq) 데이터찾음");
            c.moveToNext();
            int freq = c.getInt(c.getColumnIndex("a_frequency"));
            return freq;
        } else { //데이터가 없다면, 만들어주고 freq=0, return
            Log.d(TAG_DB, "(getFreq) 데이터없어서만듦");
            Log.d(TAG_DB, "Cursor is null in insert new Pattern");
            Log.d(TAG_DB, "getFreq insertNew d:" + p.day + ",t:" + p.time + ",place" + p.pid + ",placetype:" + p.ptype);
            mPatternDatabase.insertSQL(TABLE_PATTERN, p.getContentValue());
            return 0;
        }
    }

    public void saveP_PatternInfo(PatternInfo p) {

        Cursor c = mPatternDatabase.rawQuery("SELECT * from " + TABLE_PATTERN + " where a_day = " +
                p.day + " AND a_time = " + p.time + " AND a_placeid = '" + p.pid + "';");

        if (c.getCount() > 0) {
            mPatternDatabase.execSQL("UPDATE " + TABLE_PATTERN + " SET a_frequency = "
                    + p.freq + " where a_time = " + p.time + " AND a_day = " + p.day + " AND a_placeid = '" + p.pid + "';");
            Log.d(TAG_DB, "(update Pattern) 저장함 pid =" + p.pid + ", freq = " + p.freq);

        } else
            Log.d(TAG_DB, "(saveP_PatternInfo) 이런일은 있을수가 없음 ");


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
                if (placeLikelihoods.getCount() != 0) {
                    Place mPlace = placeLikelihoods.get(0).getPlace();
                    Log.d("TAG", "name : " + mPlace.getName());
                    Log.d("TAG", "ID :" + mPlace.getId());
                    place_Id = mPlace.getId();
                } else {
                    place_Id = null;
                    Log.d(TAG, "PlaceLikelihood size is 0 !!");
                }

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

                    place_type = typeArray[0] + "|" + typeArray[1];
                    //type += typeArray[typeArray.length - 2];
                    //place_type = type;
                    Log.d(TAG, "type = " + place_type);
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
