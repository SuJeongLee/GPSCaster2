package com.example.idis.gpscaster.GPSCollecting;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IDIS on 2016-12-14.
 */

public class Classificiation {

    int day; // 0-6
    int time; //2h : 0-11

    public int getDay(SimpleDateFormat dateFormat){
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(calendar.DAY_OF_WEEK);

        return day;
    }
    public int getTime(SimpleDateFormat dateFormat){
        Date date = new Date();
        String strTime = dateFormat.format(date);
        strTime = strTime.substring(0,1);
        Log.d("TIME", "time = "+strTime);
        time = Integer.parseInt(strTime);

        return time/2;
    }
}
