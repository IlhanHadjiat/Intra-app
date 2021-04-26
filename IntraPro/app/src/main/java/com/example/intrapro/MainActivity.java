package com.example.intrapro;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    public JSONObject getIdentity(final VolleyCallback callback) {

        JRequest("https://auth.etna-alternance.net/identity", new VolleyCallback() {
            @Override
            public JSONObject jsonSuccess(JSONObject result) {
                try {
                    String login = result.getString("login");
                    String url = "https://gsa-api.etna-alternance.net/students/" + login + "/logs";
                    JRequest(url, new VolleyCallback() {
                        @Override
                        public JSONObject jsonSuccess(JSONObject result) {
                            callback.jsonSuccess(result);
                            return result;
                        }

                        @Override
                        public Bitmap imageSuccess(Bitmap image) {
                            return null;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public Bitmap imageSuccess(Bitmap image) {
                return null;
            }
        });
        return null;
    }

    public void IRequest(String url, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        ImageRequest imageRequest = new ImageRequest(
                url, // Image URL
                new Response.Listener<Bitmap>() { // Bitmap listener
                    @Override
                    public void onResponse(Bitmap response) {
                        // Do something with response
                        callback.imageSuccess(response);

                    }
                },
                0, // Image width
                0, // Image height
                ImageView.ScaleType.CENTER_CROP, // Image scale type
                Bitmap.Config.RGB_565, //Image decode configuration
                new Response.ErrorListener() { // Error listener
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Do something with error response
                        // error.printStackTrace();
                        // Snackbar.make(mCLayout,"Error",Snackbar.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Cookie", "authenticator=" + getString(R.string.token));

                return params;
            }
        };

        // Add ImageRequest to the RequestQueue
        queue.add(imageRequest);

    }


    public void JRequest(String url, final VolleyCallback callback) {
        //final TextView FullName = (TextView) findViewById(R.id.fullname);

        RequestQueue queue = Volley.newRequestQueue(this);
        //String url = "https://intra-api.etna-alternance.net/promo";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                callback.jsonSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "request failed");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Cookie", "authenticator=" + getString(R.string.token));
                return params;
            }
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                Log.d("response",response.headers.toString());
                Map<String, String> responseHeaders = response.headers;
                //String rawCookies = responseHeaders.get("Set-Cookie");
               // Log.d("cookies",rawCookies);
                return super.parseNetworkResponse(response);
            }
        };
        queue.add(req);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        getIdentity(new VolleyCallback() {
            @Override
            public JSONObject jsonSuccess(JSONObject result) {
                TextView destination = (TextView) findViewById(R.id.login);
                Log.e("RESULT", result.toString());
                try {
                    JSONObject student = result.getJSONObject("student");
                    destination.setText(student.getString("login"));
                    destination = findViewById(R.id.fullname);
                    destination.setText(student.getString("firstname") + " " + student.getString("lastname"));
                    String url = "https://auth.etna-alternance.net/api/users/" + student.getString("login") + "/photo";
                    IRequest(url, new VolleyCallback() {
                        @Override
                        public JSONObject jsonSuccess(JSONObject result) {

                            return null;
                        }

                        @Override
                        public Bitmap imageSuccess(Bitmap image) {
                            ImageView mImageView = findViewById(R.id.picture);
                            mImageView.setImageBitmap(image);
                            return null;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public Bitmap imageSuccess(Bitmap image) {
                return null;
            }
        });
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}