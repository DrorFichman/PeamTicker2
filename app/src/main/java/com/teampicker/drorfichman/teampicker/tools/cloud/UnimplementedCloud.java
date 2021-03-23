package com.teampicker.drorfichman.teampicker.tools.cloud;

import android.content.Context;

public class UnimplementedCloud implements CloudSync {
    @Override
    public void syncToCloud(Context ctx, SyncProgress handler) {

    }

    @Override
    public void pullFromCloud(Context ctx, SyncProgress handler) {

    }

    @Override
    public void storeAccountData() {

    }
}
