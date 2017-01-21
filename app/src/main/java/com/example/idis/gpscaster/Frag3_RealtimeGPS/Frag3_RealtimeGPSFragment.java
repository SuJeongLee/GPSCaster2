package com.example.idis.gpscaster.Frag3_RealtimeGPS;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.idis.gpscaster.R;
import com.example.idis.gpscaster.RecommendationService.PlaceRecommendationService;

/**
 * A simple {@link Fragment} subclass.
 */
public class Frag3_RealtimeGPSFragment extends Fragment {
    private Button btn_PlaceRecommend;
    PlaceRecommendationService placeRecommendationService;
    public static Frag3_RealtimeGPSFragment newInstance() {
        Frag3_RealtimeGPSFragment fragment =
                new Frag3_RealtimeGPSFragment();
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
        View rootView = inflater.inflate(R.layout.frag3_realtime_gps, container, false);

        btn_PlaceRecommend = (Button)rootView.findViewById(R.id.btn_place_recommend);
        btn_PlaceRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //  getActivity().startService(new Intent(getActivity(),PlaceRecommendationService.class));

                placeRecommendationService = placeRecommendationService.getInstance(getContext(), getResources());

                /*
                p.insertPlaceId("aaa");
                p.insertPlaceId("aaa");
                p.insertPlaceId("aaa");
                p.insertPlaceId("aaa");
                p.insertPlaceId("aaa");
                p.insertPlaceId("aaa");
                p.insertPlaceId("aaa");
                p.insertPlaceId("aaa");
                p.insertPlaceId("bbb");
                p.insertPlaceId("ccc");
                //a-4 b-3 c-3*/

                placeRecommendationService.notificationService();

            }
        });
        return rootView;
    }
}
