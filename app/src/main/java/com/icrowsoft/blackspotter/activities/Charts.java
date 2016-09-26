package com.icrowsoft.blackspotter.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.icrowsoft.blackspotter.R;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;
import com.icrowsoft.blackspotter.sqlite_db.BlackspotDBHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by teardrops on 8/16/16.
 */
public class Charts extends AppCompatActivity {
    private int[] cases;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // use actual data
        useActual();
    }

    private void useActual() {
        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        // create array of months
        String[] months = new String[12];
        // fill array with months
        months[0] = "Jan";
        months[1] = "Feb";
        months[2] = "Mar";
        months[3] = "Apr";
        months[4] = "May";
        months[5] = "Jun";
        months[6] = "Jul";
        months[7] = "Aug";
        months[8] = "Sep";
        months[9] = "Oct";
        months[10] = "Nov";
        months[11] = "Dec";

        // create array of cases per month
        cases = new int[12];
        // fill array with zeros
        for (int i = 0; i < cases.length; i++) {
            cases[i] = 0;
        }

        // dynamically get points to plot
        List<MyPointOnMap> all_points = new BlackspotDBHandler(getBaseContext()).getAllPoints();
        for (int i = 0; i < all_points.size(); i++) {
            getMonthFromMillis(all_points.get(i).getLastModified());
        }

        ArrayList<Entry> entries = new ArrayList<>();

        // fill entries
        for (int i = 0; i < cases.length; i++) {
            entries.add(new Entry((float) cases[i], i));
        }

        LineDataSet dataset = new LineDataSet(entries, "LEGEND");

        LineData data = new LineData(months, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCircles(true);//wCubic(true);
        dataset.setDrawFilled(true);

        lineChart.setData(data);
        lineChart.animateY(5000);
    }

    private void useDummy() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry((float) 15.11, 0));
        entries.add(new Entry((float) 30.5, 1));
        entries.add(new Entry((float) 25.6, 2));
        entries.add(new Entry((float) 9.0, 3));
        entries.add(new Entry((float) 58.22, 4));
        entries.add(new Entry((float) 20.9, 5));

        LineDataSet dataset = new LineDataSet(entries, "LEGEND");

        ArrayList<String> labels = new ArrayList<>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCircles(true);//wCubic(true);
        dataset.setDrawFilled(true);

        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        lineChart.setData(data);
        lineChart.animateY(5000);
    }


    private void getMonthFromMillis(String milliSeconds) {
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliSeconds));

        int month = calendar.get(Calendar.MONTH) + 1;

        switch (month) {
            case 1:
                cases[0] = cases[0] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 2:
                cases[1] = cases[1] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 3:
                cases[2] = cases[2] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 4:
                cases[3] = cases[3] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 5:
                cases[4] = cases[4] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 6:
                cases[5] = cases[5] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 7:
                cases[6] = cases[6] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 8:
                cases[7] = cases[7] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 9:
                cases[8] = cases[8] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 10:
                cases[9] = cases[9] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
            case 11:
                cases[10] = cases[10] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));


                break;
            case 12:
                cases[11] = cases[11] + Integer.parseInt(milliSeconds.substring(milliSeconds.length() - 1));

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cmd_use_actual:
                useActual();
                break;
            case R.id.cmd_use_dummy:
                useDummy();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
