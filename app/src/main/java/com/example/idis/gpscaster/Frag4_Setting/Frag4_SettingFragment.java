package com.example.idis.gpscaster.Frag4_Setting;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.idis.gpscaster.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag4_SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag4_SettingFragment extends Fragment {
    public static Frag4_SettingFragment newInstance() {
        Frag4_SettingFragment fragment = new Frag4_SettingFragment();
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
        View rootView = inflater.inflate(R.layout.frag4_setting_gps, container, false);
        return rootView;
    }
}
