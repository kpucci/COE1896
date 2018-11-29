package com.coe1896.puckperfectapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class ProfilePageAdapter extends FragmentPagerAdapter {

    public String TAG = "ProfilePageAdapter";
    protected Context mContext;

    public ProfilePageAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    // This method returns the fragment associated with
    // the specified position.
    //
    // It is called when the Adapter needs a fragment
    // and it does not exists.
    public Fragment getItem(int position) {

//        Log.i(TAG, "-----Position " + position + "-----");
        switch (position) {
            case 0:
                // return new StatsFragment();
            case 1:
                // return new PracticeFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0: return "Stats";
            case 1: return "Practice";
            default: return null;
        }
    }
}
