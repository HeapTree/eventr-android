package com.eventr.app.eventr.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.eventr.app.eventr.EventViewPagerFragment;

import java.util.ArrayList;

/**
 * Created by Suraj on 04/08/16.
 */
public class EventViewPagerFragmentAdapter extends FragmentPagerAdapter {
    private final CharSequence[] rsvpTitles = {"Nearby", "Going", "Interested"};

    public EventViewPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return EventViewPagerFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return rsvpTitles[position];
    }
}
