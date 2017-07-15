package com.keemsa.seasonify.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorInt;
import android.util.Log;

import com.keemsa.colorwheel.ColorElement;
import com.keemsa.colorwheel.ColorPickerView;
import com.keemsa.seasonify.injection.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static final String KEY_COLOR_COMBINATIONS = "prf_color_combinations";

    private final SharedPreferences mPref;

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
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

    public List<int[]> retrieveColorCombinations() {
        Set<String> sCombinations = mPref.getStringSet(KEY_COLOR_COMBINATIONS, null);
        List<int[]> listCombs = new ArrayList<>();

        if(sCombinations !=  null) {
            Iterator iter = sCombinations.iterator();
            while(iter.hasNext()) {
                String sCurrentComb = (String) iter.next();
                StringTokenizer st = new StringTokenizer(sCurrentComb, ";");
                int[] iCurrentComb = new int[st.countTokens()];
                int i = 0;
                while(st.hasMoreTokens()) {
                    iCurrentComb[i] = Integer.valueOf(st.nextToken());
                }
                listCombs.add(iCurrentComb);
            }
        }

        return listCombs;
    }

    public void addColorCombination(@ColorInt int[] colors) {
        int[] iComb = Arrays.copyOf(colors, colors.length); // copy to avoid problems in the palette
        Arrays.sort(iComb); // sort so when converting to string combinations are not repeated
        Set<String> sCombinations;
        sCombinations = mPref.getStringSet(KEY_COLOR_COMBINATIONS, null);

        if(sCombinations == null) {
            sCombinations = new HashSet<>();
        }

        String sComb = "";
        for(int color : iComb) {
            sComb += String.valueOf(color) + ";";
        }
        sComb = sComb.substring(0, sComb.length() - 1);
        sCombinations.add(sComb);

        mPref.edit().putStringSet(KEY_COLOR_COMBINATIONS, sCombinations).apply();
    }

    public boolean hasColorCombination(int[] colors) {
        Set<String> sCombinations = mPref.getStringSet(KEY_COLOR_COMBINATIONS, null);

        if(sCombinations != null) {

            int[] iComb = Arrays.copyOf(colors, colors.length); // copy to avoid problems in the palette
            Arrays.sort(iComb); // sort so when converting to string combinations are not repeated

            String sComb = "";
            for(int color : iComb) {
                sComb += String.valueOf(color) + ";";
            }
            sComb = sComb.substring(0, sComb.length() - 1);

            return sCombinations.contains(sComb);
        }

        return false;
    }

    public boolean deleteColorCombination(int[] colors) {
        Set<String> sCombinations = mPref.getStringSet(KEY_COLOR_COMBINATIONS, null);

        if(sCombinations != null) {
            int[] iComb = Arrays.copyOf(colors, colors.length); // copy to avoid problems in the palette
            Arrays.sort(iComb);

            String sComb = "";
            for(int color : iComb) {
                sComb += String.valueOf(color) + ";";
            }
            sComb = sComb.substring(0, sComb.length() - 1);

            int originalCount = sCombinations.size();
            Iterator iter = sCombinations.iterator();
            while(iter.hasNext()) {
                String sCurrentComb = (String) iter.next();

                if(sComb.equals(sCurrentComb)) {
                    iter.remove();
                    break;
                }
            }
            int finalCount = sCombinations.size();

            if(finalCount < originalCount) {
                mPref.edit().putStringSet(KEY_COLOR_COMBINATIONS, sCombinations).apply();
                return true;
            }
        }

        return false;
    }

}
