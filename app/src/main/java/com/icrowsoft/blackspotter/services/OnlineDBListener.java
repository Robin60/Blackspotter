package com.icrowsoft.blackspotter.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

/**
 * Created by teardrops on 8/13/16.
 */
public class OnlineDBListener extends Service {
    private DatabaseReference my_db_ref;
    private ChildEventListener childEventListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final DatabaseReference online_DB = FirebaseDatabase.getInstance().getReference();

        my_db_ref = online_DB.child("blackspots");

        // set child event listener
        my_db_ref.child("KE").addChildEventListener(childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                Log.e("Kibet", "Added to KE: " + dataSnapshot);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        // get firebase key
                        String firebase_key = dataSnapshot.getKey();

                        // get country
                        String country = "KE";

//                        // loop through json
//                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        MyPointOnMap my_point = new MyPointOnMap();
                        my_point.setName(dataSnapshot.child("name").getValue().toString());
                        my_point.setLatitude(dataSnapshot.child("latitude").getValue().toString());
                        my_point.setLongitude(dataSnapshot.child("longitude").getValue().toString());
                        my_point.setCases(Integer.parseInt(dataSnapshot.child("cases").getValue().toString()));
                        my_point.setLastModified(dataSnapshot.child("lastModified").getValue().toString());
                        my_point.setCountry(country);
                        my_point.setDescription(dataSnapshot.child("description").getValue().toString());
                        my_point.setCause(dataSnapshot.child("cause").getValue().toString());
                        my_point.setPhoto(dataSnapshot.child("photo").getValue().toString());
                        my_point.setFirebaseKey(firebase_key);
                        new_point.setPostedBy();

                        // insert new points to DB
                        new BlackspotDBHandler(getBaseContext()).addMyPoinOnMap(my_point, false);
//                        }

                        // send broadcast
                        sendBroadcast(new Intent("REFRESH_MARKERS"));
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                Log.e("Kibet", "Changed in KE: " + dataSnapshot);
            }

            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {
                Log.e("Kibet", "Removed from KE: " + dataSnapshot);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {

                        // get country
                        String country = "KE";

                        // get database reference
                        BlackspotDBHandler my_offline_db = new BlackspotDBHandler(getBaseContext());

//                        // truncate DB
//                        my_offline_db.truncateBlackspotsTable();

//                        // loop through json
//                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        // get firebase key
                        String firebase_key = dataSnapshot.getKey();

                        MyPointOnMap my_point = new MyPointOnMap();
                        my_point.setName(dataSnapshot.child("name").getValue().toString());
                        my_point.setLatitude(dataSnapshot.child("latitude").getValue().toString());
                        my_point.setLongitude(dataSnapshot.child("longitude").getValue().toString());
                        my_point.setCases(Integer.parseInt(dataSnapshot.child("cases").getValue().toString()));
                        my_point.setLastModified(dataSnapshot.child("lastModified").getValue().toString());
                        my_point.setCountry(country);
                        my_point.setDescription(dataSnapshot.child("description").getValue().toString());
                        my_point.setCause(dataSnapshot.child("cause").getValue().toString());
                        my_point.setPhoto(dataSnapshot.child("photo").getValue().toString());
                        my_point.setFirebaseKey(firebase_key);
                        new_point.setPostedBy();

                        // insert new points to DB
                        my_offline_db.deletePoint(my_point);
//                        }

                        // send broadcast
                        sendBroadcast(new Intent("REFRESH_MARKERS"));
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        my_db_ref.removeEventListener(childEventListener);
    }
}
