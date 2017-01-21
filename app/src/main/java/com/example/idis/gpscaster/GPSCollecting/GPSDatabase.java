package com.example.idis.gpscaster.GPSCollecting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by IDIS on 2016-11-29.
 */

public class GPSDatabase {

    static final String DB_NAME = "GPS.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_GPSDATA = "GPSDATA";
    private String FILE_PATH ;
    private static final String TAG = "DB";

    Context mContext = null;
    private static GPSDatabase mDatabase = null; //singleton
    private SQLiteDatabase mDb;
    private DatabaseHelper dbHelper;

    public GPSDatabase(Context context){
        mContext = context;
        FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+
                File.separator+DB_NAME;
    }

    public static GPSDatabase getInsance(Context context){
        if(mDatabase == null)
            mDatabase = new GPSDatabase(context);

        return mDatabase;
    }
    public boolean open() {
        dbHelper = new DatabaseHelper(mContext, FILE_PATH);
        mDb = dbHelper.getWritableDatabase();
        Log.e(TAG,"open");
        return true;
    }

    public void close() {
        mDb.close();
        mDatabase = null;
    }

    public Cursor rawQuery(String SQL) {
        Log.e(TAG,"rawQuery start");
        Cursor c1 = null;
        try {
            c1 = mDb.rawQuery(SQL, null);
            Log.e(TAG,"get cursor");
        } catch (Exception ex) {

        }
        return c1;
    }


    public boolean execSQL(String SQL) {
        try {
            mDb.execSQL(SQL);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    public void insertSQL(String table, ContentValues values){
        Log.e(TAG,"insertdb start");
        try{
            mDb.insert(table, null, values);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



    private class DatabaseHelper extends SQLiteOpenHelper {

        SQLiteDatabase db1 ;
        public DatabaseHelper(Context context, String filePath) {
            super(context, filePath, null, DB_VERSION);
            Log.e(TAG,"dbhelper start");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            this.db1 = db;
            //Create Database Table
            String SQL = "create table if not exists "+TABLE_GPSDATA +
                    "(a_id INTEGER PRIMARY KEY AUTOINCREMENT, "+//ID Index
                    "a_lat REAL,"+
                    "a_lng REAL,"+ //Time
                    "a_date TEXT,"+
                    "a_time TEXT,"+
                    "a_day INT,"+//sunday-1 monday-2 ..sat-6
                    "a_placeid TEXT,"+
                    "a_placetype TEXT);";//cafe|point_of_interest..
            db.execSQL(SQL);
            Log.d(TAG,"createDB");

        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            super.onOpen(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists "+ TABLE_GPSDATA);
            onCreate(db);
        }
    }
}
