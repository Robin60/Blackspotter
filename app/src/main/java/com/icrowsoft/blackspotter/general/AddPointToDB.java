package com.icrowsoft.blackspotter.general;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.icrowsoft.blackspotter.encryption.SimpleCrypto;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

/**
 * Created by teardrops on 7/29/16.
 */
public class AddPointToDB {
    private final Context _my_context;
    private DatabaseReference my_db_ref;

    public AddPointToDB(Context my_context) {
        _my_context = my_context;
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

                // try fetching country from latlng
                String result = new OnlineChecker().Go_Online(url);

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
                            if (databaseError == null) {
                                // save point to local DB
                                new BlackspotDBHandler(_my_context).addMyPoinOnMap(new_point);

                                // show success
                                myToaster("Success");
                            } else {
                                // show error
                                myToaster("Error adding location");
                            }
                        }
                    });
                } catch (Exception e) {
                    myToaster("Error encrypting: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    private void myToaster(String message) {
        Toast my_toast = Toast.makeText(_my_context, message, Toast.LENGTH_SHORT);
        my_toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        my_toast.show();
    }
}
