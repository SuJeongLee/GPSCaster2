package com.example.idis.gpscaster.Frag4_CreatePattern;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.Frag5_Setting.ListData;
import com.example.idis.gpscaster.GPSCollecting.GPSDatabase;
import com.example.idis.gpscaster.GPSCollecting.PatternDatabase;
import com.example.idis.gpscaster.R;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IDIS on 2016-12-16.
 */

public class ExtractPattern {


    private static ListData listData;
    PatternDatabase mDatabase = null;
    GPSDatabase gpsDatabase = null;
    String TABLE_PATTERN = "PATTERNDATA";
    String TABLE_GPSDATA = "GPSDATA";
    String TAG = "ExtractPattern";
    static String TAGs = "ExtractPatternstatic";
    ArrayList<String> arrPlaceid;
    ArrayList<String> arrPlacetype;

    ArrayList<PlaceInfo> home = new ArrayList<PlaceInfo>();
    ArrayList<PlaceInfo> company = new ArrayList<PlaceInfo>();

    /*HTTP CONNECTION*/
    private String key = "AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";

    //sigleton
    private static ExtractPattern extractPattern;

    //db를 건드려서
    //그시간대에 가장 큰 아이를 뽑아내고
    //가장 큰 아이의 정보를 Notification에 요청해서
    //Notification을 주던지
    //아니면 여기서 쓰레드를 돌려서
    //노티피케이션을 새로 띄우던지
    private Activity activity;
    private Context context;
    private Resources resources;
    private View view;
    private TextView tv;

    public static ArrayList<PlaceInfo> placeInfos2;
    private String http1;


    public static ExtractPattern getInstance(Activity a, Context c, Resources r, View v) {
        if (extractPattern == null) {
            extractPattern = new ExtractPattern(a, c, r, v);
            Log.d(TAGs, "get Instance1 ");
            listData = listData.getInstace(c);
        }

        Log.d(TAGs, "get Instance2 ");
        Log.d(TAGs, "ARRAY SIZE =" + placeInfos2.size());
        return extractPattern;
    }

    private ExtractPattern(Activity activity, Context context, Resources resources, View rootView) {
        Log.d(TAG, "EXTRACT pattern Constructure!!! it must be one  ");
        this.activity = activity;
        this.context = context;
        this.resources = resources;
        this.view = rootView;

        mDatabase = mDatabase.getInsance(context);
        mDatabase.open();
        gpsDatabase = gpsDatabase.getInsance(context);
        gpsDatabase.open();

        placeInfos2 = new ArrayList<PlaceInfo>();
        arrPlaceid = new ArrayList<String>();
        arrPlacetype = new ArrayList<String>();
    }

