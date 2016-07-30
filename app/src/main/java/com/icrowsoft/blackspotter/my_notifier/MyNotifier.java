package com.icrowsoft.blackspotter.my_notifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.icrowsoft.blackspotter.R;
import com.icrowsoft.blackspotter.activities.Home;

public class MyNotifier {
    public void notify_user(Context context, String title, String msg) {

        Intent intent = new Intent(context, Home.class);// TODO: 7/30/16 create a new class or handle notif clicks

        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true);

        // preference manager
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean sounds_on = settings.getBoolean("allow_notifications", true);
        boolean vibration = settings.getBoolean("allow_vibration", true);

        String ringtone = settings.getString("notif_sound", "");

        if (ringtone.equals("")) {
            ringtone = Settings.System.DEFAULT_NOTIFICATION_URI.getPath();
        }

        // set ringtone
        if (sounds_on) {
            mBuilder.setSound(Uri.parse(ringtone));
        }

        // set vibration
        if (vibration) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        // cancel any pending notifications
        mNotificationManager.cancel(0);

        // spawn new notification
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
