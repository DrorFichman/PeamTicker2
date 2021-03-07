package com.teampicker.drorfichman.teampicker.tools;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class AuthHelper {

    public static void requireLogin(Activity ctx, int activityResultCode) {
        FirebaseUser user = AuthHelper.getUser();
        if (user == null) {
            Log.i("AccountFB", "User not found");
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create and launch sign-in intent with Auth providers
            ctx.startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    activityResultCode);
        } else {
            Log.i("AccountFB", "User found " + user.getEmail() + " - " + user.getUid());
            Toast.makeText(ctx, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
        }
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getUerUID() {
        return getUser().getUid();
    }
}