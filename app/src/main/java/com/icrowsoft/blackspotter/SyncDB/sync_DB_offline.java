package com.icrowsoft.blackspotter.SyncDB;

import android.content.Context;
import android.os.AsyncTask;

import com.icrowsoft.blackspotter.R;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.blackspot_handler;

/**
 * Created by teardrops on 7/17/16.
 */
public class sync_DB_offline extends AsyncTask<String, String, String> {
    private final Context _context;
    private blackspot_handler my_db;

    public sync_DB_offline(Context context) {
        this._context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        // get database reference
        my_db = new blackspot_handler(_context);

        // sync xml resources into DB
        sync_database_offline();
        return null;
    }

    public void sync_database_offline() {
        // fetch from resources
        String[] names = _context.getResources().getStringArray(R.array.black_spot_names);
        String[] lats = _context.getResources().getStringArray(R.array.black_spot_latitudes);
        String[] lons = _context.getResources().getStringArray(R.array.black_spot_longitdes);

        // insert each
        for (int i = 0; i < names.length; i++) {
            MyPointOnMap my_point = new MyPointOnMap();
            my_point.setName(names[i]);
            my_point.setLatitude(lats[i]);
            my_point.setLongitude(lons[i]);
            my_point.setCases(0);
            my_point.setLastModified(0);
            my_point.setCountry("");
            my_point.setDescription("");

            insert_into_table(my_point);
        }

        // fetch online
        // todo fech online and merge
        // todo send broadcast of completion
    }

    public void insert_into_table(MyPointOnMap my_point) {
        // save if not exists
        if (!my_db.getPoint(my_point.getLatitude())) {
            // new insert
            my_db.addMyPoinOnMap(my_point);
        }
    }
}
