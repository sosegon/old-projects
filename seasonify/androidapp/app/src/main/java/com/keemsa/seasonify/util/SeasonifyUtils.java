package com.keemsa.seasonify.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by sebastian on 4/27/17.
 */

public class SeasonifyUtils {

    public static String getFileNameNoExtension(String fileName) {
        String noExt = new StringBuilder(fileName).reverse().toString();
        noExt = noExt.substring(4);
        return new StringBuilder(noExt).reverse().toString();
    }

    public static Uri generateUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                "com.keemsa.seasonify.fileprovider",
                file
        );
    }
}