    public void extractHome() {
        Cursor c = mDatabase.rawQuery("SELECT DISTINCT a_frequency, a_placeid from " + TABLE_PATTERN +
                " where a_time between 0 and 2 group by a_placeid order by a_frequency desc;");  // freq 오름차순 select

        String result = "";
        double lat=0, lng=0;
        if (c != null) {
            if (c.getCount() > 0) {
                Log.d(TAG, "(getPattern) # of cursor = " + c.getCount());
                while (c.moveToNext()) {

                    result = "placeid : " + c.getString(c.getColumnIndex("a_placeid")) + "  freq : " + c.getInt(c.getColumnIndex("a_frequency"));
                    Cursor c1 = gpsDatabase.rawQuery("SELECT DISTINCT a_lat, a_lng from " + TABLE_GPSDATA +
                            " where a_placeid = '" + c.getString(c.getColumnIndex("a_placeid")) + "';");
                    if (c1 != null) {
                        if (c1.getCount() > 0){
                            c1.moveToNext();
                            lat = c1.getDouble(c1.getColumnIndex("a_lat"));
                            lng = c1.getDouble(c1.getColumnIndex("a_lng"));
                            Log.d(TAG, "lat = " + lat + ", lng= " + lng);
                        }
                    } else
                        Log.d(TAG, "c1 is null");
                    //home -> vicinity, lat, lng, paceid
                    Log.d(TAG, result);
                    PlaceInfo p = new PlaceInfo();
                    p.setVicinity(latlngToaddress(lat, lng));
                    p.setLat(lat); p.setLng(lng);
                    p.setPlace_id(c.getString(c.getColumnIndex("a_placeid")));
                    home.add(p);
                }
            }
        }
        saveUserPlace(home, "home");
        Log.d(TAG, "home size is +"+home.size());
    }
    public void extractCompany() {
        Cursor c = mDatabase.rawQuery("SELECT DISTINCT a_frequency, a_placeid from " + TABLE_PATTERN +
                " where a_time between 5 and 7 group by a_placeid order by a_frequency desc;");  // freq 오름차순 select

        String result = "";
        double lat=0, lng=0;
        if (c != null) {
            if (c.getCount() > 0) {
                Log.d(TAG, "(getPattern) # of cursor = " + c.getCount());
                while (c.moveToNext()) {

                    result = "placeid : " + c.getString(c.getColumnIndex("a_placeid")) + "  freq : " + c.getInt(c.getColumnIndex("a_frequency"));
                    Cursor c1 = gpsDatabase.rawQuery("SELECT DISTINCT a_lat, a_lng from " + TABLE_GPSDATA +
                            " where a_placeid = '" + c.getString(c.getColumnIndex("a_placeid")) + "';");
                    if (c1 != null) {
                        if (c1.getCount() > 0){
                            c1.moveToNext();
                            lat = c1.getDouble(c1.getColumnIndex("a_lat"));
                            lng = c1.getDouble(c1.getColumnIndex("a_lng"));
                            Log.d(TAG, "lat = " + lat + ", lng= " + lng);
                        }
                    } else
                        Log.d(TAG, "c1 is null");

                    Log.d(TAG, result);
                    PlaceInfo p = new PlaceInfo();
                    p.setVicinity(latlngToaddress(lat, lng));
                    p.setLat(lat); p.setLng(lng);
                    p.setPlace_id(c.getString(c.getColumnIndex("a_placeid")));
                    company.add(p);
                }
            }
        }
        saveUserPlace(company,"company");
        Log.d(TAG, "company size is +"+company.size());
    }
    public String latlngToaddress(double lat, double lng){
        String add;
        List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(context);
            try {
                addressList = geocoder.getFromLocation(lat, lng, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        return addressList.get(0).getAddressLine(0)+","+addressList.get(0).getAddressLine(1);
    }
    public void getPattern(int day, int time) {
        /*
        * 1. day, time으로 현재 시간 알아오기
        * 2. db에서 select해서 해당 시간대 뽑아오기
        * 3. 1-3위 일단 뽑아내기 -> text로 뽑아내기
        * 4. view에다가 표시하자
        * 5. 그리고 notification 연결
        * */

        Cursor c = mDatabase.rawQuery("SELECT * from " + TABLE_PATTERN + " where a_day = " +
                day + " AND a_time = " + time + " order by a_frequency desc;");  // freq 오름차순 select
        tv = (TextView) view.findViewById(R.id.tv_lookupDb);
        String result = "";

        if (c != null) {
            if (c.getCount() > 0) {
                Log.d(TAG, "(getPattern) # of cursor = " + c.getCount());
                while (c.moveToNext()) {
                    result += "day : " + c.getInt(c.getColumnIndex("a_day")) + ", time = " + c.getInt(c.getColumnIndex("a_time")) + ", " +
                            "placeid : " + c.getString(c.getColumnIndex("a_placeid")) + ", placetype : " + c.getString(c.getColumnIndex("a_placetype")) +
                            "freq : " + c.getInt(c.getColumnIndex("a_frequency")) + "\n";
                    //nearby를 찾기위한 저장
                    arrPlaceid.add(c.getString(c.getColumnIndex("a_placeid")));
                    arrPlacetype.add(c.getString(c.getColumnIndex("a_placetype")));
                }
                tv.setText(result);
            } else {
                result = "There is no pattern";
            }
            tv.setText(result);
        } else
            tv.setText("c is null");

        //일단우선순위가장높은걸로 search, 추후에는 Dialog를통해 사용자에게 물어볼것
        findNearbyPlaces(arrPlaceid.get(0));

    }

    //이걸 Thread로 해야할듯 찾는거 자체는!
    public void findNearbyPlaces(String placeid) {

        //움직이는 상태이면 dailyrecommendation을 실시하지 않는다
        if(listData.getIsMoving() == true)
        {
            Log.d(TAG, "device is moving now :: can not run daily recommendation service");
            Toast.makeText(context, "device is moving now", Toast.LENGTH_LONG).show();
            return ;
        }

        Log.d(TAG, "listData.getMoving is "+listData.getIsMoving());

        double lat = 0, lng = 0;
        String type = "";
        Cursor c = gpsDatabase.rawQuery("SELECT * from " + TABLE_GPSDATA + " where a_placeid = '" +
                placeid + "';");  // freq 오름차순 select
        if (c != null) {
            if (c.getCount() > 0) { //딱 하나만 필요함
                c.moveToNext();
                lat = c.getDouble(c.getColumnIndex("a_lat"));
                lng = c.getDouble(c.getColumnIndex("a_lng"));
                type = c.getString(c.getColumnIndex("a_placetype"));

                Log.d(TAG, "(findNearbyPlaces) cursor result = " + lat + "," + lng + "," + type);
            } else
                tv.setText("findNearbyPlace c.getCount = 0");
        } else
            tv.setText("findNearbyPlace c.getCount = 0");

        /*Thread 구동중*/
        NearbyThread thread = new NearbyThread(lat, lng, type);  // nearby place들을 arrayList에 담자!
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ParsingThread thread1 = new ParsingThread(http1);
        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "ARRAY size = " + placeInfos2.size());

        //여기서 check 해서 아니면 빠꾸
        dailynotificationService();


    }

    public void dailynotificationService() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.profile2)  //상태표시줄에 보이는 아이콘 모양
                .setTicker("ALPHAGO")                                     //알림이 발생될 때 잠시 보이는 글씨
                .setContentTitle("DAILY REPORT")                                //알림창에서의 제목
                .setContentText("Do you want me to recommend nearby places?");

