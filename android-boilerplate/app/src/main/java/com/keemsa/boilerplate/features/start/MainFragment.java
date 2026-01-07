package com.keemsa.boilerplate.features.start;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.boilerplate.R;
import com.keemsa.boilerplate.base.BaseFragment;
import com.keemsa.boilerplate.util.RxEventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainFragment extends BaseFragment implements MainMvpView{

    @Inject
    MainPresenter mPresenter;

    @Inject
    RxEventBus mEventBus;

    private ImageView[] selectionIcons;

    @BindView(R.id.txt_name)
    TextView txt_season;

    @BindView(R.id.btn_add)
    Button btn_add;


    @OnClick(R.id.btn_add)
    public void addMessage() {
        mPresenter.addMessage(String.valueOf(txt_season.getText()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, view);

        activityComponent().inject(this);

        mPresenter.attachView(this);

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
        if(mPresenter.isViewAttached()) {
            mPresenter.detachView();
        }
    }
}
