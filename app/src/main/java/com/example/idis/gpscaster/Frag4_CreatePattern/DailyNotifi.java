package com.example.idis.gpscaster.Frag4_CreatePattern;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.idis.gpscaster.CustomUi.DailyNotifi_ListViewAdapter;
import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;
import com.example.idis.gpscaster.RecommendationService.MarkerItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import static android.widget.AdapterView.*;

public class DailyNotifi extends FragmentActivity implements
        OnMapReadyCallback,
                GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener,
                OnItemClickListener{

    private static final String TAG = "DailyNotifi";
    ExtractPattern extractPattern;
    ArrayList<PlaceInfo> placeInfos3;
    private String TAGs = "ExtractPatternstatic";

    private GoogleMap mMap;
    View marker_root_view;
    private ImageView iv_marker;
    private String p;
    private Marker prvMarker = null;
    private int marker_num = 0;

    //ListView
    private DailyNotifi_ListViewAdapter listViewAdapter;
    private ListView list;

    //sliding Up Panel
    private SlidingUpPanelLayout mLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_notifi);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        extractPattern = extractPattern.getInstance(this, getApplicationContext(), getResources(), getCurrentFocus());
        Log.d(TAGs, "ExtractPattern 4");
        placeInfos3 = extractPattern.getPlaceInfos();
        Log.d(TAG, "(OnCreate)");

        for (int j = 0; j < placeInfos3.size(); j++)
            Log.d("ARRAY", placeInfos3.get(j).getName());

        listViewAdapter = new DailyNotifi_ListViewAdapter(getApplicationContext(), placeInfos3);
        list = (ListView)findViewById(R.id.list1);
        list.setAdapter(listViewAdapter);



        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
        mLayout.setFadeOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "(onMapClick)");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (prvMarker != null)
            prvMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        TextView tv_name = (TextView)findViewById(R.id.tv_pname);
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        prvMarker = marker;
        int idx=0 ;
        for (int i = 0; i < marker_num; i++) {
            if (marker.getTitle().compareTo(placeInfos3.get(i).getName()) == 0) {  // 원하는 정보 여기에 뿌리기
                idx = i;
                tv_name.setText(placeInfos3.get(i).getName());
                break;
            }
        }
        Log.d("ListViewAdapter","Marker Clicked ! idx = "
                +idx+" name = "+placeInfos3.get(idx).getName());

        listViewAdapter.changeData(idx);
        listViewAdapter.notifyDataSetChanged();

        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "(onMapReady)");
        Log.d(TAG, "onMapReaady");
        mMap = googleMap;

        //center 잡기
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(placeInfos3.get(0).getLat(),placeInfos3.get(0).getLng()), 15));
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        setCustomMarkerView(googleMap);
        getSampleMarkerItems(googleMap);
    }
    public void setCustomMarkerView(GoogleMap googleMap) {

        Log.d(TAG, "setCustomMarkerView");

        marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
        iv_marker = (ImageView) marker_root_view.findViewById(R.id.marker);
    }
    private void getSampleMarkerItems(GoogleMap googleMap) {

        Log.d(TAG, "getSampleMarkerItems");

        int a = getIntent().getIntExtra("i1", 0);
        Log.d(TAG, "a = " + a);

        ArrayList<MarkerItem> sampleList = new ArrayList();

        if(placeInfos3.size() != 0) {
            int min = Math.min(5, placeInfos3.size());
            for (int i = 0; i < min; i++)
                sampleList.add(new MarkerItem(placeInfos3.get(i).getLat(), placeInfos3.get(i).getLng()));

            for (int i = 0; i < sampleList.size(); i++)
                addMarker(sampleList.get(i), false, i);
        }

        marker_num = sampleList.size();
    }

    private Marker addMarker(MarkerItem markerItem, boolean isSelectedMarker, int index) {
        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLng());
        Log.d(TAG, "Add marker ! ");

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        markerOptions.alpha(0.7f);

        if(placeInfos3.size() !=0 )
            markerOptions.title(placeInfos3.get(index).getName()); //index로 지정

        return mMap.addMarker(markerOptions);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getApplicationContext(),
                "OnItem Click - Position = "+position, Toast.LENGTH_LONG);
    }
}
