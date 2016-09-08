package com.icrowsoft.blackspotter.SyncDB;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.icrowsoft.blackspotter.general.AddPointToOnlineDB;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import java.util.List;

/**
 * Created by teardrops on 7/17/16.
 */
public class sync_DB_online extends AsyncTask<String, String, String> {
    private final Context _context;
    private final Activity _activity;
    private final GoogleMap _map;
    private final View _fab;
    private final Handler _handler;
    private DatabaseReference my_db_ref;

    public sync_DB_online(Context context, Handler handler, Activity activity, GoogleMap map, View view) {
        _context = context;
        _activity = activity;
        _map = map;
        _fab = view;
        _handler = handler;
    }

    @Override
    protected String doInBackground(String... strings) {

        final DatabaseReference online_DB = FirebaseDatabase.getInstance().getReference();

        my_db_ref = online_DB.child("blackspots");

        my_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // check if data is already online
                if (!dataSnapshot.exists()) {
                    // get database reference
                    BlackspotDBHandler my_db = new BlackspotDBHandler(_context);

                    // fetch all points
                    List<MyPointOnMap> all_map_points = my_db.getAllPoints();
                    Log.i("Kibet", "All points: " + all_map_points.size());

                    for (final MyPointOnMap my_point : all_map_points) {
                        // fetch country and save online
                        new AddPointToOnlineDB(_context, _handler, _activity, _map, _fab).add_this_point(my_point);
                    }
                } else {
                    Log.i("Kibet", "-- Data already in DB");// TODO: 7/29/16 delete
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return null;
    }
}
