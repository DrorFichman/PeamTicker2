package com.teampicker.drorfichman.teampicker.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Adapter.GameAdapter;
import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapterGameHistory;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class GamesActivity extends AppCompatActivity {
    private static final String EXTRA_PLAYER_FILTER = "EXTRA_PLAYER_FILTER";
    private static final String EXTRA_PLAYER_COLLABORATOR = "EXTRA_PLAYER_COLLABORATOR";

    private String mPlayerName;
    private String mPlayerCollaborator;

    private ListView gamesList;
    private GameAdapter gamesAdapter;

    private int mCurrGameId;
    private ArrayList<Player> mTeam1;
    private ArrayList<Player> mTeam2;

    private View gameDetails;
    private ListView team1List;
    private ListView team2List;

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

        gamesList = findViewById(R.id.games_list);
        gameDetails = findViewById(R.id.game_details_layout);

        team1List = findViewById(R.id.game_details_team1);
        team2List = findViewById(R.id.game_details_team2);

        team1List.setOnItemLongClickListener(onPlayerClick);
        team2List.setOnItemLongClickListener(onPlayerClick);

        gamesList.setOnItemClickListener((adapterView, view, position, l) -> {
            Game game = (Game) view.getTag(R.id.game);
            onGameClick(game);
        });

        gamesList.setOnItemLongClickListener((adapterView, view, position, l) -> {
            onGameLongClick(((Game) view.getTag(R.id.game)).gameId);
            return true;
        });

        refreshGames();
    }

    private void getPlayers() {
        Intent intent = getIntent();
        mPlayerName = intent.getStringExtra(EXTRA_PLAYER_FILTER);
        mPlayerCollaborator = intent.getStringExtra(EXTRA_PLAYER_COLLABORATOR);

        String addTitle = "";
        if (mPlayerName != null) addTitle = " : " + mPlayerName;
        if (mPlayerName != null && mPlayerCollaborator != null ) addTitle += " + " + mPlayerCollaborator;
        setTitle(getTitle() + addTitle);
    }

    private void refreshTeams() {
        mTeam1 = DbHelper.getCurrTeam(this, mCurrGameId, TeamEnum.Team1, 0);
        mTeam2 = DbHelper.getCurrTeam(this, mCurrGameId, TeamEnum.Team2, 0);

        mTeam1.sort(Comparator.comparing(Player::name));
        mTeam2.sort(Comparator.comparing(Player::name));

        team1List.setAdapter(new PlayerTeamAdapterGameHistory(this, mTeam1, mPlayerName, mPlayerCollaborator));
        team2List.setAdapter(new PlayerTeamAdapterGameHistory(this, mTeam2, mPlayerName, mPlayerCollaborator));
    }

    private void refreshGames() {

        ArrayList<Game> games;
        if (mPlayerName != null && mPlayerCollaborator != null) { // games in which both played
            games = DbHelper.getGames(this, mPlayerName, mPlayerCollaborator);
        } else if (mPlayerName != null) { // games in which selected player played
            games = DbHelper.getGames(this, mPlayerName);
        } else { // all games
            games = DbHelper.getGames(this);
        }

        // Attach cursor adapter to the ListView
        gamesAdapter = new GameAdapter(this, games, mCurrGameId);
        gamesList.setAdapter(gamesAdapter);
    }

    private void refreshSelectedGame() {
        gamesAdapter.setSelectedGameId(mCurrGameId);
        gamesAdapter.notifyDataSetChanged();
    }

    //region game click
    private void onGameClick(Game game) {
        if (mCurrGameId == game.gameId) {
            mCurrGameId = -1;
            gameDetails.setVisibility(View.GONE);
        } else {
            mCurrGameId = game.gameId;
            gameDetails.setVisibility(View.VISIBLE);
        }
        refreshSelectedGame();
        refreshTeams();
    }
    //endregion

    //region game long clicked
    private void onGameLongClick(int gameId) {
        if (mCurrGameId > 0 && mCurrGameId == gameId) { // selected game - copy
            checkCopyGame();
        } else { // non-selected game - delete
            checkGameDeletion(gameId);
        }
    }

    private void checkCopyGame() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.copy);
        alertDialogBuilder
                .setMessage("Copy coming players and teams?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    copyGamePlayers();
                    dialog.dismiss();
                });

        alertDialogBuilder.create().show();
    }

    private void copyGamePlayers() {
        DbHelper.clearComingPlayers(this);
        DbHelper.setPlayerComing(this, mTeam1);
        DbHelper.setPlayerComing(this, mTeam2);
        DbHelper.saveTeams(this, mTeam1, mTeam2);
        Toast.makeText(this, R.string.copy_players_success, Toast.LENGTH_SHORT).show();
    }

    private void checkGameDeletion(final int game) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.delete);
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
    //endregion

    //region player click
    private AdapterView.OnItemLongClickListener onPlayerClick = (parent, view, position, id) -> {
        Player player = (Player) parent.getItemAtPosition(position);
        checkPlayerChange(player);
        return false;
    };

    private void checkPlayerChange(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Modify");
        alertDialogBuilder
                .setMessage("Do you want to modify this player attendance?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    movePlayer(player);
                    dialog.dismiss();
                });

        alertDialogBuilder.create().show();
    }

    private void movePlayer(Player player) {

        DbHelper.modifyPlayerResult(this, mCurrGameId, player.mName);
        refreshTeams();
    }
    //endregion
}
