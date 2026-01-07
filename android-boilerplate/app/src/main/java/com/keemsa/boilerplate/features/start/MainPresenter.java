package com.keemsa.boilerplate.features.start;

import com.keemsa.boilerplate.base.BasePresenter;
import com.keemsa.boilerplate.data.DataManager;
import com.keemsa.boilerplate.injection.ConfigPersistent;

import javax.inject.Inject;

@ConfigPersistent
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;

    @Inject
    public MainPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    public void addMessage(String message) {

    }

}
