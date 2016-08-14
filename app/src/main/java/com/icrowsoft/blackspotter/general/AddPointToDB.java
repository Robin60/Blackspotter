package com.icrowsoft.blackspotter.general;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.icrowsoft.blackspotter.SyncDB.complete_day_simulator;
import com.icrowsoft.blackspotter.encryption.SimpleCrypto;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

/**
 * Created by teardrops on 7/29/16.
 */
public class AddPointToDB {
    private final Context _context;
    private final Activity _activity;
    private final GoogleMap _map;
    private final View _fab;
    private final Handler _handler;
    private DatabaseReference my_db_ref;

    public AddPointToDB(Context my_context, Handler handler, Activity activity, GoogleMap map, View view) {
        _context = my_context;
        _activity = activity;
        _map = map;
        _handler = handler;
        _fab = view;
    }

    public void add_this_point(final MyPointOnMap new_point) {
        Log.i("Kibet", "Add point requested for: " + new_point.getName());

        // create a background thread
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                // prepare url
                String url = "http://ws.geonames.org/countryCode?lat=" + new_point.getLatitude() +
                        "&lng=" + new_point.getLongitude() + "&username=sdcrow";

                // check if internet is connected
                if (CheckConnection.is_internet_connected(_context)) {
                    // try fetching country from latlng
                    String result = new OnlineChecker().Go_Online(url);

                    if (result == null || result.startsWith("timeout")) {
                        Log.e("Kibet", "Error: Result is null|timeout");
                    } else {
                        if (result.startsWith("Unable to resolve host") || result.startsWith("failed to connect to")) {
                            notify_internet_error();
                        } else {
                            // set time to server time(last_modified)
                            new_point.setLastModified("" + System.currentTimeMillis());

                            // set the country
                            new_point.setCountry(result);

                            String encrypted_latitude;
                            try {
                                // get and encrypt latitude
                                encrypted_latitude = SimpleCrypto.encrypt("blackspotter", new_point.getLatitude());

                                // get Firebase reference
                                DatabaseReference online_DB = FirebaseDatabase.getInstance().getReference();
                                my_db_ref = online_DB.child("blackspots").child(result).child(encrypted_latitude);

                                // save new location
                                my_db_ref.setValue(new_point, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                        Log.i("Kibet","Push successful: "+databaseReference.getKey());
//                                        Log.i("Kibet","Push parent: "+databaseReference.getDatabase());
//                                        Log.i("Kibet","Push root: "+databaseReference.getRoot());
//                                        Log.i("Kibet","Reference: "+databaseReference);

                                        if (databaseError == null) {
                                            // check if its an accident scene
                                            if (new_point.getDescription().equals("Accident scene")) {
                                                // get key
                                                String key = databaseReference.getKey();
                                                String latitude = new_point.getLatitude();
                                                String longitude = new_point.getLongitude();

                                                // trigger delete after 24hrs
                                                new complete_day_simulator(_context, _handler).execute(key, latitude, longitude);
                                            }

                                            // save point to local DB
                                            new BlackspotDBHandler(_context).addMyPoinOnMap(new_point);

                                            // show success
                                            myToaster("Added Successfully");

                                            // re-add markers
                                            new AddMarkersToMap(_context, _activity, _map).execute();
                                        } else {
                                            // show error
                                            myToaster("Error adding location");
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                Log.e("Kibet", "Error encrypting: " + e.getMessage());
                            }
                        }
                    }
                } else {
                    notify_internet_error();
                }
                return null;
            }

            private void notify_internet_error() {
                _activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(_fab, "Internet connection failed!", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }.execute();
    }

    private void myToaster(String message) {
        Toast my_toast = Toast.makeText(_context, message, Toast.LENGTH_SHORT);
        my_toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        my_toast.show();
    }
}
