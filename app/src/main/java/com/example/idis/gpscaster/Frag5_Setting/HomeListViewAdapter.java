package com.example.idis.gpscaster.Frag5_Setting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by IDIS on 2016-12-18.
 */

public class HomeListViewAdapter extends BaseAdapter{

    String TAG = "HomeListViewAdapter";
    int attr_size = 1;
    TextView tv_title;
    ImageView iv_icon;
    Context context;
    public ArrayList<String> listviewItem1 = new ArrayList<String>(); //homelist
    Bitmap icon;
    Boolean initial=true;
    ListData listData;

    public HomeListViewAdapter(){
        Log.d(TAG, "(Constructor) ");
        listData = listData.getInstace(context);
        listviewItem1.add(listData.getPlace("home").getVicinity());
/*
        PlaceInfo p = new PlaceInfo();
        p.setVicinity("");
        homeinfos.add(p);
*/

    }

    public void setListviewItem(int position, String place){
        listviewItem1.add(position, place);
    }
    @Override
    public int getCount() {
        return attr_size;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void changeData(){
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.home_list_item, parent, false);
        context = parent.getContext();
        if(initial == true)
        {
            changeData();
            initial = false;
        }
        icon = drawableToBitmap(R.drawable.check);
        tv_title = (TextView)convertView.findViewById(R.id.home_text1);
        iv_icon = (ImageView)convertView.findViewById(R.id.home_image1);

        tv_title.setText(listviewItem1.get(position));
        iv_icon.setImageBitmap(icon);

        Log.d(TAG, "(get View) ! position = "+position);
        return convertView;

    }

    public Bitmap drawableToBitmap(int d){
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),d);

        return  icon;
    }
}
