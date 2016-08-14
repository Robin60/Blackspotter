package com.icrowsoft.blackspotter.services;

import android.app.Service;
import android.content.Intent;
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

        // create child event listener
        create_childEventListener();

        // set child event listener
        my_db_ref.addChildEventListener(childEventListener);

        return super.onStartCommand(intent, flags, startId);
    }

    private void create_childEventListener() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i("Kibet", "Added: " + s + " --Snapshot: " + dataSnapshot);
                // get country
                String country = dataSnapshot.getKey();

                // loop through json
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    String key = postSnapshot.getKey();

                    MyPointOnMap my_point = new MyPointOnMap();
                    my_point.setName(postSnapshot.child("name").getValue().toString());
                    my_point.setLatitude(postSnapshot.child("latitude").getValue().toString());
                    my_point.setLongitude(postSnapshot.child("longitude").getValue().toString());
                    my_point.setCases(Integer.parseInt(postSnapshot.child("cases").getValue().toString()));
                    my_point.setLastModified(postSnapshot.child("lastModified").getValue().toString());
                    my_point.setCountry(country);
                    my_point.setDescription(postSnapshot.child("description").getValue().toString());

                    // insert new points to DB
                    insert_into_table(my_point);

                    // send broadcast
                    sendBroadcast(new Intent("REFRESH_MARKERS"));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // get country
                String country = dataSnapshot.getKey();

                // loop through json
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    String key = postSnapshot.getKey();

                    MyPointOnMap my_point = new MyPointOnMap();
                    my_point.setName(postSnapshot.child("name").getValue().toString());
                    my_point.setLatitude(postSnapshot.child("latitude").getValue().toString());
                    my_point.setLongitude(postSnapshot.child("longitude").getValue().toString());
                    my_point.setCases(Integer.parseInt(postSnapshot.child("cases").getValue().toString()));
                    my_point.setLastModified(postSnapshot.child("lastModified").getValue().toString());
                    my_point.setCountry(country);
                    my_point.setDescription(postSnapshot.child("description").getValue().toString());

                    // insert new points to DB
                    insert_into_table(my_point);

                    // send broadcast
                    sendBroadcast(new Intent("REFRESH_MARKERS"));
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i("Kibet", "Removed: " + dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.i("Kibet", "Moved: " + s + " --Snapshot: " + dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            public void insert_into_table(MyPointOnMap my_point) {
                // get database reference
                BlackspotDBHandler my_offline_db = new BlackspotDBHandler(getBaseContext());

                // new insert
                my_offline_db.addMyPoinOnMap(my_point);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        my_db_ref.removeEventListener(childEventListener);
    }
}
