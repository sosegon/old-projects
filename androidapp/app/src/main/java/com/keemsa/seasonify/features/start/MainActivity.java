package com.keemsa.seasonify.features.start;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.seasonify.R;
import com.squareup.picasso.Picasso;
import com.thebluealliance.spectrum.SpectrumPalette;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainMvpView {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean onActivityResultCalled = false;
    private File mPhotoFile;

    private MainPresenter mPresenter;

    @BindView(R.id.imv_face)
    ImageView imv_face;

    @BindView(R.id.palette)
    SpectrumPalette palette;

    @BindView(R.id.ctb)
    CollapsingToolbarLayout ctb;

    @BindView(R.id.tb)
    Toolbar tb;

    @OnClick(R.id.fab_scan)
    public void start_camera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                mPhotoFile = mPresenter.createImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (mPhotoFile != null) {
                Uri uriPhoto = mPresenter.generateUri(this, mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPhoto);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new MainPresenter(this);
        mPresenter.initTensorFlowAndLoadModel(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(tb);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mPresenter.isViewAttached()) {
            mPresenter.attachView(this);
        }

        /*
            onResume gets called after onActivityResult, but
            the latter is not called every time, just when the
            camera activity has been started.
            To avoid conflict when updating the UI, ta flag is used,
            so previous results are loaded in any situation except
            when the camera activity has been started.
         */
        if(!onActivityResultCalled) {
            mPresenter.loadPreviousResults(this);
        }

        onActivityResultCalled = false; // if it was called
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPresenter.isViewAttached()) {
            mPresenter.detachView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mPresenter.classifyImage(this, mPhotoFile);
            onActivityResultCalled = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public void updateFaceView(Uri uriPhoto) {
        Picasso.with(this).load(uriPhoto).into(imv_face);
    }

    @Override
    public void updateResult(String result) {
        ctb.setTitle(result);
    }

    @Override
    public void updatePalette(int[] colors) {
        if(colors.length != 0) {
            palette.setVisibility(View.VISIBLE);
            palette.setColors(colors);
        }
    }
}
