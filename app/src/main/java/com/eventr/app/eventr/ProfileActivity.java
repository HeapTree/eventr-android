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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Suraj on 21/08/16.
 */
public class ProfileActivity extends AppCompatActivity {
    private SharedPreferences userPreferences;
    private String name, email, picUrl;
    private int credits, eventsAttended;
    private JSONObject userData;
    private static final String USER_DATA_URL = "http://52.26.148.176/api/v1/user-profile";
    private String accessToken;
    private ImageLoader imageLoader;

    private static final String REQUEST_TAG = "profile_activity";

    @BindView(R.id.profile_name) public TextView nameView;
    @BindView(R.id.toolbar_profile) public Toolbar toolbar;
    @BindView(R.id.profile_email) public TextView emailView;
    @BindView(R.id.events_attended_count) public TextView attendedView;
    @BindView(R.id.total_points) public TextView totalPointsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getString(R.string.access_token_key), null);

        imageLoader = EventrRequestQueue.getInstance().getImageLoader();
        setToolbar();
        setProfileData();
        getUserData();
    }

    private void setToolbar() {
        toolbar.setTitle(R.string.profile_title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setProfileData() {
        name = userPreferences.getString(getString(R.string.name), null);
        email = userPreferences.getString(getString(R.string.email), null);
        picUrl = userPreferences.getString("pic_url", null);
        credits = userPreferences.getInt(getString(R.string.eventr_credits), 0);
        eventsAttended = userPreferences.getInt(getString(R.string.total_events_attended), 0);

        if (name != null) {
            nameView.setText(name);
        }
        if (email != null) {
            emailView.setText(email);
        }

        attendedView.setText(eventsAttended + "");
        totalPointsView.setText(credits + "");

        final CircleImageView imageView = (CircleImageView) findViewById(R.id.profile_image);
        if (picUrl != null) {
            imageLoader.get(picUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        imageView.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
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
        request.setTag(REQUEST_TAG);
        EventrRequestQueue.getInstance().add(request);
    }

    private void setUserData() {
        try {
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString(getString(R.string.name), userData.getString("name"));
            editor.putString(getString(R.string.pic_url), userData.getString("pic_url"));
            editor.putString(getString(R.string.fb_id), userData.getString("fb_id"));
            editor.putInt(getString(R.string.eventr_credits), userData.getInt("eventr_credits"));
            editor.putInt(getString(R.string.total_events_attended), userData.getInt("total_events_attended"));
            if (userData.getString("email") != null) {
                editor.putString(getString(R.string.email), userData.getString("email"));
            }
            editor.apply();
            setProfileData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventrRequestQueue.getInstance().cancel(REQUEST_TAG);
    }
}
