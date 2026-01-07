package com.keemsa.boilerplate.features.start;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.keemsa.boilerplate.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tb)
    Toolbar tb;

    @BindView(android.R.id.tabhost)
    FragmentTabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //setSupportActionBar(tb);

        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        tabHost.addTab(
                tabHost.newTabSpec("main").setIndicator(getString(R.string.lbl_main), null),
                MainFragment.class, savedInstanceState
        );

        tabHost.addTab(
                tabHost.newTabSpec("palettes").setIndicator(getString(R.string.lbl_list), null),
                ListFragment.class, savedInstanceState
        );
    }
}
