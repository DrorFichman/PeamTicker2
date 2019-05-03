package com.teampicker.drorfichman.teampicker.View;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.teampicker.drorfichman.teampicker.Adapter.GameAdapter;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;

public class GamesActivity extends AppCompatActivity {

    private static final String PLAYER = "PLAYER";

    private ListView gamesList;
    private GameAdapter gamesAdapter;
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

        ArrayList<Game> games = getGames();
        gamesAdapter = new GameAdapter(this, games);

        gamesList = (ListView) findViewById(R.id.games_list);

        gamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String details = (String) view.getTag(R.id.game_details);
                int gameId = (int) view.getTag(R.id.game_id);
                showTeamsDialog(gameId, details);
            }
        });

        gamesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                checkGameDeletion((Integer) view.getTag(R.id.game_id));
                return true;
            }
        });

        gamesList.setAdapter(gamesAdapter);
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
        DialogFragment newFragment = GameDetailsDialogFragment.newInstance(this, gameId, details);
        newFragment.show(ft, "game_dialog");
    }

    private void checkGameDeletion(final int game) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.delete);

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to remove this game?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        DbHelper.deleteGame(GamesActivity.this, game);
                        refreshGames();
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void refreshGames() {
        ArrayList<Game> games = getGames();

        // Attach cursor adapter to the ListView
        gamesList.setAdapter(new GameAdapter(this, games));
    }
}
