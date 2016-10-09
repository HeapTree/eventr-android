package com.eventr.app.eventr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.eventr.app.eventr.adapters.EventViewPagerFragmentAdapter;
import com.eventr.app.eventr.utils.LocationHelper;
import com.eventr.app.eventr.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.eventr.app.eventr.utils.Utils.MY_PERMISSIONS_REQUEST_KEY;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String REQUEST_TAG = "main_activity";

    @BindView(R.id.drawer_layout_main) public DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view_main) public NavigationView navView;
    @BindView(R.id.toolbar_main) public Toolbar toolbar;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    private Activity thisActivity;

    private LocationRequest mLocationRequest;

    private SharedPreferences userPreferences;

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private static final String SEND_BIRD_APP_ID = "007AC74D-AEBF-451E-8D23-977F39096C4E";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setToolbar();
        setProgressDialog();

        thisActivity = this;

        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);

        permissionsCheck();

        SendBird.init(SEND_BIRD_APP_ID, this);
    }

    private void permissionsCheck() {
        boolean isLocationPermitted = Utils.isLocationPermitted(this);
        boolean isWriteStoragePermitted = Utils.isWriteStoragePermitted(this);
        boolean isReadStoragePermitted = Utils.isReadStoragePermitted(this);
        if (isLocationPermitted && isWriteStoragePermitted && isReadStoragePermitted) {
            setupGoogleApiClient();
        } else {
            Utils.askPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    setupGoogleApiClient();
                } else {
                    finish();
                }
            }
        }
    }

    private void setToolbar() {
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        new Navigation(getApplicationContext(), navView, mDrawerLayout);
    }

    public void setTabs() {
        EventViewPagerFragmentAdapter adapter = new EventViewPagerFragmentAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_toolbar, menu);
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
            case R.id.groups_button:
                startGroupsActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startGroupsActivity() {
        Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }


    private synchronized void setupGoogleApiClient() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            showLocationNotFound();
            return;
        }
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        buildLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        showLocationNotFound();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showLocationNotFound();
        }
    }

    private void showLocationNotFound() {
        mProgressDialog.dismiss();
        Utils.showCloseActivityWindow(this, "Location cannot be fetched");
    }

    private void buildLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder locationSettingBuilder = new LocationSettingsRequest.Builder().setAlwaysShow(true).addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingBuilder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        sendLocationRequest();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    thisActivity,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            showLocationNotFound();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        showLocationNotFound();
                        break;
                }
            }
        });
    }

    private void sendLocationRequest() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putLong(getString(R.string.latitude), Double.doubleToRawLongBits(location.getLatitude()));
        editor.putLong(getString(R.string.longitude), Double.doubleToRawLongBits(location.getLongitude()));
        editor.apply();
        setTabs();
        connectSendBird();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventrRequestQueue.getInstance().cancel(REQUEST_TAG);
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
    }

    private void setProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching location. Please wait");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        sendLocationRequest();
                        break;
                    case Activity.RESULT_CANCELED:
                        showLocationNotFound();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void connectSendBird() {
        String uuid = userPreferences.getString(getString(R.string.user_uuid), null);

        Log.d("UUID", uuid);

        SendBird.connect(uuid, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                mProgressDialog.dismiss();
                if (e != null) {
                    Toast.makeText(MainActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

//                String nickname = getString(R.string.name);
//                String picUrl = getString(R.string.pic_url);

//                SendBird.updateCurrentUserInfo(nickname, picUrl, new SendBird.UserInfoUpdateHandler() {
//                    @Override
//                    public void onUpdated(SendBirdException e) {
//                        if (e != null) {
//                            Toast.makeText(MainActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        mProgressDialog.dismiss();
//                    }
//                });
            }
        });
    }
}
