package com.example.nicholaskriss.represent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class results extends AppCompatActivity {
    public static String idSelect = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(MainActivity.loc);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final RequestQueue queue = Volley.newRequestQueue(this);
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        ArrayList<String> ids = MainActivity.ids;

        for (int i = 0; i < ids.size(); i++) {
            final String id = ids.get(ids.size() - i - 1);

            final ImageView image = new ImageView(this);
            linearLayout.addView(image);
            image.setAdjustViewBounds(true);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.getLayoutParams().height = 800;
            image.getLayoutParams().width = 640;
            image.setPadding(0, 50, 0, 0);
            String image_url = "http://bioguide.congress.gov/bioguide/photo/" + id.charAt(0)
                    + "/" + id + ".jpg";
            ImageRequest photoRequest = new ImageRequest(image_url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            image.setImageBitmap(response);
                        }
                    }, 260, 300, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
            queue.add(photoRequest);

            final TextView text = new TextView(this);
            linearLayout.addView(text);
            text.setTextSize(20);
            text.setTextColor(Color.BLACK);
            text.setGravity(Gravity.CENTER);
            text.setPadding(0, 35, 0, 35);
            String url = "https://api.propublica.org/congress/v1/members/" + id
                    + ".json";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject results = response.getJSONArray("results").getJSONObject(0);
                                JSONObject roles = results.getJSONArray("roles").getJSONObject(0);
                                String title = roles.getString("short_title");
                                String first_name = results.getString("first_name");
                                String last_name = results.getString("last_name");
                                String current_party = results.getString("current_party");
                                if (current_party.equals("D")) {
                                    current_party = "Democrat";
                                } else if (current_party.equals("R")) {
                                    current_party = "Republican";
                                } else {
                                    current_party = "Independent";
                                }
                                String web_url = results.getString("url");
                                if (web_url.equals("null")) {
                                    web_url = "No website listed";
                                }
                                String contact_form = roles.getString("contact_form");
                                if (contact_form.equals("null")) {
                                    contact_form = "No contact listed";
                                }
                                String output = title + " " + first_name + " " + last_name + "\n"
                                        + current_party + "\n" + web_url + "\n" + contact_form;
                                SpannableStringBuilder spanOutput = new SpannableStringBuilder(output);
                                int start = title.length() + first_name.length() + last_name.length() + current_party.length() + 4;
                                URLSpan urlSpan = new URLSpan(web_url);
                                URLSpan contactSpan = new URLSpan(contact_form);
                                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.rgb(0, 0, 255));
                                RelativeSizeSpan sizeSpan1 = new RelativeSizeSpan(1.5f);
                                RelativeSizeSpan sizeSpan2 = new RelativeSizeSpan(1.25f);
                                if (!web_url.equals("No website listed")) {
                                    spanOutput.setSpan(urlSpan, start, start + web_url.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                if (!contact_form.equals("No contact listed")) {
                                    spanOutput.setSpan(contactSpan, start + web_url.length(), output.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                if (!web_url.equals("No website listed") && contact_form.equals("No contact listed")) {
                                    spanOutput.setSpan(colorSpan, start, output.length() - contact_form.length() - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                } else if (web_url.equals("No website listed") && !contact_form.equals("No contact listed")) {
                                    spanOutput.setSpan(colorSpan, start + web_url.length() + 1, output.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                } else if (!web_url.equals("No website listed") && !contact_form.equals("No contact listed")) {
                                    spanOutput.setSpan(colorSpan, start, output.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                spanOutput.setSpan(sizeSpan1, 0, start - current_party.length() - 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                spanOutput.setSpan(sizeSpan2, start - current_party.length() - 2, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                text.setText(spanOutput);
                                text.setMovementMethod(LinkMovementMethod.getInstance());
                            } catch(org.json.JSONException e) {
                                MainActivity.errorResponse = "Data not available";
                                startActivity(new Intent(results.this, error.class));
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            MainActivity.errorResponse = "Call failed";
                            startActivity(new Intent(results.this, error.class));
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
            queue.add(request);

            Button button = new Button(this);
            linearLayout.addView(button);
            button.setText("MORE INFO");
            button.setTextSize(30);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    idSelect = id;
                    startActivity(new Intent(results.this, more_info.class));
                }
            });
        }
    }

}
