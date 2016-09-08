package com.icrowsoft.blackspotter.SyncDB;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

/**
 * Created by teardrops on 7/17/16.
 */
public class complete_day_simulator extends AsyncTask<String, String, String> {
    private final Context _context;
    private final Handler _handler;
    private final MyPointOnMap _new_point;
    private DatabaseReference online_DB;

    public complete_day_simulator(Context context, Handler handler, MyPointOnMap new_point) {
        this._context = context;
        this._handler = handler;
        this._new_point = new_point;
    }

    @Override
    protected String doInBackground(String... strings) {
        // get online DB
        online_DB = FirebaseDatabase.getInstance().getReference();

        // preference manager
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);

        // get expiry time
        String scene_expiry_time = settings.getString("point_expiry_time", "180");

        // start reminder
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // delete data
                online_DB.child("blackspots").child("KE").child(_new_point.getFirebaseKey()).removeValue();

                // remove callbacks
                _handler.removeCallbacks(this);

                // send broadcast
                _context.sendBroadcast(new Intent("REFRESH_MARKERS"));

                // delete from local database
                new BlackspotDBHandler(_context).deletePoint(_new_point.getFirebaseKey());
            }
        }, (Integer.parseInt(scene_expiry_time) * 1000));

        return null;
    }
}
