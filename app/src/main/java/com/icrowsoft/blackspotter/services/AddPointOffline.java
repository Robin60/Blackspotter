package com.icrowsoft.blackspotter.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

/**
 * Created by teardrops on 7/30/16.
 */
public class AddPointOffline extends IntentService {

    public AddPointOffline() {
        super("AddPointOffline");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get bundle
        Bundle data = intent.getExtras();

        // get point
        MyPointOnMap new_point = data.getParcelable("new_point");

        // insert new points to DB
        new BlackspotDBHandler(getBaseContext()).addMyPointOnMap(new_point, false);

        // send broadcast
        sendBroadcast(new Intent("REFRESH_MARKERS"));
    }
}
