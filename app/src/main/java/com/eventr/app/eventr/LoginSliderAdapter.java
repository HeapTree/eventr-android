package com.eventr.app.eventr;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Suraj on 11/08/16.
 */
public class LoginSliderAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_PAGES = 3;
    public LoginSliderAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new LoginSliderFragmentOne();
            case 1: return new LoginSliderFragmentTwo();
            case 2: return new LoginSliderFragmentThree();
            default: return new LoginSliderFragmentOne();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
