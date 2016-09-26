package com.icrowsoft.blackspotter.general;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by teardrops on 9/23/16.
 */

public class MyTimeHandler {
    public static String convert_timestamp_to_millis(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date testDate = null;
        if (dateString != null) {
            try {
                testDate = sdf.parse(dateString);
            } catch (java.text.ParseException e) {
                Log.e("Kibet", "Error parsing date: " + e.getMessage());
            }
        }
        return "" + testDate.getTime();
    }

    public static String getDateForServer(long milliSeconds) {
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        String str_month = (month > 9) ? "" + month : "0" + month;
        String str_day = (day > 9) ? "" + day : "0" + day;
        String str_hour = (hour > 9) ? "" + hour : "0" + hour;
        String str_min = (min > 9) ? "" + min : "0" + min;
        String str_sec = (sec > 9) ? "" + sec : "0" + sec;

        return calendar.get(Calendar.YEAR) + "-" +
                str_month + "-" +
                str_day + " " +
                str_hour + ":" +
                str_min + ":" +
                str_sec;
    }
}
