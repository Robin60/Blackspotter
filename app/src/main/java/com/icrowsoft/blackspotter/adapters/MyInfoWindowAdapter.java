package com.icrowsoft.blackspotter.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.icrowsoft.blackspotter.R;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;

import java.util.HashMap;

/**
 * Created by teardrops on 9/3/16.
 */
public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View myContentsView;
    private final HashMap<String, MyPointOnMap> _my_markers;
    private final Activity _activity;

    public MyInfoWindowAdapter(Activity activity, HashMap<String, MyPointOnMap> my_markers) {
        myContentsView = activity.getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        this._my_markers = my_markers;
        _activity = activity;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView lbl_cause = ((TextView) myContentsView.findViewById(R.id.cause));
        TextView lbl_name = ((TextView) myContentsView.findViewById(R.id.name));
        ImageView image = ((ImageView) myContentsView.findViewById(R.id.img));

        MyPointOnMap aa = _my_markers.get(marker.getTitle());
//        Toast.makeText(_activity.getBaseContext(), "" + marker.getTitle(), Toast.LENGTH_SHORT).show();

        lbl_name.setText(aa.getName());
        lbl_cause.setText(aa.getCause());

        String cause = aa.getCause();
        String photo = aa.getPhoto();

        if (photo.equals("null")) {
            if (cause.equals("Weather")) {
                image.setImageResource(R.drawable.weather);
            } else if (cause.equals("Loose Chippings")) {
                image.setImageResource(R.drawable.chippings);
            } else if (cause.equals("Damaged Bridge")) {
                image.setImageResource(R.drawable.bad_bridge);
            } else if (cause.equals("Drunk Driving")) {
                image.setImageResource(R.drawable.drunk_driving);
            } else if (cause.equals("Recklessness")) {
                image.setImageResource(R.drawable.reckless_driving);
            } else if (cause.equals("Poor Roads")) {
                image.setImageResource(R.drawable.bad_roads);
            }
        } else {
            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            image.setImageBitmap(decodedByte);
        }
        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO Auto-generated method stub
        return null;
    }
}