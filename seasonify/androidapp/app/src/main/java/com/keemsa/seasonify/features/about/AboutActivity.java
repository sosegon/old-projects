package com.keemsa.seasonify.features.about;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.keemsa.seasonify.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sebastian on 03/07/17.
 */

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.tb_about)
    Toolbar tb_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setSupportActionBar(tb_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}
