package com.dulemata.emiliano.biker.util;

import android.content.Context;
import android.support.v7.app.AlertDialog;

/**
 * Created by Emiliano on 20/03/2017.
 */

public class Dialog {

    public static AlertDialog.Builder showAlert(Context context, AlertDialog dialog, String title, String message, boolean isCanellable) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(isCanellable);
        return builder;
    }

}
