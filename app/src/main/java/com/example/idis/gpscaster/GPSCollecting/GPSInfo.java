package com.example.idis.gpscaster.GPSCollecting;

import android.content.ContentValues;

/**
 * Created by IDIS on 2016-12-15.
 */

public class GPSInfo {

    double lat=0;
    double lng=0;
    String date="";
    String time="";
    int day=-1;
    String pid="";
    String ptype="";

    GPSInfo(double lat, double lng, String date, String time, int day, String pid, String ptype){
        this.lat=lat;
        this.lng=lng;
        this.date=date;
        this.time=time;
        this.day=day;
        this.pid=pid;
        this.ptype=ptype;
    }

    public ContentValues getContentValue(){
        ContentValues c = new ContentValues();
        c.put("a_lat",this.lat);
        c.put("a_lng",this.lng);
        c.put("a_date",this.date);
        c.put("a_time",this.time);
        c.put("a_day",this.day);
        c.put("a_placeid",this.pid);
        c.put("a_placetype",this.ptype);

        return c;
    }
}
