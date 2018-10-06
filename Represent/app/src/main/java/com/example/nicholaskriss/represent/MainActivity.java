package com.example.nicholaskriss.represent;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;

    public static String errorResponse = "";
    public static String loc = "";
    public static ArrayList<String> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        final TextView zipCode = findViewById(R.id.zipCode);
        final Button goButton = findViewById(R.id.goButton);
        final Button currentLocationButton = findViewById(R.id.currentLocationButton);
        final Button randomLocationButton = findViewById(R.id.randomLocationButton);
        final RequestQueue queue = Volley.newRequestQueue(this);

        /*if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            errorResponse = "Please allow location access for this app";
            startActivity(new Intent(MainActivity.this, error.class));
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());*/

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(zipCode.getText().toString()).equals("")) {
                    int zip = Integer.parseInt(zipCode.getText().toString());
                    String zipString = Integer.toString(zip);
                    int length = zipString.length();
                    for (int i = 0; i < 5 - length; i++) {
                        zipString = "0" + zipString;
                    }
                    String url = "https://api.geocod.io/v1.3/geocode?q=" + zipString
                            + "&fields=cd&api_key=3b2564e6be424945344b3bf5b5523b963e2b945";
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONArray results = response.getJSONArray("results");
                                        if (results.length() == 0) {
                                            errorResponse = "ZIP code is not valid";
                                            startActivity(new Intent(MainActivity.this, error.class));
                                        } else {
                                            ids.clear();
                                            loc = results.getJSONObject(0).getString("formatted_address");
                                            JSONObject fields = results.getJSONObject(0).getJSONObject("fields");
                                            JSONArray congressional_districts = fields.getJSONArray("congressional_districts");
                                            for (int i = 0; i < congressional_districts.length(); i++) {
                                                JSONArray current_legislators = congressional_districts.getJSONObject(i).getJSONArray("current_legislators");
                                                for (int j = 0; j < current_legislators.length(); j++) {
                                                    if (!(current_legislators.getJSONObject(j).getString("type").equals("senator")
                                                            && i != congressional_districts.length() - 1)) {
                                                        JSONObject references = current_legislators.getJSONObject(j).getJSONObject("references");
                                                        ids.add(references.getString("bioguide_id"));
                                                    }
                                                }
                                            }
                                            startActivity(new Intent(MainActivity.this, results.class));
                                        }
                                    } catch(org.json.JSONException e) {
                                        errorResponse = "ZIP code is not valid";
                                        startActivity(new Intent(MainActivity.this, error.class));
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            errorResponse = "ZIP code is not valid";
                            startActivity(new Intent(MainActivity.this, error.class));
                        }
                    });
                    queue.add(request);
                } else {
                    errorResponse = "Please enter a ZIP code";
                    startActivity(new Intent(MainActivity.this, error.class));
                }
            }
        });

        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    errorResponse = "Please allow location access for this app";
                    startActivity(new Intent(MainActivity.this, error.class));
                }

                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(100000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationCallback = new LocationCallback();

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                builder.addLocationRequest(mLocationRequest);
                mLocationSettingsRequest = builder.build();
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.i("latitude", Double.toString(latitude));
                            Log.i("longitude", Double.toString(longitude));
                            String url = "https://api.geocod.io/v1.3/reverse?q=" + Double.toString(latitude) + ","
                                    + Double.toString(longitude) + "&fields=cd&api_key=3b2564e6be424945344b3bf5b5523b963e2b945";
                            Log.d("url", url);
                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                JSONArray results = response.getJSONArray("results");
                                                if (results.length() == 0) {
                                                    errorResponse = "Location is not valid";
                                                    startActivity(new Intent(MainActivity.this, error.class));
                                                } else {
                                                    ids.clear();
                                                    JSONObject address_components = results.getJSONObject(0).getJSONObject("address_components");
                                                    loc = address_components.getString("city") + ", " + address_components.getString("state") + " " + address_components.getString("zip");
                                                    JSONObject fields = results.getJSONObject(0).getJSONObject("fields");
                                                    JSONArray congressional_districts = fields.getJSONArray("congressional_districts");
                                                    for (int i = 0; i < congressional_districts.length(); i++) {
                                                        JSONArray current_legislators = congressional_districts.getJSONObject(i).getJSONArray("current_legislators");
                                                        for (int j = 0; j < current_legislators.length(); j++) {
                                                            if (!(current_legislators.getJSONObject(j).getString("type").equals("senator")
                                                                    && i != congressional_districts.length() - 1)) {
                                                                JSONObject references = current_legislators.getJSONObject(j).getJSONObject("references");
                                                                ids.add(references.getString("bioguide_id"));
                                                            }
                                                        }
                                                    }
                                                    startActivity(new Intent(MainActivity.this, results.class));
                                                }
                                            } catch(org.json.JSONException e) {
                                                errorResponse = "Location is not valid";
                                                startActivity(new Intent(MainActivity.this, error.class));
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    errorResponse = "Location is not valid";
                                    startActivity(new Intent(MainActivity.this, error.class));
                                }
                            });
                            queue.add(request);
                        } else {
                            errorResponse = "Could not get current location";
                            startActivity(new Intent(MainActivity.this, error.class));
                        }
                    }
                });
            }
        });

        randomLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomLocationButton.setText("LOADING...");
                Random generator = new Random();
                ArrayList<JsonObjectRequest> requests = new ArrayList<>();
                final boolean[] finished = {false};
                for (int j = 0; j < 10; j++) {
                    int zip = generator.nextInt(99999);
                    String zipString = Integer.toString(zip);
                    int length = zipString.length();
                    for (int i = 0; i < 5 - length; i++) {
                        zipString = "0" + zipString;
                    }
                    String url = "https://api.geocod.io/v1.3/geocode?q=" + zipString
                            + "&fields=cd&api_key=3b2564e6be424945344b3bf5b5523b963e2b945";
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray results = response.getJSONArray("results");
                                if (results.length() != 0) {
                                    ids.clear();
                                    finished[0] = true;
                                    loc = results.getJSONObject(0).getString("formatted_address");
                                    JSONObject fields = results.getJSONObject(0).getJSONObject("fields");
                                    JSONArray congressional_districts = fields.getJSONArray("congressional_districts");
                                    for (int i = 0; i < congressional_districts.length(); i++) {
                                        JSONArray current_legislators = congressional_districts.getJSONObject(i).getJSONArray("current_legislators");
                                        for (int j = 0; j < current_legislators.length(); j++) {
                                            if (!(current_legislators.getJSONObject(j).getString("type").equals("senator") && i != congressional_districts.length() - 1)) {
                                                JSONObject references = current_legislators.getJSONObject(j).getJSONObject("references");
                                                ids.add(references.getString("bioguide_id"));
                                            }
                                        }
                                    }
                                }
                            } catch (org.json.JSONException e) {

                            }
                        }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                    });
                    requests.add(request);
                }
                for (int i = 0; i < requests.size(); i++) {
                    queue.add(requests.get(i));
                }
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                startActivity(new Intent(MainActivity.this, results.class));

            }
        });
    }
}
