package com.keemsa.seasonify.features.start;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.keemsa.colorpalette.ColorPalette;
import com.keemsa.colorwheel.ColorElement;
import com.keemsa.colorwheel.OnCenterSelectedListener;
import com.keemsa.colorwheel.OnColorsChangedListener;
import com.keemsa.colorwheel.OnColorsSelectedListener;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.features.about.AboutActivity;
import com.keemsa.colorwheel.ColorPickerView;
import com.keemsa.seasonify.util.SeasonifyImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    private ImageView[] selectionIcons;

    @BindView(R.id.txt_season)
    TextView txt_season;

    @BindView(R.id.color_wheel)
    ColorPickerView color_wheel;

    @BindView(R.id.tb)
    Toolbar tb;

    @BindView(R.id.plt_combination)
    ColorPalette plt_combination;

    @BindView(R.id.imv_single_sel)
    ImageView imv_single_sel;

    @BindView(R.id.imv_complementary_sel)
    ImageView imv_complementary_sel;

    @BindView(R.id.imv_triad_sel)
    ImageView imv_triad_sel;

    @BindView(R.id.imv_analogous_sel)
    ImageView imv_analogous_sel;

    @BindView(R.id.imv_quad_sel)
    ImageView imv_quad_sel;

    @BindView(R.id.ll_main)
    FrameLayout ll_main;

    @BindView(R.id.ll_just_started)
    LinearLayout ll_just_started;

    AnimatedVectorDrawable avd_single, avd_complementary, avd_triad, avd_analogous, avd_quad;

    @OnClick(R.id.imv_camera)
    public void launchCamera() {
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

        initLayoutElements();

        initAd();

        initColorWheel();

        initAnimatables();  // before initColorSelection() to make use of them

        initColorSelection();
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
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share, mPresenter.getStoredPrediction(this)));
                shareIntent.setType("text/plain");

                startActivity(Intent.createChooser(shareIntent, getString(R.string.lbl_share)));

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateResult(String result) {
        try {
            String upperString = result.substring(0, 1).toUpperCase() + result.substring(1);
            txt_season.setText(upperString);
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(LOG_TAG, e.getMessage());
            txt_season.setText(result);
        }
    }

    @Override
    public void updateColorWheel(@NonNull final int[] colors, @NonNull Bitmap bitmap) {

        color_wheel.updateColors(colors);
        color_wheel.updateCenter(bitmap);

        ll_just_started.setVisibility(View.GONE);
        ll_main.setVisibility(View.VISIBLE);

        float[] coords = mPresenter.getStoredSelectedColorCoords(this);
        color_wheel.selectColors(coords[0], coords[1]);
        updateColorsPalette(color_wheel.getCurrentColorElements());
    }

    @Override
    public void updateColorSelection(int index) {
        for(ImageView selector : selectionIcons) {
            selector.setSelected(false);
        }

        try {
            selectionIcons[index].setSelected(true);
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private void initColorWheel() {

        int indexSelection = mPresenter.getStoredColorSelectionType(this);
        color_wheel.setColorSelection(ColorPickerView.COLOR_SELECTION.indexOf(indexSelection));

        // Add a tree observer so the color wheel can
        // be updated once is ready, that is all its
        // dimensions are available
        color_wheel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                color_wheel.getViewTreeObserver().removeOnGlobalLayoutListener(this); // avoid more than one call
                if(!onActivityResultCalled) {
                    mPresenter.loadStoredPrediction(MainActivity.this);
                }
            }
        });

        color_wheel.addOnColorsChangedListener(new OnColorsChangedListener() {
            @Override
            public void onColorsChanged(List<ColorElement> colors) {
                updateColorsPalette(colors);
            }
        });

        color_wheel.addOnCenterSelectedListener(new OnCenterSelectedListener() {
            @Override
            public void onCenterSelected() {
                launchCamera();
            }
        });

        color_wheel.addOnColorsSelectedListener(new OnColorsSelectedListener() {
            @Override
            public void onColorsSelected(List<ColorElement> colors) {
                updateColorsPalette(colors);
                mPresenter.storeSelectedColorCoords(MainActivity.this, colors);
            }
        });

    }

    private void initColorSelection() {
        imv_single_sel.setOnTouchListener(createColorSelectionListener(ColorPickerView.COLOR_SELECTION.SINGLE, avd_single));
        imv_complementary_sel.setOnTouchListener(createColorSelectionListener(ColorPickerView.COLOR_SELECTION.COMPLEMENTARY, avd_complementary));
        imv_triad_sel.setOnTouchListener(createColorSelectionListener(ColorPickerView.COLOR_SELECTION.TRIAD, avd_triad));
        imv_analogous_sel.setOnTouchListener(createColorSelectionListener(ColorPickerView.COLOR_SELECTION.ANALOGOUS, avd_analogous));
        imv_quad_sel.setOnTouchListener(createColorSelectionListener(ColorPickerView.COLOR_SELECTION.SQUARE, avd_quad));

        selectionIcons = new ImageView[]{
                imv_single_sel,
                imv_complementary_sel,
                imv_triad_sel,
                imv_analogous_sel,
                imv_quad_sel
        };
    }

    private void initLayoutElements() {
        if(mPresenter.hasStoredPrediction(this)) {
            ll_just_started.setVisibility(View.GONE);
            ll_main.setVisibility(View.VISIBLE);
        } else {
            ll_just_started.setVisibility(View.VISIBLE);
            ll_main.setVisibility(View.GONE);
        }
    }

    private void initAnimatables() {
        avd_single        = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_anim_sel_single);
        avd_complementary = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_anim_sel_complementary);
        avd_triad         = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_anim_sel_triad);
        avd_analogous     = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_anim_sel_analogous);
        avd_quad          = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_anim_sel_quad);
    }

    private void updateColorsPalette(List<ColorElement> colors) {
        int[] numColors = new int[colors.size()];

        for(int i = 0; i < colors.size(); i++) {
            numColors[i] = colors.get(i).getColor();
        }

        plt_combination.setColors(numColors);

        plt_combination.setVisibility(View.VISIBLE);
    }

    private View.OnTouchListener createColorSelectionListener(final ColorPickerView.COLOR_SELECTION colorSelection, final AnimatedVectorDrawable anim) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                color_wheel.setColorSelection(colorSelection);
                updateColorsPalette(color_wheel.getCurrentColorElements());
                int index = mPresenter.storeColorSelectionType(MainActivity.this, colorSelection);
                Log.e(LOG_TAG, "selection type: " + index);
                updateColorSelection(index);
                ((ImageView) v).setImageDrawable(anim);
                anim.start();

                return false;
            }
        };
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