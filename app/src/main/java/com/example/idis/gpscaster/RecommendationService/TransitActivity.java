package com.example.idis.gpscaster.RecommendationService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.idis.gpscaster.Frag3_RealtimeGPS.PlaceInfo;
import com.example.idis.gpscaster.R;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class TransitActivity extends Activity implements
        Serializable, View.OnClickListener{

    String TAG = "TransitActivity";
    ArrayList<PlaceInfo> placeInfos;
    Button btn1;
    TextView tv1 ;
    PlaceRecommendationService placeRecommendationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit);

        Intent i = getIntent();
        String in = i.getStringExtra("intent1");

        btn1 = (Button)findViewById(R.id.transit_btn1);
        btn1.setOnClickListener((View.OnClickListener) this);
        tv1 = (TextView)findViewById(R.id.transit_tv1);
        tv1.setText("FROM INTENT = "+in);

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()){

            case R.id.transit_btn1 :

                //placeRecommendationService = PlaceRecommendationService.getInstance(getApplicationContext(), getResources());
                readArr();
                Log.d("InfoConfirm","Trainsit : size = "+placeInfos.size());

                /*tv1.setText(placeRecommendationService.getPlace_arr().size()+" asdasd");*/

               /* ParsingThread pt = new ParsingThread();
                pt.start();
                try {
                    pt.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tv1.setText("FINISHING") ;*/
                break;
        }
    }

    class ParsingThread extends Thread{
        @Override
        public void run(){

        }
    }

    public void readArr(){
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        File data = getDir(getApplicationContext().getPackageName(), MODE_PRIVATE);

            try {
                fis = new FileInputStream(new File(data+"/"+"arr.txt"));
                ois = new ObjectInputStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            placeInfos = (ArrayList)ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(fis != null)
                    fis.close();
                if(ois != null)
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }

    }


}
