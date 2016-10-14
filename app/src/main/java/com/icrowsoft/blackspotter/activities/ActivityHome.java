package com.icrowsoft.blackspotter.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.PolyUtil;
import com.icrowsoft.blackspotter.R;
import com.icrowsoft.blackspotter.SyncDB.sync_DB_offline;
import com.icrowsoft.blackspotter.adapters.MyInfoWindowAdapter;
import com.icrowsoft.blackspotter.base64.ImageBase64;
import com.icrowsoft.blackspotter.general.AddMarkersToMap;
import com.icrowsoft.blackspotter.general.DirectionsJSONParser;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.roundImage.TextDrawable;
import com.icrowsoft.blackspotter.services.AddPointOnline;
import com.icrowsoft.blackspotter.services.Notifier;
import com.icrowsoft.blackspotter.services.OnlineDBListener;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.ProviderException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ActivityHome extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, View.OnClickListener {

    private static final int CAMERA_REQUEST_CODE = 999;
    private static final int VOICE_REQUEST_CODE = 777;
    private static final int PLACE_PICKER_REQUEST_CODE = 888;
    private GoogleMap mMap;
    private Animation fab_close;
    private Animation fab_open;
    private FloatingActionButton fab_fullscreen;
    private boolean fab_add_new_clicked;
    private FrameLayout mInterceptorFrame;
    private TextView lbl_accuracy;
    private View warning_dot;
    private Polyline current_polyline;
    private ActivityHome my_activity;
    private List<MyPointOnMap> all_points;
    private TextView lbl_accidents;
    private TextView lbl_blackspots;
    private TextView lbl_danger_zones;
    private Location my_current_location;
    private TextDrawable BS_fab_icon, AS_fab_icon, DZ_fab_icon;
    private FloatingActionButton fab_add_new, fab_black_spot, fab_danger_zone, fab_accident_scene;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private Marker my_location_marker;
    private TextToSpeech textToSpeech;
    private Handler handler;
    private HashMap<String, MyPointOnMap> my_markers;
    private MyPointOnMap new_point;
    private String logged_in_user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // change icon
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.ic_launcher));

        // get my context
        my_activity = this;

        // create handler
        handler = new Handler();

        SharedPreferences prefs = getSharedPreferences("LoggedInUsersPrefs", 0);

        // get user_id to test if session exists
        logged_in_user_email = prefs.getString("email", "");

        BlackspotDBHandler my_db = new BlackspotDBHandler(getBaseContext());

        // fetch all points from DB
        all_points = my_db.getAllPoints();

        // get view to use on the toolbar
        warning_dot = getLayoutInflater().inflate(R.layout.toolbar_home, null);
        toolbar.addView(warning_dot);

        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(400); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

        // start animation
        warning_dot.startAnimation(animation);

        // set on click to clear animation
        warning_dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop animation
                view.clearAnimation();

                // hide the view
                view.setVisibility(View.GONE);

                // get notification manager
                final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // clear any notifications
                mNotificationManager.cancel(0);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // handle clicks outside FAB
        mInterceptorFrame = (FrameLayout) findViewById(R.id.fl_interceptor);
        mInterceptorFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (fab_add_new_clicked) {
                    animateFAB();
                    return true;
                }
                return false;
            }
        });


        // force actionbar overflow
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        // get text views
        lbl_accuracy = (TextView) findViewById(R.id.lbl_accuracy);
        lbl_accidents = (TextView) findViewById(R.id.lbl_accidents);
        lbl_blackspots = (TextView) findViewById(R.id.lbl_blackspots);
        lbl_danger_zones = (TextView) findViewById(R.id.lbl_danger_zones);

        // get FABs
        final FloatingActionButton fab_speak = (FloatingActionButton) findViewById(R.id.fab_speak);
        fab_fullscreen = (FloatingActionButton) findViewById(R.id.fab_fullscreen);
        fab_add_new = (FloatingActionButton) findViewById(R.id.fab_add_new);
        fab_black_spot = (FloatingActionButton) findViewById(R.id.fab_black_spot);
        fab_accident_scene = (FloatingActionButton) findViewById(R.id.fab_accident_scene);
        fab_danger_zone = (FloatingActionButton) findViewById(R.id.fab_danger_zone);

        // set onclick listener
        fab_fullscreen.setOnClickListener(this);
        fab_speak.setOnClickListener(this);

        // add click events
        fab_black_spot.setOnClickListener(this);
        fab_accident_scene.setOnClickListener(this);
        fab_danger_zone.setOnClickListener(this);

