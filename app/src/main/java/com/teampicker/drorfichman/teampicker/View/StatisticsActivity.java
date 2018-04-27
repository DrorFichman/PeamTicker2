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
    private int games = -1;

    private static final int ACTIVITY_RESULT_PLAYER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_statistics_activity);

        gradeTitle = (TextView) findViewById(R.id.stat_player_grade);
        gradeTitle.setText("Grade");

        ((TextView) findViewById(R.id.player_name)).setText("Name");
        ((TextView) findViewById(R.id.stat_success)).setText("Success");
        ((TextView) findViewById(R.id.stat_games_count)).setText("Games");
        ((TextView) findViewById(R.id.stat_wins_percentage)).setText("Win rate");

        playersList = (ListView) findViewById(R.id.players_statistics_list);

        refreshList(games);

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
                        ScreenshotHelper.takeScreenShot(StatisticsActivity.this);
                        Log.d("teams", "Exit send mode - Shot taken");
                        exitSendMode();
                    }
                };

                new Handler().postDelayed(r, 400);

                break;

            case R.id.action_last_10_games:
                games = 10;
                break;
            case R.id.action_last_50_games:
                games = 50;
                break;
            case R.id.action_no_limit:
                games = -1;
                break;
        }

        if (games != 0) {
            refreshList(games);
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
        refreshList(games);
    }

    private void exitSendMode() {
        gradeTitle.setVisibility(View.VISIBLE);
        refreshList(games);
    }

    private void showHideGrades(ArrayList<Player> players) {
        boolean isGradeVisible = gradeTitle.getVisibility() == View.VISIBLE;

        for (Player p : players) {
            p.showGrade(isGradeVisible);
        }
    }

    private void refreshList(int games) {

        ArrayList<Player> players = DbHelper.getPlayersStatistics(getApplicationContext(), games);

        updateList(players);

        sortByNameHandler(players);
        sortByGradeHandler(players);
        sortBySuccessHandler(players);
        sortByGamesHandler(players);
        sortByWinRateHandler(players);

        showHideGrades(players);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_PLAYER) {
            Log.d("TEAMS", "TODO : refresh statistics list on result?");
            refreshList(games);
        }
    }

    private void updateList(ArrayList<Player> players) {
        playersAdapter = new PlayerStatisticsAdapter(StatisticsActivity.this, players);
        playersList.setAdapter(playersAdapter);
    }

    private void sortByWinRateHandler(final ArrayList<Player> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return (Float.valueOf(p2.statistics.getWinRate()).compareTo(Float.valueOf(p1.statistics.getWinRate())));
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.stat_wins_percentage).setOnClickListener(sort);
    }

    private void sortByGamesHandler(final ArrayList<Player> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return ((Integer) p2.statistics.gamesCount).compareTo(p1.statistics.gamesCount);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.stat_games_count).setOnClickListener(sort);
    }

    private void sortBySuccessHandler(final ArrayList<Player> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return ((Integer) p2.statistics.successRate).compareTo(p1.statistics.successRate);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.stat_success).setOnClickListener(sort);
    }

    private void sortByGradeHandler(final ArrayList<Player> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return ((Integer) p2.mGrade).compareTo(p1.mGrade);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.stat_player_grade).setOnClickListener(sort);
    }

    private void sortByNameHandler(final ArrayList<Player> players) {
        View.OnClickListener sort = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(players, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return p1.mName.compareTo(p2.mName);
                    }
                });
                updateList(players);
            }
        };
        findViewById(R.id.player_name).setOnClickListener(sort);
    }
}
