package com.teampicker.drorfichman.teampicker.Data;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;

public class AccountData {

    public String email = "asa";
    public String displayName = "aaa";
    public String uid;
    public String photoUrl;
    public long lastSignInTimestamp;
    public long creationTimestamp;

    public AccountData(FirebaseUser user) {
        if (user != null) {
            this.email = user.getEmail();
            this.displayName = user.getDisplayName();
            this.uid = user.getUid();
            this.photoUrl = user.getPhotoUrl().toString();
            if (user.getMetadata() != null) {
                this.lastSignInTimestamp = user.getMetadata().getLastSignInTimestamp();
                this.creationTimestamp = user.getMetadata().getCreationTimestamp();
            }
        }
    }
}
