package com.icrowsoft.blackspotter.services;


import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.icrowsoft.blackspotter.SyncDB.complete_day_simulator;
import com.icrowsoft.blackspotter.encryption.SimpleCrypto;
import com.icrowsoft.blackspotter.general.CheckConnection;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.online_handlers.AddPointToPHP;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by teardrops on 7/30/16.
 */
public class AddPointOnline extends IntentService {

    private DatabaseReference my_db_ref;
    private MyPointOnMap new_point;

    public AddPointOnline() {
        super("AddPointOnline");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get bundle
        Bundle data = intent.getExtras();

        // get point
        new_point = data.getParcelable("new_point");

        // prepare url
        String url = "http://ws.geonames.org/countryCode?lat=" + new_point.getLatitude() +
                "&lng=" + new_point.getLongitude() + "&username=sdcrow";

        // check if internet is connected
        if (CheckConnection.is_internet_connected(getBaseContext())) {
            // try fetching country from latlng
//                    String result = OnlineChecker.Go_Online(url);

//                    if (result == null || result.startsWith("timeout")) {
//                        Log.e("Kibet", "Error: Result is null|timeout");
//                    } else {
//                        if (result.startsWith("Unable to resolve host") || result.startsWith("failed to connect to")) {
//                            notify_internet_error();
//                        } else {
            // handle no country
            // TODO: 9/4/16
//                }
//            }

            // set time to server time(last_modified)
            new_point.setLastModified("" + System.currentTimeMillis());

            // set the country
            new_point.setCountry("KE");

            try {
                // get and encrypt latitude
                String encrypted_latlong = SimpleCrypto.encrypt("blackspotter", new_point.getLatitude() + new_point.getLongitude() + new_point.getLastModified());

                // get Firebase reference
                DatabaseReference online_DB = FirebaseDatabase.getInstance().getReference();
                my_db_ref = online_DB.child("blackspots").child(new_point.getCountry()).child(encrypted_latlong);

                // save new location
                my_db_ref.setValue(new_point, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError == null) {
                            // set firebase key
                            new_point.setFirebaseKey(databaseReference.getKey());

                            // save point to local DB
                            new BlackspotDBHandler(getBaseContext()).addMyPointOnMap(new_point, false);

                            // add to PHP
                            new AddPointToPHP().addPoint(getBaseContext(), new_point);

                            // check if its an accident scene
                            if (new_point.getDescription().equals("Accident scene")) {
                                // create handler
                                Handler _handler = new Handler();

                                // trigger delete after 24hrs
                                new complete_day_simulator(getBaseContext(), _handler, new_point).execute();
                            }

//                            // show success
//                            myToaster("Added Successfully");

                            // sendBroadcast to re-add markers
                            sendBroadcast(new Intent("REFRESH_MARKERS"));

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Log.e("Kibet", "Sleep interrupted: " + e.getMessage());
                            }

                            // check for close points
                            triggerAI(new_point);
                        } else {
//                            // show error
//                            myToaster("Error adding location");
                            Log.e("Kibet", "Error adding location: Online Firebase error");
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("Kibet", "Error encrypting: " + e.getMessage());
            }
        } else {
            // send broadcast to notify no internet
            sendBroadcast(new Intent("NOTIFY_NO_INTERNET"));
        }
    }

    public void triggerAI(MyPointOnMap check_point) {
        // fetch all points
        List<MyPointOnMap> all_map_points = new BlackspotDBHandler(getBaseContext()).getAllPoints();

        List<MyPointOnMap> references_to_delete = new ArrayList<>();

        // LatLng of location to check against
        LatLng ref_latlong = new LatLng(Double.parseDouble(check_point.getLatitude()), Double.parseDouble(check_point.getLongitude()));

        String new_photo = "null";
        String new_cause = "null";
        String new_long = "null";
        String new_lat = "null";

        for (final MyPointOnMap point_from_DB : all_map_points) {
            // LatLng of reference point
            LatLng pointOnMap = new LatLng(Double.parseDouble(point_from_DB.getLatitude()), Double.parseDouble(point_from_DB.getLongitude()));

            // calculate distance
            float distance_in_metres = CalculationByDistance(ref_latlong, pointOnMap);

            Log.i("Kibet", "Checking " + point_from_DB.getName() + " >> " + check_point.getName() + "{" + distance_in_metres + "}");

            // check if within proximity
            if (distance_in_metres < 2 && point_from_DB.getLongitude() != check_point.getLongitude() && point_from_DB.getLatitude() != check_point.getLatitude()) {
                Log.i("Kibet", "To be deleted " + point_from_DB.getName());

                // delete
                references_to_delete.add(point_from_DB);

                // mirror its details
                new_photo = point_from_DB.getPhoto();
                new_cause = point_from_DB.getCause();
                new_lat = point_from_DB.getLatitude();
                new_long = point_from_DB.getLongitude();
            }
        }

        Log.i("Kibet", "To be deleted size: " + references_to_delete.size());// TODO: 12/10/2016 DELETE

        // check if point has been marked more than thrice
        if (references_to_delete.size() > 2) {

            // delete these locations
            for (int i = 0; i < references_to_delete.size(); i++) {
                new BlackspotDBHandler(getBaseContext()).deletePoint(references_to_delete.get(i));
                Log.e("Kibet", "Requesting delete: " + references_to_delete.get(i));// TODO: 12/10/2016 DELETE
            }

            SharedPreferences prefs = getBaseContext().getSharedPreferences("LoggedInUsersPrefs", 0);

            // get user_id to test if session exists
            String logged_in_user_email = prefs.getString("email", "");


            // create a point from current location
            MyPointOnMap new_point = new MyPointOnMap();

            // set fields
            new_point.setCases(0);
            new_point.setCountry("");
            new_point.setName("Black spot");// use description as name before resolve
            new_point.setLastModified("" + System.currentTimeMillis());
            new_point.setLatitude(new_lat);
            new_point.setLongitude(new_long);
            new_point.setDescription("Black spot");
            new_point.setFirebaseKey("null");
            new_point.setPhoto(new_photo);
            new_point.setCause(new_cause);
            new_point.setPostedBy(logged_in_user_email);

            Log.i("Kibet", "Adding " + new_point.getName());

            // add point to DB
            add_this_point_AI(new_point);
        }
    }

    public int CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return meterInDec;
    }


    public void add_this_point_AI(final MyPointOnMap new_point) {
        Log.i("Kibet", "AI ---- Adding : " + new_point.getName());

        // create a background thread
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                // set time to server time(last_modified)
                new_point.setLastModified("" + System.currentTimeMillis());

                // set the country
                new_point.setCountry("KE");

                try {
                    // get and encrypt latitude
                    String encrypted_latlong = SimpleCrypto.encrypt("blackspotter", new_point.getLatitude() + new_point.getLongitude() + new_point.getLastModified());

                    // get Firebase reference
                    DatabaseReference online_DB = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference my_db_ref = online_DB.child("blackspots").child(new_point.getCountry()).child(encrypted_latlong);

                    // save new location
                    my_db_ref.setValue(new_point, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {
                                // set firebase key
                                new_point.setFirebaseKey(databaseReference.getKey());

                                // save point to local DB
                                new BlackspotDBHandler(getBaseContext()).addMyPointOnMap(new_point, false);

                                // send broadcast to re-add markers
                                getBaseContext().sendBroadcast(new Intent("REFRESH_MARKERS"));
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("Kibet", "Error encrypting: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}
