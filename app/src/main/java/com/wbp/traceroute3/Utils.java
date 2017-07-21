package com.wbp.traceroute3;

import android.support.compat.BuildConfig;
import android.util.Log;

/**
 * Created by wbp on 2017/7/21.
 */

public class Utils {
    private static final String APP_TAG = "TraceRoute";

    public static void log(String content) {
        if (BuildConfig.DEBUG) {
            content = Thread.currentThread().getName() + " Thread >> " + content;
            Log.d(APP_TAG, content);
        }
    }
}
