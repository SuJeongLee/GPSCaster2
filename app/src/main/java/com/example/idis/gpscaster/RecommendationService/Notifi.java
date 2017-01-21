package com.example.idis.gpscaster.RecommendationService;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class Notifi extends FragmentActivity implements

        //얘는 arrayList 읽어서 notification 하는애임
        //얘를 사용할거면 따로 arrayList 만들어서 얘가 읽게 만들면 됨
        // 근데 그렇게 할 경우 충돌이 예상되긴 함 !

        //아예 다르게 하는게 더 좋을거같음
        Serializable,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

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

    private ArrayList<PlaceInfo> placeInfos;
    private TextView tv1;
    private Marker prvMarker = null;

    private int marker_num = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifi);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        String a = i.getStringExtra("KEY");

        tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setMovementMethod( new ScrollingMovementMethod());
        tv1.setText(a);

        Log.d(TAG, "(OnCreate) ! ");

        //Bitmap Setting
        marker_off = BitmapFactory.decodeResource(getBaseContext().getResources(),
                R.drawable.marker_off);
        marker_on = BitmapFactory.decodeResource(getBaseContext().getResources(),
                R.drawable.marker_on);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        readArr();

        Log.d(TAG, "onMapReaady");
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(placeInfos.get(0).getLat(), placeInfos.get(0).getLat()), 15));
        Log.d(TAG, "moveCamera "+placeInfos.get(0).getName()+ placeInfos.get(0).getVicinity());
        //mMap.addMarker(new MarkerOptions().position(start).title("Marker in Sydney"));

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

        for (int i = 0; i < placeInfos.size(); i++)
            sampleList.add(new MarkerItem(placeInfos.get(i).getLat(), placeInfos.get(i).getLng()));

        for (int i = 0; i < sampleList.size(); i++)
            addMarker(sampleList.get(i), false, i);

        marker_num = sampleList.size();
    }

    private Marker addMarker(MarkerItem markerItem, boolean isSelectedMarker, int index) {
        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLng());
        Log.d(TAG, "Add marker ! ");

        if (isSelectedMarker) {
            iv_marker.setImageBitmap(marker_on);
        } else {
            iv_marker.setImageBitmap(marker_off);
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markerOptions.alpha(0.7f);
        markerOptions.title(placeInfos.get(index).getName()); //index로 지정
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
        if (prvMarker != null)
            prvMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        prvMarker = marker;

        for (int i = 0; i < marker_num; i++) {
            if (marker.getTitle().compareTo(placeInfos.get(i).getName()) == 0) {  // 원하는 정보 여기에 뿌리기
                tv1.setText("NAME\n" + marker.getTitle() + "\nTYPE\n" + placeInfos.get(i).getTypes()[0] + "," +
                        placeInfos.get(i).getTypes()[1] + "\nOPEN NOW\n" + placeInfos.get(i).getOpen_now() + "\nVICINITY\n" + placeInfos.get(i).getVicinity()
                        + "\nRATING\n" + placeInfos.get(i).getRating() + "\nPRICE LEVEL\n" + placeInfos.get(i).getPrice_level());
            }
        }


        return false;
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Notifi Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    public void readArr() {

        Log.d(TAG, "Read Arr ! ");
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        File data = getDir(getApplicationContext().getPackageName(), MODE_PRIVATE);

        try {
            fis = new FileInputStream(new File(data + "/" + "arr.txt"));
            ois = new ObjectInputStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            placeInfos = (ArrayList) ois.readObject();
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

}
