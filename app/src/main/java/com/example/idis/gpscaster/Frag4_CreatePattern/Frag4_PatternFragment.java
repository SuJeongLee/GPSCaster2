package com.example.idis.gpscaster.Frag4_CreatePattern;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.idis.gpscaster.GPSCollecting.PatternDatabase;
import com.example.idis.gpscaster.R;

import info.hoang8f.widget.FButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag4_PatternFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag4_PatternFragment extends Fragment implements View.OnClickListener {

    String TAGs = "ExtractPatternstatic";
    /*Pattern DB*/
    static PatternDatabase  mPatternDatabase;
    EditText tv_selectday;
    EditText tv_selecttime ;
    TextView tv_lookupDb;
    View rootView;

    ExtractPattern extractPattern=null;

    public static Frag4_PatternFragment newInstance() {
        Frag4_PatternFragment fragment = new Frag4_PatternFragment();
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
        rootView = inflater.inflate(R.layout.frag4_create_pattern, container, false);
        tv_selectday = (EditText)rootView.findViewById(R.id.tv_selectday);
        tv_selecttime = (EditText)rootView.findViewById(R.id.tv_selecttime);
        tv_lookupDb = (TextView)rootView.findViewById(R.id.tv_lookupDb);
        tv_lookupDb.setMovementMethod(new ScrollingMovementMethod());
        extractPattern = extractPattern.getInstance(getActivity(), getContext(), getResources(), rootView);


        Button btn_ok = (Button)rootView.findViewById(R.id.btn_homecompany);
        btn_ok.setOnClickListener(this);
        Button btn_lookupdb = (Button)rootView.findViewById(R.id.btn_lookupdb);
        btn_lookupdb.setOnClickListener(this);
        Button btn_extractPattern = (Button)rootView.findViewById(R.id.btn_extractpattern);
        btn_extractPattern.setOnClickListener(this);

        mPatternDatabase = mPatternDatabase.getInsance(getActivity());
        mPatternDatabase.open();


        return rootView;
    }

    @Override
    public void onClick(View v) {
        int selectday = -1;
        int selecttime = -1;

        switch (v.getId()){
            case R.id.btn_homecompany :
                extractPattern.extractHome();
                extractPattern.extractCompany();
                break;
            case R.id.btn_lookupdb :
                String result = mPatternDatabase.lookupDb();
                tv_lookupDb.setText(result);
                break;
            case R.id.btn_extractpattern : //Daily Report Notification 띄워주기
                if(tv_selecttime.getText().toString().compareTo("")!=0 && tv_selectday.getText().toString().compareTo("")!=0){
                    selectday = Integer.parseInt(tv_selectday.getText().toString()); // day : 0~6
                    selecttime = Integer.parseInt(( tv_selecttime.getText().toString())); //time : 0~11
                }
                if(selectday != -1 && selecttime != -1 ){
                    Log.d(TAGs, "get Instance3 ");
                    extractPattern.getPattern(selectday, selecttime);
                }else
                    Toast.makeText(getActivity().getApplicationContext(), "Enter the day and time >> ",Toast.LENGTH_LONG).show();
                break;


        }
    }
}
