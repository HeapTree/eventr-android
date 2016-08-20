package com.eventr.app.eventr;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Suraj on 08/08/16.
 */
public class LoginActivity extends FragmentActivity {
    private Timer timer, fbTimer;
    private static final int NUM_PAGES = 3;
    private int page = 0;
    private LoginSliderViewPager mPager;
    private AccessToken accessToken;
    private EventrUserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        slider();
        FacebookSdk.sdkInitialize(this);
        userManager = new EventrUserManager(this);
    }

    private void slider() {
        mPager = (LoginSliderViewPager) findViewById(R.id.login_slider);
        PagerAdapter mPagerAdapter = new LoginSliderAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        pageSwitcher(4);
    }

    private void pageSwitcher(int sec) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new SlideTask(), 0, sec * 1000);
    }

    private class SlideTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (page >= NUM_PAGES) {
                        page = 0;
                    }
                    mPager.setCurrentItem(page++);
                }
            });
        }
    }

    public void loadFBButton() {
        RelativeLayout container = (RelativeLayout)findViewById(R.id.login_action_container);
        container.removeAllViews();

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fmTrans = fm.beginTransaction();

        FacebookLoginFragment fragment = new FacebookLoginFragment();
        fmTrans.add(R.id.login_action_container, fragment);
        fmTrans.commit();
    }

    private void checkKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.eventr.app.eventr",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    public void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public EventrUserManager getUserManager() {
        return userManager;
    }
}
