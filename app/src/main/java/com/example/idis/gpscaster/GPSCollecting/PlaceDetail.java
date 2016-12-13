package com.example.idis.gpscaster.GPSCollecting;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IDIS on 2016-11-26.
 */

public class PlaceDetail {

    Context mContext;
    StringBuilder mResponseBuilder = new StringBuilder();
    URL url = null;
    HttpURLConnection urlConnection = null;
    BufferedInputStream buf = null;
    String line = null;
    String page = "";
    String page2="";
    private String key = "AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";
    //Place nearby and same type
    private String testHttp = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=52.20956489999999,21.0208235&radius=400&types=cafe&key=AIzaSyAkEp3BvsggrTFL6u2cQeLDZOmwSjyrk68";

    PlaceDetail(Context context, String placeID) {
        mContext = context;

        try {
            url = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key="+key);
            urlConnection = (HttpURLConnection) url.openConnection();
            buf = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(buf, "UTF-8"));

            while ((line = bufferedReader.readLine()) != null) {
                Log.d("BUFF", line);
                page += line;
            }

        }catch(MalformedURLException me){
        me.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
}

    public void jsonparsing() {

        Log.d("BUFF","jsonparsing function");
        try {
            JSONObject json = new JSONObject(page);
            JSONObject json1 = json.getJSONObject("result");
            JSONArray typeObj = json1.getJSONArray("types");
            String types = typeObj.toString().replaceAll("\"","");
            types = types.substring(1,types.length()-2);
            String[] typeArray = types.split(",");
            JSONObject location = json1.getJSONObject("geometry").getJSONObject("location");
            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");
            Log.d("BUFF","location lat"+lat+",lng="+lng);
            Log.d("BUFF","types="+types);
            for(int i=0; i<typeArray.length; i++)
            {
                Log.d("BUFF","type"+(i+1)+typeArray[i]);
            }

            requestNearByPlace(lat, lng, typeArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void requestNearByPlace(Double lat, Double lng, String[] typeArray)
    {
        String type="";
        page = "";
        for(int i=0 ; i<typeArray.length-2; i++)
            type+=typeArray[i]+"|";
        type+=typeArray[typeArray.length-2];
        Log.d("BUFF2","Buff2 type="+type);
        try {
            /*
            url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location="+lat+","+lng+"&radius=100&types="+type+
                    "&key="+key);
            */
            url = new URL(testHttp);
            urlConnection = (HttpURLConnection) url.openConnection();
            buf = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(buf, "UTF-8"));
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("BUFF2", line);
                page += line;
            }
        }catch(MalformedURLException me){
            me.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        makeNearbyList(page);
    }

    void makeNearbyList(String page)
    {
        Log.d("BUFF3","makeNearbyList");
        try {
            JSONObject json = new JSONObject(page);
            Log.d("BUFF3",page);

            Pattern p = Pattern.compile("geometry");
            Matcher m = p.matcher(page);

            int count = 0;
            for( int i = 0; m.find(i); i = m.end())
                count++;

            //☆★☆★☆★☆★☆★☆★Didn't decide yet☆★☆★☆★☆★☆★☆★☆★☆★
            //☆★☆★☆★☆★☆★☆★Think Again!!☆★☆★☆★☆★☆★☆★☆★☆★
            if(count>1)
            {
                JSONArray jsonArray = json.getJSONArray("results");
                Log.d("BUFF3","length="+jsonArray.length());
            }else {
                JSONObject jsonObject = json.getJSONObject("results");
            }


            //JSONArray jsonArray = jsonObject.getJSONArray("geometry");
            //Log.d("BUFF3","geometry length="+jsonArray.length());
            /*
            JSONArray typeObj = json1.getJSONArray("types");
            String types = typeObj.toString().replaceAll("\"","");
            types = types.substring(1,types.length()-2);
            String[] typeArray = types.split(",");
            JSONObject location = json1.getJSONObject("geometry").getJSONObject("location");
            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");
            Log.d("BUFF","location lat"+lat+",lng="+lng);
            Log.d("BUFF","types="+types);
            for(int i=0; i<typeArray.length; i++)
            {
                Log.d("BUFF","type"+(i+1)+typeArray[i]);
            }*/

            //requestNearByPlace(lat, lng, typeArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
