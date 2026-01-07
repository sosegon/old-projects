package com.keemsa.boilerplate.features.list;

import com.keemsa.boilerplate.base.BasePresenter;
import com.keemsa.boilerplate.data.DataManager;
import com.keemsa.boilerplate.injection.ConfigPersistent;

import javax.inject.Inject;

@ConfigPersistent
public class MessagesPresenter extends BasePresenter<MessagesMvpView> {

    private final DataManager mDataManager;

    @Inject
    public MessagesPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

}
