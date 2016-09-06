package com.eventr.app.eventr.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.eventr.app.eventr.LoginActivity;
import com.eventr.app.eventr.R;
import com.facebook.login.LoginManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Suraj on 29/08/16.
 */
public class Utils {
    private static int PERMISSION_REQUEST_CODE_LOCATION = 1;
    public static boolean isInternetConnected(Context context) {
        return isNetworkAvailable(context);
    }

    public static boolean isLocationPermitted(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void askLocationPermission(Context context) {
        ActivityCompat.requestPermissions((Activity) context , new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, PERMISSION_REQUEST_CODE_LOCATION);
    }

    /**
     * Don't use this method in other classes, use isInternetConnected method instead
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public static void showActionableSnackBar(View view, String message, String actionText, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setAction(actionText, onClickListener);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.GRAY);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    public static void showAlertWindow(Context context, String message, String actionText, DialogInterface.OnClickListener onOKClickListener, DialogInterface.OnClickListener onCancelClickListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setPositiveButton(actionText, onOKClickListener);
        alertDialogBuilder.setNegativeButton("cancel", onCancelClickListener);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(message);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void showCloseActivityWindow(final Context context, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((Activity) context).finish();
            }
        });
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(message);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void logout(Context context) {
        logoutFB();
        clearUserData(context);
    }

    public static void logoutFB() {
        LoginManager.getInstance().logOut();
    }

    private static void clearUserData(Context context) {
        SharedPreferences userPreferences = context.getSharedPreferences(context.getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.remove(context.getString(R.string.name));
        editor.remove(context.getString(R.string.pic_url));
        editor.remove(context.getString(R.string.fb_id));
        editor.remove(context.getString(R.string.email));
        editor.remove(context.getString(R.string.access_token_key));
        editor.commit();

        Intent intent = new Intent(context.getApplicationContext(), LoginActivity.class);
        context.startActivity(intent);
    }

    public static Date getDateFromString(String date) {
        SimpleDateFormat incomingFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return incomingFormat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDateAndMonth(Date date) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM\r\ndd", java.util.Locale.getDefault());
        try {
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTimeString(Date date) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault());
        try {
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void showNewGroupDialog(Context context, DialogInterface.OnClickListener onOKClickListener, DialogInterface.OnClickListener onCancelClickListener) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setPositiveButton("create", onOKClickListener);
        alertDialogBuilder.setNegativeButton("cancel", onCancelClickListener);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle(R.string.add_group_text);
        alertDialogBuilder.setView(inflater.inflate(R.layout.custom_dialog_fragment, null));
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static boolean isLocationServiceActive(final Context context) {
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (gpsEnabled | networkEnabled) return true;
        else return false;
    }
}
