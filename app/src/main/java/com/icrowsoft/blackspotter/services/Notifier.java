package com.icrowsoft.blackspotter.services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.icrowsoft.blackspotter.my_notifier.MyNotifier;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import java.text.DecimalFormat;
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
    private MyNotifier my_notifier_instance;
    private boolean use_metres;
    private String distance_to_notify;
    private String reminder_interval;
    private boolean allowed_notifications;
    private boolean allowed_reminders;
    private float global_distance_in_metres;
    private Location my_location;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Kibet", "Service started");// TODO: 8/8/16 delete

        // get handler instance
        my_handler = new Handler();

        // set has notified to true
        user_already_notified = false;

        // create my location
        my_location = new Location("dummy_provider");
        my_location.setLatitude(0);
        my_location.setLongitude(0);

        // get my location
        getMyLocation();

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

    private void getMyLocation() {
        final LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // location access denied
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000,
                500, new LocationListener() {// TODO: 8/6/16 change values here to match settings
                    @Override
                    public void onLocationChanged(Location location) {
                        // update my location
                        my_location = location;

                        //noinspection MissingPermission
                        mLocationManager.removeUpdates(this);
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                });
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

                // ngara
                LatLng ngara = new LatLng(-1.2746653, 36.82906460000004);
                // tuk
                LatLng tuk = new LatLng(-1.299923, 36.824155);

                // calculate distance
                float distance_in_metres = CalculationByDistance(
                        new LatLng(ngara.latitude,
                                ngara.longitude),
                        new LatLng(tuk.latitude, tuk.longitude));

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

                // start handler to ensure reminders
                my_handler.postDelayed(notifier_resetter, (Integer.parseInt(reminder_interval) * 60000));

// TODO: 7/30/16 check whether reminders are on


                Log.i("Kibet", ">>>" + distance_in_metres + "<<<");

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
//        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
//                + " Meter   " + meterInDec);

        return meterInDec;
    }

//    private float calculate_distance(MyPointOnMap black_spot) {
//        // prepare array from results
//        float[] distance_results = new float[4];
//
//        // calculate distance
//        Location.distanceBetween(
//                my_location.getLatitude(), my_location.getLongitude(),
//                Double.parseDouble(black_spot.getLatitude()), Double.parseDouble(black_spot.getLongitude()),
//                distance_results);
//
//        return distance_results[0];
//    }
}