        Intent intent = new Intent(context, DailyNotifi.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent test
        intent.putExtra("intent1", "aaa");
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pending);
        builder.setAutoCancel(true); // 클릭시 자동으로 알림 삭제

        Notification notification = builder.build();
        manager.notify(0, notification);
    }

    public ArrayList<PlaceInfo> getPlaceInfos() {
        return placeInfos2;
    }

    public void saveUserPlace(ArrayList<PlaceInfo> arrayList, String filename) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File data = context.getDir(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            fos = new FileOutputStream(new File(data+File.separator+filename+".txt"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(arrayList);
            Log.d("FILE", "output arr size = "+arrayList.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null)
                try {
                    fos.close();
                    Log.d("FILE","File close ! fos is closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (oos != null)
                try {
                    oos.close();
                    Log.d("FILE","OutputStream close ! oos is closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    class NearbyThread extends Thread {

        double lat;
        double lng;
        String type;

        public NearbyThread(double a_lat, double a_lng, String a_type) {
            lat = a_lat;
            lng = a_lng;
            type = a_type;
        }

        @Override
        public void run() {

            URL url = null;
            HttpURLConnection urlConnection = null;
            BufferedInputStream buf = null;
            String line = "";
            String page = "";

            Log.d(TAG, "(NearbyThread-run)");

            try {
                // radius = 100 고정 -> 추후에 바꾸기 !
                url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=" + lat + "," + lng + "&radius=100&types=" + type +
                        "&key=" + key);
                urlConnection = (HttpURLConnection) url.openConnection();
                buf = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(buf, "UTF-8"));
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d(TAG, line);
                    page += line;
                }
                http1 = page;// Notification에 전달할 거 옮겨담기
            } catch (MalformedURLException me) {
                me.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ParsingThread extends Thread {

        String http;

        public ParsingThread(String http1) {
            http = http1; //결과전달, 이제 파싱하면됨 !
        }

        @Override
        public void run() {
            try {
                JSONObject json = new JSONObject(http);
                JSONArray jsonArray = json.getJSONArray("results");
                Log.d(TAG, "len = " + jsonArray.length());
                placeInfos2 = new ArrayList<PlaceInfo>();
                for (int k = 0; k < jsonArray.length(); k++) {
                    PlaceInfo p1 = new PlaceInfo();
                    JSONObject temp = jsonArray.getJSONObject(k);

                    if (temp == null)
                        Log.d(TAG, "json temp is null");
                    Log.d(TAG, "(ParsingThread) length = " + k);

                    p1.setLat(temp.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                    p1.setLng(temp.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                    p1.setName(temp.getString("name"));

                    //No value= -1
                    if (!temp.isNull("opening_hours")) {
                        p1.setOpen_now(temp.getJSONObject("opening_hours").getString("open_now"));
                        Log.d(TAG, "opening_hours : " + temp.getString("opening_hours"));
                    } else {
                        Log.d(TAG, "opening_now is null in " + p1.getName());
                        p1.setOpen_now("-1");
                    }

                    if (temp.getString("vicinity") != null)
                        p1.setVicinity(temp.getString("vicinity"));
                    else
                        p1.setVicinity("-1");

                    if (!temp.isNull("rating"))
                        p1.setRating(temp.getDouble("rating"));
                    else
                        p1.setRating(-1.0);
                    String types = temp.getString("types").replaceAll("\"", "");

                    types = types.substring(1, types.length() - 2);
                    String[] typeArray = types.split(",");
                    p1.setTypes(typeArray);

                    Log.d(TAG, "(ParsingThread) parsing result :: name=" + k + p1.getName() + ", vicinity=" + p1.getVicinity()
                            + ", rating=" + p1.getRating() + ", type=" + p1.getTypes() + ", open_now=" + p1.getOpen_now());

                    placeInfos2.add(p1);
                }
                //requestNearByPlace(lat, lng, typeArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
