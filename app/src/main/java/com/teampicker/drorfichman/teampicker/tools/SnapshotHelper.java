package com.teampicker.drorfichman.teampicker.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.teampicker.drorfichman.teampicker.R;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

public class SnapshotHelper {

    public static String ginnegar = "Ginnegar_snapshot.xls";
    public static String autodesk = "Autodesk_snapshot.xls";

    public static void checkImportApproved(Context ctx, final DBSnapshotUtils.ImportListener handler, final String importPath) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);

        alertDialogBuilder.setTitle("Import Data Warning");

        alertDialogBuilder
                .setMessage("Delete local data and import selected file?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog, id) -> {

                    DBSnapshotUtils.importDBSnapshotSelected(ctx, importPath, handler);

                    dialog.dismiss();
                });

        alertDialogBuilder.create().show();
    }

    public static void sendSnapshot(Activity ctx, File snapshotFile) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri snapshotURI = FileProvider.getUriForFile(ctx,
                ctx.getApplicationContext().getPackageName() + ".team.picker.share.screenshot",
                snapshotFile);

        intent.putExtra(Intent.EXTRA_STREAM, snapshotURI);
        intent.setType("*/*");
        ctx.startActivity(Intent.createChooser(intent, "Send snapshot"));
    }
}
