package com.keemsa.seasonify.features.start;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.keemsa.colorpalette.ColorPalette;
import com.keemsa.colorwheel.ColorElement;
import com.keemsa.colorwheel.ColorPickerView;
import com.keemsa.seasonify.BuildConfig;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.base.BaseActivity;
import com.keemsa.seasonify.features.about.AboutActivity;
import com.keemsa.seasonify.util.RxEvent;
import com.keemsa.seasonify.util.RxEventBus;
import com.keemsa.seasonify.util.SeasonifyImage;
import com.keemsa.seasonify.util.SeasonifyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION;
import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION.ANALOGOUS;
import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION.COMPLEMENTARY;
import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION.SINGLE;
import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION.SQUARE;
import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION.TRIAD;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_CHANGED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_COMBINATION_LIKED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_COMBINATION_UPDATED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_COORDS_SELECTED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_SELECTION_SELECTED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_SELECTION_UPDATED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.PREDICTION_CHANGED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.PROCESSING_ENDED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.PROCESSING_STARTED;

public class MainActivity extends BaseActivity implements MainMvpView {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int IMAGE_CAPTURE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private boolean onActivityResultCalled = false;
    private File mPhotoFile;
    private InterstitialAd mInterstitialAd;
    private SharedPreferences permissionStatus;

    @Inject
    MainPresenter mPresenter;

    @Inject
    RxEventBus mEventBus;

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

    @BindView(R.id.imv_fav)
    ImageView imv_fav;

    @BindView(R.id.pgr)
    ProgressBar pgr;

    @BindView(R.id.vfl)
    ViewFlipper vfl;

    @BindArray(R.array.autumn_colors)
    int[] autumn_colors;

    @BindArray(R.array.spring_colors)
    int[] spring_colors;

    @BindArray(R.array.summer_colors)
    int[] summer_colors;

    @BindArray(R.array.winter_colors)
    int[] winter_colors;

    AnimatedVectorDrawable avd_single, avd_complementary, avd_triad, avd_analogous, avd_quad;

    @OnClick(R.id.imv_camera)
    public void launchCamera() {
        if(mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            displayCamera();
        }
    }

