package com.icrowsoft.blackspotter.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.icrowsoft.blackspotter.R;
import com.icrowsoft.blackspotter.SyncDB.sync_DB_offline;
import com.icrowsoft.blackspotter.SyncDB.sync_DB_online;

import java.lang.reflect.Field;

public class Home extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private String[] names, lats, lons;
    private Animation fab_close;
    private Animation fab_open;
    private FloatingActionButton fab, fab1, fab2;
    private boolean fab_clicked;
    private FrameLayout mInterceptorFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // syc database offline
        new sync_DB_offline(getBaseContext()).execute();

        // syc database online
        new sync_DB_online(getBaseContext()).execute();

        // fetch from resources
        names = getResources().getStringArray(R.array.black_spot_names);
        lats = getResources().getStringArray(R.array.black_spot_latitudes);
        lons = getResources().getStringArray(R.array.black_spot_longitdes);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // handle clicks outside FAB
        mInterceptorFrame = (FrameLayout) findViewById(R.id.fl_interceptor);
        mInterceptorFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (fab_clicked) {
                    animateFAB();
                    return true;
                }
                return false;
            }
        });

        // get FAB animations
        fab_close = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_open);

        // handle FABs (floating action buttons)
        handleFABs();

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

        // get the accuracy TextView
        TextView accuracy = (TextView) findViewById(R.id.lbl_accuracy);
        accuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Voice listener
                startActivity(new Intent(getBaseContext(), VoiceListener.class));
            }
        });

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast my_toast = Toast.makeText(getBaseContext(), "Permission denied", Toast.LENGTH_SHORT);
            my_toast.setGravity(Gravity.CENTER, 0, 0);
            my_toast.show();

        } else {
            mMap.setMyLocationEnabled(true);
        }

        // set up map options
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setOnInfoWindowClickListener(this);

        // loop to create markers
        for (int i = 0; i < names.length; i++) {
            // Add a marker
            LatLng new_point = new LatLng(Double.parseDouble(lats[i]), Double.parseDouble(lons[i]));
            MarkerOptions point_on_map = new MarkerOptions();
            point_on_map.title(names[i]);
            point_on_map.draggable(false);
            point_on_map.alpha(0.9f);
            point_on_map.position(new_point);
            mMap.addMarker(point_on_map);
        }
    }


    private void handleFABs() {

        // link to FABs
        fab = (FloatingActionButton) findViewById(R.id.floating_atcion_bar);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        // add click events
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        // track main FAB clicks
        fab_clicked = false;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFAB();
            }
        });
    }

    public void animateFAB() {

        if (fab_clicked) {

//            fab.startAnimation(rotate_backward);
            rotateFabBackward();
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab_clicked = false;

        } else {

//            fab.startAnimation(rotate_forward);
            rotateFabForward();
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab_clicked = true;

        }
    }

    public void rotateFabForward() {
        ViewCompat.animate(fab)
                .rotation(45)
                .setDuration(400)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    public void rotateFabBackward() {
        ViewCompat.animate(fab)
                .rotation(0)
                .setDuration(400)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab1:
                animateFAB();
                break;
            case R.id.fab2:
                // hide fabs
                animateFAB();
                break;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Snackbar.make(mInterceptorFrame, "" + marker.getTitle() + " clicked", Snackbar.LENGTH_SHORT).show();
    }
}
