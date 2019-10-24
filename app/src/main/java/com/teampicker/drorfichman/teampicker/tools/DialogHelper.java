package com.teampicker.drorfichman.teampicker.tools;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class DialogHelper {

    public static void showApprovalDialog(Context ctx, String title, String message,
                                          DialogInterface.OnClickListener positive) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", positive);

        alertDialogBuilder.create().show();
    }
}
