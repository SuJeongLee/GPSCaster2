package com.example.idis.gpscaster.RecommendationService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;

public class Notifi extends FragmentActivity implements
        Serializable,
        OnMapReadyCallback,
GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private String TAG = "Notifi";

    private GoogleMap mMap;
    Marker selectedMarker;
    View marker_root_view;
    private ImageView iv_marker;
    private String p;
    private double lat; //for marker
    private double lng; //for marker
    private Bitmap marker_on;
    private Bitmap marker_off;

    //From the PlaceRecommendation Service ->> Take Place ArrayList
    private PlaceRecommendationService placeRecommendationService;
    private ArrayList<PlaceInfo> placeInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifi);

        Log.d(TAG,"page = "+p );
        Log.d(TAG, "lat = "+lat+", lng = "+lng);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Bitmap Setting
        marker_off = BitmapFactory.decodeResource(getBaseContext().getResources(),
                R.drawable.marker_off);
        marker_on = BitmapFactory.decodeResource(getBaseContext().getResources(),
                R.drawable.marker_on);

        Intent intent = getIntent();
        /*placeInfos = intent.getParcelableArrayListExtra("array");
        if(placeInfos == null)
            Log.d(TAG, "placeinfos is null") ;
*/
        //Log.d(TAG, placeInfos.size()+"ddddd!!");
        /*placeRecommendationService = (PlaceRecommendationService)intent.getSerializableExtra("ArrayList");
        Log.d(TAG,placeRecommendationService.getPlace_arr().size()+" ddd");
*/
    }
    @Override
    public void onMapReady(GoogleMap googleMap){

        Log.d(TAG, "onMapReaady");
        mMap = googleMap;

        LatLng start = new LatLng(52.217135, 21.014932); //첫 포코스 맞추기
        mMap.addMarker(new MarkerOptions().position(start).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        setCustomMarkerView(googleMap);
        getSampleMarkerItems(googleMap);
    }
    public void setCustomMarkerView(GoogleMap googleMap){

        Log.d(TAG, "setCustomMarkerView");
        //placeRecommendationService = placeRecommendationService.getInstance(getApplicationContext(), getResources());
        marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
        //원래 가격 추가
        iv_marker = (ImageView) marker_root_view.findViewById(R.id.marker);
    }

    private void getSampleMarkerItems(GoogleMap googleMap) {

        Log.d(TAG, "getSampleMarkerItems");

        //placeInfos = placeRecommendationService.getPlace_arr();
        //Log.d(TAG, "placeinfos len = "+placeInfos.size());

        int a = getIntent().getIntExtra("i1",0);
        Log.d(TAG, "a = "+a);

        /*for(int i=0; i<placeInfos.size(); i++)
        { //TEST
            Log.d(TAG, placeInfos.get(i).getName());
            Log.d(TAG, placeInfos.get(i).getTypes()[0]+placeInfos.get(i).getTypes()[1]);
        }*/

        ArrayList<MarkerItem> sampleList = new ArrayList();

        /*for(int i=0; i<placeInfos.size() ; i++){
            sampleList.add(new MarkerItem(placeInfos.get(i).getLat(), placeInfos.get(i).getLng()));
        }
        for (MarkerItem markerItem : sampleList) {
            addMarker(markerItem, false, googleMap);
        }*/
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start);
    }
    private Marker addMarker(MarkerItem markerItem, boolean isSelectedMarker, GoogleMap googleMap){
        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLng());
        Log.d(TAG, "Add marker ! ");
/*
        if (isSelectedMarker) {
            googleMap.addMarker(optSecond).showInfoWindow();
        } else {
            iv_marker.setImageBitmap(marker_off);
        }
*/

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));

        return mMap.addMarker(markerOptions);
    }

    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
}
