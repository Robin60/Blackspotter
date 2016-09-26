package com.icrowsoft.blackspotter.online_handlers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.icrowsoft.blackspotter.my_objects.MyPointOnMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by teardrops on 9/8/16.
 */
public class AddPointToPHP {
    public void addPoint(final Context _context, final MyPointOnMap point) {
        Log.e("Kibet", "Add to PHP triggered");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                //Creating a string request
                StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.43.169/blackspotter/add_point.php",
                        new Response.Listener<String>() {
                            public int current_version;

                            @Override
                            public void onResponse(String response) {
                                Log.e("Gdane", "Add to PHP response >> " + response);// TODO: 8/25/16 delete
                                JSONObject j = null;
                                current_version = 0;
                                try {
                                    //Parsing the fetched Json String to JSON Object
                                    j = new JSONObject(response);

                                    if (j.getString("success").equals("404")) {
                                        Log.e("Gdane", "No version online!");
                                    } else if (j.getString("success").equals("200")) {
                                        Log.i("Kibet", "Added successfully");
                                    }
                                } catch (JSONException e) {
                                    Log.e("Kibet", "JSON Error adding point: " + e.getMessage());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // get error
                                Log.e("Kibet", "Error adding point: " + error.getMessage());
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("name", point.getName());
                        params.put("latitude", point.getLatitude());
                        params.put("longitude", point.getLongitude());
                        params.put("cases", String.valueOf(point.getCases()));
                        params.put("last_modified", String.valueOf(point.getLastModified()));
                        params.put("description", String.valueOf(point.getDescription()));
                        params.put("photo", String.valueOf(point.getPhoto()));
                        params.put("cause", String.valueOf(point.getCause()));
                        params.put("firebase_key", String.valueOf(point.getFirebaseKey()));
                        params.put("country", String.valueOf(point.getCountry()));

//                        Log.e("Kibet", "Add to PHP params >> " + params);

                        return params;
                    }
                };

                //progress.dismiss();
                //Creating a request queue
                RequestQueue requestQueue = Volley.newRequestQueue(_context);
                //Adding request to the queue
                requestQueue.add(stringRequest);
                return null;
            }
        }.execute();
    }
}
