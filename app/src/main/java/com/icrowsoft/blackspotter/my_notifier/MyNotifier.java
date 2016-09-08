package com.icrowsoft.blackspotter.my_notifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.icrowsoft.blackspotter.R;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class MyNotifier {
    public static void notify_user(Context context, String title, String msg) {

        // preference manager
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean sounds_on = settings.getBoolean("allow_notifications", true);

        String ringtone = settings.getString("notif_sound", "");//DEFAULT_SOUND

        // set ringtone
        if (sounds_on) {
            //spawn notification
            PugNotification.with(context)
                    .load()
                    .title(title)
                    .message(msg)
//                .bigTextStyle("You are approaching a known blackspot")
                    .smallIcon(R.mipmap.ic_launcher)
//                .largeIcon(R.drawable.pugnotification_ic_launcher)
//                    .flags(Notification.DEFAULT_ALL)
                    .ticker("Blackspotter warning. Please pay attention")
                    .sound(Uri.parse(ringtone))
                    .autoCancel(true)
                    .simple()
                    .build();
        }



//        Intent intent = new Intent(context, Home.class);// TODO: 7/30/16 handle notif clicks
//
//        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle(title)
//                .setVibrate(null)
//                .setContentText(msg)
//                .setAutoCancel(true);
//
//        // preference manager
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//
//        boolean sounds_on = settings.getBoolean("allow_notifications", true);
//
//        String ringtone = settings.getString("notif_sound", "");//DEFAULT_SOUND
//
//        // set ringtone
//        if (sounds_on) {
//            mBuilder.setSound(Uri.parse(ringtone));
//        }
//
//        // cancel any pending notifications
//        mNotificationManager.cancel(0);
//
//        // spawn new notification
//        mBuilder.setContentIntent(contentIntent);
//        mNotificationManager.notify(0, mBuilder.build());
    }
}
