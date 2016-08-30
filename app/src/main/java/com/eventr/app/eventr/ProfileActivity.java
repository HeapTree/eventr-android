package com.eventr.app.eventr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Suraj on 21/08/16.
 */
public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView navView;
    private SharedPreferences userPreferences;
    private String name;
    private String email;
    private JSONObject userData;
    private static final String USER_DATA_URL = "http://52.26.148.176/api/v1/user-profile";
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getString(R.string.access_token_key), null);
        setToolbar();
        setProfileData();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        toolbar.setTitle(R.string.profile_title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setProfileData() {
        name = userPreferences.getString(getString(R.string.name), null);
        email = userPreferences.getString(getString(R.string.email), null);

        TextView nameView = (TextView) findViewById(R.id.profile_name);
        if (name != null) {
            nameView.setText(name);
        }

        TextView emailView = (TextView) findViewById(R.id.profile_email);
        if (email != null) {
            emailView.setText(email);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getUserData() {
        JSONObject requestObject = new JSONObject();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    userData = response.getJSONObject("data");
                    setUserData();
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

        String userDataUrl = USER_DATA_URL;
        JsonObjectRequest request = new CustomJsonRequest(userDataUrl, null, listener, errorListener, accessToken);
        EventrRequestQueue.getInstance().add(request);
    }

    private void setUserData() {
        try {
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString(getString(R.string.name), userData.getString("name"));
            editor.putString(getString(R.string.pic_url), userData.getString("pic_url"));
            editor.putString(getString(R.string.fb_id), userData.getString("fb_id"));
            if (userData.getString("email") != null) {
                editor.putString(getString(R.string.email), userData.getString("email"));
            }
            editor.apply();
            setProfileData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
