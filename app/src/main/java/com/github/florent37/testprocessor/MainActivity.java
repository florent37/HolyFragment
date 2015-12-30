package com.github.florent37.testprocessor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportFragmentManager().beginTransaction()
        //        .add(R.id.frame, HolyMyFragment.newInstance(3,"Florent"))
        //        .commit();
    }

}
