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

public class CompanyListViewAdapter extends BaseAdapter {


    String TAG = "CompanyListViewAdapter";
    private Context context;
    ArrayList<PlaceInfo> cominfos ;
    int attr_size = 1;
    TextView tv_title;
    ImageView iv_icon;
    Bitmap icon;
    ListData listData;

    ArrayList<String> listviewItem2 = new ArrayList<String>(); //companylist

    public CompanyListViewAdapter(){
        Log.d(TAG, "(Constructor) ");
        listData = listData.getInstace(context);
        listviewItem2.add(listData.getPlace("company").getVicinity());
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

/*    public void changeData(){

        listData = listData.getInstace(context);  // context is null
        listviewItem1.add(listData.getPlace("home").getVicinity());
    }*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.company_list_item, parent, false);
        context = parent.getContext();
        icon = drawableToBitmap(R.drawable.check);
        tv_title = (TextView)convertView.findViewById(R.id.company_text1);
        iv_icon = (ImageView)convertView.findViewById(R.id.company_image1);
        tv_title.setText(listviewItem2.get(position));
        iv_icon.setImageBitmap(icon);

        Log.d(TAG, "(get View) ! position = "+position);
        return convertView;
    }

    public Bitmap drawableToBitmap(int d){
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),d);
        return  icon;
    }

    //db에서 해당시간대 뽑아서 넣는거 까지하기
}
