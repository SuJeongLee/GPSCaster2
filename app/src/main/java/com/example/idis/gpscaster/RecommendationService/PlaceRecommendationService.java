package com.example.idis.gpscaster.RecommendationService;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;

import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;


// GMS
import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.GPSCollecting.GPSDatabase;
import com.example.idis.gpscaster.GPSCollecting.PlaceDetail;
import com.example.idis.gpscaster.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import static android.util.Log.*;


public class PlaceRecommendationService implements
        Serializable,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {


    public static final String TAG = "PlaceRecommendationService";
    public static final String TAG2 = "InfoConfirm";
    private Context context;
    private Resources resources;

    //Thread
    private Handler mHandler;
    private boolean mIsRunning;
    private int mStartId = 0;

    //Place_id List
    private ArrayList<String> PlaceId;
    private ArrayList<Integer> PlaceIdCount;

    //For Compute
    private int count_id = 0;
    private int max_countid = 10;
    private double primary_percent=0.6;

    //GoogleClientApi
    GoogleApiClient mGoogleApiClient=null;
    PlaceDetail placeDetail=null;
    private static final int GOOGLE_API_CLIENT_ID=0;

    //GooglePlaceDetail Web Key
    private String key = "AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";
    private String testHttp = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=52.20956489999999,21.0208235&radius=400&types=cafe&key=AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";

    //Its own
    private static PlaceRecommendationService placeRecommendationService=null;

    //For type searching
    GPSDatabase mGPSDatabase;
    private static final String TABLE_GPSDATA = "GPSDATA";
    String max_placeid="";

    //Variable in RUNNNNN Thread
    Double lat_; Double lng_;
    String page_; //Notification에 전달할 json

    //PlaceDetail ArrayList
    private static ArrayList<PlaceInfo> place_arr=null ;

    //View
    View rootView;

/*
GpsDataDeliver Serivce에서 PlaceId를 가져다 주면
ArrayList에 저장해두었다가 10번의 횟수가 채워지면
그때 쭉 정렬해서 점유율 계산 - 가장 큰 점유율을 가진 PlaceId가 0.8이 넘으면
그때는 장소추천서비스 실시(json parsing)
*/

    public PlaceRecommendationService(Context context, Resources resources) {
        this.context = context;
        this.resources = resources;

        PlaceId = new ArrayList<String>();
        PlaceIdCount = new ArrayList<Integer>();

        if( (mGoogleApiClient == null) || (mGoogleApiClient.isConnected()==false))
            setGoogleApiClient();

        mGPSDatabase = mGPSDatabase.getInsance(context);
        mGPSDatabase.open();

        place_arr = new ArrayList<PlaceInfo>();



    }


    public static PlaceRecommendationService getInstance(Context context, Resources resources){
        if(placeRecommendationService == null){
            placeRecommendationService = new PlaceRecommendationService(context,resources);
            Log.d(TAG2, "PlaceRecommendationService new ");
        }

        return placeRecommendationService;
    }

    public static PlaceRecommendationService returnObject(){
        return placeRecommendationService;
    }

    public void insertPlaceId(String placeid) {

        Log.d(TAG, "insertPlaceId : " + placeid);

        if(placeid == null)
            return;

        int index = PlaceId.indexOf(placeid);
        if (index > -1) {
            PlaceIdCount.set(index, PlaceIdCount.get(index) + 1); //해당 index에 있는 숫자를 가져와서 +1한것을 다시 넣음
        } else {
            PlaceId.add(placeid);// Id 추가
            PlaceIdCount.add(1);  // Count는 1로 초기화
        }
        count_id++; //10번까지 가능
        if (count_id == max_countid) // String배열이 꽉 찬 경우
        {
            count_id = 0;
            lookupPlaceId(); // 10번의 수집이 끝나는 경우

            PlaceId = new ArrayList<String>(); //새롭게 다시 시작
            PlaceIdCount = new ArrayList<Integer>(); //새롭게 다시 시작
        }

    }

    public void lookupPlaceId() {
        d(TAG, "lookupPlaceid : ");

        int max=-1;
        int max_index=-1;
        max_placeid="";


        for(int i=0; i<PlaceIdCount.size();i++) {
            if(max<PlaceIdCount.get(i)){
                max_index = i;
                max = PlaceIdCount.get(i);
            }
        }

        for(int i=0; i<PlaceIdCount.size(); i++)
        {
            d(TAG,"ID : "+PlaceId.get(i)+", N : "+PlaceIdCount.get(i));
        }

        max_placeid = PlaceId.get(max_index);
        if(max/max_countid >= primary_percent){
            Thread1 t1 = new Thread1();
            t1.start();
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //이제 장소를 추천해줄 타이밍 placdid 넘겨서 주변장소 받아야함
            Thread2 t2 = new Thread2();
            t2.start();
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /*ArrayList size check */
            Log.d(TAG2, "FROM NOW NOTIFICATION !! ");
            Log.d(TAG2, "place arr size = "+place_arr.size());
            Log.d(TAG2, "place arr size2 = "+getPlace_arr().size());
            notificationService();
        }
    }

    public void setGoogleApiClient(){
                /*☆★☆★☆★☆★Able to fail☆★☆★☆★☆★☆★*/
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    public void notificationService(){

        NotificationManager manager= (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.profile)  //상태표시줄에 보이는 아이콘 모양
                .setTicker("ALPHAGO")                                     //알림이 발생될 때 잠시 보이는 글씨
                .setContentText("ALPHOGO")                                //알림창에서의 제목
                .setContentText("Do you want me to suggest the places nearby?");

        Intent intent = new Intent(context, Notifi.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("intent1", "aaa");
        savearr();
        //클릭할 때 까지 액티비티 실행을 보류하고 있는 PendingIntent 객체 생성
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pending);
        builder.setAutoCancel(true); // 클릭시 자동으로 알림 삭제

        Notification notification = builder.build();
        manager.notify(0, notification);
        //NotificationManager가 알림(notification)표시, id는 알림 구분용
    }

    @Override
    public void onConnected(Bundle bundle) { Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        d(TAG, "onConnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        d(TAG, "onConnected");
    }

    public ArrayList<PlaceInfo> getPlace_arr(){
        Log.d(TAG2, "getPlace_arr return ");
        Log.d(TAG2, "place arr  in GetPlaceArr  = "+place_arr.size());
        return place_arr;
    }

    public void savearr(){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        File data = context.getDir(context.getPackageName(), Context.MODE_PRIVATE);
        try{
            fos = new FileOutputStream(new File(data+"/"+"arr.txt"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(place_arr);

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(oos != null)
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }


    class Thread1 extends Thread{
        @Override
                 public void run() {
                    String placeType=null;
                    URL url = null;
                    HttpURLConnection urlConnection = null;
                    BufferedInputStream buf = null;
                    String line = "";
                    String page = "";

                    Log.d(TAG, "Going to search place detail!! "+max_placeid);
                    Cursor c = mGPSDatabase.rawQuery("SELECT * FROM "+TABLE_GPSDATA+
                            " where a_placeid='"+max_placeid+"';");
                    if(c==null || c.moveToFirst() == false)
                        Log.e(TAG, "Cursor Error!");
                    else{
                        double lat = c.getDouble(c.getColumnIndex("a_lat"));
                        double lng = c.getDouble(c.getColumnIndex("a_lng"));

                        lat_ = lat;
                        lng_ = lng;

                        String type = c.getString(c.getColumnIndex("a_placetype"));
                        Log.d(TAG, "lat = "+lat+", lng="+lng+", type = "+type);

                        try {
                            String a = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                                    "location="+lat+","+lng+"&radius=100&types="+type+
                                    "&key="+key;
                            Log.d(TAG2, a);
                            url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                                    "location="+lat+","+lng+"&radius=100&types="+type+
                                    "&key="+key);

                            //url = new URL(testHttp);
                            urlConnection = (HttpURLConnection) url.openConnection();
                            buf = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(buf, "UTF-8"));
                            while ((line = bufferedReader.readLine()) != null) {
                                Log.d(TAG, line);
                                page += line;
                            }
                            page_ = page;// Notification에 전달할 거 옮겨담기

                        }catch(MalformedURLException me){
                            me.printStackTrace();
                        }catch(IOException e) {
                            e.printStackTrace();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
    }
    class Thread2 extends Thread{
        @Override
        public void run(){
            try {
                JSONObject json = new JSONObject(page_);
                JSONArray jsonArray = json.getJSONArray("results");
                Log.d(TAG, "len = "+jsonArray.length());
                int k=0;

                for(k=0; k<jsonArray.length(); k++)
                {
                    PlaceInfo p1 = new PlaceInfo();
                    JSONObject temp = jsonArray.getJSONObject(k);

                    if(temp == null)
                        Log.d(TAG, "json temp is null");
                    Log.d(TAG2, "k---------------"+k);
                    Log.d(TAG, "lat = "+temp.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                    p1.setLat(temp.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                    p1.setLng(temp.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                    p1.setName(temp.getString("name"));
                    //No value= -1
                    if(!temp.isNull("opening_hours")){
                        p1.setOpen_now(temp.getJSONObject("opening_hours").getString("open_now"));
                        Log.d(TAG, "opening_hours : "+temp.getString("opening_hours"));
                    }else
                    {
                        Log.d(TAG, "opening_now is null in "+p1.getName());
                        p1.setOpen_now("-1");
                    }
                    if(temp.getString("vicinity")!= null)
                        p1.setVicinity(temp.getString("vicinity"));
                    else
                        p1.setVicinity("-1");
                    if(!temp.isNull("rating"))
                        p1.setRating(temp.getDouble("rating"));
                    else
                        p1.setRating(-1.0);
                    String types = temp.getString("types").replaceAll("\"", "");
                   // String types = temp.getString("types").replaceAll("\"", "");
                    types = types.substring(1, types.length() - 2);
                    String[] typeArray = types.split(",");
                    p1.setTypes(typeArray);

                    //Log.d(TAG, "p1="+p1.getName()+p1.getLat()+p1.getLng()+p1.getVicinity()+p1.getRating()+p1.getTypes()[0]);
                    //Log.d(TAG, "p1 opening = "+p1.getOpen_now());
                    Log.d(TAG2,"p"+k+p1.getName());
                    Log.d(TAG2,"p"+k+p1.getVicinity());
                    Log.d(TAG2,"p"+k+p1.getRating());
                    Log.d(TAG2,"p"+k+p1.getTypes());
                    Log.d(TAG2,"p"+k+p1.getOpen_now());

                    place_arr.add(p1);
                }
/*
                JSONArray typeObj = json1.getJSONArray("types");
                String types = typeObj.toString().replaceAll("\"", "");
                types = types.substring(1, types.length() - 2);
                String[] typeArray = types.split(",");
                JSONObject location = json1.getJSONObject("geometry").getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                Log.d(TAG, "location lat" + lat + ",lng=" + lng);
                Log.d(TAG, "types=" + types);

                 2; i++)
                    type += typeArray[i] + "|";for (int i = 0; i < typeArray.length -
                type += typeArray[typeArray.length - 2];
                place_type = type;
*/

                //requestNearByPlace(lat, lng, typeArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
