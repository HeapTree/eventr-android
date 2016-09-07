package com.eventr.app.eventr;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.eventr.app.eventr.adapters.LoginSliderAdapter;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Suraj on 08/08/16.
 */
public class LoginActivity extends FragmentActivity {
    private static final String REQUEST_TAG = "login_activity";

    private Timer timer, fbTimer;
    private static final int NUM_PAGES = 3;
    private int page = 0;
    private LoginSliderViewPager mPager;

    private AccessToken accessToken;
    private CallbackManager callbackManager;

    private EventrUserManager userManager;

    private LoginButton loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        progressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
        slider();
        userManager = new EventrUserManager(this);
        manageFacebookLogin();
    }

    public void showFBButton() {
        loginButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public void hideFBButton() {
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public EventrUserManager getUserManager() {
        return userManager;
    }

    private void manageFacebookLogin() {
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email , rsvp_event , user_events");
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                getUserManager().login(accessToken);
            }

            @Override
            public void onCancel() {
                Log.d("FB_LOGIN:", "Cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("FB_LOGIN:", "Error");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
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

    @Override
    public void onStop() {
        super.onStop();
        EventrRequestQueue.getInstance().cancel(REQUEST_TAG);
    }
}
