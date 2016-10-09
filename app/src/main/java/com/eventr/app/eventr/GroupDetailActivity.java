package com.eventr.app.eventr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.eventr.app.eventr.adapters.MembersRecyclerAdapter;
import com.eventr.app.eventr.models.Event;
import com.eventr.app.eventr.models.EventGroup;
import com.eventr.app.eventr.models.GroupMember;
import com.eventr.app.eventr.utils.CustomDialogFragment;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
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
    private String accessToken, userFbId;
    private ArrayList<GroupMember> allRequestedMembers = new ArrayList<GroupMember>();
    private ArrayList<GroupMember> groupMembers = new ArrayList<GroupMember>();
    private ArrayList<GroupMember> requestedMembers = new ArrayList<GroupMember>();
    private MembersRecyclerAdapter recyclerAdapter;
    private MembersRecyclerAdapter requestedMemberRecyclerAdapter;

    private boolean canMarkAttendance, canMakeAdmin, canAcceptJoinRequest, canJoinGroup, isJoinRequestSent, isJoinRequestRejected, showJoinView, isJoinRequestApproved;
    private  boolean showFloatingButton, showJoinGroupView, showRequestRejected, showRequestSent, showRequestAccepted, showAdminText, showMarkAttendanceText, showEventOverNotJoinedText, showAttendedText, showNotAttendedText, showAttendancePendingText, showChatButton;

    private static final String MEMBERS_URL = "http://52.26.148.176/api/v1/group-members/";
    private static final String JOIN_GROUP_URL = "http://52.26.148.176/api/v1/join-group/";
    private static final String REQUEST_TAG = "group_detail_activity";
    private static final String DIALOG_TYPE = "confirm";

    private static final String USER_STATUS_APPROVED = "approved";
    private static final String USER_STATUS_REQUESTED = "requested";
    private static final String USER_STATUS_REJECTED = "rejected";

    private static final String REQUESTED_TYPE = "requested";
    private static final String ACTIVE_TYPE = "active";

    private static final String ADMIN_ROLE = "admin";
    private static final String OWNER_ROLE = "owner";
    private static final String MEMBER_ROLE = "member";

    private static final String ATTENDED_TEXT = "attended";
    private static final String NOT_ATTENDED_TEXT = "not_attended";

    private CustomDialogFragment joinGroupDialog = CustomDialogFragment.newInstance(DIALOG_TYPE, false);

    @BindView(R.id.toolbar_group_detail) public Toolbar toolbar;
    @BindView(R.id.group_detail_progress_bar) public ProgressBar progressBar;
    @BindView(R.id.group_members_recycler) public RecyclerView membersRecycler;
    @BindView(R.id.requested_members_recycler) public RecyclerView requestedMembersRecycler;
    @BindView(R.id.group_members_container) public LinearLayout membersContainer;
    @BindView(R.id.floating_action) public FloatingActionButton floatingActionButton;
    @BindView(R.id.request_sent) public TextView requestSentView;
    @BindView(R.id.request_rejected) public TextView requestRejectedView;
    @BindView(R.id.request_accepted) public TextView requestAcceptedView;
    @BindView(R.id.admin_view) public TextView adminView;
    @BindView(R.id.join_group) public RelativeLayout joinGroupView;
    @BindView(R.id.attended_view) public TextView attendedView;
    @BindView(R.id.not_attended_view) public TextView notAttendedView;
    @BindView(R.id.event_over_not_joined_view) public TextView eventOverNotJoinedView;
    @BindView(R.id.mark_attendance_text_view) public TextView markAttendanceView;
    @BindView(R.id.requested_members_container) public LinearLayout requestedMembersContainer;
    @BindView(R.id.active_members_container) public LinearLayout activeMembersContainer;

    private MenuItem groupRequestsButton, groupRequestsCloseButton, chatButton;

    private Menu menu;

    private int requestListBeforeModEleCount;

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
        userFbId = userPreferences.getString(getString(R.string.fb_id), null);

        setToolbar();
        setMembersAdapter();
        setRequestedMembersAdapter();
        setJoinGroupDialog();
        getMembers();
        setListeners();
    }

    private void setToolbar() {
        toolbar.setTitle(groupDetail.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_detail_toolbar, menu);
        groupRequestsButton  = menu.findItem(R.id.group_requests_button);
        groupRequestsCloseButton = menu.findItem(R.id.group_requests_close_button);
        chatButton = menu.findItem(R.id.chat_button);
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
                finish();
                return true;
            case R.id.group_requests_button:
                showGroupRequestsView();
                break;
            case R.id.group_requests_close_button:
                hideGroupRequestsView();
                break;
            case R.id.chat_button:
                startChatActivity();
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
                    allRequestedMembers.clear();
                    requestedMembers.clear();

                    try {
                        JSONArray allG = response.getJSONArray("data");
                        for (int i = 0; i < allG.length(); i++) {
                            GroupMember mem = new GroupMember((JSONObject) allG.get(i));
                            allRequestedMembers.add(mem);
                            if (mem.getRole().equals("owner") || mem.getRole().equals("admin") || mem.getStatus().equals("approved")) {
                                groupMembers.add(mem);
                            } else if (mem.getStatus().equals("requested")) {
                                requestedMembers.add(mem);
                            }
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
        updateGroupActions();
        recyclerAdapter.notifyDataSetChanged();
        requestedMemberRecyclerAdapter.notifyDataSetChanged();
        renderGroupActions();
    }

    private void setMembersAdapter() {
        membersRecycler.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerAdapter = new MembersRecyclerAdapter(groupMembers, getListActionType(ACTIVE_TYPE), getSupportFragmentManager(), this, accessToken);
        membersRecycler.setAdapter(recyclerAdapter);
    }

    private void setRequestedMembersAdapter() {
        requestedMembersRecycler.setLayoutManager(new GridLayoutManager(this, 1));
        requestedMemberRecyclerAdapter = new MembersRecyclerAdapter(requestedMembers, getListActionType(REQUESTED_TYPE), getSupportFragmentManager(), this, accessToken);
        requestedMembersRecycler.setAdapter(requestedMemberRecyclerAdapter);
    }

    private void updateGroupActions() {
        groupDetail.setUserGroupStatusFromMembers(allRequestedMembers, userFbId);
        boolean isEventOver = groupDetail.isEventOver();
        boolean isUserOwner = groupDetail.isUserOwner();
        boolean isUserAdmin = groupDetail.isUserAdmin();
        String attendedEvent = groupDetail.attendedEventStatus();
        String joinRequestStatus = groupDetail.joinRequestStatus();

        canMarkAttendance = false;
        canMakeAdmin = false;
        canAcceptJoinRequest = false;
        canJoinGroup = false;
        isJoinRequestSent = joinRequestStatus.equals(USER_STATUS_REQUESTED);
        isJoinRequestRejected = joinRequestStatus.equals(USER_STATUS_REJECTED);
        isJoinRequestApproved = joinRequestStatus.equals(USER_STATUS_APPROVED);

        showFloatingButton = false;
        showJoinGroupView = false;
        showRequestSent = false;
        showRequestRejected = false;
        showRequestAccepted = false;
        showAdminText = false;
        showMarkAttendanceText = false;
        showEventOverNotJoinedText = false;
        showAttendedText = false;
        showNotAttendedText = false;
        showChatButton = false;


        if (isEventOver) {
            if (isUserAdmin) {
                canMarkAttendance = true;
                showMarkAttendanceText = true;
                showChatButton = true;
            }

            if (!isUserAdmin && !isUserOwner) {
                if (!isJoinRequestApproved) {
                    showEventOverNotJoinedText = true;
                } else if (attendedEvent.equals(ATTENDED_TEXT)) {
                    showAttendedText = true;
                } else if (attendedEvent.equals(NOT_ATTENDED_TEXT)) {
                    showNotAttendedText = true;
                }
            }
        } else {
            if (isUserAdmin) {
                canAcceptJoinRequest = true;
                showChatButton = true;
                showAdminText = true;
            }
            if (isUserOwner) {
                canMakeAdmin = true;
            }

            if (isJoinRequestApproved || isJoinRequestSent || isJoinRequestRejected) {
                canJoinGroup = false;
            } else {
                canJoinGroup = true;
            }

            if (canJoinGroup) {
                showFloatingButton = true;
                showJoinGroupView = true;
            }

            if (isJoinRequestSent) {
                showRequestSent = true;
            }

            if (isJoinRequestRejected) {
                showRequestRejected = true;
            }

            if (isJoinRequestApproved) {
                showRequestAccepted = true;
            }

            if (isJoinRequestApproved) {
                showChatButton = true;
            }
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

        if (showRequestSent) {
            requestSentView.setVisibility(View.VISIBLE);
        } else {
            requestSentView.setVisibility(View.GONE);
        }

        if (showRequestRejected) {
            requestRejectedView.setVisibility(View.VISIBLE);
        } else {
            requestRejectedView.setVisibility(View.GONE);
        }

        if (showRequestAccepted && !showAdminText) {
            requestAcceptedView.setVisibility(View.VISIBLE);
        } else {
            requestAcceptedView.setVisibility(View.GONE);
        }

        if (showAdminText) {
            adminView.setVisibility(View.VISIBLE);
        } else {
            adminView.setVisibility(View.GONE);
        }

        if (canAcceptJoinRequest) {
            groupRequestsButton.setVisible(true);
        }

        if (showAttendedText) {
            attendedView.setVisibility(View.VISIBLE);
        } else {
            attendedView.setVisibility(View.GONE);
        }

        if (showNotAttendedText) {
            notAttendedView.setVisibility(View.VISIBLE);
        } else {
            notAttendedView.setVisibility(View.GONE);
        }

        if (showEventOverNotJoinedText) {
            eventOverNotJoinedView.setVisibility(View.VISIBLE);
        } else {
            eventOverNotJoinedView.setVisibility(View.GONE);
        }

        if (showMarkAttendanceText) {
            markAttendanceView.setVisibility(View.VISIBLE);
        } else {
            markAttendanceView.setVisibility(View.GONE);
        }

        if (showChatButton) {
            chatButton.setVisible(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventrRequestQueue.getInstance().cancel(REQUEST_TAG);
    }

    private void setListeners() {
        setJoinGroupClickHandlers();
    }

    private void setJoinGroupClickHandlers() {
        joinGroupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroupDialog.show(getSupportFragmentManager(), "JOIN_GROUP_DIALOG");
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroupDialog.show(getSupportFragmentManager(), "JOIN_GROUP_DIALOG");
            }
        });
    }

    private void setJoinGroupDialog() {
        joinGroupDialog.setTitle("Event action");
        joinGroupDialog.setMessage("Do you want to join this group?");
        joinGroupDialog.setPositiveButton("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroup();
            }
        });

        joinGroupDialog.setNegativeButton("No", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroupDialog.dismiss();
            }
        });
    }

    private void joinGroup() {
        boolean isInternetConnected = Utils.isInternetConnected(this);
        if (!isInternetConnected) {
            onJoinGroupInternetFail();
            return;
        }
        joinGroupDialog.showProgressBar();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                joinGroupDialog.hideProgressBar();
                joinGroupDialog.hideError();
                joinGroupDialog.dismiss();
                getMembers();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                joinGroupDialog.hideProgressBar();
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        onJoinGroupInternetFail();
                    }

                    if (error.getClass().equals(NoConnectionError.class)) {
                        onJoinGroupInternetFail();
                    }
                } else {
                    NetworkResponse networkResponse = error.networkResponse;
                    joinGroupDialog.dismiss();
                    if (networkResponse.statusCode == 401) {
                        Utils.logout(mContext);
                    }

                    if (networkResponse.statusCode == 400) {
                        getMembers();
                    }
                }
            }
        };

        JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, JOIN_GROUP_URL + groupDetail.getUuid(), null, listener, errorListener, accessToken);
        request.setTag(REQUEST_TAG);
        EventrRequestQueue.getInstance().add(request);
    }

    private void onJoinGroupInternetFail() {
        joinGroupDialog.showError("No internet connection");
    }

    private void showGroupRequestsView() {
        activeMembersContainer.setVisibility(View.GONE);
        requestedMembersContainer.setVisibility(View.VISIBLE);
        groupRequestsButton.setVisible(false);
        chatButton.setVisible(false);
        groupRequestsCloseButton.setVisible(true);
        requestListBeforeModEleCount = requestedMembers.size();

    }

    private void hideGroupRequestsView() {
        activeMembersContainer.setVisibility(View.VISIBLE);
        requestedMembersContainer.setVisibility(View.GONE);
        groupRequestsButton.setVisible(true);
        chatButton.setVisible(true);
        groupRequestsCloseButton.setVisible(false);
        if (requestListBeforeModEleCount != requestedMembers.size()) {
            getMembers();
        }
    }

    private String getListActionType(String listType) {
        String actionType = "NO_ACTION";
        String userRole = groupDetail.getUserRole();
        boolean isEventOver = groupDetail.isEventOver();
        if (listType.equals(ACTIVE_TYPE)) {
            if (userRole.equals(ADMIN_ROLE)) {
                if (isEventOver) {
                    actionType = "EVENT_OVER_ADMIN_ACTIONS";
                } else {
                    actionType = "EVENT_ACTIVE_ADMIN_ACTIVE_LIST_ACTIONS";
                }
            }

            if (userRole.equals(OWNER_ROLE)) {
                if (isEventOver) {
                    actionType = "EVENT_OVER_OWNER_ACTIONS";
                } else {
                    actionType = "EVENT_ACTIVE_OWNER_ACTIVE_LIST_ACTIONS";
                }
            }
        }

        if (listType.equals(REQUESTED_TYPE)) {
            if (userRole.equals(ADMIN_ROLE)) {
                if (isEventOver) {
                    actionType = "NO_ACTION";
                } else {
                    actionType = "EVENT_ACTIVE_ADMIN_REQUEST_LIST_ACTIONS";
                }
            }

            if (userRole.equals(OWNER_ROLE)) {
                if (isEventOver) {
                    actionType = "NO_ACTION";
                } else {
                    actionType = "EVENT_ACTIVE_OWNER_REQUEST_LIST_ACTIONS";
                }
            }
        }

        return actionType;
    }

    private void startChatActivity() {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(getString(R.string.intent_group_detail_key), groupDetail);
        startActivity(intent);
    }
}
