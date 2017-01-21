package com.example.idis.gpscaster.GPSCollecting;

import android.content.ContentValues;

/**
 * Created by IDIS on 2016-12-15.
 */

public class PatternInfo {

    public int day=-1;
    public int time=-1;
    public String pid=null;
    public String ptype=null;
    public int freq=-1;

    PatternInfo(int day, int time, String pid, String ptype, int freq){
        this.day = day;
        this.time =  time;
        this.pid = pid;
        this.ptype = ptype;
        this.freq = freq;
    }

    public ContentValues getContentValue() {
        ContentValues c = new ContentValues();
        c.put("a_time", this.time);
        c.put("a_day", this.day);
        c.put("a_placeid", this.pid);
        c.put("a_placetype", this.ptype);
        c.put("a_frequency", this.freq);
        return c;
    }
}