//        // set on long click listener
//        fab_black_spot.setOnLongClickListener(this);
//        fab_accident_scene.setOnLongClickListener(this);
//        fab_danger_zone.setOnLongClickListener(this);

        // get FAB animations
        fab_close = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_open);

        // track main FAB clicks
        fab_add_new_clicked = false;

        // on click listener for the main click
        fab_add_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFAB();
            }
        });

        // check if online DB listener is running
        if (!isMyServiceRunning(OnlineDBListener.class)) {
            // start Online DB listener service
            startService(new Intent(getBaseContext(), OnlineDBListener.class));
        } else {
            Log.i("Kibet", "Online DB Listener already running---");
        }

        // check if notifier service is running
        if (!isMyServiceRunning(Notifier.class)) {
            // start Online DB listener service
            startService(new Intent(getBaseContext(), Notifier.class));
        } else {
            Log.i("Kibet", "Notifier already running---");
        }

        // Listener for new point additions online
        BroadcastReceiver refresh_markers_listener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // check if map is ready
                if (mMap != null) {
                    // refresh markers
                    new AddMarkersToMap(getBaseContext(), my_activity, mMap).execute();
                } else {
                    Log.e("Kibet", "Map is NULL");
                }
            }
        };

        // register receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("REFRESH_MARKERS");
        registerReceiver(refresh_markers_listener, intentFilter);

        // Listener for no internet
        BroadcastReceiver no_internet_listener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // show error
                Snackbar.make(fab_speak, "Internet connection failed!", Snackbar.LENGTH_LONG).show();
            }
        };

        // register receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction("NOTIFY_NO_INTERNET");
        registerReceiver(no_internet_listener, intentFilter);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // get map reference
        mMap = googleMap;

        // syc database offline
        new sync_DB_offline(getApplicationContext(), handler, my_activity, mMap, fab_add_new).execute();

        // set dummy location
        my_current_location = new Location("dummy_provider");
        my_current_location.setLatitude(-1.29207);
        my_current_location.setLongitude(36.8219);
//        my_current_location.setLatitude(0);
//        my_current_location.setLongitude(0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast my_toast = Toast.makeText(getBaseContext(), "Location Access denied", Toast.LENGTH_SHORT);
            my_toast.setGravity(Gravity.CENTER, 0, 0);
            my_toast.show();

            // request permission
            ActivityCompat.requestPermissions(
                    my_activity,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        } else {
            // track my location changes
            track_me();

            // enable my location
            mMap.setMyLocationEnabled(true);
        }

        // set up map options
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // add markers
        new AddMarkersToMap(getBaseContext(), my_activity, mMap).execute();

        // set clicks on info windows
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this, my_markers));
    }

    @SuppressWarnings("MissingPermission")
    private void track_me() {
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                // remove my location marker
                if (my_location_marker != null) {
                    my_location_marker.remove();
                }

                float accuracy = location.getAccuracy();

                // update my location
                my_current_location = location;

                // display lbl_accuracy
                lbl_accuracy.setText("Acc: " + accuracy);
            }
        });

        // get location manager
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000,
                    500, new LocationListener() {// TODO: 8/6/16 change values here to match settings
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i("Kibet", "My location updated by GPS");

                            // remove my location marker
                            if (my_location_marker != null) {
                                my_location_marker.remove();
                            }

                            float accuracy = location.getAccuracy();

                            // update my location
                            my_current_location = location;

                            // display lbl_accuracy
                            lbl_accuracy.setText("Acc: " + accuracy);
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
        } catch (ProviderException ex) {
            Log.e("Kibet", "Error: GPS not available");
        }

