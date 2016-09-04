package com.icrowsoft.blackspotter.general;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by teardrops on 8/6/16.
 */
public class CheckConnection {
    public static boolean is_internet_connected(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null) {
            return false;
        }
        if (!i.isConnected()) {
            return false;
        }
        if (!i.isAvailable()) {
            return false;
        }
        return true;
    }
}
