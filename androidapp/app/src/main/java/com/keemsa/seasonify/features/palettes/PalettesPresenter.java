package com.keemsa.seasonify.features.palettes;

import com.keemsa.seasonify.base.BasePresenter;
import com.keemsa.seasonify.injection.ConfigPersistent;

import javax.inject.Inject;

/**
 * Created by sebastian on 20/07/17.
 */

@ConfigPersistent
public class PalettesPresenter extends BasePresenter<PalettesMvpView> {

    @Inject
    public PalettesPresenter() {
    }
}
