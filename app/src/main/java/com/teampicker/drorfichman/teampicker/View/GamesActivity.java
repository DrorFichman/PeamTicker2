package com.teampicker.drorfichman.teampicker.View;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
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

    private ListView gamesList;
    private GameAdapter gamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_games_activity);

        gamesList = (ListView) findViewById(R.id.games_list);

        ArrayList<Game> games = DbHelper.getGames(getApplicationContext());
        gamesAdapter = new GameAdapter(this, games);

        gamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<Player> team1 = DbHelper.getCurrTeam(GamesActivity.this, Integer.valueOf((String) view.getTag(R.id.game_id)), TeamEnum.Team1, 0);
                ArrayList<Player> team2 = DbHelper.getCurrTeam(GamesActivity.this, Integer.valueOf((String) view.getTag(R.id.game_id)), TeamEnum.Team2, 0);
                String details = (String) view.getTag(R.id.game_details);
                String gameId = (String) view.getTag(R.id.game_id);
                showTeamsDialog(team1, team2, gameId, details);
            }
        });

        gamesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                checkGameDeletion((String) view.getTag(R.id.game_id));
                return true;
            }
        });

        gamesList.setAdapter(gamesAdapter);
    }

    private void showTeamsDialog(ArrayList<Player> team1, ArrayList<Player> team2,
                                 String gameId, String details) {

        // Create and show the dialog.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameDetailsDialogFragment.newInstance(team1, team2, gameId, details);
        newFragment.show(ft, "game_dialog");
    }

    private void checkGameDeletion(final String game) {

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
        ArrayList<Game> games = DbHelper.getGames(getApplicationContext());

        // Attach cursor adapter to the ListView
        gamesList.setAdapter(new GameAdapter(this, games));
    }
}
