package com.teampicker.drorfichman.teampicker.View;

import android.os.AsyncTask;
import android.view.View;

import com.teampicker.drorfichman.teampicker.Controller.CollaborationHelper;
import com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import java.lang.ref.WeakReference;

class AsyncTeamsAnalysis extends AsyncTask<Void, Void, String> {

    private WeakReference<MakeTeamsActivity> ref;

    AsyncTeamsAnalysis(MakeTeamsActivity activity) {
        ref = new WeakReference<>(activity);
    }

    @Override
    protected String doInBackground(Void... params) {
        MakeTeamsActivity activity = ref.get();
        if (activity == null || activity.isFinishing()) return null;

        activity.analysisResult = CollaborationHelper.getCollaborationData(activity, activity.players1, activity.players2);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MakeTeamsActivity activity = ref.get();
        if (activity == null || activity.isFinishing()) return;

        activity.teamStatsLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MakeTeamsActivity activity = ref.get();
        if (activity == null || activity.isFinishing()) return;

        activity.refreshPlayers();

        activity.teamStatsLayout.setVisibility(View.VISIBLE);
    }
}
