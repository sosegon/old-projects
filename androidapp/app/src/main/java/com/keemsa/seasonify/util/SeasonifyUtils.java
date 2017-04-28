package com.keemsa.seasonify.util;

/**
 * Created by sebastian on 4/27/17.
 */

public class SeasonifyUtils {

    public static String getFileNameNoExtension(String fileName) {
        String noExt = new StringBuilder(fileName).reverse().toString();
        noExt = noExt.substring(4);
        return new StringBuilder(noExt).reverse().toString();
    }
}
