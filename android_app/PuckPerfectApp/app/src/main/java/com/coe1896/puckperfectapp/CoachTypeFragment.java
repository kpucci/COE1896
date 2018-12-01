package com.coe1896.puckperfectapp;

import android.content.Context;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class CoachTypeFragment extends Fragment implements View.OnClickListener {

    View inflatedView;
    RequestQueue queue;
    public static final String TAG = "CoachRegistration";

    public CoachTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CoachTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoachTypeFragment newInstance() {
        return new CoachTypeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedView = inflater.inflate(R.layout.fragment_coach_type, container, false);
        final Button register = (Button) inflatedView.findViewById(R.id.coachRegister);
        register.setOnClickListener(this);

        return inflatedView;
    }

    @Override
    public void onClick(View view)
    {
        Log.i(TAG, "-----POST REQUEST-----");
        final EditText firstName = (EditText) inflatedView.findViewById(R.id.coachFirstName);
        final EditText lastName = (EditText) inflatedView.findViewById(R.id.coachLastName);
        final EditText email = (EditText) inflatedView.findViewById(R.id.coachEmail);
        final EditText password = (EditText) inflatedView.findViewById(R.id.coachPass);
        final EditText passwordConfirm = (EditText) inflatedView.findViewById(R.id.coachPassConfirm);

        boolean continueFlag = true;
        if(firstName.getText() == null)
        {
            // Display error message
            continueFlag = false;
        }
        else if(lastName.getText() == null)
        {
            // Display error message
            continueFlag = false;
        }
        else if(email.getText() == null)
        {
            // Display error message
            continueFlag = false;
        }
        else if(password == null)
        {
            // Display error message
            continueFlag = false;
        }
        else if(passwordConfirm == null)
        {
            // Display error message
            continueFlag = false;
        }
        else if(password != passwordConfirm)
        {
            // Display error message
            continueFlag = false;
        }


        String url ="http://www.katiepucci.com/puckperfect/coaches";

        queue = MySingleton.getInstance(getActivity().getApplicationContext()).
                getRequestQueue();

        JSONObject json = new JSONObject();
        try
        {
            json.put("email",email.getText());
            json.put("password",password.getText());
            json.put("first_name",firstName.getText());
            json.put("last_name", lastName.getText());
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
                        // Log player in

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
