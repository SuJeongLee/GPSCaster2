package com.example.idis.gpscaster.Frag2_GPSCollecting;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.idis.gpscaster.GPSCollecting.GpsDataDeliverService;
import com.example.idis.gpscaster.GPSCollecting.RestartServiceForGpsData;
import com.example.idis.gpscaster.R;

import info.hoang8f.widget.FButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class Frag2_GPSCollectingFragment extends Fragment {
    Button btn_GPSstart;
    Button btn_PlaceRecommend;

    BroadcastReceiver receiver;
    Intent intentMyService;

    GpsDataDeliverService gpsDataDeliverService;


    public static Frag2_GPSCollectingFragment newInstance() {
        Frag2_GPSCollectingFragment fragment = new Frag2_GPSCollectingFragment();
        return fragment;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e("upload", "resume");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e("upload","pause");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag2_gpscollecting, container, false);


        intentMyService = new Intent(getActivity(), GpsDataDeliverService.class);
        receiver = new RestartServiceForGpsData();

        //Collecting GPS Service
        btn_GPSstart = (Button)rootView.findViewById(R.id.btn_start_gps2);
        btn_GPSstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(getActivity(),GpsDataDeliverService.class));
            }
        });


        return rootView;
    }



}
