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
 * Created by IDIS on 2016-12-14.
 */

public class PatternDatabase {

    static final String DB_NAME = "GPS.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_GPSDATA = "PATTERNDATA";
    private String FILE_PATH ;
    private static final String TAG = "DB2";
    private static final String TAG_DB = "PatternDB";

    Context mContext = null;
    private static PatternDatabase mDatabase = null; //singleton
    private static String TABLE_PATTERN = "PATTERNDATA";
    private SQLiteDatabase mDb;
    private DatabaseHelper dbHelper;


    public PatternDatabase(Context context){
        mContext = context;
        FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+
                File.separator+DB_NAME;
    }

    public static PatternDatabase getInsance(Context context){
        if(mDatabase == null)
            mDatabase = new PatternDatabase(context);

        return mDatabase;
    }
    public boolean open() {
        dbHelper = new DatabaseHelper(mContext, FILE_PATH);
        mDb = dbHelper.getWritableDatabase();
        dbHelper.createTable(mDb);
        Log.e(TAG,"open");
        return true;
    }

    public void close() {
        mDb.close();
        mDatabase = null;
    }

    public Cursor rawQuery(String SQL) {
        Log.e(TAG_DB,"rawQuery start");
        Cursor c1 = null;
        try {
            c1 = mDb.rawQuery(SQL, null);
            Log.e(TAG_DB,"get cursor");
        } catch (Exception ex) {

        }
        return c1;
    }
    public boolean execSQL(String SQL) {
        try {
            mDb.execSQL(SQL);
            Log.d(TAG_DB, "execSQL!!");
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    public void insertSQL(String table, ContentValues values){
        Log.d(TAG_DB,"insert in PatternDB");
        try{
            mDb.insert(table, null, values);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String filePath) {
            super(context, filePath, null, DB_VERSION);
            Log.e(TAG,"dbhelper2 start");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            //Create Database Table
            String SQL = "create table if not exists "+TABLE_GPSDATA +
                    "(a_id INTEGER PRIMARY KEY AUTOINCREMENT, "+//ID Index
                    "a_day INT,"+//sunday-1 monday-2 ..sat-6
                    "a_time INT,"+
                    "a_placeid TEXT,"+
                    "a_placetype TEXT," +
                    "a_frequency INT);";//cafe|point_of_interest..
            db.execSQL(SQL);
            Log.e(TAG,"createDB");

        }
        public void createTable(SQLiteDatabase db)
        {
            //Create Database Table
            String SQL = "create table if not exists "+TABLE_GPSDATA +
                    "(a_id INTEGER PRIMARY KEY AUTOINCREMENT, "+//ID Index
                    "a_day INT,"+//sunday-1 monday-2 ..sat-6
                    "a_time INT,"+
                    "a_placeid TEXT,"+
                    "a_placetype TEXT," +
                    "a_frequency INT);";//cafe|point_of_interest..
            db.execSQL(SQL);
            Log.e(TAG,"createDB");
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
    public String lookupDb()
    {
        String result="";
        Cursor c = mDatabase.rawQuery("SELECT * from "+TABLE_PATTERN+";");
        while(c != null && c.moveToNext()){
            Log.d(TAG_DB, "num of cursor = "+c.getCount());
            int day = c.getInt(c.getColumnIndex("a_day"));
            int time = c.getInt(c.getColumnIndex("a_time"));
            String placeid = c.getString(c.getColumnIndex("a_placeid"));
            String placetype = c.getString(c.getColumnIndex("a_placetype"));
            int freq = c.getInt(c.getColumnIndex("a_frequency"));
            result += "day:"+day+", time:"+time+", placeid="+placeid + ", placetype:"+placetype+"freq:"+freq+"\n";
        }

        return result;
    }
}
