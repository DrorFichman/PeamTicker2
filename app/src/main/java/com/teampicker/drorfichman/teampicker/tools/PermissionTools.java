package com.teampicker.drorfichman.teampicker.tools;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionTools {

    public interface onPermissionGranted {
        void execute();
    }

    public static void checkPermissionsForExecution(Activity ctx, int requestCode,
                                                    onPermissionGranted handler,
                                                    String... check) {

        for (String per : check) {
            int permission = ActivityCompat.checkSelfPermission(ctx, per);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ctx, check, requestCode);
                return;
            }
        }

        handler.execute();
    }
}
