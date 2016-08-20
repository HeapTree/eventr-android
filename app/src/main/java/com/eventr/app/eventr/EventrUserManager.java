package com.eventr.app.eventr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Suraj on 16/08/16.
 */
public class EventrUserManager {
    private String accessToken;
    private SharedPreferences userPreferences;
    private Context context;

    public EventrUserManager(Context actContext) {
        context = actContext;
        userPreferences = context.getSharedPreferences(context.getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(context.getString(R.string.access_token_key), null);

        if (accessToken != null) {
            getUserData();
        } else {
            ((LoginActivity) context).loadFBButton();
        }
    };

    public void login(String accessToken) {
        saveAccessToken(accessToken);
    }

    private void saveAccessToken(String accessToken) {
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putString(context.getString(R.string.access_token_key), accessToken);
        editor.apply();
    }

    private void loginAndGetUserData() {

    }

    private void getUserData() {
        ((LoginActivity) context).startMainActivity();
    }
}
