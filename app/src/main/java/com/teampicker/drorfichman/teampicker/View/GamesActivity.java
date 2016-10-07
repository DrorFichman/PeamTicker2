package com.teampicker.drorfichman.teampicker.View;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Adapter.GameAdapter;
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
                showTeams(team1, team2, details);

            }
        });

        gamesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                checkGameDeletion((String) view.getTag());
                return true;
            }
        });

        gamesList.setAdapter(gamesAdapter);
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    private void showTeams(ArrayList<Player> team1, ArrayList<Player> team2, String details) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(details);

        String teams = "";
        int size = Math.max(team1.size(), team2.size());
        for (int i = 0; i < size; ++i) {
            String team1Name = "";
            String team2Name = "";
            if (i < team1.size()) {
                team1Name = team1.get(i).mName;
            }
            if (i < team1.size()) {
                team2Name = team2.get(i).mName;
            }

            // TODO improve spacing
            teams += padRight(team2Name, 30) + " - " + padLeft(team1Name, 30) + "\n";
        }

        // set dialog message
        alertDialogBuilder
                .setMessage(teams)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void checkGameDeletion(final String game) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Delete");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to remove this game?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // TODO delete/edit game results?
                        Log.d("teams", "TODO delete game");
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
