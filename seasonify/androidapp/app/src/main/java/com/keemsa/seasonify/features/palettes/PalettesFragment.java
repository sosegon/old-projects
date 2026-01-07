package com.keemsa.seasonify.features.palettes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keemsa.seasonify.R;
import com.keemsa.seasonify.base.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sebastian on 20/07/17.
 */

public class PalettesFragment extends BaseFragment implements PalettesMvpView {

    @Inject
    PalettesPresenter mPresenter;

    @BindView(R.id.rcv_palettes)
    RecyclerView rcv_palettes;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_palettes, container, false);

        ButterKnife.bind(this, view);

        activityComponent().inject(this);

        mPresenter.attachView(this);

        rcv_palettes.setLayoutManager(new LinearLayoutManager(getContext()));

        rcv_palettes.setAdapter(mPresenter.createAdapter());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mPresenter.isViewAttached()) {
            mPresenter.attachView(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter.isViewAttached()) {
            mPresenter.detachView();
        }
    }

}
