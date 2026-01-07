package com.keemsa.seasonify.features.palettes;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.base.BasePresenter;
import com.keemsa.seasonify.data.DataManager;
import com.keemsa.seasonify.data.model.Palette;
import com.keemsa.seasonify.injection.ConfigPersistent;
import com.keemsa.seasonify.util.SeasonifyImage;

import javax.inject.Inject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by sebastian on 20/07/17.
 */

@ConfigPersistent
public class PalettesPresenter extends BasePresenter<PalettesMvpView> {

    private final DataManager mDataManager;

    @Inject
    public PalettesPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    public FirebaseRecyclerAdapter createAdapter() {

        String userId = mDataManager.getPreferencesHelper().retrieveUserId();
        DatabaseReference palRef = mDataManager.getFirebaseHelper().retrievePalettes(userId);

        return new FirebaseRecyclerAdapter<Palette, PaletteViewHolder>(
                Palette.class,
                R.layout.item_palette,
                PaletteViewHolder.class,
                palRef) {
            @Override
            protected void populateViewHolder(PaletteViewHolder viewHolder, Palette model, int position) {
                int[] colors = SeasonifyImage.colorsAsIntArray(model.getColors());
                String season = model.getSeason();
                viewHolder.setColors(colors);

                if(!model.isFav()){
                    viewHolder.setVisibility(GONE);
                } else {
                    viewHolder.setVisibility(VISIBLE);
                    viewHolder.addDeleteListener((y) -> {
                        mDataManager.getPreferencesHelper().deleteColorPalette(colors);
                        mDataManager
                                .getFirebaseHelper()
                                .processColorPalette(userId, colors, season, false);
                    });
                }
            }
        };

    }
}
