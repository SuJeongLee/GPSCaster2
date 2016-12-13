package com.example.idis.gpscaster.RecommendationService;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;
import com.google.android.gms.location.places.Place;

import java.io.Serializable;
import java.util.ArrayList;

public class TransitActivity extends AppCompatActivity
implements Serializable{

    ArrayList<PlaceInfo> placeInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit);

        Intent i = getIntent();
        String in = i.getStringExtra("intent1");

        Serializable intentListData = i.getSerializableExtra("arrayList");
       placeInfos = (ArrayList<PlaceInfo>)intentListData;

        TextView tv1 = (TextView)findViewById(R.id.tv1);
        if(placeInfos != null)
            tv1.setText("placeInfos is not null");
        else
            tv1.setText("placeInfos is null");
    }

}
