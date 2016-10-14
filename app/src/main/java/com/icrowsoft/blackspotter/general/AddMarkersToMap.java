package com.icrowsoft.blackspotter.general;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.icrowsoft.blackspotter.adapters.MyInfoWindowAdapter;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import java.util.HashMap;
import java.util.List;

/**
 * Created by teardrops on 8/6/16.
 */
public class AddMarkersToMap extends AsyncTask<String, String, String> {
    private final Context _context;
    private final Activity _activity;
    private final GoogleMap _map;
    private final HashMap<String, MyPointOnMap> my_markers;

    public AddMarkersToMap(Context context, Activity map_activity, GoogleMap map) {
        _context = context;
        _activity = map_activity;
        _map = map;
        my_markers = new HashMap<>();
    }

    @Override
    protected String doInBackground(String... strings) {

        // clear all markers on map
        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // clear markers
                _map.clear();
            }
        });

        // get database reference
        BlackspotDBHandler my_db = new BlackspotDBHandler(_context);

        // fetch all points from DB
        List<MyPointOnMap> all_points = my_db.getAllPoints();

        // loop to create markers
        for (int i = 0; i < all_points.size(); i++) {
            // get point reference
            final MyPointOnMap my_point = all_points.get(i);

            // get description
            String description = my_point.getDescription();

            // Add a marker
            LatLng new_point = new LatLng(Double.parseDouble(my_point.getLatitude()), Double.parseDouble(my_point.getLongitude()));
            final MarkerOptions point_on_map = new MarkerOptions();
            point_on_map.title("" + i);
            point_on_map.draggable(false);
            point_on_map.alpha(0.9f);
            point_on_map.position(new_point);

            // know the type of point we are dealing with
            if (description.equals("Black spot")) {
                point_on_map.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else if (description.equals("Danger zone")) {
                point_on_map.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            } else if (description.equals("Accident scene")) {
                point_on_map.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }

            // check if 0 0
            if (!my_point.getLatitude().equals("0.0") && !my_point.getLongitude().equals("0.0")) {
                // add markers on main thread
                _activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //add marker
                        Marker marker = _map.addMarker(point_on_map);

                        // add to my markers
                        my_markers.put(marker.getTitle(), my_point);

                        // set clicks on info windows
                        _map.setInfoWindowAdapter(new MyInfoWindowAdapter(_activity, my_markers));
                    }
                });
            }
        }
        return null;
    }
}
