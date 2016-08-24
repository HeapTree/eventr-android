package com.eventr.app.eventr;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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
        tempAccessToken = accessToken;
        JSONObject requestObject = new JSONObject();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("RESP", response.toString());
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
                    Log.d("RESP", response.toString());
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
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.statusCode == 401) {
                    ((LoginActivity) context).loadFBButton();
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
}
