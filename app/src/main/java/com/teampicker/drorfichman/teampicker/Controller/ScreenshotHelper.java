package com.teampicker.drorfichman.teampicker.Controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by drorfichman on 11/11/16.
 */
public class ScreenshotHelper {

    public static void takeScreenShot(Activity activity) {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            takeScreenshotPermitted(activity, DbHelper.getNow() + "-" + System.currentTimeMillis());
        }
    }

    private static void takeScreenshotPermitted(Activity activity, String name) {

        try {

            // create bitmap screen capture
            View v1 = activity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imagePath = new File(Environment.getExternalStorageDirectory().toString() + "/TeamPicker/");
            imagePath.mkdirs();
            File imageFile = new File(imagePath, name + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 50;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(activity, imageFile);

        } catch (Throwable e) {

            // Several error may come out with file handling or OOM
            Toast.makeText(activity, "Failed to take screenshot " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private static void openScreenshot(Context ctx, File imageFile) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        intent.setType("image/*");
        ctx.startActivity(Intent.createChooser(intent, "Share screenshot"));
    }
}
