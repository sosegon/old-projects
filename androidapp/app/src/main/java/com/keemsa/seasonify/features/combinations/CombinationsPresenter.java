package com.keemsa.seasonify.features.combinations;

import com.keemsa.seasonify.base.BasePresenter;
import com.keemsa.seasonify.injection.ConfigPersistent;

import javax.inject.Inject;

/**
 * Created by sebastian on 20/07/17.
 */

@ConfigPersistent
public class CombinationsPresenter extends BasePresenter<CombinationsMvpView> {

    @Inject
    public CombinationsPresenter() {
    }
}
