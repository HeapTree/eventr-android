package com.eventr.app.eventr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Created by Suraj on 11/08/16.
 */
public class FacebookLoginFragment extends Fragment {
    CallbackManager callbackManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fb_login, container, false);
        manageFacebookLogin(view);
        return view;
    }

    private void manageFacebookLogin(ViewGroup view) {
        final Context appContext = getActivity().getApplicationContext();
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email , rsvp_event , user_events");
        loginButton.setFragment(this);
        FacebookSdk.sdkInitialize(appContext);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                (((LoginActivity) getActivity()).getUserManager()).login(accessToken);
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
}
