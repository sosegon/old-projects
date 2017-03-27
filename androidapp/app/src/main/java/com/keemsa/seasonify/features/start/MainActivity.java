package com.keemsa.seasonify.features.start;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.seasonify.R;
import com.squareup.picasso.Picasso;

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
    private Uri mUriPhoto;
    private File mPhotoFile;

    private MainPresenter mPresenter;

    @BindView(R.id.imv_face)
    ImageView imv_face;

    @BindView(R.id.txt_results)
    TextView txt_results;

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
                mUriPhoto = FileProvider.getUriForFile(
                        this,
                        "com.keemsa.seasonify.fileprovider",
                        mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhoto);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (mUriPhoto == null && savedInstanceState.getString("uri_file_path") != null) {
                mUriPhoto = Uri.parse(savedInstanceState.getString("uri_file_path"));
            }
        }

        mPresenter = new MainPresenter();
        mPresenter.initTensorFlowAndLoadModel(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mPresenter.classifyImage(this, mPhotoFile, mUriPhoto);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mUriPhoto != null)
            outState.putString("uri_file_path", mUriPhoto.toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    public void updateFaceView() {
        Picasso.with(this).load(mUriPhoto).into(imv_face);
    }

    @Override
    public void updateResult(String result) {
        txt_results.setText(result);
    }
}
