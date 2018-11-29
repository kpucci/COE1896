package com.coe1896.puckperfectapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.android.volley.RequestQueue;

public class RegistrationActivity extends FragmentActivity{

    RadioGroup typeGroup;
    RadioButton playerType;
    RadioButton coachType;
    RadioButton parentType;

    private static final String TAG = "RegistrationActivity";

    Fragment frag;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        typeGroup = (RadioGroup) findViewById(R.id.radioGroup);

        playerType = (RadioButton) findViewById(R.id.radioButton1);
        coachType = (RadioButton) findViewById(R.id.radioButton2);
        parentType = (RadioButton) findViewById(R.id.radioButton3);

        playerType.setChecked(true);

        Log.i(TAG,"-----START PLAYER TYPE FRAGMENT-----");

        frag = new PlayerTypeFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, frag);
        fragmentTransaction.commit();

        typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();

                switch (checkedId)
                {
                    case R.id.radioButton1:
                        fragmentTransaction.remove(frag);
                        frag = new PlayerTypeFragment();
                        fragmentTransaction.add(R.id.frameLayout, frag);
                        fragmentTransaction.commit();
                        break;

                    case R.id.radioButton2:
                        fragmentTransaction.remove(frag);
                        frag = new CoachTypeFragment();
                        fragmentTransaction.add(R.id.frameLayout, frag);
                        fragmentTransaction.commit();
                        break;

                    case R.id.radioButton3:
                        fragmentTransaction.remove(frag);
                        frag = new ParentTypeFragment();
                        fragmentTransaction.add(R.id.frameLayout, frag);
                        fragmentTransaction.commit();
                        break;
                }
            }
        });

    }
}
