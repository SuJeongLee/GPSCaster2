package com.example.idis.gpscaster.Frag5_Setting;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.List;

import info.hoang8f.widget.FButton;

public class SearchingHomeActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener {


    String TAG = "SearchingHomeActivity";
    /*Place AutoComplete*/
    protected GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private PlacesAutoCompleteAdapter mPlacesAdapter;
    Place place; //final place chosen
    LatLng   warsaw = new LatLng(52.2277251, 21.0051025); // warsaw로 fix 해놓음
    private ListData listData;

    AutoCompleteTextView myLocation;
    FButton BtnDone;
    GoogleMap mMap;
    Marker curMarker = null;
    double lng = 0, lat = 0;
    Geocoder geocoder;

    //clicked placeinfo
    PlaceInfo placeInfo = new PlaceInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addApi(AppIndex.API)
                .build();

        if(mGoogleApiClient.isConnected() == false)
                mGoogleApiClient.connect();

        listData = listData.getInstace(getApplicationContext());
        geocoder = new Geocoder(this);

        BtnDone = (FButton)findViewById(R.id.btn_done);
        BtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listData.getMode().compareTo("home") == 0)
                    listData.setPlace(placeInfo, "home");
                else
                    listData.setPlace(placeInfo, "comapany");

                finish();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.searchingMap);
        mapFragment.getMapAsync(this);

        setAutoCompelete();
        Toast.makeText(getApplicationContext(), "intent " + lat + ", " + lng, Toast.LENGTH_LONG).show();

    }


    public void setAutoCompelete(){
        myLocation = (AutoCompleteTextView)findViewById(R.id.editText);
        mPlacesAdapter = new PlacesAutoCompleteAdapter(this, android.R.layout.simple_list_item_1,
                mGoogleApiClient, toBounds(warsaw, 10000), null); // radius = 10km
        myLocation.setOnItemClickListener(mAutocompleteClickListener);
        myLocation.setAdapter(mPlacesAdapter);

    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlacesAutoCompleteAdapter.PlaceAutocomplete item = mPlacesAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Log.d("AUTO","private AdapterView.OnItem");
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("place", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            place = places.get(0);
        }
    };

    public void onMapSearch(View view) {
        String location = myLocation.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

              if(curMarker != null)
                curMarker.remove();
            mMap.clear();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            markerOptions.alpha(0.7f);
            markerOptions.title(location); //index로 지정
            String name = place.getName().toString();
            if(name == null)
                name = myLocation.getText().toString();
            markerOptions.snippet(name); // 이 place가 먹히는가 두고보자

            placeInfo.setVicinity(address.getAddressLine(0)+","+address.getAddressLine(1));
            placeInfo.setLat(latLng.latitude); placeInfo.setLng(latLng.longitude);

            curMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(address.getFeatureName()));
            mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera

        //    Toast.makeText(getApplicationContext(), "LAT = "+lat+", LNG = "+lng, Toast.LENGTH_LONG).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 15));
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
    }


    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
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

    @Override
    public void onMapClick(LatLng latLng) {
        List<Address> list = null;
        Log.d(TAG, "onMapClick");
        try {
            list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null) {
            if (list.size()==0) {
                Toast.makeText(getApplicationContext(), "No list on MapClick ", Toast.LENGTH_LONG).show();
            } else {
                myLocation.setText(list.get(0).getAddressLine(0)+list.get(0).getAddressLine(1));
                mMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                markerOptions.alpha(0.7f);
                markerOptions.title(list.get(0).toString()); //index로 지정
                String name = list.get(0).toString();
                placeInfo.setVicinity(list.get(0).getAddressLine(0)+","+list.get(0).getAddressLine(1));
                placeInfo.setLat(latLng.latitude); placeInfo.setLng(latLng.longitude);

                if(name == null)
                    Log.d(TAG, "name is null in onMapClick");
                else
                    markerOptions.snippet(name); // 이 place가 먹히는가 두고보자
                curMarker = mMap.addMarker(markerOptions);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onConnected( Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    class Thread3 extends Thread {

        LocationManager locationManager;
        Boolean isGPSEnabled = false;
        Boolean isNetworkEnabled = false;


        @Override
        public void run() {

            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

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
                            Toast.makeText(getApplicationContext(), lat + " " + lng, Toast.LENGTH_SHORT).show();
                        }

                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onProviderDisabled(String provider) {
                        }
                    };

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
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

}
