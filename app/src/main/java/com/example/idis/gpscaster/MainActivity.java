package com.example.idis.gpscaster;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.idis.gpscaster.CustomUi.CustomViewPager;
import com.example.idis.gpscaster.Frag2_GPSCollecting.Frag2_GPSCollectingFragment;
import com.example.idis.gpscaster.Frag3_RealtimeGPS.Frag3_RealtimeGPSFragment;
import com.example.idis.gpscaster.Frag4_CreatePattern.Frag4_PatternFragment;
import com.example.idis.gpscaster.Frag5_Setting.Frag5_SettingFragment;
import com.example.idis.gpscaster.Frag5_Setting.ListData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    /*Google GPS Test*/
    protected GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private TextView logView;
    /*GPS Permission*/
    private static final int PERMISSION_REQUEST_CODE = 100;
    /*Main UI*/
    private static Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private MainPagerAdapter mainPagerAdapter;
    private CustomViewPager mainPager;
    /*SecondUI*/
    private String f1;
    private String f2;
    private TextView userInfo;
    private String userInfoStr;
    private String userType;
    private Button btn_gpscollecting;
    private Button btn_realtimegps;
    private Button btn_setting;
    private Button btn_extra1;
    private Button btn_extra2;

    private ListData listData;


    private View.OnClickListener BtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_gpscollecting:
                    drawerLayout.closeDrawers();
                    mainPagerAdapter.notifyDataSetChanged();
                    mainPager.setCurrentItem(0);
                    Log.i("Main", "Btn1 Clicked!");
                    break;
                case R.id.btn_realtime:
                    drawerLayout.closeDrawers();
                    mainPagerAdapter.notifyDataSetChanged();
                    mainPager.setCurrentItem(1);
                    Log.i("Main", "Btn2 Clicked!");
                    break;
                case R.id.btn_setting:
                    drawerLayout.closeDrawers();
                    mainPagerAdapter.notifyDataSetChanged();
                    mainPager.setCurrentItem(2);
                    Log.i("Main", "Btn3 Clicked!");
                    break;
                case R.id.btn_extra1 :
                    drawerLayout.closeDrawers();
                    mainPagerAdapter.notifyDataSetChanged();
                    mainPager.setCurrentItem(3);
                    Log.i("Main", "Btn4 Clicked!");
                    break;
                default:
                    drawerLayout.closeDrawers();
                    mainPagerAdapter.notifyDataSetChanged();
                    Log.i("Main", "Extra btn Clicked! ");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //MultiDex.install(getApplicationContext());



        GPSDetect();
        primaryUiSetting();
        secondUiSetting();
        listData = listData.getInstace(getApplicationContext());

    }
    public void primaryUiSetting(){
        // Set a toolbar to  replace to action bar
        /*toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        // Display toggle Icon for Drawer Menu
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        */
        // Setting the Drawer Toggle Icon
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle=new ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) { super.onDrawerClosed(drawerView); }
        };

        drawerLayout.setDrawerListener(drawerToggle);


        //ViewPager
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        mainPager = (CustomViewPager) findViewById(R.id.mainPager);
        mainPager.setAdapter(mainPagerAdapter);
        mainPager.setPagingEnabled(false);
        mainPager.requestDisallowInterceptTouchEvent(false);
        mainPager.setOffscreenPageLimit(4);
        mainPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }
        });



    }
    public void secondUiSetting(){
            userInfo = (TextView) findViewById(R.id.userInfo);
            btn_gpscollecting = (Button) findViewById(R.id.btn_gpscollecting);
            btn_realtimegps = (Button) findViewById(R.id.btn_realtime);
            btn_setting = (Button) findViewById(R.id.btn_setting);
            btn_extra1 = (Button) findViewById(R.id.btn_extra1);
            btn_extra2 = (Button) findViewById(R.id.btn_extra2);


        btn_gpscollecting.setOnClickListener(BtnOnClickListener);
        btn_realtimegps.setOnClickListener(BtnOnClickListener);
        btn_setting.setOnClickListener(BtnOnClickListener);
        btn_extra1.setOnClickListener(BtnOnClickListener);
        btn_extra2.setOnClickListener(BtnOnClickListener);
     }


    public void GPSDetect() {

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

//                logView.setText("latitude: " + lat + ", longitude: " + lng);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
//                logView.setText("onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                logView.setText("onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
//                logView.setText("onProviderDisabled");
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        // 수동으로 위치 구하기
        String locationProvider = LocationManager.GPS_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLatitude();
            double lat = lastKnownLocation.getLatitude();
            Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("MAIN", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }



    //Fragment ViewPager Adapter
    private class  MainPagerAdapter extends FragmentPagerAdapter {

        //Fragments that will be showed at the main view pager
        //private HomeFragment dummy1Fragment;
        private Frag2_GPSCollectingFragment dummy2Fragment;
        private Frag3_RealtimeGPSFragment dummy3Fragment;
        private Frag4_PatternFragment dummy4Fragment;
        private Frag5_SettingFragment dummy5Fragment;


        public MainPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            //dummy1Fragment = HomeFragment.newInstance();
            dummy2Fragment = Frag2_GPSCollectingFragment.newInstance();
            dummy3Fragment = Frag3_RealtimeGPSFragment.newInstance();
            dummy4Fragment = Frag4_PatternFragment.newInstance();
            dummy5Fragment = new Frag5_SettingFragment();
        }

        @Override
        public int getItemPosition(Object item){
            return POSITION_NONE;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch(position){
                case 0:
                    Log.e("main","MainPagerAdapter getItem0");
                    return dummy2Fragment;
                case 1:
                    Log.e("main","MainPagerAdapter getItem1");
                    return dummy3Fragment;
                case 2:
                    Log.e("main","MainPagerAdapter getItem2");
                    return dummy4Fragment;
                case 3:
                    Log.e("main", "MainPagerAdapter getItem3");
                    return dummy5Fragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return "모니터";
                case 1:
                    return "임시";
                case 2:
                    return "Pattern";
                case 3:
                    return "Setting";
            }

            return null;
        }

    }
    public CustomViewPager getInnerViewPager(){
        return mainPager;
    }
}
