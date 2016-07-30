package com.icrowsoft.blackspotter.SyncDB;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.icrowsoft.blackspotter.general.AddPointToDB;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by teardrops on 7/17/16.
 */
public class sync_DB_online extends AsyncTask<String, String, String> {
    private final Context _context;
    private DatabaseReference my_db_ref;

    public sync_DB_online(Context context) {
        this._context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.i("Kibet", "Online SYNC started");

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

                    for (final MyPointOnMap my_point : all_map_points) {
                        // fetch country and save online
                        new AddPointToDB(_context).add_this_point(my_point);

//                        // save online
//                        DatabaseReference my_data_save_ref = my_db_ref.push();
//                        my_data_save_ref.setValue(my_point);
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

    public static String getCountryName(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address result;

            Log.i("Kibet", "Addresses: " + addresses);

            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryName();
            }
        } catch (IOException ignored) {
            //do something
        }
        return null;
    }
}