//        try {
//            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500000,
//                    1000, new LocationListener() {// TODO: 8/6/16 change values here to match settings
//                        @Override
//                        public void onLocationChanged(Location location) {
//                            Log.i("Kibet", "My location updated by Cell tower");
//
//                            // remove my location marker
//                            if (my_location_marker != null) {
//                                my_location_marker.remove();
//                            }
//
//                            float accuracy = location.getAccuracy();
//
//                            // update my location
//                            my_current_location = location;
//
//                            // create marker
//                            MarkerOptions marker = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("My Location");
//
//                            // Changing marker icon
//                            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//
//                            // adding marker
//                            my_location_marker = mMap.addMarker(marker);
//
//                            // display lbl_accuracy
//                            lbl_accuracy.setText("Acc: " + accuracy);xxxx
//                        }
//
//                        @Override
//                        public void onStatusChanged(String s, int i, Bundle bundle) {
//
//                        }
//
//                        @Override
//                        public void onProviderEnabled(String s) {
//
//                        }
//
//                        @Override
//                        public void onProviderDisabled(String s) {
//
//                        }
//                    });
//        } catch (ProviderException ex) {
//            Log.e("Kibet", "Error: Cell tower not available");
//        }
    }

    public void animateFAB() {

        if (fab_add_new_clicked) {
            // rotate main FAB
            rotateFabBackward();

            // animate small FABs
            fab_black_spot.startAnimation(fab_close);
            fab_accident_scene.startAnimation(fab_close);
            fab_danger_zone.startAnimation(fab_close);

            // disable clicks
            fab_black_spot.setClickable(false);
            fab_accident_scene.setClickable(false);
            fab_danger_zone.setClickable(false);

            // change state of click on main FAB
            fab_add_new_clicked = false;

        } else {
            // rotate main FAB
            rotateFabForward();

            // animate small FABs
            fab_black_spot.startAnimation(fab_open);
            fab_accident_scene.startAnimation(fab_open);
            fab_danger_zone.startAnimation(fab_open);

            // enable clicks
            fab_black_spot.setClickable(true);
            fab_accident_scene.setClickable(true);
            fab_danger_zone.setClickable(true);

            // change state of click on main FAB
            fab_add_new_clicked = true;
        }
    }

    public void rotateFabForward() {
        ViewCompat.animate(fab_add_new)
                .rotation(45)
                .setDuration(400)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    public void rotateFabBackward() {
        ViewCompat.animate(fab_add_new)
                .rotation(0)
                .setDuration(400)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak e.g.\n\"Set accident scene...\"");
        startActivityForResult(intent, VOICE_REQUEST_CODE);
    }

    private void toggleFullscreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags ^= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);

        // get full screen status
        boolean fullScreen = (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;

        // change icon
        if (fullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab_fullscreen.setImageDrawable(getResources().getDrawable(R.drawable.collapse, getBaseContext().getTheme()));
            } else {
                fab_fullscreen.setImageDrawable(getResources().getDrawable(R.drawable.collapse));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab_fullscreen.setImageDrawable(getResources().getDrawable(R.drawable.expand, getBaseContext().getTheme()));
            } else {
                fab_fullscreen.setImageDrawable(getResources().getDrawable(R.drawable.expand));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isWithinSpot(String description, Location my_loc) {
        // get database reference
        BlackspotDBHandler my_db = new BlackspotDBHandler(getBaseContext());

        // fetch all points
        List<MyPointOnMap> all_map_points = my_db.getAllPoints();

        for (final MyPointOnMap point_from_DB : all_map_points) {
            // LatLng of my location
            LatLng myLocation = new LatLng(my_loc.getLatitude(), my_loc.getLongitude());

            // LatLng of reference point
            LatLng pointOnMap = new LatLng(Double.parseDouble(point_from_DB.getLatitude()), Double.parseDouble(point_from_DB.getLongitude()));

            // calculate distance
            float distance_in_metres = CalculationByDistance(myLocation, pointOnMap);

            // check if within proximity
            if (distance_in_metres < 2) {
                Snackbar.make(fab_add_new, "Already within a " + description + ": " + point_from_DB.getName(), Snackbar.LENGTH_SHORT).show();
                return true;
            }
        }

        return false;
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_black_spot:
                boolean found = new BlackspotDBHandler(getBaseContext()).doesPointExist("" + my_current_location.getLatitude(), "" + my_current_location.getLongitude());

                // check for duplicates
                if (!found) {
                    if (!isWithinSpot("Black spot", my_current_location)) {
                        // handle add this location to map
                        add_location_to_DB_via_click("Add Black Spot", "Add this location as an accident black spot?", "Black spot", my_current_location);
                    }
                } else {
                    Snackbar.make(fab_add_new, "Duplicate. Vary position a bit", Snackbar.LENGTH_SHORT).show();
                }
                // hide fabs
                animateFAB();
                break;
            case R.id.fab_accident_scene:
                found = new BlackspotDBHandler(getBaseContext()).doesPointExist("" + my_current_location.getLatitude(), "" + my_current_location.getLongitude());

                // check for duplicates
                if (!found) {
                    // handle add this location to map
                    add_location_to_DB_via_click("Add Accident Scene", "Add this location as an accident scene?", "Accident scene", my_current_location);
                } else {
                    Snackbar.make(fab_add_new, "Duplicate. Vary position a bit", Snackbar.LENGTH_SHORT).show();
                }
                // hide fabs
                animateFAB();
                break;
            case R.id.fab_danger_zone:
                found = new BlackspotDBHandler(getBaseContext()).doesPointExist("" + my_current_location.getLatitude(), "" + my_current_location.getLongitude());

                // check for duplicates
                if (!found) {
                    if (!isWithinSpot("Danger Zone", my_current_location)) {
                        // handle add this location to map
                        add_location_to_DB_via_click("Add Danger Zone", "Add this location as a danger zone?", "Danger zone", my_current_location);
                    }
                } else {
                    Snackbar.make(fab_add_new, "Duplicate. Vary position a bit", Snackbar.LENGTH_SHORT).show();
                }

                // hide fabs
                animateFAB();
                break;
            case R.id.fab_fullscreen:
                // toogle fullscreen mode
                toggleFullscreen();
                break;
            case R.id.fab_speak:
                // Check if speech recognition is supported
                PackageManager pm = getPackageManager();
                List<ResolveInfo> activities = pm.queryIntentActivities(
                        new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
                if (activities.size() == 0) {
                    Snackbar.make(fab_add_new, "Recognizer not present", Snackbar.LENGTH_SHORT).show();
                } else {
                    // start voice recognition activity
                    startVoiceRecognitionActivity();
                }
                break;
        }
    }

    /*
    * Add point to database via click
    */
    private void add_location_to_DB_via_click(String title, String body, final String description, final Location point) {
        // display confirmation
        new MaterialDialog.Builder(my_activity)
                .title(title)
                .content(body)
                .positiveText("Proceed")
                .negativeText("Cancel")
                .positiveColor(getResources().getColor(android.R.color.holo_green_dark))
                .negativeColor(getResources().getColor(android.R.color.holo_red_dark))
                .icon(getResources().getDrawable(R.mipmap.ic_launcher))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // positive button
                        dialog.dismiss();

                        // create a point from current location
                        new_point = new MyPointOnMap();

                        // set fields
                        new_point.setCases(0);
                        new_point.setCountry("");
                        new_point.setName(description);// use description as name before resolve
                        new_point.setLastModified("" + System.currentTimeMillis());
                        new_point.setLatitude(String.valueOf(point.getLatitude()));
                        new_point.setLongitude(String.valueOf(point.getLongitude()));
                        new_point.setDescription(description);
                        new_point.setFirebaseKey("null");
                        new_point.setPostedBy(logged_in_user_email);

                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                // negative button
                dialog.dismiss();
            }
        }).show();
    }

    /*
    * Add point to database via Voice command
    */
    private void add_location_to_DB_via_voice(String description, final Location point) {
        // create a point using current location
        MyPointOnMap new_point = new MyPointOnMap();

        // set fields
        new_point.setCases(0);
        new_point.setCountry("");
        new_point.setName(description);// use description as name before resolve
        new_point.setLastModified("" + System.currentTimeMillis());
        new_point.setLatitude(String.valueOf(point.getLatitude()));
        new_point.setLongitude(String.valueOf(point.getLongitude()));
        new_point.setDescription(description);
        new_point.setFirebaseKey("");
        new_point.setPostedBy(logged_in_user_email);

        // create bundle
        Bundle data = new Bundle();
        data.putParcelable("new_point", new_point);

        // create new intent
        Intent dataIntent = new Intent(getBaseContext(), AddPointOnline.class);
        dataIntent.putExtras(data);

        // start background save action
        startService(dataIntent);

//        // add point to DB
//        new AddPointToOnlineDB(getApplicationContext(), handler, my_activity, mMap, fab_add_new).add_this_point(new_point);

        // play sound
        textToSpeech.speak(description + " successfully added", TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Handle clicks on markers on the map
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        // display click
        Snackbar.make(fab_add_new, marker.getTitle(), Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Handle menu clicks
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cmd_settings:
                startActivity(new Intent(getBaseContext(), ActivityHome_Prefence.class));
                break;
            case R.id.cmd_charts:
                startActivity(new Intent(getBaseContext(), ActivityCharts.class));
                break;
            case R.id.cmd_logout:
                SharedPreferences prefs = getSharedPreferences("LoggedInUsersPrefs", 0);

                //prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", "admin@blackspotter.com");
                editor.commit();

                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(getBaseContext(), ActivityLogin.class));
                break;
            case R.id.cmd_directions:

                if (my_current_location == null) {
                    Snackbar.make(fab_add_new, "Cannot find myLocation!!", Snackbar.LENGTH_LONG).show();
                } else {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // request permission
                        ActivityCompat.requestPermissions(
                                my_activity,
                                new String[]{
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION}, 111);
                    } else {
                        try {
                            Toast.makeText(getBaseContext(), "Pick a destination", Toast.LENGTH_SHORT).show();
                            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
//                                    .setFilter(typeFilter)
                                    .build(this);
                            startActivityForResult(intent, PLACE_PICKER_REQUEST_CODE);
                        } catch (GooglePlayServicesRepairableException e) {
                            Toast.makeText(getBaseContext(), "Repair Google Play services", Toast.LENGTH_SHORT).show();
                            // TODO: Handle the error.
                        } catch (GooglePlayServicesNotAvailableException e) {
                            // TODO: Handle the error.
                            Toast.makeText(ActivityHome.this, "Google Play Services Missing", Toast.LENGTH_SHORT).show();
                        }

//                        // request for location picker
//                        try {
//                            int PLACE_PICKER_REQUEST = 888;
//                            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//                            // start place picker
//                            startActivityForResult(builder.build(my_activity), PLACE_PICKER_REQUEST);
//                        } catch (GooglePlayServicesRepairableException e) {
//                            Toast.makeText(my_activity, "Repair your GooglePlayServices", Toast.LENGTH_SHORT).show();
//                        } catch (GooglePlayServicesNotAvailableException e) {
//                            Toast.makeText(my_activity, "GooglePlayServices not found", Toast.LENGTH_SHORT).show();
//                        }
                    }
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 111:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted... user can retry operation
                    Snackbar.make(fab_add_new, "Re-try Request", Snackbar.LENGTH_LONG).show();

                    // enable my location
                    mMap.setMyLocationEnabled(true);
                } else {
                    // toast
                    Snackbar.make(fab_add_new, "User Denied Request", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // trigger rerun of notifier
        sendBroadcast(new Intent("REQUEST_TO_SERVICE"));
    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 777:
                    // get String values the recognition engine thought it heard
                    ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    // handle voice command
                    handle_voice_command(matches);

                    break;
                case 888:
                    final Place place = PlacePicker.getPlace(data, this);

                    LatLng origin = new LatLng(my_current_location.getLatitude(), my_current_location.getLongitude());
                    LatLng dest = place.getLatLng();

                    Log.e("Kibet", "Origin:\nLat>>" + my_current_location.getLatitude() + "\nLong>>" + my_current_location.getLongitude());

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);

                    break;
                case 999:

                    Bitmap photo = (Bitmap) data.getExtras().get("data");

                    String encodedRes = ImageBase64
                            .with(getApplicationContext())
                            .requestSize(512, 512)
                            .encodeFile(photo);

                    new_point.setPhoto(encodedRes);

                    new MaterialDialog.Builder(this)
                            .title("Description")
                            .items(new String[]{"Weather", "Loose Chippings", "Damaged Bridge", "Drunk Driving", "Recklessness", "Poor Roads"})
                            .positiveColor(Color.GREEN)
                            .positiveText("Proceed")
                            .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    // set cause
                                    new_point.setCause(text.toString());

                                    // create bundle
                                    Bundle data = new Bundle();
                                    data.putParcelable("new_point", new_point);

                                    // create new intent
                                    Intent dataIntent = new Intent(getBaseContext(), AddPointOnline.class);
                                    dataIntent.putExtras(data);

                                    // start background save action
                                    startService(dataIntent);

//                                    // add point to DB
//                                    new AddPointToOnlineDB(getApplicationContext(), handler, my_activity, mMap, fab_add_new).add_this_point(new_point);
                                    return true;
                                }
                            })
                            .negativeColor(Color.RED)
                            .negativeText("Cancel")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    new_point = null;
                                }
                            })
                            .show();

                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handle_voice_command(ArrayList<String> matches) {

        // initialize match found
        boolean match_found = true;

        // loop through all matches
        String confirmation = "";
        for (int i = 0; i < matches.size(); i++) {
            // get current word in loop
            String wording = matches.get(i);
            Log.i("Kibet", "Checking " + wording);

            // check for matches
            if (wording.equals("set black spot")) {
                // add point to DB
                add_location_to_DB_via_voice("Black spot", my_current_location);

                // set confirmation message
                confirmation = "Black spot added";

                // break loop
                break;
            } else if (wording.equals("set danger zone")) {
                // add point to DB
                add_location_to_DB_via_voice("Danger zone", my_current_location);

                // set confirmation message
                confirmation = "Danger zone added";

                // break loop
                break;
            } else if (wording.equals("set accident scene")) {
                // add point to DB
                add_location_to_DB_via_voice("Accident scene", my_current_location);

                // set confirmation message
                confirmation = "Accident scene added";

                // break loop
                break;
            } else if (wording.equals("cancel")) {
                // set confirmation message
                confirmation = "Cancel";
            } else {
                // no match found
                match_found = false;
            }
        }

        // check if there was no matches
        if (!match_found) {
            if (confirmation.equals("Cancel")) {
                // ask for retrial
                textToSpeech.speak("Request canceled", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                // ask for retrial
                textToSpeech.speak("Please try again", TextToSpeech.QUEUE_FLUSH, null);

                // sleep
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // error
                }

                // relaunch speech recognizer
                PackageManager pm = getPackageManager();
                List<ResolveInfo> activities = pm.queryIntentActivities(
                        new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
                if (activities.size() == 0) {
                    Snackbar.make(fab_add_new, "Recognizer not present", Snackbar.LENGTH_SHORT).show();
                } else {
                    // start voice recognition activity
                    startVoiceRecognitionActivity();
                }
            }
        } else {
            // confirm action
            textToSpeech.speak(confirmation, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }

            Log.i("Kibet", "Direct JSON >> " + data);// TODO: 8/8/16  delete

            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }

        /**
         * A method to download json data from url
         */
        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            Log.i("Kibet", jsonData.toString());

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                Log.i("Kibet", "Error parsing google places: " + e.getMessage());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            Log.i("Kibet", "RESULT >> " + result.toString());
            if (!(result.size() > 0)) {
                Snackbar.make(fab_add_new, "No Start OR API Quota Reached", Snackbar.LENGTH_LONG).show();
            } else {

                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.BLUE);
                }

                // remove any polyline on the map
                if (current_polyline != null) {
                    current_polyline.remove();
                }

                // Drawing polyline in the Google Map for the i-th route
                current_polyline = mMap.addPolyline(lineOptions);
                //// TODO: 7/29/16 add a marker to the destination

                // check if any point lies on the path
                getBlackspotsOnPath(points);

                // zoom map to show path
                auto_zoom_map_to_show_path(points);
            }
        }

        private void auto_zoom_map_to_show_path(final ArrayList<LatLng> points) {
            my_activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    // add all points on polyline
                    for (LatLng item : points) {
                        builder.include(item);
                    }

                    LatLngBounds bounds = builder.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                    mMap.animateCamera(cu);
                }
            });
        }

        /**
         * Computes whether the given point lies on or near a polyline, within a specified
         * tolerance in meters. The polyline is composed of great circle segments if geodesic
         * is true, and of Rhumb segments otherwise. The polyline is not closed -- the closing
         * segment between the first point and the last point is not included.
         */
        public void getBlackspotsOnPath(List<LatLng> polyline_points) {
            // set distance to road of points
            double tolerance = 1000; // meters

            // initialize counters
            int blackspot_count = 0;
            int accident_scene_count = 0;
            int danger_zone_count = 0;

            for (int i = 0; i < all_points.size(); i++) {
                // get point reference
                MyPointOnMap my_point = all_points.get(i);

                // get description
                String description = my_point.getDescription();

                // generate LatLng
                LatLng latlong = new LatLng(Double.parseDouble(my_point.getLatitude()), Double.parseDouble(my_point.getLongitude()));

                // check if location is on map
                boolean isLocationOnPath = PolyUtil.isLocationOnPath(latlong, polyline_points, true, tolerance);

                if (isLocationOnPath) {

                    // know the type of point we are dealing with
                    if (description.equals("Black spot")) {
                        blackspot_count += 1;
                    } else if (description.equals("Danger zone")) {
                        danger_zone_count += 1;
                    } else if (description.equals("Accident scene")) {
                        accident_scene_count += 1;
                    }
                }
            }
            // update UI
            lbl_blackspots.setText("B-S: " + blackspot_count);
            lbl_accidents.setText("A-S: " + accident_scene_count);
            lbl_danger_zones.setText("D-Z: " + danger_zone_count);
        }
    }

