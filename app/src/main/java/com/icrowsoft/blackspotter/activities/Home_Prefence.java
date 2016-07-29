package com.icrowsoft.blackspotter.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import com.icrowsoft.blackspotter.R;

/**
 * Created by teardrops on 7/27/16.
 */
public class Home_Prefence extends PreferenceActivity {
    private SharedPreferences settings;
    private boolean allowed_notifications_value;
    private EditTextPreference distance_to_notify_view;
    private CheckBoxPreference allowed_notifications_view;
    private CheckBoxPreference chk_metres;
    private CheckBoxPreference chk_yards;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // add layout file
        addPreferencesFromResource(R.xml.home_settings);

        // preference manager
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // get notification preference value
        allowed_notifications_value = settings.getBoolean("allow_notifications", true);

        // get allowed notifications view
        allowed_notifications_view = (CheckBoxPreference) findPreference("allow_notifications");
        // get distance to notify view
        distance_to_notify_view = (EditTextPreference) findPreference("distance_to_notify");
        // add on change listener
        allowed_notifications_view.setOnPreferenceChangeListener(on_toggle_notifications());

        // get metres view
        chk_metres = (CheckBoxPreference) findPreference("chk_metres");
        // add on change listener
        chk_metres.setOnPreferenceChangeListener(on_toggle_metres());

        // get metres view
        chk_yards = (CheckBoxPreference) findPreference("chk_yards");
        // add on change listener
        chk_yards.setOnPreferenceChangeListener(on_toggle_yards());

        // check notification status and change affected views
        check_notification_status();


    }

    private Preference.OnPreferenceChangeListener on_toggle_notifications() {
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // change allowed notifications value
                if (allowed_notifications_value) {
                    // get notification preference value
                    allowed_notifications_value = false;

                    // change view
                    allowed_notifications_view.setChecked(false);
                } else {
                    // change
                    allowed_notifications_value = true;

                    // change view
                    allowed_notifications_view.setChecked(true);
                }

                // check notification status and change affected views
                check_notification_status();
                return false;
            }
        };
    }

    private Preference.OnPreferenceChangeListener on_toggle_yards() {
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // change allowed notifications value
                if (!chk_yards.isChecked()) {
                    // change views
                    chk_yards.setChecked(true);
                    chk_metres.setChecked(false);
                }
                return false;
            }
        };
    }

    private Preference.OnPreferenceChangeListener on_toggle_metres() {
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // change allowed notifications value
                if (!chk_metres.isChecked()) {
                    // change views
                    chk_metres.setChecked(true);
                    chk_yards.setChecked(false);
                }
                return false;
            }
        };
    }

    private void check_notification_status() {
        // check if notifications are allowed
        if (allowed_notifications_value) {
            distance_to_notify_view.setEnabled(true);
            chk_metres.setEnabled(true);
            chk_yards.setEnabled(true);
        } else {
            distance_to_notify_view.setEnabled(false);
            chk_metres.setEnabled(false);
            chk_yards.setEnabled(false);
        }
    }
}
