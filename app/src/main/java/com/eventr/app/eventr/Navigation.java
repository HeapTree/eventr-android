package com.eventr.app.eventr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by Suraj on 21/08/16.
 */
public class Navigation {
    private NavigationView view;
    private DrawerLayout drawer;

    public Navigation(final Context context, NavigationView mView, DrawerLayout mDrawer) {
        view = mView;
        drawer = mDrawer;

        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawer.closeDrawers();
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.profile_button:
                        Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                        view.getContext().startActivity(intent);
                }
                return true;
            }
        });
    }

}