//    private class addMarkersToMap extends AsyncTask<String, String, String> {
//        @Override
//        protected String doInBackground(String... strings) {
//            // get database reference
//            BlackspotDBHandler my_db = new BlackspotDBHandler(getBaseContext());
//
//            // fetch all points from DB
//            all_points = my_db.getAllPoints();
//
//            // loop to create markers
//            for (int i = 0; i < all_points.size(); i++) {
//                // get point reference
//                MyPointOnMap my_point = all_points.get(i);
//
//                // get description
//                String description = my_point.getDescription();
//
//                // Add a marker
//                LatLng new_point = new LatLng(Double.parseDouble(my_point.getLatitude()), Double.parseDouble(my_point.getLongitude()));
//                final MarkerOptions point_on_map = new MarkerOptions();
//                point_on_map.title(my_point.getName());
//                point_on_map.draggable(false);
//                point_on_map.alpha(0.9f);
//                point_on_map.position(new_point);
//
//                // know the type of point we are dealing with
//                if (description.equals("Black spot")) {
//                    point_on_map.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                } else if (description.equals("Danger zone")) {
//                    point_on_map.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
//                } else if (description.equals("Accident scene")) {
//                    point_on_map.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                }
//
//                // add markers on main thread
//                my_activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //add marker
//                        mMap.addMarker(point_on_map);
//                    }
//                });
//
//
//            }
//            return null;
//        }
//    }
}
