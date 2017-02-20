package com.eventr.app.eventr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eventr.app.eventr.models.EventGroup;
import com.eventr.app.eventr.utils.CustomDialogFragment;
import com.eventr.app.eventr.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.json.JSONObject;

/**
 * Created by Suraj on 21/08/16.
 */
public class Navigation {
    private NavigationView view;
    private DrawerLayout drawer;
    private SharedPreferences userPreferences;
    private String name, accessToken;
    private CallbackManager callbackManager;

    private String[] emailRecipients = new String[]{"support@eventr.biz"};
    private String emailSubject, emailDefaultBody;

    private static final String PRIVACY_POLICY_URL = "http://eventr.biz/privacy.html";
    private static final String DIALOG_TYPE = "edit_text";
    private static final String JOIN_GROUP_INVITE_URL = "http://52.26.148.176/api/v1/join-group-invite";

    private static final String REQUEST_TAG = "join_group_invite";

    private CustomDialogFragment joinGroupDialog = CustomDialogFragment.newInstance(DIALOG_TYPE, false);

    public Navigation(final Context context, NavigationView mView, DrawerLayout mDrawer) {
        view = mView;
        drawer = mDrawer;
        View headerLayout = view.getHeaderView(0);
        TextView userName = (TextView) headerLayout.findViewById(R.id.drawer_header_user_name);

        userPreferences = context.getSharedPreferences(context.getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        name = userPreferences.getString(context.getString(R.string.name), null);
        accessToken = userPreferences.getString(context.getString(R.string.access_token_key), null);

        userName.setText(name);

        emailSubject = context.getString(R.string.feedback_email_sub);
        emailDefaultBody = context.getString(R.string.feedback_email_default_body);

        setJoinGroupDialog();

        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawer.closeDrawers();
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.profile_button:
                        Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                        view.getContext().startActivity(intent);
                        break;
                    case R.id.logout_button:
                        Utils.logout(view.getContext());
                        break;
                    case R.id.privacy_button:
                        startPrivacyPolicy();
                        break;
                    case R.id.feedback_button:
                        startFeedbackActivity();
                        break;
                    case R.id.join_invited_group_button:
                        openJoinInvitedGroupDialog();

                }
                return true;
            }
        });
    }

    private void startPrivacyPolicy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(PRIVACY_POLICY_URL));
        view.getContext().startActivity(intent);
    }

    private void startFeedbackActivity() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + emailRecipients[0]));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailRecipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT   , emailDefaultBody);
        view.getContext().startActivity(emailIntent);
    }

    private void setJoinGroupDialog() {
        joinGroupDialog.setTitle("Join a group");
        joinGroupDialog.setMessage("Enter group invite code to join a group.");
        joinGroupDialog.setInputPlaceholder("Enter your invite code");
        joinGroupDialog.setPositiveButton("Join", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inviteCode = joinGroupDialog.getEditTextValue();
                if (inviteCode != null && !inviteCode.isEmpty()) {
                    joinGroup(inviteCode);
                } else {
                    joinGroupDialog.showError("Please enter a group name");
                }

            }
        });

        joinGroupDialog.setNegativeButton("Cancel", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroupDialog.dismiss();
            }
        });
    }

    private void openJoinInvitedGroupDialog() {
        joinGroupDialog.show(((AppCompatActivity) view.getContext()).getSupportFragmentManager(), "JOIN_GROUP_DIALOG");
    }

    private void joinGroup(String inviteCode) {
        boolean isInternetConnected = Utils.isInternetConnected(view.getContext());
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
                try {
                    startGroupDetailActivity(response.getJSONObject("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    if (networkResponse.statusCode == 401) {
                        joinGroupDialog.dismiss();
                        Utils.logout(view.getContext());
                    }

                    if (networkResponse.statusCode == 400) {
                        try {
                            JSONObject err = new JSONObject(new String(networkResponse.data));
                            joinGroupDialog.showError(err.getString("message"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("invite_code", inviteCode);

            JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, JOIN_GROUP_INVITE_URL, requestObject, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onJoinGroupInternetFail() {
        joinGroupDialog.showError("No internet connection");
    }

    private void startGroupDetailActivity(JSONObject groupDetail) {
        Intent intent = new Intent(view.getContext(), GroupDetailActivity.class);
        intent.putExtra(view.getContext().getString(R.string.intent_group_detail_key), new EventGroup(groupDetail));
        view.getContext().startActivity(intent);
    }
}
