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
import com.teampicker.drorfichman.teampicker.Controller.ScreenshotHelper;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerParticipationActivity extends AppCompatActivity {

    private ListView playersList;
    private PlayerParticipationAdapter playersAdapter;
    private ArrayList<PlayerParticipation> players = new ArrayList<>();

    private static final String PLAYER = "PLAYER";
    private static final String BLUE = "BLUE";
    private static final String ORANGE = "ORANGE";
    private int games = 50;

    private Player pPlayer;
    private ArrayList<Player> blue;
    private ArrayList<Player> orange;

    sortType sort = sortType.gamesWith;
    boolean originalOrder = true;

    @NonNull
    public static Intent getPlayerParticipationActivity(Context context, String playerName,
                                                        ArrayList<Player> blue, ArrayList<Player> orange) {
        Intent intent = new Intent(context, PlayerParticipationActivity.class);
        intent.putExtra(PlayerParticipationActivity.PLAYER, playerName);
        intent.putExtra(PlayerParticipationActivity.BLUE, blue);
        intent.putExtra(PlayerParticipationActivity.ORANGE, orange);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_participation_activity);

        Intent intent = getIntent();
        if (intent.hasExtra(PLAYER)) {
            pPlayer = DbHelper.getPlayer(this, intent.getStringExtra(PLAYER));
            orange = (ArrayList<Player>) intent.getSerializableExtra(ORANGE);
            blue = (ArrayList<Player>) intent.getSerializableExtra(BLUE);
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

        playersList = (ListView) findViewById(R.id.players_participation_list);

        refreshPlayers();

        setHeadlineSorting(R.id.player_name, sortType.name);

        ((TextView) findViewById(R.id.part_games_count_with)).setText("Games\nWith");
        setHeadlineSorting(R.id.part_games_count_with, sortType.gamesWith);

        ((TextView) findViewById(R.id.part_wins_percentage_with)).setText("Success\nWith");
        setHeadlineSorting(R.id.part_wins_percentage_with, sortType.successWith);

        ((TextView) findViewById(R.id.part_games_count_against)).setText("Games\nVs");
        setHeadlineSorting(R.id.part_games_count_against, sortType.gamesVs);

        ((TextView) findViewById(R.id.part_wins_percentage_against)).setText("Success\nVs");
        setHeadlineSorting(R.id.part_wins_percentage_against, sortType.successVs);
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
                final Runnable r = new Runnable() {
                    public void run() {
                        ScreenshotHelper.takeListScreenshot(PlayerParticipationActivity.this,
                                playersList, findViewById(R.id.titles), playersAdapter);
                    }
                };
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

        defineSorting();

        playersAdapter = new PlayerParticipationAdapter(PlayerParticipationActivity.this, players, blue, orange);
        playersList.setAdapter(playersAdapter);
    }

    //region sort
    private void defineSorting() {
        Collections.sort(players, new Comparator<PlayerParticipation>() {
            @Override
            public int compare(PlayerParticipation first, PlayerParticipation second) {
                PlayerParticipation p1 = first;
                PlayerParticipation p2 = second;
                if (!originalOrder) {
                    p1 = second;
                    p2 = first;
                }

                if (sort.equals(sortType.name)) {
                    return p1.mName.compareTo(p2.mName);
                } else if (sort.equals(sortType.gamesWith)) {
                    return byGames(p1, p2);
                } else if (sort.equals(sortType.gamesVs)) {
                    return nyGamesVs(p1, p2);
                } else if (sort.equals(sortType.successWith)) {
                    return bySuccessWith(p1, p2);
                } else if (sort.equals(sortType.successVs)) {
                    return bySuccessVs(p1, p2);
                } else {
                    return p1.mName.compareTo(p2.mName);
                }
            }
        });
    }

    private int bySuccessVs(PlayerParticipation p1, PlayerParticipation p2) {
        if (p2.statisticsVs.successRate == p1.statisticsVs.successRate)
            return Integer.compare(p2.statisticsVs.getWinRate(), p1.statisticsVs.getWinRate());
        else
            return Integer.compare(p2.statisticsVs.successRate, p1.statisticsVs.successRate);
    }

    private int bySuccessWith(PlayerParticipation p1, PlayerParticipation p2) {
        if (p2.statisticsWith.successRate == p1.statisticsWith.successRate)
            return Integer.compare(p2.statisticsWith.getWinRate(), p1.statisticsWith.getWinRate());
        else
            return Integer.compare(p2.statisticsWith.successRate, p1.statisticsWith.successRate);
    }

    private int nyGamesVs(PlayerParticipation p1, PlayerParticipation p2) {
        if (p2.statisticsVs.gamesCount == p1.statisticsVs.gamesCount)
            return Integer.compare(p2.statisticsVs.successRate, p1.statisticsVs.successRate);
        else
            return Integer.compare(p2.statisticsVs.gamesCount, p1.statisticsVs.gamesCount);
    }

    private int byGames(PlayerParticipation p1, PlayerParticipation p2) {
        if (p2.statisticsWith.gamesCount == p1.statisticsWith.gamesCount)
            return Integer.compare(p2.statisticsWith.successRate, p1.statisticsWith.successRate);
        else
            return Integer.compare(p2.statisticsWith.gamesCount, p1.statisticsWith.gamesCount);
    }

    private void setHeadlineSorting(int field, final sortType sorting) {
        findViewById(field).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sort == sorting) {
                    originalOrder = !originalOrder;
                } else {
                    originalOrder = true;
                    sort = sorting;
                }
                refreshPlayers();
            }
        });
    }
    //endregion
}
