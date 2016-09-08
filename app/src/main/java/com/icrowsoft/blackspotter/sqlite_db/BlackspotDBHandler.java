package com.icrowsoft.blackspotter.sqlite_db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teardrops on 7/17/16.
 */
public class BlackspotDBHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "blackspots";

    // BLACKSPOTS table name
    private static final String TABLE_BLACKSPOTS = "blackspots";

    // BLACKSPOTS Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_LATITUDE = "lat";
    private static final String KEY_LONGITUDE = "lon";
    private static final String KEY_CASES = "cases";
    private static final String KEY_LAST_MODIFIED = "last_modified";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_CAUSE = "cause";
    private static final String KEY_FIREBASE_KEY = "firebase_key";
    private final Context _context;

    public BlackspotDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this._context = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BLACKSPOTS + "(" +
                KEY_NAME + " text," +
                KEY_LATITUDE + " text," +
                KEY_LONGITUDE + " text," +
                KEY_CASES + " int," +
                KEY_LAST_MODIFIED + " text," +
                KEY_DESCRIPTION + " text," +
                KEY_PHOTO + " text," +
                KEY_CAUSE + " text," +
                KEY_FIREBASE_KEY + " text," +
                KEY_COUNTRY + " text)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // Drop older table if existed
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
//
//        // Create tables again
//        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    // Adding new blackspot
    public void addMyPoinOnMap(MyPointOnMap my_point, boolean by_pass_check) {
        try {
            // check if duplicate
            boolean found = doesPointExist(my_point.getLatitude(), my_point.getLongitude());

            ContentValues values = new ContentValues();
            values.put(KEY_NAME, my_point.getName()); // Contact Name
            values.put(KEY_LATITUDE, my_point.getLatitude());
            values.put(KEY_LONGITUDE, my_point.getLongitude());
            values.put(KEY_CASES, my_point.getCases());
            values.put(KEY_LAST_MODIFIED, my_point.getLastModified());
            values.put(KEY_DESCRIPTION, my_point.getDescription());
            values.put(KEY_PHOTO, my_point.getPhoto());
            values.put(KEY_CAUSE, my_point.getCause());
            values.put(KEY_COUNTRY, my_point.getCountry());
            values.put(KEY_FIREBASE_KEY, my_point.getFirebaseKey());

            SQLiteDatabase db = this.getWritableDatabase();

//        if (by_pass_check) {
            // Inserting point
            db.insert(TABLE_BLACKSPOTS, null, values);
//        } else {

//            // proceed if not found
//            if (!found) {
//                List<MyPointOnMap> all_map_points = getAllPoints();
//                LatLng ref_latlong = new LatLng(Double.parseDouble(my_point.getLatitude()), Double.parseDouble(my_point.getLongitude()));
//                boolean save = false;
//                for (final MyPointOnMap point_from_DB : all_map_points) {
//                    // LatLng of reference point
//                    LatLng pointOnMap = new LatLng(Double.parseDouble(point_from_DB.getLatitude()), Double.parseDouble(point_from_DB.getLongitude()));
//                    // calculate distance
//                    float distance_in_metres = CalculationByDistance(ref_latlong, pointOnMap);
//
//                    Log.i("Kibet", "Distance: " + distance_in_metres);
//
//                    // check if within proximity
//                    if (distance_in_metres < 200) {
//                        save = false;
//                        break;
//                    } else {
//                        save = true;
//                    }
//                }

//                if (!found) {
            // Inserting point
//                    db.insert(TABLE_BLACKSPOTS, null, values);

//                    // check if accident has occured here many times
//                    checkAI(my_point);
//                }
//            } else {
//                db.update(TABLE_BLACKSPOTS, values, KEY_LATITUDE + "=? AND " + KEY_LONGITUDE + "=?",
//                        new String[]{my_point.getLatitude(), my_point.getLongitude()});
//            }
//        }
            // Closing database connection
            db.close();
        } catch (SQLiteDatabaseLockedException e) {
            Log.e("Kibet", "DB locked");
        }
    }

    // Getting single point
    public void truncateBlackspotsTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_BLACKSPOTS);
        db.close();
    }

    // Getting single point
    public boolean doesPointExist(String lat, String lon) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BLACKSPOTS,
                new String[]{KEY_LATITUDE, KEY_LONGITUDE}, KEY_LATITUDE + "=? AND " + KEY_LONGITUDE + "=?",
                new String[]{lat, lon}, null, null, null, null);

        boolean found;
        if (cursor.moveToFirst()) {
            found = true;
        } else {
            found = false;
        }

        cursor.close();
        db.close();

        // return contact
        return found;
    }

    // Getting All Contacts
    public List<MyPointOnMap> getAllPoints() {

        List<MyPointOnMap> myPointsList = new ArrayList<>();

        try {
            // Select All Query
            String selectQuery = "SELECT * FROM " + TABLE_BLACKSPOTS;

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);

//        Log.i("Kibet", "COUNT >> " + cursor.getCount());// TODO: 7/29/16 remove this

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    MyPointOnMap my_point = new MyPointOnMap();
                    my_point.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    my_point.setLatitude(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE)));
                    my_point.setLongitude(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE)));
                    my_point.setCases(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_CASES))));
                    my_point.setLastModified(cursor.getString(cursor.getColumnIndex(KEY_LAST_MODIFIED)));
                    my_point.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
                    my_point.setCountry(cursor.getString(cursor.getColumnIndex(KEY_COUNTRY)));
                    my_point.setPhoto(cursor.getString(cursor.getColumnIndex(KEY_PHOTO)));
                    my_point.setCause(cursor.getString(cursor.getColumnIndex(KEY_CAUSE)));
                    my_point.setFirebaseKey(cursor.getString(cursor.getColumnIndex(KEY_FIREBASE_KEY)));

                    // Adding contact to list
                    myPointsList.add(my_point);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("Kibet", e.getMessage());
        }

        // return contact list
        return myPointsList;
    }

    // Deleting single contact
    public void deletePoint(String firebase_key) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BLACKSPOTS, KEY_FIREBASE_KEY + " = ?",
                new String[]{String.valueOf(firebase_key)});
        db.close();

        // Get database reference
        DatabaseReference online_DB = FirebaseDatabase.getInstance().getReference();
        DatabaseReference my_db_ref = online_DB.child("blackspots").child("KE").child(firebase_key);

        // delete online
        my_db_ref.removeValue();
    }

}