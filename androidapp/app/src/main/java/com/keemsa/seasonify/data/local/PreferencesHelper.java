package com.keemsa.seasonify.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorInt;

import com.keemsa.colorwheel.ColorElement;
import com.keemsa.colorwheel.ColorPickerView;
import com.keemsa.seasonify.injection.ApplicationContext;
import com.keemsa.seasonify.util.SeasonifyImage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "seasonify_pref_file";

    public static final String KEY_PHOTO_PATH = "prf_photo_path";
    public static final String KEY_PREDICTION = "prf_prediction";
    public static final String KEY_SELECTION_TYPE = "prf_selection_type";
    public static final String KEY_COLOR_COORDS = "prf_color_coords";
    public static final String KEY_COLOR_PALETTE = "prf_color_palette";
    public static final String KEY_USER_ID = "prf_user_id";
    public static final String KEY_PREDICTION_ID = "prf_prediction_id";

    private final SharedPreferences mPref;

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

    public String retrieveUserId() {
        return mPref.getString(KEY_USER_ID, "");
    }

    public void storeUserId(String userId) {
        mPref.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String retrievePredictionId() {
        return mPref.getString(KEY_PREDICTION_ID, "");
    }

    public void storePredictionId(String predictionId) {
        mPref.edit().putString(KEY_PREDICTION_ID, predictionId).apply();
    }

    public String retrievePhotoPath() {
        return mPref.getString(KEY_PHOTO_PATH, "");
    }

    public void storePhotoPath(String path) {
        mPref.edit().putString(KEY_PHOTO_PATH, path).apply();
    }

    public String retrievePrediction() {
        return mPref.getString(KEY_PREDICTION, "");
    }

    public void storePrediction(String prediction) {
        mPref.edit().putString(KEY_PREDICTION, prediction).apply();
    }

    public int retrieveColorSelectionType() {
        return mPref.getInt(KEY_SELECTION_TYPE, 0);
    }

    public int storeColorSelectionType(ColorPickerView.COLOR_SELECTION colorSelection) {
        int index = ColorPickerView.COLOR_SELECTION.indexOf(colorSelection);
        mPref.edit().putInt(KEY_SELECTION_TYPE, index).apply();
        return index;
    }

    public float[] retrieveSelectedColorCoords() {
        String coords = mPref.getString(KEY_COLOR_COORDS, "0;0");
        StringTokenizer st = new StringTokenizer(coords, ";");
        float x = Float.parseFloat(st.nextToken());
        float y = Float.parseFloat(st.nextToken());

        return new float[]{x, y};
    }

    public void storeSelectedColorCoords(List<ColorElement> colors) {
        try {
            ColorElement main = colors.get(0);
            float x = main.getX();
            float y = main.getY();
            String coords = String.valueOf(x) + ";" + String.valueOf(y);
            mPref.edit().putString(KEY_COLOR_COORDS, coords).apply();
        } catch (IndexOutOfBoundsException e) {
            Timber.e(e.getMessage());
        }
    }

    public List<int[]> retrieveColorPalette() {
        Set<String> sPalette = mPref.getStringSet(KEY_COLOR_PALETTE, null);
        List<int[]> listPalettes = new ArrayList<>();

        if(sPalette !=  null) {
            Iterator iter = sPalette.iterator();
            while(iter.hasNext()) {
                String sCurrentPalette = (String) iter.next();
                StringTokenizer st = new StringTokenizer(sCurrentPalette, ";");
                int[] iCurrentPalette = new int[st.countTokens()];
                int i = 0;
                while(st.hasMoreTokens()) {
                    iCurrentPalette[i] = Integer.valueOf(st.nextToken());
                }
                listPalettes.add(iCurrentPalette);
            }
        }

        return listPalettes;
    }

    // colors has to be sorted
    public void addColorPalette(@ColorInt int[] colors) {
        Set<String> sPalette = mPref.getStringSet(KEY_COLOR_PALETTE, null);

        if(sPalette == null) {
            sPalette = new HashSet<>();
        }

        String sPalet = SeasonifyImage.colorsAsString(colors);
        sPalette.add(sPalet);

        mPref.edit().putStringSet(KEY_COLOR_PALETTE, sPalette).apply();
    }

    // colors has to be sorted
    public boolean hasColorPalette(int[] colors) {
        Set<String> sPalette = mPref.getStringSet(KEY_COLOR_PALETTE, null);

        if(sPalette != null) {
            String sPalet = SeasonifyImage.colorsAsString(colors);

            return sPalette.contains(sPalet);
        }

        return false;
    }

    // colors has to be sorted
    public boolean deleteColorPalette(int[] colors) {
        Set<String> sPalette = mPref.getStringSet(KEY_COLOR_PALETTE, null);

        if(sPalette != null) {
            String sPalet = SeasonifyImage.colorsAsString(colors);

            int originalCount = sPalette.size();
            Iterator iter = sPalette.iterator();
            while(iter.hasNext()) {
                String sCurrentPalette = (String) iter.next();

                if(sPalet.equals(sCurrentPalette)) {
                    iter.remove();
                    break;
                }
            }
            int finalCount = sPalette.size();

            if(finalCount < originalCount) {
                mPref.edit().putStringSet(KEY_COLOR_PALETTE, sPalette).apply();
                return true;
            }
        }

        return false;
    }

    // colors has to be sorted
    public void processColorPalette(int[] colors) {
        if(hasColorPalette(colors)) {
            deleteColorPalette(colors);
        } else {
            addColorPalette(colors);
        }
    }

}
