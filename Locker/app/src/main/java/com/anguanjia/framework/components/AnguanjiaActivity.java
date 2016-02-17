package com.anguanjia.framework.components;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ctflab.locker.utils.PreferencesData;

public class AnguanjiaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesData.setShowAuthentication(false);
    }
}
