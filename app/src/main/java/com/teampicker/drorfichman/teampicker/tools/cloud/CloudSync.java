package com.teampicker.drorfichman.teampicker.tools.cloud;

import android.content.Context;

public interface CloudSync {
    void syncToCloud(Context ctx, SyncProgress handler);

    void pullFromCloud(Context ctx, SyncProgress handler);

    void storeAccountData();
}
