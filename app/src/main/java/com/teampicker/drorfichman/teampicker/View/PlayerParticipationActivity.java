package com.teampicker.drorfichman.teampicker.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    private static final String PLAYER = "PLAYER";
    private int games = 50;
    private Player pPlayer;

    @NonNull
    public static Intent getPlayerParticipationActivity(Context context, String playerName) {
        Intent intent = new Intent(context, PlayerParticipationActivity.class);
        intent.putExtra(PlayerParticipationActivity.PLAYER, playerName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_participation_activity);

        Intent intent = getIntent();
        if (intent.hasExtra(PLAYER)) {
            pPlayer = DbHelper.getPlayer(this, intent.getStringExtra(PLAYER));
        }

        ((TextView) findViewById(R.id.player_name)).setText(pPlayer.mName + " + ");

        ((TextView) findViewById(R.id.part_games_count_with)).setText("Games\nWith");
        ((TextView) findViewById(R.id.part_wins_percentage_with)).setText("Success\nWith");

        ((TextView) findViewById(R.id.part_games_count_against)).setText("Games\nVs");
        ((TextView) findViewById(R.id.part_wins_percentage_against)).setText("Success\nVs");

        playersList = (ListView) findViewById(R.id.players_participation_list);

        refreshList();
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
                refreshList();
                break;
            case R.id.action_last_50_games:
                games = 50;
                refreshList();
                break;
            case R.id.action_no_limit:
                games = -1;
                refreshList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshList() {

        HashMap<String, PlayerParticipation> result = DbHelper.getPlayersParticipationsStatistics(getApplicationContext(), games, pPlayer.mName);
        ArrayList<PlayerParticipation> players = new ArrayList<>();
        players.addAll(result.values());

        Collections.sort(players, new Comparator<PlayerParticipation>() {
            @Override
            public int compare(PlayerParticipation p1, PlayerParticipation p2) {
                return Integer.compare(p2.statisticsWith.gamesCount, p1.statisticsWith.gamesCount);
            }
        });
        updateList(players);

        sortByNameHandler(players);
        sortByGamesHandler(players);
        sortByWinRateHandler(players);
    }

    private void updateList(ArrayList<PlayerParticipation> players) {
        playersAdapter = new PlayerParticipationAdapter(PlayerParticipationActivity.this, players);
        playersList.setAdapter(playersAdapter);
    }

    private void sortByWinRateHandler(final ArrayList<PlayerParticipation> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<PlayerParticipation>() {
                    @Override
                    public int compare(PlayerParticipation p1, PlayerParticipation p2) {
                        // sort by success (instead of win percentage)
                        if (p2.statisticsWith.successRate != p1.statisticsWith.successRate)
                            return Integer.compare(p2.statisticsWith.successRate, p1.statisticsWith.successRate);
                        else
                            return Integer.compare(p2.statisticsWith.gamesCount, p1.statisticsWith.gamesCount);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.part_wins_percentage_with).setOnClickListener(sort);

        View.OnClickListener sortVs = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<PlayerParticipation>() {
                    @Override
                    public int compare(PlayerParticipation p1, PlayerParticipation p2) {
                        // sort by success (instead of win percentage)
                        if (p2.statisticsVs.successRate != p1.statisticsVs.successRate)
                            return Integer.compare(p2.statisticsVs.successRate, p1.statisticsVs.successRate);
                        else
                            return Integer.compare(p2.statisticsVs.gamesCount, p1.statisticsVs.gamesCount);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.part_wins_percentage_against).setOnClickListener(sortVs);
    }

    private void sortByGamesHandler(final ArrayList<PlayerParticipation> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<PlayerParticipation>() {
                    @Override
                    public int compare(PlayerParticipation p1, PlayerParticipation p2) {
                        return Integer.compare(p2.statisticsWith.gamesCount, p1.statisticsWith.gamesCount);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.part_games_count_with).setOnClickListener(sort);

        View.OnClickListener sortVs = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<PlayerParticipation>() {
                    @Override
                    public int compare(PlayerParticipation p1, PlayerParticipation p2) {
                        return Integer.compare(p2.statisticsVs.gamesCount, p1.statisticsVs.gamesCount);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.part_games_count_against).setOnClickListener(sortVs);
    }

    private void sortByNameHandler(final ArrayList<PlayerParticipation> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<PlayerParticipation>() {
                    @Override
                    public int compare(PlayerParticipation p1, PlayerParticipation p2) {
                        return p1.mName.compareTo(p2.mName);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.player_name).setOnClickListener(sort);
    }
}