    @OnClick(R.id.imv_fav)
    public void favCombination() {
        mEventBus.post(new RxEvent(COLOR_COMBINATION_LIKED, plt_combination.getColors()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPresenter.attachView(this);

        setSupportActionBar(tb);

        initLayoutElements();

        initAd();

        initColorWheel();

        initPalette();

        initFav();

        initAnimatables();  // before initColorSelection() to make use of them

        initColorSelection();

        initPgr();

        initVfl();
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

            mEventBus.post(new RxEvent(PROCESSING_STARTED));
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
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share, mPresenter.getStoredPrediction()));
                shareIntent.setType("text/plain");

                startActivity(Intent.createChooser(shareIntent, getString(R.string.lbl_share)));

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updatePrediction(@NonNull String prediction, @NonNull Bitmap bitmap, @NonNull boolean isNewPrediction) {
        try {
            if(isNewPrediction) {
                mEventBus.post(new RxEvent(PREDICTION_CHANGED, prediction));
            }

            String upperString = prediction.substring(0, 1).toUpperCase() + prediction.substring(1);
            txt_season.setText(upperString);

            int[] colors = getSeasonalColors(prediction);
            color_wheel.updateColors(colors);
            color_wheel.updateCenter(bitmap);

            float[] coords = mPresenter.getStoredSelectedColorCoords();
            color_wheel.selectColors(coords[0], coords[1]);

            mEventBus.post(new RxEvent(COLOR_CHANGED, color_wheel.getCurrentColorElements()));

            ll_just_started.setVisibility(View.GONE);
            ll_main.setVisibility(View.VISIBLE);
        } catch (StringIndexOutOfBoundsException e) {
            Timber.e(e.getMessage());
        }
    }

    @Override
    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void endProcessing() {
        mEventBus.post(new RxEvent(PROCESSING_ENDED));
    }

    private void updateColorSelection(int index) {
        for(ImageView selector : selectionIcons) {
            selector.setSelected(false);
        }

        try {
            selectionIcons[index].setSelected(true);
        } catch (IndexOutOfBoundsException e) {
            Timber.e(e.getMessage());
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

    private void initVfl() {
        Consumer<Object> vflConsumer = (y) ->
        {
            if(((RxEvent)y).getType() == PROCESSING_STARTED) {
                vfl.setVisibility(View.GONE);
            } else if (((RxEvent)y).getType() == PROCESSING_ENDED) {
                vfl.setVisibility(View.VISIBLE);
            }
        };
        mEventBus.observable().subscribe(vflConsumer);
    }

    private void initPgr(){
        Consumer<Object> pgrConsumer = (y) ->
        {
            if(((RxEvent)y).getType() == PROCESSING_STARTED) {
                pgr.setVisibility(View.VISIBLE);
            } else if (((RxEvent)y).getType() == PROCESSING_ENDED) {
                pgr.setVisibility(View.GONE);
            }
        };
        mEventBus.observable().subscribe(pgrConsumer);
    }

    private void initColorWheel() {

        int indexSelection = mPresenter.getStoredColorSelectionType();
        color_wheel.setColorSelection(ColorPickerView.COLOR_SELECTION.indexOf(indexSelection));

        // Add a tree observer so the color wheel can
        // be updated once is ready, that is all its
        // dimensions are available
        color_wheel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                color_wheel.getViewTreeObserver().removeOnGlobalLayoutListener(this); // avoid more than one call
                if(!onActivityResultCalled) {
                    mPresenter.loadSavedPhoto();
                }
            }
        });

        color_wheel.addOnColorsChangedListener((colors) -> mEventBus.post(new RxEvent(COLOR_CHANGED, colors)));
        color_wheel.addOnCenterSelectedListener(() -> launchCamera() );
        color_wheel.addOnColorsSelectedListener((colors) -> mEventBus.post(new RxEvent(COLOR_COORDS_SELECTED, colors)));

        Consumer<Object> colorWheelConsumer = (y) ->
        {
            if(((RxEvent)y).getType() == COLOR_SELECTION_UPDATED) {
                try {
                    int index = (int)(((RxEvent) y).getArgument());
                    color_wheel.setColorSelection(COLOR_SELECTION.indexOf(index));
                    List<ColorElement> colors = color_wheel.getCurrentColorElements();
                    mEventBus.post(new RxEvent(COLOR_CHANGED, colors));
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        };
        mEventBus.observable().subscribe(colorWheelConsumer);
    }

    private void initPalette() {
        Consumer<Object> paletteConsumer = (y) ->
        {
            if(((RxEvent)y).getType() == COLOR_CHANGED) {
                try {
                    List<ColorElement> colors = (List<ColorElement>)(((RxEvent) y).getArgument());
                    updateColorsPalette(colors);
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        };
        mEventBus.observable().subscribe(paletteConsumer);
    }

    private void initFav() { // TODO update fav at start
        Consumer<Object> favConsumer = (y) ->
        {
            if(((RxEvent)y).getType() == COLOR_COMBINATION_UPDATED) {
                try {
                    boolean isFav = (boolean)(((RxEvent) y).getArgument());
                    imv_fav.setSelected(isFav);
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        };
        mEventBus.observable().subscribe(favConsumer);
    }

    private void initColorSelection() {
        imv_single_sel.setOnClickListener(v -> clickColorSelection(SINGLE));
        imv_complementary_sel.setOnClickListener(v -> clickColorSelection(COMPLEMENTARY));
        imv_triad_sel.setOnClickListener(v -> clickColorSelection(TRIAD));
        imv_analogous_sel.setOnClickListener(v -> clickColorSelection(ANALOGOUS));
        imv_quad_sel.setOnClickListener(v -> clickColorSelection(SQUARE));

        selectionIcons = new ImageView[]{
                imv_single_sel,
                imv_complementary_sel,
                imv_triad_sel,
                imv_analogous_sel,
                imv_quad_sel
        };

        Consumer<Object> selectionConsumer = (y) ->
        {
            if(((RxEvent)y).getType() == COLOR_SELECTION_UPDATED) {
                try {
                    int index = (int)(((RxEvent) y).getArgument());
                    updateColorSelection(index);
                    animateSelection(index);
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        };
        mEventBus.observable().subscribe(selectionConsumer);

        updateColorSelection(mPresenter.getStoredColorSelectionType());
    }

    private void initLayoutElements() {
        if(mPresenter.hasStoredPrediction()) {
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

        boolean existCombination = mPresenter.existColorCombination(numColors);
        imv_fav.setSelected(existCombination);
    }

    private boolean clickColorSelection(ColorPickerView.COLOR_SELECTION colorSelection) {
        mEventBus.post(new RxEvent(COLOR_SELECTION_SELECTED, colorSelection));
        return false;
    }

    private void animateSelection(int index) {
        ImageView imv = null;
        AnimatedVectorDrawable avd = null;

        switch (index) {
            case 0:
                imv = imv_single_sel;
                avd = avd_single;
                break;
            case 1:
                imv = imv_complementary_sel;
                avd = avd_complementary;
                break;
            case 2:
                imv = imv_triad_sel;
                avd = avd_triad;
                break;
            case 3:
                imv = imv_analogous_sel;
                avd = avd_analogous;
                break;
            case 4:
                imv = imv_quad_sel;
                avd = avd_quad;
                break;
        }

        try {
            imv.setImageDrawable(avd);
            avd.start();
        } catch(ExceptionInInitializerError e) {
            Timber.e(e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == IMAGE_CAPTURE_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayCameraWithPermission();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    requestPermissionWithMessage();
                } else {
                    Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void displayCamera() {
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(BuildConfig.DEBUG) {
            cameraPermission = -1;
        }

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                requestPermissionWithMessage();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, IMAGE_CAPTURE_PERMISSION_CONSTANT);
            }
        } else {
            displayCameraWithPermission();
        }
    }

    private void requestPermissionWithMessage() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.lbl_permission))
                .setMessage(getString(R.string.msg_seasonify_camera_permission))
                .setPositiveButton(R.string.lbl_grant, (d, i) -> {
                    d.cancel();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, IMAGE_CAPTURE_PERMISSION_CONSTANT);
                })
                .setNegativeButton(R.string.lbl_cancel, (d, i) -> {
                    d.cancel();
                })
                .show();
    }
    private void displayCameraWithPermission() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                mPhotoFile = SeasonifyImage.createImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (mPhotoFile != null) {
                Uri uriPhoto = SeasonifyUtils.generateUri(this, mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPhoto);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private int[] getSeasonalColors(String season) {
        if (season.equals("autumn")) {
            return autumn_colors;
        } else if (season.equals("spring")) {
            return spring_colors;
        } else if (season.equals("summer")) {
            return summer_colors;
        } else if (season.equals("winter")) {
            return winter_colors;
        }

        return new int[]{};
    }

}