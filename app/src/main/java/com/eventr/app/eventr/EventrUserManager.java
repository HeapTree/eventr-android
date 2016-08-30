package com.eventr.app.eventr;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Suraj on 16/08/16.
 */
public class EventrUserManager {
    private String accessToken;
    private String tempAccessToken;
    private SharedPreferences userPreferences;
    private Context context;
    private static final String LOGIN_URL = "http://52.26.148.176/api/v1/login";
    private static final String USER_DATA_URL = "http://52.26.148.176/api/v1/user-profile";
    private JSONObject userData;

    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;

    public EventrUserManager(Context actContext) {
        context = actContext;

        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        userPreferences = context.getSharedPreferences(context.getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(context.getString(R.string.access_token_key), null);
        if (accessToken != null) {
            getUserData();
        } else {
            ((LoginActivity) context).showFBButton();
        }
    }

    public void login(String accessToken) {
        tempAccessToken = accessToken;
        loginCall();
    }

    public void loginCall() {
        ((LoginActivity) context).hideFBButton();
        JSONObject requestObject = new JSONObject();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    saveAccessToken(response.getString("access_token"));
                    userData = response.getJSONObject("data");
                    setUserData();
                    ((LoginActivity) context).startMainActivity();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        onLoginInternetFail();
                    }

                    if (error.getClass().equals(NoConnectionError.class)) {
                        onLoginInternetFail();
                    }
                } else {
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse.statusCode == 401) {
                        Utils.logoutFB();
                        ((LoginActivity) context).showFBButton();
                    }
                }
            }
        };

        try {
            requestObject.put("fb_access_token", tempAccessToken);
            JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, LOGIN_URL, requestObject, listener, errorListener, accessToken);
            EventrRequestQueue.getInstance().add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveAccessToken(String accessToken) {
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putString(context.getString(R.string.access_token_key), accessToken);
        editor.apply();
    }

    private void getUserData() {
        JSONObject requestObject = new JSONObject();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    userData = response.getJSONObject("data");
                    setUserData();
                    ((LoginActivity) context).startMainActivity();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        onUserDataInternetFail();
                    }

                    if (error.getClass().equals(NoConnectionError.class)) {
                        onUserDataInternetFail();
                    }
                } else {
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse.statusCode == 401) {
                        Utils.logoutFB();
                        ((LoginActivity) context).showFBButton();
                    }
                }
            }
        };

        String userDataUrl = USER_DATA_URL;
        JsonObjectRequest request = new CustomJsonRequest(userDataUrl, null, listener, errorListener, accessToken);
        EventrRequestQueue.getInstance().add(request);
    }

    private void setUserData() {
        try {
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString(context.getString(R.string.name), userData.getString("name"));
            editor.putString(context.getString(R.string.pic_url), userData.getString("pic_url"));
            editor.putString(context.getString(R.string.fb_id), userData.getString("fb_id"));
            if (userData.getString("email") != null) {
                editor.putString(context.getString(R.string.email), userData.getString("email"));
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onLoginInternetFail() {
        Utils.showAlertWindow(context, "Internet connection failed", "Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                loginCall();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                ((Activity) context).finish();
            }
        });
    }

    private void onUserDataInternetFail() {
        Utils.showAlertWindow(context, "Internet connection failed", "Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                getUserData();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                ((Activity) context).finish();
            }
        });
    }
}
