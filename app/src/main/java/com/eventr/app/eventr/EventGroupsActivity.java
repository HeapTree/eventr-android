package com.eventr.app.eventr;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eventr.app.eventr.adapters.EventAllGroupsRecyclerAdapter;
import com.eventr.app.eventr.models.Event;
import com.eventr.app.eventr.models.EventGroup;
import com.eventr.app.eventr.utils.CustomDialogFragment;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Suraj on 03/09/16.
 */
public class EventGroupsActivity extends AppCompatActivity {
    private Event eventDetail;
    private Context mContext;

    private static final String REQUEST_TAG = "event_groups_activity";

    @BindView(R.id.event_groups_container) public LinearLayout eventGroupsContainer;
    @BindView(R.id.event_groups_progress_bar) public ProgressBar progressBar;
    @BindView(R.id.floating_action) public FloatingActionButton floatingButton;
    @BindView(R.id.my_group_view) public LinearLayout myGroupView;
    @BindView(R.id.all_groups_view) public LinearLayout allGroupView;
    @BindView(R.id.new_group) public RelativeLayout createGroupView;
    @BindView(R.id.all_groups_recycler) RecyclerView allGroupsRecycler;

    private String accessToken;
    private SharedPreferences userPreferences;
    private EventGroup myGroup;
    private RelativeLayout myGroupResource;
    private ArrayList<EventGroup> eventGroups = new ArrayList<EventGroup>();
    private CustomDialogFragment newGroupDialog;
    private EventAllGroupsRecyclerAdapter recyclerAdapter;

    private static final String EVENT_GROUPS_URL = "http://52.26.148.176/api/v1/event-groups?fb_event_id=";
    private static final String CREATE_GROUP_URL = "http://52.26.148.176/api/v1/groups";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_groups);

        mContext = this;
        ButterKnife.bind(this);

        Bundle intentExtras = getIntent().getExtras();
        eventDetail = (Event)intentExtras.getSerializable(getResources().getString(R.string.intent_event_detail_key));

        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getString(R.string.access_token_key), null);

        setToolbar();
        setAllGroupsRecycler();
        getMyGroupTemplate();
        getEventGroups();
        createGroupClickHandler();
        myGroupClickHander();
        floatingButtonClickHandler();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_event_groups);
        toolbar.setTitle("Groups - " + eventDetail.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setAllGroupsRecycler() {
        allGroupsRecycler.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerAdapter = new EventAllGroupsRecyclerAdapter(eventGroups);
        allGroupsRecycler.setAdapter(recyclerAdapter);
    }

    private void getEventGroups() {
        boolean isInternetConnected = Utils.isInternetConnected(this);
        if (!isInternetConnected) {
            onInternetFail();
        } else {
            showProgressBar();
            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("EVENT_GROUPS", response.toString());

                    hideProgressBar();
                    eventGroups.clear();
                    JSONObject myG = null;
                    try {
                        myG = (response.getJSONObject("data")).getJSONObject("my_group");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        myGroup = myG.length() > 0 ? new EventGroup(myG) : null;

                        JSONArray allG = (response.getJSONObject("data")).getJSONArray("groups");
                        for (int i = 0; i < allG.length(); i++) {
                            eventGroups.add(new EventGroup((JSONObject) allG.get(i)));
                        }

                        onEventGroups();

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

            String eventsUrl = EVENT_GROUPS_URL + eventDetail.getId();

            JsonObjectRequest request = new CustomJsonRequest(eventsUrl, null, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        }
    }

    private void onInternetFail() {
        Utils.showAlertWindow(this, "Internet connection failed", "Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                getEventGroups();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                finish();
            }
        });
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        eventGroupsContainer.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        eventGroupsContainer.setVisibility(View.GONE);
    }

    private void onEventGroups() {
        recyclerAdapter.notifyDataSetChanged();
        if (getGroupsCount() > 0) {
            allGroupView.setVisibility(View.VISIBLE);
            if (!isMyGroupPresent()) {
                floatingButton.setVisibility(View.VISIBLE);
            }
        } else {
            allGroupView.setVisibility(View.GONE);
            floatingButton.setVisibility(View.GONE);
        }

        if (isMyGroupPresent()) {
            myGroupView.setVisibility(View.VISIBLE);
            createGroupView.setVisibility(View.GONE);
            renderMyGroup();
        } else {
            myGroupView.setVisibility(View.GONE);
            createGroupView.setVisibility(View.VISIBLE);
        }

    }

    private int getGroupsCount() {
        return eventGroups.size();
    }

    private boolean isMyGroupPresent() {
        return myGroup != null;
    }

    private void renderMyGroup() {
        TextView alphabetIndex = (TextView) findViewById(R.id.group_alphabet_index);
        TextView groupTitleView = (TextView) findViewById(R.id.group_title);
        groupTitleView.setText(myGroup.getName());
        alphabetIndex.setText(myGroup.getName().substring(0, 1).toUpperCase());
    }

    private void getMyGroupTemplate() {
        LayoutInflater inflater = getLayoutInflater();
        myGroupResource = (RelativeLayout) inflater.inflate(R.layout.event_group_row, null, false);
        myGroupView.addView(myGroupResource);
    }

    private void createGroupClickHandler() {
        createGroupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewGroupDialog();
            }
        });
    }

    private void showNewGroupDialog() {
        if (newGroupDialog == null) {
            newGroupDialog = CustomDialogFragment.newInstance("edit_text", false);
        }

        newGroupDialog.setTitle("Create a group");

        newGroupDialog.setPositiveButton("Create", newGroupPositiveListener);
        newGroupDialog.setNegativeButton("Cancel", newGroupNegativeListener);

        newGroupDialog.show(getSupportFragmentManager(), "NEW_GROUP_DIALOG");
    }

    private View.OnClickListener newGroupPositiveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String groupName = newGroupDialog.getEditTextValue();
            if (groupName != null && !groupName.isEmpty()) {
                createGroup(groupName);
            } else {
                newGroupDialog.showError("Please enter a group name");
            }
        }
    };

    public View.OnClickListener newGroupNegativeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            newGroupDialog.dismiss();
        }
    };

    private void createGroup(String groupName) {
        newGroupDialog.showProgressBar();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                newGroupDialog.hideProgressBar();
                newGroupDialog.hideError();
                newGroupDialog.dismiss();
                getEventGroups();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                newGroupDialog.hideProgressBar();
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        onCreateInternetFail();
                    }

                    if (error.getClass().equals(NoConnectionError.class)) {
                        onCreateInternetFail();
                    }
                } else {
                    NetworkResponse networkResponse = error.networkResponse;
                    newGroupDialog.dismiss();
                    if (networkResponse.statusCode == 401) {
                        Utils.logout(mContext);
                    }
                }
            }
        };

        try {
            JSONObject requestObject = new JSONObject();
            JSONObject groupObject = new JSONObject();
            groupObject.put("name", groupName);
            groupObject.put("fb_event_id", eventDetail.getId());
            requestObject.put("group", groupObject);
            JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, CREATE_GROUP_URL, requestObject, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onCreateInternetFail() {
        newGroupDialog.showError("No internet connection");
    }

    private void myGroupClickHander() {
        myGroupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMyGroupDetailActivity();
            }
        });
    }

    private void startMyGroupDetailActivity() {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra(getString(R.string.intent_group_detail_key), myGroup);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventrRequestQueue.getInstance().cancel(REQUEST_TAG);
    }

    private void floatingButtonClickHandler() {
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewGroupDialog();
            }
        });
    }
}
