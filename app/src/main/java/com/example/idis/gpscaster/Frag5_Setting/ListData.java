package com.example.idis.gpscaster.Frag5_Setting;

import android.content.Context;
import android.util.Log;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by IDIS on 2016-12-20.
 */

public class ListData {
    public static String TAG = "ListData";

    //for daily recommendation  이동상태일때는 daily recommendation을 하지 않기 위해
    private static boolean isMoving = false;

    private static ListData listData = null;
    private static PlaceInfo homeInfo = null;
    private static PlaceInfo companyInfo = null;
    private static Context context = null;
    ArrayList<PlaceInfo> homeinfos;
    ArrayList<PlaceInfo> companyinfos;

    private static Boolean MODE = true;

    private ListData(Context context) {
        this.context = context;
        readArr(homeinfos, "home");
        readArr(companyinfos, "company");
        homeInfo = homeinfos.get(0);
        companyInfo = companyinfos.get(0);
    }

    public static ListData getInstace(Context c) {
        if (listData == null)
            listData = new ListData(c);

        return listData;
    }
    public static void setIsMoving(boolean b){
        isMoving = b;
    }
    public static boolean getIsMoving(){
        return isMoving;
    }
    public static void setMODE(String s) {
        if (s.compareTo("home") == 0)
            MODE = true;
        else
            MODE = false;
    }
    public static String getMode(){
        if(getModeStatue() == true)
            return "home";
        else
            return "company";
    }
    private static Boolean getModeStatue(){
        return MODE;
    }

    public void setPlace(PlaceInfo place, String s) {
        if (s.compareTo("home") == 0) {
            homeInfo = place;
            homeinfos.set(0, homeInfo);
            saveArr(homeinfos, "home");
        } else {
            companyInfo = place;
            companyinfos.set(0, companyInfo);
            saveArr(companyinfos, "company");
        }


    }

    public PlaceInfo getPlace(String s) {
        if (s.compareTo("home") == 0)
            return homeInfo;
        else if (s.compareTo("company") == 0)
            return companyInfo;
        else
            return null;
    }

    public void readArr(ArrayList<PlaceInfo> parr, String filename) {
        Log.d(TAG, "Read Arr ! ");
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        File data = context.getDir(context.getPackageName(), MODE_PRIVATE);

        try {
            fis = new FileInputStream(new File(data +File.separator+filename+ ".txt"));
            ois = new ObjectInputStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(filename.compareTo("home") == 0)
                homeinfos = (ArrayList) ois.readObject();
            else
                companyinfos = (ArrayList)ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void saveArr(ArrayList<PlaceInfo> parr, String filename) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        File data = context.getDir(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            fos = new FileOutputStream(new File(data + "/" + filename + ".txt"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(parr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null)
                try {
                    fos.close();
                    Log.d("FILE", "File close ! fos is closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (oos != null)
                try {
                    oos.close();
                    Log.d("FILE", "OutputStream close ! oos is closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
