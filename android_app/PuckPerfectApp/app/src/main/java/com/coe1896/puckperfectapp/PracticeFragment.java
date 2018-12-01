package com.coe1896.puckperfectapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.coe1896.puckperfectapp.ConeDrillGif;
import com.coe1896.puckperfectapp.RecordActivity;

public class PracticeFragment extends Fragment implements View.OnClickListener{

    View inflatedView;
    ConeDrillGif coneDrill = null;

    private static final String TAG = "PracticeFragment";

    public PracticeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment PracticeFragment.
     */
    public static PracticeFragment newInstance() {
        return new PracticeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        inflatedView = inflater.inflate(R.layout.fragment_practice, container, false);
        Button practiceButton = (Button) inflatedView.findViewById(R.id.practiceButton);
        practiceButton.setOnClickListener(this);

        // Create the SurfaceViewThread object.
        coneDrill = new ConeDrillGif(getActivity().getApplicationContext());

        // Get frame layout
        FrameLayout coneLayout = (FrameLayout) inflatedView.findViewById(R.id.coneDrillFrame);
        coneLayout.addView(coneDrill);

        return inflatedView;
    }

    /**
     * Go to drill page
     * @param view
     */
    @Override
    public void onClick(View view)
    {
        Intent intent = new Intent(getActivity(), RecordActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStop()
    {
        coneDrill.stop();
        super.onStop();
    }
}
