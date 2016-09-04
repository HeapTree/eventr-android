package com.eventr.app.eventr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Suraj on 23/08/16.
 */
public class EventDetailActivity extends AppCompatActivity {
    private static final String EVENT_DETAIL_URL = "http://52.26.148.176/api/v1/events/";

    private TextView description;
    private NetworkImageView cover;
    private TextView toggleLines;
    private Events eventDetail;
    private ProgressBar progressBar;
    private NestedScrollView eventDetailContainer;
    private TextView startTime;
    private TextView locationView;
    private FloatingActionButton floatingButton;
    private String rsvpStatus;
    private TextView attendingCount;

    private Context mContext;

    private SharedPreferences userPreferences;
    private String accessToken;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Bundle intentExtras = getIntent().getExtras();

        eventDetail = (Events)intentExtras.getSerializable(getResources().getString(R.string.intent_event_detail_key));
        setToolbar();

        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getString(R.string.access_token_key), null);
        rsvpStatus = eventDetail.getRsvpStatus();
        progressBar = (ProgressBar) findViewById(R.id.event_detail_progress_bar);
        eventDetailContainer = (NestedScrollView) findViewById(R.id.event_detail_container);
        locationView = (TextView) findViewById(R.id.event_location);
        floatingButton = (FloatingActionButton) findViewById(R.id.floating_action);
        attendingCount = (TextView) findViewById(R.id.attending_count);

        mContext = this;

        description = (TextView) findViewById(R.id.event_description);
        cover = (NetworkImageView) findViewById(R.id.event_detail_pic);
        startTime = (TextView) findViewById(R.id.start_time);

        getEventDetail();
        setFloatingButtonAction();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_event_detail);
        toolbar.setTitle(eventDetail.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void getEventDetail() {
        boolean isInternetConnected = Utils.isInternetConnected(this);
        if (!isInternetConnected) {
            onInternetFail();
        } else {
            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideProgressBar();
                    try {
                        Log.d("RESPONSE", response.toString());
                        JSONObject event = response.getJSONObject("data");
                        eventDetail.setDetails(event, rsvpStatus);
                        updateEventPage();
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
                            onInternetFail();
                        }

                        if (error.getClass().equals(NoConnectionError.class)) {
                            onInternetFail();
                        }
                    } else {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse.statusCode == 401) {
                            Utils.logout(mContext);
                        }
                    }
                }
            };

            String eventsUrl = EVENT_DETAIL_URL + eventDetail.getId();

            JsonObjectRequest request = new CustomJsonRequest(eventsUrl, null, listener, errorListener, accessToken);
            EventrRequestQueue.getInstance().add(request);
        }
    }

    private void onInternetFail() {

    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        eventDetailContainer.setVisibility(View.VISIBLE);
        floatingButton.setVisibility(View.VISIBLE);
    }

    private void updateEventPage() {
        description.setText(eventDetail.getDescription());
        cover.setImageUrl(eventDetail.getPicUrl(), EventrRequestQueue.getInstance().getImageLoader());
        startTime.setText(eventDetail.getTimeString());
        locationView.setText(eventDetail.getLocationString());
        attendingCount.setText(eventDetail.getAttendingString());
    }

    private void startGroupsActivity() {
        Intent intent = new Intent(getApplicationContext(), EventGroupsActivity.class);
        startActivity(intent);
    }

    private void setFloatingButtonAction() {
        floatingButton.setOnClickListener(null);
        switch(rsvpStatus) {
            case "attending": {
                floatingButton.setImageResource(R.drawable.ic_people_white);
                floatingButton.setOnClickListener(floatingButtonGroupsListener);
                break;
            }
            default: {
                floatingButton.setOnClickListener(floatingButtonGoingListener);
            }
        }
    }

    private View.OnClickListener floatingButtonGroupsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startGroupsActivity();
        }
    };
    private View.OnClickListener floatingButtonGoingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("CHANGE_STATUS", "attending");
        }
    };
}
