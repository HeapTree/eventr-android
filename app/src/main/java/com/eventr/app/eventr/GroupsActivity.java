package com.eventr.app.eventr;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * Created by Suraj on 15/08/16.
 */
public class GroupsActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setToolbar();
        setGroupsList();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_groups);
        toolbar.setTitle(R.string.groups_title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_groups);
        navView = (NavigationView) findViewById(R.id.navigation_view_groups);
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
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            items.add("item #" + i);
        }

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
        recyclerView.setAdapter(new GroupsRecyclerAdapter(items));
    }
}
