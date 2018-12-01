package com.coe1896.puckperfectapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import android.support.design.widget.NavigationView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/* Main profile page */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String MY_PREFS_NAME = "PuckPerfectPrefs";
    private String tokenURL ="http://www.katiepucci.com/puckperfect/api/token";
    private String idURL ="http://www.katiepucci.com/puckperfect/api/userid";
    private String playersURL ="http://www.katiepucci.com/puckperfect/players/";

    private RequestQueue queue; // Queue for HTTP requests through volley
    private SharedPreferences.Editor editor;    // Preferences
    private DrawerLayout mDrawerLayout; // Navigation drawer

    // Slide view
    private ProfilePageAdapter mProfilePageAdapter;
    private ViewPager mViewPager;


    /**
     * Initialization of main activity. Called when app opens
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "----- ON CREATE METHOD INVOKED -----");

        // Instantiate the preference editor
        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

        // Attempt to login using stored token (if it exists)
        checkToken();

        setContentView(R.layout.activity_main);
    }

    /**
     * Called when activity is made visible
     */
    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.i(TAG, "----- ON RESUME METHOD INVOKED -----");

        queue = MySingleton.getInstance(getApplicationContext()).
                getRequestQueue();
    }

    /**
     * Called when activity is no longer visible
     */
    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    /**
     * Check if the stored JWT token is valid
     */
    public void checkToken()
    {
        Log.i(TAG, "----- CHECK TOKEN METHOD INVOKED -----");
        // GET request to api/token to check token validity
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, tokenURL, null, new Response.Listener<JSONObject>() {

                    // If HTTP request returns without error, the token is valid
                    @Override
                    public void onResponse(JSONObject response) {

                        // Check if user id is already in preferences
                        // Get user id
                        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                        String userId = prefs.getString("user_id", null);

                        if(userId != null && !userId.isEmpty())
                            getPlayerInfo(userId);

                        else
                            getId();
                    }

                }, new Response.ErrorListener() {

                    // If there was an error, JWT is invalid --> force user to login again
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // Token is incorrect or has expired --> start login activity
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                })
        {
            // Set HTTP headers
            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                String token = prefs.getString("jwt_token", null);
                Log.i(TAG, token);
                headers.put("Authorization", "JWT " + token);
                return headers;
            }
        };

        jsonObjectRequest.setTag(TAG);
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Get the user id associated with the JWT token
     */
    public void getId()
    {
        Log.i(TAG, "----- GET ID METHOD INVOKED -----");
        // GET request to api/userid to receive userid
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, idURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            String id = response.getString("id");
                            editor.putString("user_id", id);
                            editor.apply();
                            getPlayerInfo(id);
                        }
                        catch(JSONException e)
                        {
                            Toast.makeText(getApplicationContext(),"getId Error",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"getId Error",Toast.LENGTH_LONG).show();
                    }
                })
        {
            // Set HTTP headers
            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                String token = prefs.getString("jwt_token", null);
                headers.put("Authorization", "JWT " + token);
                return headers;
            }
        };

        jsonObjectRequest.setTag(TAG);
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Get the player's user data from database
     */
    public void getPlayerInfo(String userId)
    {
        Log.i(TAG, "----- GET PLAYER INFO METHOD INVOKED -----");

        // GET request to players/userId to retrieve user data
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, playersURL + userId, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            // Set profile title to player's name
                            String firstName = response.getString("first_name");
                            String lastName = response.getString("last_name");
                            setTitle(firstName + " " + lastName);

                            // Show view
                            setContentView(R.layout.activity_main);

                            Log.i(TAG, "----- SET CONTENT VIEW -----");

                            // Setup navigation drawer
                            Toolbar toolbar = findViewById(R.id.toolbar);
                            setSupportActionBar(toolbar);
                            ActionBar actionbar = getSupportActionBar();
                            actionbar.setDisplayHomeAsUpEnabled(true);
                            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

                            mDrawerLayout = findViewById(R.id.drawer_layout);

                            Log.i(TAG, "----- SETUP TOOLBAR -----");

                            NavigationView navigationView = findViewById(R.id.nav_view);
                            navigationView.setNavigationItemSelectedListener(
                                    new NavigationView.OnNavigationItemSelectedListener() {
                                        @Override
                                        public boolean onNavigationItemSelected(MenuItem menuItem) {
                                            // set item as selected to persist highlight
                                            menuItem.setChecked(true);
                                            // close drawer when item is tapped
                                            mDrawerLayout.closeDrawers();

                                            CharSequence itemTitle = menuItem.getTitle();

                                            switch(itemTitle.toString())
                                            {
                                                case "Settings":
                                                    loadSettingsFragment();
                                                    break;
                                                case "Logout":
                                                    logout();
                                                    break;
                                                case "Bluetooth":
                                                    // TODO: Open bluetooth page
                                                    break;
                                                default:
                                                    // Something went really wrong
                                                    Log.i(TAG, "getPlayerInfo error");
                                            }

                                            return true;
                                        }
                                    });

                            Log.i(TAG, "----- SETUP NAVIGATION VIEW -----");

                            mDrawerLayout.addDrawerListener(
                                    new DrawerLayout.DrawerListener() {
                                        @Override
                                        public void onDrawerSlide(View drawerView, float slideOffset) {
                                            // Respond when the drawer's position changes
                                        }

                                        @Override
                                        public void onDrawerOpened(View drawerView) {
                                            // Respond when the drawer is opened
                                        }

                                        @Override
                                        public void onDrawerClosed(View drawerView) {
                                            // Respond when the drawer is closed
                                        }

                                        @Override
                                        public void onDrawerStateChanged(int newState) {
                                            // Respond when the drawer motion state changes
                                        }
                                    }
                            );

                            Log.i(TAG, "----- ADDED DRAWER VIEW -----");


                            mProfilePageAdapter = new ProfilePageAdapter(getSupportFragmentManager(), getApplicationContext());
                            mViewPager = (ViewPager) findViewById(R.id.pager);
                            mViewPager.setAdapter(mProfilePageAdapter);

                            Log.i(TAG, "----- SETUP PAGE ADAPTER -----");

                            // Give the TabLayout the ViewPager
                            TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
                            tabLayout.setupWithViewPager(mViewPager);

                            Log.i(TAG, "----- SETUP TAB LAYOUT -----");


                        }catch(JSONException e)
                        {
                            // TODO: Error handling
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"getPlayerInfo Error",Toast.LENGTH_LONG).show();
                    }
                });

        jsonObjectRequest.setTag(TAG);
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void loadSettingsFragment()
    {

    }

    public void logout()
    {
        editor.clear();
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
