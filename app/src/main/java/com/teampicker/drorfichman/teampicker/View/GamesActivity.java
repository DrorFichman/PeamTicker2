package com.teampicker.drorfichman.teampicker.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.teampicker.drorfichman.teampicker.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class GamesActivity extends AppCompatActivity {
    private static final String EXTRA_PLAYER_FILTER = "EXTRA_PLAYER_FILTER";
    private static final String EXTRA_PLAYER_COLLABORATOR = "EXTRA_PLAYER_COLLABORATOR";

    private String mPlayerName;
    private String mPlayerCollaborator;

    @NonNull
    public static Intent getGameActivityIntent(Context context, String playerName, String collaborator) {
        Intent intent = new Intent(context, GamesActivity.class);
        intent.putExtra(EXTRA_PLAYER_FILTER, playerName);
        intent.putExtra(EXTRA_PLAYER_COLLABORATOR, collaborator);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_games_activity);

        getPlayers();

        GamesFragment gamesFragment =
                (GamesFragment) getSupportFragmentManager().findFragmentById(R.id.games_container);

        if (gamesFragment == null) {
            gamesFragment = new GamesFragment(mPlayerName, mPlayerCollaborator);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.games_container, gamesFragment);
            transaction.commit();
        }
    }

    private void getPlayers() {
        Intent intent = getIntent();
        mPlayerName = intent.getStringExtra(EXTRA_PLAYER_FILTER);
        mPlayerCollaborator = intent.getStringExtra(EXTRA_PLAYER_COLLABORATOR);

        String addTitle = "";
        if (mPlayerName != null) addTitle = " : " + mPlayerName;
        if (mPlayerName != null && mPlayerCollaborator != null)
            addTitle += " + " + mPlayerCollaborator;
        setTitle(getTitle() + addTitle);
    }
}
