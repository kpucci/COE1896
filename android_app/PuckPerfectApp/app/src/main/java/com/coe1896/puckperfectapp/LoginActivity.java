package com.coe1896.puckperfectapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    private String idURL ="http://www.katiepucci.com/puckperfect/api/userid";
    private String authURL ="http://www.katiepucci.com/puckperfect/auth";

    // Queue for HTTP requests through volley
    RequestQueue queue;

    // Preferences
    private SharedPreferences.Editor editor;
    private static final String MY_PREFS_NAME = "PuckPerfectPrefs";

    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

        // Set up the login form.
        email = (EditText) findViewById(R.id.usernameText);

        Button signIn = (Button) findViewById(R.id.signInButton);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getToken();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        queue = MySingleton.getInstance(getApplicationContext()).
                getRequestQueue();
    }

    /**
     * Get JWT token with credentials
     */
    public void getToken()
    {
        Log.i(TAG, "-----Get Token-----");

        queue = MySingleton.getInstance(getApplicationContext()).getRequestQueue();

        // Get email and password from text boxes
        email = (EditText) findViewById(R.id.usernameText);
        final EditText password = (EditText) findViewById(R.id.passwordText);

        // Setup data to send with POST request
        JSONObject json = new JSONObject();
        try
        {
            json.put("username",email.getText().toString());
            json.put("password",password.getText().toString());
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

        // POST request to auth to receive JWT token
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, authURL, json, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            String token = response.getString("access_token");
                            if(token.length() > 0)
                            {
                                // Store token
                                editor.putString("jwt_token", token);
                                editor.apply();

                                // Get user id from token
                                getId();
                            }
                            else
                            {
                                loginError();
                            }
                        }catch(JSONException e)
                        {
                            // TODO: Error handling
                        }

                    }
                }, new Response.ErrorListener() {

                    // If an error occurred, then credentials were wrong
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loginError();
                    }
                });

        jsonObjectRequest.setTag(TAG);
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Get the user id associated with the JWT token
     */
    public void getId()
    {
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
                            login();
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
     * Start register activity
     * @param view - current view object
     */
    public void register(View view)
    {
        Log.i(TAG, "-----Register-----");
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    /**
     * Open the main page
     */
    public void login()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Login error message
     */
    public void loginError()
    {
        Log.i(TAG, "-----LoginActivity Error-----");
        Toast.makeText(getApplicationContext(),"Incorrect email or password.",Toast.LENGTH_LONG).show();
    }
}
