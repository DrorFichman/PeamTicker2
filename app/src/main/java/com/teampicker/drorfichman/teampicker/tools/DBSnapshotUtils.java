package com.teampicker.drorfichman.teampicker.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.ajts.androidmads.library.ExcelToSQLite;
import com.ajts.androidmads.library.SQLiteToExcel;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.PlayerContract;

import java.io.File;

import androidx.core.app.ActivityCompat;

/**
 * Created by drorfichman on 4/14/18.
 */
public class DBSnapshotUtils {

    private static final String SNAPSHOT_FILE_NAME = "peamticker_snapshot.xls";
    private static final String EXPORT_PATH = "/TeamPicker/DBs/";

    public interface ExportListener {
        void exportStarted();
        void exportCompleted(File filePath);
        void exportError(String msg);
    }

    public interface ImportListener {
        void preImport();
        void importStarted();
        void importCompleted();
        void importError(String msg);
    }

    public static void takeDBSnapshot(Activity activity, ExportListener listener) {

        PermissionTools.checkPermissionsForExecution(activity, 3,
                () -> takeDBSnapshotPermitted(activity, listener),
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private static void takeDBSnapshotPermitted(final Context ctx, final ExportListener listener) {

        final File dbs = new File(Environment.getExternalStorageDirectory().toString() + EXPORT_PATH);
        dbs.mkdirs();

        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(ctx, DbHelper.DATABASE_NAME, dbs.getAbsolutePath());
        sqliteToExcel.exportSpecificTables(PlayerContract.getTables(), SNAPSHOT_FILE_NAME,
                new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {
                listener.exportStarted();
            }

            @Override
            public void onCompleted(String filePath) {
                listener.exportCompleted(new File(dbs, SNAPSHOT_FILE_NAME));
            }

            @Override
            public void onError(Exception e) {
                listener.exportError(e.getMessage());
                Log.e("EXPORT", "Failed export", e);
            }
        });
    }

    public static void importDBSnapshotSelected(final Context ctx, String snapshotPath,
                                                final ImportListener listener) {

        Log.d("IMPORT", "Import from " + snapshotPath);

        if (TextUtils.isEmpty(snapshotPath)) {
            listener.importError("attempt importing file from local file manager");
            return;
        }

        // Remove all existing DB content
        DbHelper.deleteTableContents(ctx);

        listener.preImport();

        ExcelToSQLite excelToSQLite = new ExcelToSQLite(ctx.getApplicationContext(), DbHelper.DATABASE_NAME, false);
        excelToSQLite.importFromFile(snapshotPath, new ExcelToSQLite.ImportListener() {
            @Override
            public void onStart() {
                listener.importStarted();
            }

            @Override
            public void onCompleted(String dbName) {
                listener.importCompleted();
            }

            @Override
            public void onError(Exception e) {
                Log.e("IMPORT", "Failed import", e);
                listener.importError(e.getMessage());
            }
        });
    }
}
