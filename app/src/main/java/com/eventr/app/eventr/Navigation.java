package com.eventr.app.eventr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.telecom.Call;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.eventr.app.eventr.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

/**
 * Created by Suraj on 21/08/16.
 */
public class Navigation {
    private NavigationView view;
    private DrawerLayout drawer;
    private SharedPreferences userPreferences;
    private String name;
    private CallbackManager callbackManager;

    private String[] emailRecipients = new String[]{"support@eventr.biz"};
    private String emailSubject, emailDefaultBody;

    private static final String PRIVACY_POLICY_URL = "http://eventr.biz/privacy.html";

    public Navigation(final Context context, NavigationView mView, DrawerLayout mDrawer) {
        view = mView;
        drawer = mDrawer;
        View headerLayout = view.getHeaderView(0);
        TextView userName = (TextView) headerLayout.findViewById(R.id.drawer_header_user_name);

        userPreferences = context.getSharedPreferences(context.getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        name = userPreferences.getString(context.getString(R.string.name), null);

        userName.setText(name);

        emailSubject = context.getString(R.string.feedback_email_sub);
        emailDefaultBody = context.getString(R.string.feedback_email_default_body);

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
}
