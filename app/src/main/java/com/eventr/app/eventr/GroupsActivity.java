package com.eventr.app.eventr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eventr.app.eventr.adapters.GroupsRecyclerAdapter;
import com.eventr.app.eventr.models.EventGroup;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Suraj on 15/08/16.
 */
public class GroupsActivity extends AppCompatActivity {
    @BindView(R.id.drawer_layout_groups) public DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view_groups) public NavigationView navView;
    @BindView(R.id.groups_container) public RecyclerView groupsRecycler;
    @BindView(R.id.groups_progress_bar) public ProgressBar progressBar;
    @BindView(R.id.swipe_refresh_groups) public SwipeRefreshLayout swipeRefreshLayout;

    private Context mContext;
    private ArrayList<EventGroup> userGroups = new ArrayList<EventGroup>();
    private GroupsRecyclerAdapter adapter;

    private static final String USER_GROUPS_URL = "http://52.26.148.176/api/v1/user-groups";
    private String accessToken;
    private SharedPreferences userPreferences;
    private boolean firstSuccessfulApiCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);

        mContext = this;

        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getString(R.string.access_token_key), null);

        setToolbar();
        setGroupsList();
        getUserGroups(false);
        setSwipeRefreshListener();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_groups);
        toolbar.setTitle(R.string.groups_title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        new Navigation(getApplicationContext(), navView, mDrawerLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.events_button:
                startEventsActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startEventsActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void setGroupsList() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.groups_container);

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
                break;
            default:
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }

        adapter = new GroupsRecyclerAdapter(userGroups);

        recyclerView.setAdapter(adapter);
    }

    private void getUserGroups(final boolean swipeRefresh) {
        boolean isInternetConnected = Utils.isInternetConnected(this);
        if (!isInternetConnected) {
            if (swipeRefresh) {
                swipeRefreshLayout.setRefreshing(false);
            }
            onInternetFail();
        } else {
            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (swipeRefresh) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    firstSuccessfulApiCall = true;
                    hideProgressBar();
                    userGroups.clear();
                    try {
                        JSONArray allG = (response.getJSONArray("data"));
                        for (int i = 0; i < allG.length(); i++) {
                            userGroups.add(new EventGroup((JSONObject) allG.get(i)));
                        }

                        onUserGroups();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();

                    if (swipeRefresh) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

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

            String eventsUrl = USER_GROUPS_URL;

            JsonObjectRequest request = new CustomJsonRequest(eventsUrl, null, listener, errorListener, accessToken);
            EventrRequestQueue.getInstance().add(request);
        }
    }

    private void onInternetFail() {
        if (!firstSuccessfulApiCall) {
            Utils.showAlertWindow(this, "Internet connection failed", "Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getUserGroups(false);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        } else {
            Utils.showActionableSnackBar(findViewById(R.id.coordinator_groups), "Internet connection failed", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getUserGroups(false);
                }
            });
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        groupsRecycler.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.VISIBLE);
    }

    private void onUserGroups() {
        adapter.notifyDataSetChanged();
    }

    private void setSwipeRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserGroups(true);
            }
        });
    }
}
