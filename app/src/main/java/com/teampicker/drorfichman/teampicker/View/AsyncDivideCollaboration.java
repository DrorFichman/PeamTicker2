package com.teampicker.drorfichman.teampicker.View;

import android.os.AsyncTask;
import android.view.View;

import com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import java.lang.ref.WeakReference;

class AsyncDivideCollaboration extends AsyncTask<Void, Void, String> {

    private WeakReference<MakeTeamsActivity> ref;

    AsyncDivideCollaboration(MakeTeamsActivity activity) {
        ref = new WeakReference<>(activity);
    }

    @Override
    protected String doInBackground(Void... params) {
        MakeTeamsActivity activity = ref.get();
        if (activity == null || activity.isFinishing()) return null;

        activity.divideComingPlayers(TeamDivision.DivisionStrategy.Optimize, false);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MakeTeamsActivity activity = ref.get();
        if (activity == null || activity.isFinishing()) return;

        activity.progressBarTeamDivision.setVisibility(View.VISIBLE);
        activity.teamStatsLayout.setVisibility(View.INVISIBLE);
        activity.buttonsLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MakeTeamsActivity activity = ref.get();
        if (activity == null || activity.isFinishing()) return;

        activity.postDividePlayers();
        activity.progressBarTeamDivision.setVisibility(View.GONE);
        activity.teamStatsLayout.setVisibility(View.VISIBLE);
        activity.buttonsLayout.setVisibility(View.VISIBLE);
    }
}
