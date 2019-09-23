package com.teampicker.drorfichman.teampicker.View;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.teampicker.drorfichman.teampicker.Adapter.GameAdapter;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class GamesActivity extends AppCompatActivity {

    private static final String PLAYER = "PLAYER";

    private ListView gamesList;
    private Player pPlayer;

    @NonNull
    public static Intent getGameActivityIntent(Context context, String playerName) {
        Intent intent = new Intent(context, GamesActivity.class);
        intent.putExtra(GamesActivity.PLAYER, playerName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_games_activity);

        Intent intent = getIntent();
        if (intent.hasExtra(PLAYER) && intent.getStringExtra(PLAYER) != null) {
            pPlayer = DbHelper.getPlayer(this, intent.getStringExtra(PLAYER));
        }

        gamesList = findViewById(R.id.games_list);

        gamesList.setOnItemClickListener((adapterView, view, i, l) -> {
            String details = (String) view.getTag(R.id.game_details);
            int gameId = (int) view.getTag(R.id.game_id);
            showTeamsDialog(gameId, details);
        });

        gamesList.setOnItemLongClickListener((adapterView, view, i, l) -> {
            checkGameDeletion((Integer) view.getTag(R.id.game_id));
            return true;
        });

        refreshGames();
    }

    private void refreshGames() {
        ArrayList<Game> games = getGames();

        // Attach cursor adapter to the ListView
        gamesList.setAdapter(new GameAdapter(this, games));
    }

    @NonNull
    private ArrayList<Game> getGames() {

        if (pPlayer != null) {
            return DbHelper.getGames(this, pPlayer.mName);
        } else {
            return DbHelper.getGames(this);
        }
    }

    private void showTeamsDialog(int gameId, String details) {

        // Create and show the dialog.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameDetailsDialogFragment.newInstance(gameId, details);
        newFragment.show(ft, "game_dialog");
    }

    private void checkGameDeletion(final int game) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.delete);

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to remove this game?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog, id) -> {

                    DbHelper.deleteGame(GamesActivity.this, game);
                    refreshGames();
                    dialog.dismiss();
                });

        alertDialogBuilder.create().show();
    }
}
