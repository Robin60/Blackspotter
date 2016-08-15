package com.icrowsoft.blackspotter.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.icrowsoft.blackspotter.R;

import java.util.ArrayList;

/**
 * Created by teardrops on 8/16/16.
 */
public class Charts extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charts);

        LineChart lineChart = (LineChart) findViewById(R.id.chart);

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

        lineChart.setData(data);
        lineChart.animateY(5000);
    }
}
