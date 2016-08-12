package com.icrowsoft.blackspotter.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.icrowsoft.blackspotter.my_notifier.MyNotifier;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import java.util.List;


/**
 * Created by teardrops on 7/30/16.
 */
public class Notifier extends Service {
    private Runnable notifier_resetter = new Runnable() {
        @Override
        public void run() {
            // reset has notified to false
            user_already_notified = false;

            // rerun check proximity
            check_proximity();
        }
    };
    private Handler my_handler;
    private boolean user_already_notified;
    private LatLng my_location;
    private MyNotifier my_notifier_instance;
    private boolean use_metres;
    private String distance_to_notify;
    private String reminder_interval;
    private boolean allowed_notifications;
    private boolean allowed_reminders;
    private float global_distance_in_metres;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Kibet", "Service started");// TODO: 8/8/16 delete

        // get handler instance
        my_handler = new Handler();

        // set has notified to true
        user_already_notified = false;

        // create my location
        my_location = new LatLng(0, 0);

        // create notifier instance
        my_notifier_instance = new MyNotifier();

        // check proximity to any danger zone
        check_proximity();

        // Create Broadcast
        BroadcastReceiver changes_update_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("Kibet", "Broadcast received");// TODO: 8/8/16 delete

                // cancel any call backs
                my_handler.removeCallbacks(notifier_resetter);

                // re-check proximity
                check_proximity();
            }
        };

        // register the broadcast receiver
        IntentFilter intent_filter = new IntentFilter();
        intent_filter.addAction("REQUEST_TO_SERVICE");
        registerReceiver(changes_update_receiver, intent_filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void check_proximity() {
        // TODO: 7/30/16 check range

        // notify if not already done
        if (!user_already_notified) {
            // get database reference
            BlackspotDBHandler my_db = new BlackspotDBHandler(getBaseContext());

            // fetch all points
            List<MyPointOnMap> all_map_points = my_db.getAllPoints();

            for (final MyPointOnMap my_point : all_map_points) {
                // preference manager
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                // calculate distance
                float distance_in_metres = calculate_distance(my_point);

                //capture units to use
                use_metres = settings.getBoolean("chk_metres", true);
                distance_to_notify = settings.getString("distance_to_notify", "500");
                reminder_interval = settings.getString("reminder_interval", "15");
                allowed_notifications = settings.getBoolean("allow_notifications", true);
                allowed_reminders = settings.getBoolean("allow_reminders", true);

                // check units
                if (!use_metres) {
                    // convert to yards
                    distance_in_metres = (float) (distance_in_metres * 1.09361);
                }

// TODO: 7/30/16 check whether reminders are on

                // check if within proximity
                if (distance_in_metres < Integer.parseInt(distance_to_notify)) {
                    Log.i("Kibet", "Close to >>> " + my_point.getName());
                    // reset
                    user_already_notified = true;

                    if (allowed_notifications) {
                        // check if is a reminder
                        if (global_distance_in_metres == distance_in_metres) {
                            if (allowed_reminders) {
                                //spawn notification
                                my_notifier_instance.notify_user(getBaseContext(), "Warning!", "Proximity alert: " + my_point.getName());
                            }
                        } else {
                            //spawn notification
                            my_notifier_instance.notify_user(getBaseContext(), "Warning!", "Proximity alert: " + my_point.getName());
                        }
                    }

                    // start timer to reset has notified
                    my_handler.postDelayed(notifier_resetter, (Integer.parseInt(reminder_interval) * 60000));

                    // set global reminder distance in metres
                    global_distance_in_metres = distance_in_metres;

                    // stop loop
                    break;
                }
            }
        } else {
            // notification already shown
        }
    }

    private float calculate_distance(MyPointOnMap black_spot) {
        // prepare array from results
        float[] distance_results = new float[4];

        // calculate distance
        Location.distanceBetween(
                my_location.latitude, my_location.longitude,
                Double.parseDouble(black_spot.getLatitude()), Double.parseDouble(black_spot.getLongitude()),
                distance_results);

        return distance_results[0];
    }
}
