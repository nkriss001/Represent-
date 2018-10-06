package com.example.nicholaskriss.represent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class more_info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(MainActivity.loc);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final RequestQueue queue = Volley.newRequestQueue(this);
        final TextView name = findViewById(R.id.name);
        final ImageView image = findViewById(R.id.image);
        final TextView party = findViewById(R.id.party);
        final TextView committees = findViewById(R.id.commitees);
        final TextView bills = findViewById(R.id.bills);

        String image_url = "http://bioguide.congress.gov/bioguide/photo/" + results.idSelect.charAt(0)
                + "/" + results.idSelect + ".jpg";
        ImageRequest photoRequest = new ImageRequest(image_url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        image.setImageBitmap(response);
                    }
                }, 160, 200, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("error", "error");
                        image.setImageDrawable(Drawable.createFromPath("drawable/no_image.png"));
                    }
                });
        queue.add(photoRequest);
        String url = "https://api.propublica.org/congress/v1/members/" + results.idSelect
                + ".json";
        JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject results = response.getJSONArray("results").getJSONObject(0);
                            JSONObject roles = results.getJSONArray("roles").getJSONObject(0);
                            String title = roles.getString("short_title");
                            if (title.equals("Rep.")) {
                                title = "Representative";
                            } else {
                                title = "Senator";
                            }
                            String first_name = results.getString("first_name");
                            String last_name = results.getString("last_name");
                            name.setText(title + " " + first_name + " " + last_name);
                            String current_party = results.getString("current_party");
                            if (current_party.equals("D")) {
                                current_party = "Democrat";
                                name.setBackgroundColor(Color.BLUE);
                                party.setBackgroundColor(Color.BLUE);
                            } else if (current_party.equals("R")) {
                                current_party = "Republican";
                                name.setBackgroundColor(Color.RED);
                                party.setBackgroundColor(Color.RED);
                            } else {
                                current_party = "Independent";
                                name.setBackgroundColor(Color.GRAY);
                                party.setBackgroundColor(Color.GRAY);
                            }
                            party.setText(current_party);
                            SpannableStringBuilder committeesString = new SpannableStringBuilder("");
                            JSONArray committeesObject = roles.getJSONArray("committees");
                            for (int i = 0; i < committeesObject.length(); i++) {
                                JSONObject committee = committeesObject.getJSONObject(i);
                                SpannableString committeeInfo = new SpannableString("\u2022 "
                                        + committee.getString("title") + ": " + committee.getString("name"));
                                committeeInfo.setSpan(new RelativeSizeSpan(1.00f), 0,
                                        committee.getString("title").length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                committeesString.append(committeeInfo);
                                if (i < committeesObject.length() - 1) {
                                    committeesString.append("\n");
                                }
                            }
                            committees.setText(committeesString);
                        } catch(org.json.JSONException e) {
                            committees.setText("Something went wrong");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        committees.setText("Something went wrong");
                    }
                })
        {
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("X-API-Key", "VHWYnMzi3It3lhrKGwGONzDj1yxglqUShBdESI6K");
                return headers;
            }
        };
        queue.add(request1);

        url = "https://api.propublica.org/congress/v1/members/" + results.idSelect + "/bills/introduced.json";
        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject results = response.getJSONArray("results").getJSONObject(0);
                            JSONArray billArray = results.getJSONArray("bills");
                            SpannableStringBuilder billString = new SpannableStringBuilder("");
                            for (int i = 0; i < billArray.length(); i++) {
                                JSONObject bill = billArray.getJSONObject(i);
                                SpannableString billInfo = new SpannableString("\u2022 " + bill.getString("number")
                                        + ": " + bill.getString("title")
                                        + " (" + bill.getString("introduced_date") +")");
                                billInfo.setSpan(new RelativeSizeSpan(1.00f), 0, bill.getString("number").length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                billString.append(billInfo);
                                if (i < billArray.length() - 1) {
                                    billString.append("\n\n");
                                }
                            }
                            bills.setText(billString);
                        } catch(org.json.JSONException e) {
                            bills.setText("No recent bills");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MainActivity.errorResponse = "Call failed";
                        startActivity(new Intent(more_info.this, error.class));
                    }
                })
        {
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("X-API-Key", "VHWYnMzi3It3lhrKGwGONzDj1yxglqUShBdESI6K");
                return headers;
            }
        };
        queue.add(request2);
    }

}
