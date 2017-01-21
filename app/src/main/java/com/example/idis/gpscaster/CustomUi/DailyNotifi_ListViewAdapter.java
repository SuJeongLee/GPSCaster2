package com.example.idis.gpscaster.CustomUi;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class DailyNotifi_ListViewAdapter extends BaseAdapter {

    String TAG = "ListViewAdapter";
    private Context context;
    ArrayList<PlaceInfo> placeInfos4 ;
    int attr_size = 4;
    TextView tv_title;
    ImageView iv_icon;
    ArrayList<String> listviewItem = new ArrayList<String>();
    ArrayList<Bitmap> listviewIcon = new ArrayList<Bitmap>();

    public DailyNotifi_ListViewAdapter(Context c, ArrayList<PlaceInfo> pArr){
        context = c;
        placeInfos4 = pArr;

        listviewItem.add("");
        listviewItem.add("");
        listviewItem.add("");
        listviewItem.add("");

        listviewIcon.add(drawableToBitmap(R.drawable.list_vicinity));
        listviewIcon.add(drawableToBitmap(R.drawable.list_price));
        listviewIcon.add(drawableToBitmap(R.drawable.list_time));
        listviewIcon.add(drawableToBitmap(R.drawable.list_rating));
        Log.d(TAG, "(Constructor) ");
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

    public void changeData(int idx){
        listviewItem = new ArrayList<String>();
        listviewItem.add("address ? "+placeInfos4.get(idx).getVicinity());
        listviewItem.add("price level? "+placeInfos4.get(idx).getPrice_level()+"");
        listviewItem.add("open now ? "+placeInfos4.get(idx).getOpen_now());
        listviewItem.add("rating ? "+placeInfos4.get(idx).getRating());

        Log.d(TAG, "(changeData) idx = "+idx);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       if(convertView == null)
           convertView = LayoutInflater.from(context).
                   inflate(R.layout.dailynotifi_list_item, parent, false);

        tv_title = (TextView)convertView.findViewById(R.id.text1);
        iv_icon = (ImageView)convertView.findViewById(R.id.image1);

        //item 정렬
        tv_title.setText(listviewItem.get(position));
        iv_icon.setImageBitmap(listviewIcon.get(position));


        Log.d(TAG, "(get View) ! position = "+position);
        return convertView;

    }

    public Bitmap drawableToBitmap(int d){
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),d);

        return  icon;
    }
}
