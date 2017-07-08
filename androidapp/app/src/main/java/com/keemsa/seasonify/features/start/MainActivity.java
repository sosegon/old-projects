package com.keemsa.seasonify.features.start;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.features.about.AboutActivity;
import com.keemsa.seasonify.features.colorwheel.ColorPickerView;
import com.keemsa.seasonify.util.SeasonifyImage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainMvpView {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean onActivityResultCalled = false;
    private File mPhotoFile;
    private InterstitialAd mInterstitialAd;

    private MainPresenter mPresenter;

    @BindView(R.id.txt_season)
    TextView txt_season;

    @BindView(R.id.color_wheel)
    ColorPickerView color_wheel;

    @BindView(R.id.tb)
    Toolbar tb;

    @OnClick(R.id.fab_scan)
    public void start_camera(){
        if(mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            displayCamera();
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

        initAd();

        // Add a tree observer so the color wheel can
        // be updated once is ready, that is all its
        // dimensions are available
        color_wheel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                color_wheel.getViewTreeObserver().removeOnGlobalLayoutListener(this); // avoid more than one call
                if(!onActivityResultCalled) {
                    mPresenter.loadPreviousResults(MainActivity.this);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mPresenter.isViewAttached()) {
            mPresenter.attachView(this);
        }
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
            SeasonifyImage.addImageToGallery(this, mPhotoFile.getAbsolutePath());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.act_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.act_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                // TODO: Get the season correctly
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share, txt_season.getText()));
                shareIntent.setType("text/plain");

                startActivity(Intent.createChooser(shareIntent, getString(R.string.lbl_share)));

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateResult(String result) {
        txt_season.setText(result);
    }

    @Override
    public void updateColorWheel(final int[] colors, Bitmap bitmap) {

        if(colors.length != 0) {
            color_wheel.updateColors(colors);
        }

        if(bitmap != null){
            color_wheel.updateCenter(bitmap);
        }

    }

    private void initAd() {
        MobileAds.initialize(this, getString(R.string.id_admob));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.id_adunit));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                displayCamera();
            }
        });
        AdRequest ar = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(ar);
    }

    private void displayCamera() {
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
}