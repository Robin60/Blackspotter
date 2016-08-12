package com.icrowsoft.blackspotter.SyncDB;

import android.content.Context;
import android.os.AsyncTask;

import com.icrowsoft.blackspotter.R;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

/**
 * Created by teardrops on 7/17/16.
 */
public class sync_DB_offline extends AsyncTask<String, String, String> {
    private final Context _context;
    private BlackspotDBHandler my_db;

    public sync_DB_offline(Context context) {
        this._context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        // get database reference
        my_db = new BlackspotDBHandler(_context);

        // sync xml resources into DB
        sync_database_offline();
        return null;
    }

    public void sync_database_offline() {
        // fetch black spots from resources
        String[] black_spots_names = _context.getResources().getStringArray(R.array.black_spot_names);
        String[] black_spots_lats = _context.getResources().getStringArray(R.array.black_spot_latitudes);
        String[] black_spots_lons = _context.getResources().getStringArray(R.array.black_spot_longitdes);

        // fetch danger zones from resources
        String[] danger_zone_names = _context.getResources().getStringArray(R.array.danger_zone_names);
        String[] danger_zone_lats = _context.getResources().getStringArray(R.array.danger_zone_latitudes);
        String[] danger_zone_lons = _context.getResources().getStringArray(R.array.danger_zone_longitdes);

        // insert each black spot
        for (int i = 0; i < black_spots_names.length; i++) {
            MyPointOnMap my_point = new MyPointOnMap();
            my_point.setName(black_spots_names[i]);
            my_point.setLatitude(black_spots_lats[i]);
            my_point.setLongitude(black_spots_lons[i]);
            my_point.setCases(0);
            my_point.setLastModified("" + System.currentTimeMillis());
            my_point.setCountry("");
            my_point.setDescription("Black spot");

            insert_into_table(my_point);
        }

        // insert each danger zone
        for (int i = 0; i < danger_zone_names.length; i++) {
            MyPointOnMap my_point = new MyPointOnMap();
            my_point.setName(danger_zone_names[i]);
            my_point.setLatitude(danger_zone_lats[i]);
            my_point.setLongitude(danger_zone_lons[i]);
            my_point.setCases(0);
            my_point.setLastModified("" + System.currentTimeMillis());
            my_point.setCountry("");
            my_point.setDescription("Danger zone");

            insert_into_table(my_point);
        }

        // fetch online
        // todo fech online and merge
        // todo send broadcast of completion
    }

    public void insert_into_table(MyPointOnMap my_point) {
        // new insert
        my_db.addMyPoinOnMap(my_point);
    }
}
