package com.keemsa.seasonify.data;

import android.graphics.Bitmap;

import com.keemsa.colorwheel.ColorElement;
import com.keemsa.seasonify.data.local.PreferencesHelper;
import com.keemsa.seasonify.data.remote.FirebaseHelper;
import com.keemsa.seasonify.util.RxEvent;
import com.keemsa.seasonify.util.RxEventBus;
import com.keemsa.seasonify.util.SeasonifyImage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_COMBINATION_LIKED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_COMBINATION_UPDATED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_COORDS_SELECTED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_SELECTION_SELECTED;
import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_SELECTION_UPDATED;


/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class DataManager {

    private final PreferencesHelper mPreferencesHelper;
    private final FirebaseHelper mFirebaseHelper;
    private final RxEventBus mEventBus;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper, RxEventBus eventBus) {
        mPreferencesHelper = preferencesHelper;
        mFirebaseHelper = new FirebaseHelper(); // Does not depend on anything
        mEventBus = eventBus;
        initBus();
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public FirebaseHelper getFirebaseHelper() {
        return mFirebaseHelper;
    }

    public Observable<Bitmap> loadImage(String path, int imageWidth, int imageHeight) {
        return Observable.fromCallable(() -> SeasonifyImage.retrieveFromFile(path, imageWidth, imageHeight))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Bitmap> classifyImage(String path, int imageSize) {

        return Observable.fromCallable(() -> SeasonifyImage.retrieveFromFileToClassify(path, imageSize))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

    }

    private void initBus() {
        // Favourite color
        Consumer<Object> favouriteColor = (y) -> {
            if(((RxEvent)y).getType() == COLOR_COMBINATION_LIKED) {
                try {
                    int[] colors = (int[])(((RxEvent) y).getArgument());
                    mPreferencesHelper.processColorCombination(colors);
                    mEventBus.post(new RxEvent(COLOR_COMBINATION_UPDATED, mPreferencesHelper.hasColorCombination(colors)));
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        };
        mEventBus.observable().subscribe(favouriteColor);

        // Color coords
        Consumer<Object> colorCoords = (y) -> {
            if(((RxEvent)y).getType() == COLOR_COORDS_SELECTED) {
                try {
                    List<ColorElement> colors = (List<ColorElement>)(((RxEvent) y).getArgument());
                    mPreferencesHelper.storeSelectedColorCoords(colors);
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        } ;
        mEventBus.observable().subscribe(colorCoords);

        // Selection type
        Consumer<Object> colorSelection = (y) -> {
            if(((RxEvent)y).getType() == COLOR_SELECTION_SELECTED) {
                try {
                    COLOR_SELECTION selection = (COLOR_SELECTION) (((RxEvent) y).getArgument());
                    int index = mPreferencesHelper.storeColorSelectionType(selection);
                    mEventBus.post(new RxEvent(COLOR_SELECTION_UPDATED, index));
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        } ;
        mEventBus.observable().subscribe(colorSelection);
    }
}
