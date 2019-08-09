package com.teampicker.drorfichman.teampicker.View;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerStatisticsAdapter;
import com.teampicker.drorfichman.teampicker.Controller.ScreenshotHelper;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StatisticsActivity extends AppCompatActivity {

    private ListView playersList;
    private PlayerStatisticsAdapter playersAdapter;

    TextView gradeTitle;
    private int games = 50;
    private sortType sort = sortType.success;

    private static final int ACTIVITY_RESULT_PLAYER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_statistics_activity);

        setPlayersList();
    }

    private void setPlayersList() {
        setHeadlines();

        playersList = (ListView) findViewById(R.id.players_statistics_list);
        playersAdapter = new PlayerStatisticsAdapter(this, new ArrayList<Player>(), true);

        refreshPlayers();

        playersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = EditPlayerActivity.getEditPlayerIntent(StatisticsActivity.this, (String) view.getTag(R.id.player_id));
                startActivityForResult(intent, ACTIVITY_RESULT_PLAYER);
            }
        });

        playersList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
    }

    private void setHeadlines() {
        gradeTitle = (TextView) findViewById(R.id.stat_player_grade);
        ((TextView) findViewById(R.id.stat_player_grade)).setText("Grade");
        setHeadlineSorting(R.id.stat_player_grade, sortType.grade);

        ((TextView) findViewById(R.id.player_name)).setText("Name");
        setHeadlineSorting(R.id.player_name, sortType.name);

        ((TextView) findViewById(R.id.stat_success)).setText("Success");
        setHeadlineSorting(R.id.stat_success, sortType.success);

        ((TextView) findViewById(R.id.stat_games_count)).setText("Games");
        setHeadlineSorting(R.id.stat_games_count, sortType.games);

        ((TextView) findViewById(R.id.stat_wins_percentage)).setText("Win rate");
        setHeadlineSorting(R.id.stat_wins_percentage, sortType.winPercentage);
    }

    private void setHeadlineSorting(int field, final sortType sorting) {
        findViewById(field).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sort = sorting;
                refreshPlayers();
            }
        });
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

                enterSendMode();

                final Runnable r = new Runnable() {
                    public void run() {
                        ScreenshotHelper.takeListScreenshot(StatisticsActivity.this,
                                playersList, findViewById(R.id.titles), playersAdapter);
                        Log.d("teams", "Exit send mode - Shot taken");
                        exitSendMode();
                    }
                };

                new Handler().postDelayed(r, 400);

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

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exitSendMode();
                Toast.makeText(this, "We're ready! you can now share your screenshot :)", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void enterSendMode() {
        gradeTitle.setVisibility(View.INVISIBLE);
        refreshPlayers(false);
    }

    private void exitSendMode() {
        gradeTitle.setVisibility(View.VISIBLE);
        refreshPlayers(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_PLAYER && resultCode > 0) {
            Log.d("TEAMS", "refresh statistics list on save");
            refreshPlayers();
        }
    }

    public void refreshPlayers() {
        refreshPlayers(true);
    }

    public void refreshPlayers(boolean showInternalData) {

        ArrayList<Player> players = DbHelper.getPlayersStatistics(getApplicationContext(), games);

        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                if (sort.equals(sortType.grade)) {
                    return Integer.compare(p2.mGrade, p1.mGrade);
                } else if (sort.equals(sortType.suggestedGrade)) {
                    return Integer.compare(p2.getSuggestedGradeDiff(), p1.getSuggestedGradeDiff());
                } else if (sort.equals(sortType.name)) {
                    return p1.mName.compareTo(p2.mName);
                } else if (sort.equals(sortType.age)) {
                    return Integer.compare(p2.getAge(), p1.getAge());
                } else if (sort.equals(sortType.attributes)) {
                    return Boolean.compare(p2.hasAttributes(), p1.hasAttributes());
                } else if (sort.equals(sortType.winPercentage)) {
                    return (Float.compare(p2.statistics.getWinRate(), p1.statistics.getWinRate()));
                } else if (sort.equals(sortType.games)) {
                    return Integer.compare(p2.statistics.gamesCount, p1.statistics.gamesCount);
                } else if (sort.equals(sortType.success)) {
                    return Integer.compare(p2.statistics.successRate, p1.statistics.successRate);
                } else {
                    return p1.mName.compareTo(p2.mName);
                }
            }
        });

        playersAdapter = new PlayerStatisticsAdapter(this, players, showInternalData);
        playersList.setAdapter(playersAdapter);
    }
}
