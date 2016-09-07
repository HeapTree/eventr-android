package com.eventr.app.eventr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eventr.app.eventr.adapters.EventAllGroupsRecyclerAdapter;
import com.eventr.app.eventr.adapters.MembersRecyclerAdapter;
import com.eventr.app.eventr.models.Event;
import com.eventr.app.eventr.models.EventGroup;
import com.eventr.app.eventr.models.GroupMember;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Member;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Suraj on 05/09/16.
 */
public class GroupDetailActivity extends AppCompatActivity {
    private Context mContext;
    private EventGroup groupDetail;
    private SharedPreferences userPreferences;
    private String accessToken;
    private ArrayList<GroupMember> groupMembers = new ArrayList<GroupMember>();
    private MembersRecyclerAdapter recyclerAdapter;

    private boolean canMarkAttendance, canMakeAdmin, canAcceptJoinRequest, canJoinGroup, isJoinRequestSent, isJoinRequestRejected, showJoinView, isJoinRequestApproved;
    private  boolean showFloatingButton, showJoinGroupView;

    private static final String MEMBERS_URL = "http://52.26.148.176/api/v1/group-members/";
    private static final String REQUEST_TAG = "group_detail_activity";

    @BindView(R.id.toolbar_group_detail) public Toolbar toolbar;
    @BindView(R.id.group_detail_progress_bar) public ProgressBar progressBar;
    @BindView(R.id.group_members_recycler) public RecyclerView membersRecycler;
    @BindView(R.id.group_members_container) public LinearLayout membersContainer;
    @BindView(R.id.floating_action) public FloatingActionButton floatingActionButton;
    @BindView(R.id.join_group) public RelativeLayout joinGroupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        ButterKnife.bind(this);
        mContext = this;

        Bundle intentExtras = getIntent().getExtras();
        groupDetail = (EventGroup) intentExtras.getSerializable(getResources().getString(R.string.intent_group_detail_key));

        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getString(R.string.access_token_key), null);

        setToolbar();
        setMembersAdapter();
        getMembers();
    }

    private void setToolbar() {
        toolbar.setTitle(groupDetail.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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

    private void getMembers() {
        boolean isInternetConnected = Utils.isInternetConnected(this);
        if (!isInternetConnected) {
            onInternetFail();
        } else {
            showProgressBar();
            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideProgressBar();
                    groupMembers.clear();

                    Log.d("MEMBERS_RESPONSE", response.toString());

                    try {
                        JSONArray allG = response.getJSONArray("data");
                        for (int i = 0; i < allG.length(); i++) {
                            groupMembers.add(new GroupMember((JSONObject) allG.get(i)));
                        }

                        onGroupMembers();

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

            String eventsUrl = MEMBERS_URL + groupDetail.getUuid();

            JsonObjectRequest request = new CustomJsonRequest(eventsUrl, null, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        }
    }

    private void onInternetFail() {
        Utils.showAlertWindow(this, "Internet connection failed", "Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                getMembers();
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
        membersContainer.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        membersContainer.setVisibility(View.GONE);
    }

    private void onGroupMembers() {
        recyclerAdapter.notifyDataSetChanged();
        updateGroupActions();
        renderGroupActions();
    }

    private void setMembersAdapter() {
        membersRecycler.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerAdapter = new MembersRecyclerAdapter(groupMembers);
        membersRecycler.setAdapter(recyclerAdapter);
    }

    private void updateGroupActions() {
        boolean isEventOver = groupDetail.isEventOver();
        boolean isUserOwner = groupDetail.isUserOwner();
        boolean isUserAdmin = groupDetail.isUserAdmin();
        String joinRequestStatus = groupDetail.joinRequestStatus();

        canMarkAttendance = false;
        canMakeAdmin = false;
        canAcceptJoinRequest = false;
        canJoinGroup = false;
        isJoinRequestSent = joinRequestStatus.equals("requested");
        isJoinRequestRejected = joinRequestStatus.equals("rejected");
        isJoinRequestApproved = joinRequestStatus.equals("approved");

        if (isEventOver) {
            if (isUserAdmin) {
                canMarkAttendance = true;
            }
        } else {
            if (isUserAdmin) {
                canAcceptJoinRequest = true;
            }
            if (isUserOwner) {
                canMakeAdmin = true;
            }

            if (isJoinRequestApproved || isJoinRequestSent) {
                canJoinGroup = false;
            } else {
                canJoinGroup = true;
            }
        }

        showFloatingButton = false;
        showJoinGroupView = false;

        if (canJoinGroup) {
            showFloatingButton = true;
            showJoinGroupView = true;
        }
    }

    private void renderGroupActions() {
        if (showFloatingButton) {
            floatingActionButton.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }

        if (showJoinGroupView) {
            joinGroupView.setVisibility(View.VISIBLE);
        } else {
            joinGroupView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventrRequestQueue.getInstance().cancel(REQUEST_TAG);
    }
}
