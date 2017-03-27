package com.dulemata.emiliano.biker.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class ActivityDialogInteraction extends AppCompatActivity {

    protected AlertDialog alertDialog;
    protected ProgressDialog progressDialog;

    protected void restoreScreenRotation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    protected void stopScreenRotation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    protected AlertDialog.Builder setAlert(String title, String message, boolean cancelable) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        stopScreenRotation();
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setCancelable(cancelable);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restoreScreenRotation();
            }
        });
        return builder;
    }

    protected void showProgressDialog(String title, String message, boolean cancelable) {
        stopScreenRotation();
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(cancelable);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restoreScreenRotation();
            }
        });
        progressDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null)
            alertDialog.dismiss();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

}
