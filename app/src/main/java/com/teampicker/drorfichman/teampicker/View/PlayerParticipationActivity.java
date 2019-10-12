package com.teampicker.drorfichman.teampicker.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerParticipationAdapter;
import com.teampicker.drorfichman.teampicker.tools.ScreenshotHelper;
import com.teampicker.drorfichman.teampicker.Controller.Sort.Sorting;
import com.teampicker.drorfichman.teampicker.Controller.Sort.sortType;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerParticipationActivity extends AppCompatActivity implements Sorting.sortingCallbacks {
    private static final String EXTRA_PLAYER = "EXTRA_PLAYER";
    private static final String EXTRA_BLUE = "EXTRA_BLUE";
    private static final String EXTRA_ORANGE = "EXTRA_ORANGE";

    private ArrayList<PlayerParticipation> players = new ArrayList<>();
    private PlayerParticipationAdapter playersAdapter;
    private Player pPlayer;
    private ArrayList<Player> blue;
    private ArrayList<Player> orange;

    private int games = 50;
    Sorting sorting = new Sorting(this, sortType.gamesWith);

    private ListView playersList;

    @NonNull
    public static Intent getPlayerParticipationActivity(Context context, String playerName,
                                                        ArrayList<Player> blue, ArrayList<Player> orange) {
        Intent intent = new Intent(context, PlayerParticipationActivity.class);
        intent.putExtra(PlayerParticipationActivity.EXTRA_PLAYER, playerName);
        intent.putExtra(PlayerParticipationActivity.EXTRA_BLUE, blue);
        intent.putExtra(PlayerParticipationActivity.EXTRA_ORANGE, orange);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_participation_activity);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_PLAYER)) {
            pPlayer = DbHelper.getPlayer(this, intent.getStringExtra(EXTRA_PLAYER));
            orange = (ArrayList<Player>) intent.getSerializableExtra(EXTRA_ORANGE);
            blue = (ArrayList<Player>) intent.getSerializableExtra(EXTRA_BLUE);
        }

        ImageView teamIcon = findViewById(R.id.team_icon);
        if (orange != null && orange.contains(pPlayer)) {
            teamIcon.setImageResource(R.drawable.circle_orange);
            teamIcon.setVisibility(View.VISIBLE);
        } else if (blue != null && blue.contains(pPlayer)) {
            teamIcon.setImageResource(R.drawable.circle_blue);
            teamIcon.setVisibility(View.VISIBLE);
        } else {
            teamIcon.setVisibility(View.INVISIBLE);
        }

        playersList = findViewById(R.id.players_participation_list);

        refreshPlayers();

        sorting.setHeadlineSorting(this, R.id.player_name, null, sortType.name);
        sorting.setHeadlineSorting(this, R.id.part_games_count_with, "Games\nWith", sortType.gamesWith);
        sorting.setHeadlineSorting(this, R.id.part_wins_percentage_with, "Success\nWith", sortType.successWith);
        sorting.setHeadlineSorting(this, R.id.part_games_count_against, "Games\nVs", sortType.gamesVs);
        sorting.setHeadlineSorting(this, R.id.part_wins_percentage_against, "Success\nVs", sortType.successVs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statisctics_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_statistics:
                final Runnable r = () -> ScreenshotHelper.takeListScreenshot(PlayerParticipationActivity.this,
                        playersList, findViewById(R.id.titles), playersAdapter);
                new Handler().postDelayed(r, 200);
                break;
            case R.id.action_last_10_games:
                games = 10;
                refreshPlayers();
                break;
            case R.id.action_last_50_games:
                games = 50;
                refreshPlayers();
                break;
            case R.id.action_no_limit:
                games = -1;
                refreshPlayers();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshPlayers() {

        HashMap<String, PlayerParticipation> result = DbHelper.getPlayersParticipationsStatistics(getApplicationContext(), games, pPlayer.mName);
        players.clear();
        players.addAll(result.values());

        Player player = DbHelper.getPlayer(this, pPlayer.mName, games);
        ((TextView) findViewById(R.id.player_name)).setText(
                getString(R.string.player_participation_statistics,
                        player.mName,
                        player.statistics.gamesCount,
                        player.statistics.getWinRate()));

        sorting.sort(players);

        playersAdapter = new PlayerParticipationAdapter(PlayerParticipationActivity.this, players, blue, orange);
        playersList.setAdapter(playersAdapter);
    }

    //region sort
    @Override
    public void refresh() {
        refreshPlayers();
    }
    //endregion
}
