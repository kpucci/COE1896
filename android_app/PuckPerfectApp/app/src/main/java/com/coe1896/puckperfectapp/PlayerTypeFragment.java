package com.coe1896.puckperfectapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class PlayerTypeFragment extends Fragment implements View.OnClickListener{

    View inflatedView;
    RequestQueue queue;
    public static final String TAG = "PlayerRegistration";
    private ArrayList<String> playerEmails = new ArrayList<>();

    public PlayerTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static PlayerTypeFragment newInstance() {
        return new PlayerTypeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedView = inflater.inflate(R.layout.fragment_player_type, container, false);
        final Button register = (Button) inflatedView.findViewById(R.id.playerRegister);
        register.setOnClickListener(this);

        getRegisteredPlayerEmails();

        final EditText firstName = (EditText) inflatedView.findViewById(R.id.playerFirstName);
        firstName.addTextChangedListener(new TextValidator(firstName) {
            @Override public void validate(TextView textView, String text) {
                if(text.equals(""))
                    textView.setError("You must enter a first name");
                else
                    textView.setError(null);
            }
        });

        final EditText lastName = (EditText) inflatedView.findViewById(R.id.playerLastName);
        lastName.addTextChangedListener(new TextValidator(lastName) {
            @Override public void validate(TextView textView, String text) {
                if(text.equals(""))
                    textView.setError("You must enter a last name");
                else
                    textView.setError(null);
            }
        });

        final EditText email = (EditText) inflatedView.findViewById(R.id.playerEmail);
        email.addTextChangedListener(new TextValidator(email) {
            @Override public void validate(TextView textView, String text) {
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches())
                    textView.setError("Invalid email");
                else if(text.equals(""))
                    textView.setError("You must enter an email address");
                else
                    textView.setError(null);
            }
        });

        final EditText password = (EditText) inflatedView.findViewById(R.id.playerPass);
        password.addTextChangedListener(new TextValidator(password) {
            @Override public void validate(TextView textView, String text) {
                final EditText passwordConfirm = (EditText) inflatedView.findViewById(R.id.playerPassConfirm);

                if(!Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", text))
                    textView.setError("Password must be 6 characters long and must contain at least one letter and one number");
                else if(text.equals(""))
                    textView.setError("You must enter a password");
                else
                    textView.setError(null);

                if(text != null && passwordConfirm != null && !text.equals(passwordConfirm.getText().toString()))
                    passwordConfirm.setError("Passwords must match");
            }
        });

        final EditText passwordConfirm = (EditText) inflatedView.findViewById(R.id.playerPassConfirm);
        passwordConfirm.addTextChangedListener(new TextValidator(passwordConfirm) {
            @Override public void validate(TextView textView, String text) {
                final EditText password = (EditText) inflatedView.findViewById(R.id.playerPass);

                if(text != null && password != null && !text.equals(password.getText().toString()))
                    textView.setError("Passwords must match");
                else if(text == null || text.equals(""))
                    textView.setError("You must confirm your password");
                else
                    textView.setError(null);
            }
        });

        return inflatedView;
    }

    @Override
    public void onClick(View view)
    {
        // Log.i(TAG, "-----POST REQUEST-----");
        final EditText firstName = (EditText) inflatedView.findViewById(R.id.playerFirstName);
        final EditText lastName = (EditText) inflatedView.findViewById(R.id.playerLastName);
        final EditText email = (EditText) inflatedView.findViewById(R.id.playerEmail);
        final EditText password = (EditText) inflatedView.findViewById(R.id.playerPass);
        final EditText passwordConfirm = (EditText) inflatedView.findViewById(R.id.playerPassConfirm);
        final Spinner hockeyLevel = (Spinner) inflatedView.findViewById(R.id.playerHockeyLevel);
        final Spinner skillLevel = (Spinner) inflatedView.findViewById(R.id.playerSkillLevel);
        final Switch hand = (Switch) inflatedView.findViewById(R.id.playerHand);

        int hockeyLevelVal = hockeyLevel.getSelectedItemPosition();
        int skillLevelVal = skillLevel.getSelectedItemPosition();
        boolean handVal = hand.isSelected();

        boolean continueFlag = true;
        if(firstName.getError() != null)
            continueFlag = false;
        else if(lastName.getError() != null)
            continueFlag = false;
        else if(email.getText() == null)
            continueFlag = false;
        else if(password.getError() != null)
            continueFlag = false;
        else if(passwordConfirm.getError() != null)
            continueFlag = false;
        else if(firstName.getText().toString().trim().equals(""))
        {
            firstName.setError("You must enter a first name");
            continueFlag = false;
            firstName.setFocusableInTouchMode(true);
            firstName.requestFocus();
        }
        else if(lastName.getText().toString().trim().equals(""))
        {
            lastName.setError("You must enter a last name");
            continueFlag = false;
            lastName.setFocusableInTouchMode(true);
            lastName.requestFocus();
        }
        else if(email.getText().toString().trim().equals(""))
        {
            email.setError("You must enter an email address");
            continueFlag = false;
            email.setFocusableInTouchMode(true);
            email.requestFocus();
        }
        else if(password.getText().toString().trim().equals(""))
        {
            password.setError("You must enter a password");
            continueFlag = false;
            password.setFocusableInTouchMode(true);
            password.requestFocus();
        }
        else if(playerEmails.contains(email.getText().toString()))
        {
            email.setError("An account already exists for this email");
            continueFlag = false;
            email.setFocusableInTouchMode(true);
            email.requestFocus();
        }

        if(continueFlag)
        {
            String url ="http://www.katiepucci.com/puckperfect/players";

            queue = MySingleton.getInstance(getActivity().getApplicationContext()).
                    getRequestQueue();

            JSONObject json = new JSONObject();
            try
            {
                json.put("email",email.getText());
                json.put("password",password.getText());
                json.put("first_name",firstName.getText());
                json.put("last_name", lastName.getText());
                json.put("hockey_level",hockeyLevelVal);
                json.put("skill_level",skillLevelVal);
                json.put("hand",handVal);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Go home
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Toast.makeText(getActivity().getApplicationContext(),"An error occurred during registration.",Toast.LENGTH_LONG).show();
                        }
                    }
            );

            MySingleton.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
        }
    }

    public void getRegisteredPlayerEmails()
    {
        String url ="http://www.katiepucci.com/puckperfect/players";

        queue = MySingleton.getInstance(getActivity().getApplicationContext()).
                getRequestQueue();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        playerEmails = new ArrayList<>();
                        try{
                            // Loop through the array elements
                            for(int i=0;i<response.length();i++){
                                // Get current json object
                                JSONObject email = response.getJSONObject(i);

                                // Add email to list
                                playerEmails.add(email.getString("email"));
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        jsonArrayRequest.setTag(TAG);

        MySingleton.getInstance(getActivity()).addToRequestQueue(jsonArrayRequest);
    }
}
