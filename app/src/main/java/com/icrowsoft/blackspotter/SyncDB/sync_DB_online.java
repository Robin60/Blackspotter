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
import com.google.gson.JsonObject;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.blackspot_handler;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by teardrops on 7/17/16.
 */
public class sync_DB_online extends AsyncTask<String, String, String> {
    private final Context _context;
    private blackspot_handler my_db;
    private DatabaseReference my_db_ref;

    public sync_DB_online(Context context) {
        this._context = context;
    }

    @Override
    protected String doInBackground(String... strings) {

        Log.i("Kibet", "<<< ONLINE SYNC CALLED >>> ");

        //https://blackspotter-9fe94.firebaseio.com

        // get database reference
        my_db = new blackspot_handler(_context);

        DatabaseReference online_DB = FirebaseDatabase.getInstance().getReference();

        my_db_ref = online_DB.child("blackspots");

        my_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    // fetch all points
                    List<MyPointOnMap> all_map_points = my_db.getAllPoints();

                    for (final MyPointOnMap my_point : all_map_points) {

                        Ion.with(_context).load(
                                "http://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                                        my_point.getLatitude() + "," +
                                        my_point.getLongitude() +
                                        "&sensor=false")
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    public DatabaseReference my_data_save_ref;

                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {

                                        Log.i("Kibet", "Results >> " + result);

                                        String my_json;
                                        my_json = result.toString().substring(11);
                                        my_json = my_json.substring(0, my_json.length() - 15);

                                        // break down the feedback
                                        try {
                                            JSONArray json = new JSONArray(my_json);

                                            JSONObject row = json.getJSONObject(0);
                                            JSONArray json2 = new JSONArray(row.getString("address_components"));

                                            String country = "";

                                            JSONObject row2 = json2.getJSONObject(json2.length() - 1);
                                            country = row2.getString("long_name");

                                            Log.i("Kibet", "--- " + country);

                                            // set country
                                            my_point.setCountry(country);

                                            // update
                                            my_db.updateMyPoint(my_point);

                                            // save online
                                            my_data_save_ref = my_db_ref.child(country).push();
                                            my_data_save_ref.setValue(my_point);
                                        } catch (JSONException ex) {
                                            Log.i("Kibet", "Json Error: " + ex.getMessage());
                                        }
                                    }
                                });

                        try {
                            Thread.sleep(1500);
                            Log.i("Kibet", "Sleeping...");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

//                        // get country
//                        String country = getCountryName(_context,
//                                Double.parseDouble(my_point.getLatitude()),
//                                Double.parseDouble(my_point.getLatitude()));
//                        if (country != null) {
//                            // set country
//                            my_point.setCountry(country);
//
//                            // update
//                            my_db.updateMyPoint(my_point);
//
//                        }
                    }
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
