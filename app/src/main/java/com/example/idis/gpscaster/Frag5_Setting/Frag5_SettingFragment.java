package com.example.idis.gpscaster.Frag5_Setting;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;

import java.net.URISyntaxException;
import java.util.ArrayList;

import info.hoang8f.widget.FButton;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class Frag5_SettingFragment extends Fragment implements View.OnClickListener {
    ActivityResultEvent activityResultEvent;
    View rootView;
    Button btn_refresh;
    String TAG = "F5_SettingFragment";
    ViewPager viewPager;
    ListView homelist, companylist;
    HomeListViewAdapter homeListViewAdapter;
    CompanyListViewAdapter companyListViewAdapter;
    double lat, lng;
    ListData listData;


    //listview onClick
    int list1_position, list2_position;

/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(activityResultEvent.getRequestCode(), activityResultEvent.getResultCode(), activityResultEvent.getData());
        Intent intent = activityResultEvent.getData();
        String t = intent.getStringExtra("Place");
        Log.d(TAG, "(onActivity Result)"+t);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.frag5_setting, container, false);
        btn_refresh = (Button)rootView.findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(this);

        listData = listData.getInstace(getContext());
        homelist = (ListView) rootView.findViewById(R.id.homelist1);
        homeListViewAdapter = new HomeListViewAdapter(); //임시 설정
        homelist.setAdapter(homeListViewAdapter);
        homeListViewAdapter.notifyDataSetChanged();

        companylist = (ListView) rootView.findViewById(R.id.companylist1);
        companyListViewAdapter = new CompanyListViewAdapter(); //임시 설정
        companylist.setAdapter(companyListViewAdapter);
        companyListViewAdapter.notifyDataSetChanged();

        homelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listData.setMODE("home");

                Intent i = new Intent(getActivity(), SearchingHomeActivity.class);
                i.putExtra("lat", lat);
                i.putExtra("lng", lng);
                list1_position = position;
                list2_position = -1;
                startActivity(i);
            }
        });
        companylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listData.setMODE("company");

                Intent i = new Intent(getActivity(), SearchingHomeActivity.class);
                i.putExtra("lat", lat);
                i.putExtra("lng", lng);
                list1_position = -1;
                list2_position = position ;

                startActivity(i);
            }
        });

      /*  Thread3 thread3 = new Thread3();
        thread3.start();
        try {
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Toast.makeText(getActivity().getApplicationContext(), "lat = "+lat+", lng="+lng, Toast.LENGTH_LONG).show();
*/


        return rootView;


    }

    @Override
    public void onStart() {
        super.onStart();
        companyListViewAdapter.notifyDataSetChanged();
        homeListViewAdapter.notifyDataSetChanged();
        Log.d(TAG, "On start");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");

        if(listData.getPlace("home") != null){
            homeListViewAdapter.setListviewItem(0, listData.getPlace("home").getVicinity());
            homelist.setAdapter(homeListViewAdapter);
            Log.d(TAG, "HEY!");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "On Stop");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "On Destroy");
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_refresh :

                break;
        }
    }

/*

    class Thread3 extends Thread {

        LocationManager locationManager;
        Boolean isGPSEnabled = false;
        Boolean isNetworkEnabled = false;


        @Override
        public void run() {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            // GPS 프로바이더 사용가능여부
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // 네트워크 프로바이더 사용가능여부
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            //lng, lat 0 으로 나와서
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {


                    LocationListener locationListener = new LocationListener() {
                        public void onLocationChanged(Location location) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            Toast.makeText(getActivity().getApplicationContext(), lat + " " + lng, Toast.LENGTH_SHORT).show();
                        }

                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onProviderDisabled(String provider) {
                        }
                    };

                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                             return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    String locationProvider = LocationManager.GPS_PROVIDER;
                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

                    if (lastKnownLocation != null) {
                        lng = lastKnownLocation.getLatitude();
                        lat = lastKnownLocation.getLatitude();
                        Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
                    }

                }
            });

        }
    }
*/

}
