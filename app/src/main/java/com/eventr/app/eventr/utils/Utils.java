package com.eventr.app.eventr.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.eventr.app.eventr.LoginActivity;
import com.eventr.app.eventr.R;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;

/**
 * Created by Suraj on 29/08/16.
 */
public class Utils {
    public static boolean isInternetConnected(Context context) {
        return isNetworkAvailable(context);
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
}
