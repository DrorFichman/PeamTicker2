package com.teampicker.drorfichman.teampicker.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.ajts.androidmads.library.ExcelToSQLite;
import com.ajts.androidmads.library.SQLiteToExcel;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.View.MainActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by drorfichman on 4/14/18.
 */
public class DBSnapshotUtils {

    private static final String SNAPSHOT_FILE_NAME = "peamticker_snapshot.xls";
    public static final String EXPORT_PATH = "/TeamPicker/DBs/";

    public static void takeDBSnapshot(Activity activity) {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            takeDBSnapshotPermitted(activity);
        }
    }

    private static void takeDBSnapshotPermitted(final Context ctx) {

        final File dbs = new File(Environment.getExternalStorageDirectory().toString() + EXPORT_PATH);
        dbs.mkdirs();

        ArrayList<String> tables = new ArrayList<>(3);
        tables.add("player");
        tables.add("game");
        tables.add("player_game");

        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(ctx, DbHelper.DATABASE_NAME, dbs.getAbsolutePath());
        sqliteToExcel.exportSpecificTables(tables, SNAPSHOT_FILE_NAME, new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {
                Toast.makeText(ctx, "Export Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompleted(String filePath) {
                Toast.makeText(ctx, "Export Completed " + filePath, Toast.LENGTH_SHORT).show();

                // TODO allow user to share the snapshot?
                // sendSnapshot(ctx, new File(dbs, SNAPSHOT_FILE_NAME));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ctx, "Import Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("IMPORT", "Failed export", e);
            }
        });
    }

    private static void sendSnapshot(Context ctx, File snapshotFile) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri snapshotURI = FileProvider.getUriForFile(ctx,
                ctx.getApplicationContext().getPackageName() + ".team.picker.share.screenshot",
                snapshotFile);

        intent.putExtra(Intent.EXTRA_STREAM, snapshotURI);
        intent.setType("*/*");
        ctx.startActivity(Intent.createChooser(intent, "Send snapshot"));
    }

    public static void importDBSnapshot(Activity activity) {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        } else {
            importDBSnapshotPermitted(activity);
        }
    }

    private static void importDBSnapshotPermitted(final Context ctx) {

        // TODO import snapshot from user selected path?

        String path = Environment.getExternalStorageDirectory().toString() + EXPORT_PATH + SNAPSHOT_FILE_NAME;

        Log.d("IMPORT", "Import from " + path);

        DbHelper.deletePlayer(ctx, "דרור");

        ExcelToSQLite excelToSQLite = new ExcelToSQLite(ctx.getApplicationContext(), DbHelper.DATABASE_NAME, false);
        excelToSQLite.importFromFile(path, new ExcelToSQLite.ImportListener() {
            @Override
            public void onStart() {
                Toast.makeText(ctx, "Import Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompleted(String dbName) {
                Toast.makeText(ctx, "Import Completed " + dbName, Toast.LENGTH_SHORT).show();
                if (ctx instanceof MainActivity) {
                    ((MainActivity) ctx).refreshPlayers();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ctx, "Import Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("IMPORT", "Failed import", e);
            }
        });
    }
}
