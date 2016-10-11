package com.teampicker.drorfichman.teampicker.View;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerStatisticsAdapter;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StatisticsActivity extends AppCompatActivity {

    private ListView playersList;
    private PlayerStatisticsAdapter playersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_statistics_activity);

        ((TextView) findViewById(R.id.player_name)).setText("Name");
        ((TextView) findViewById(R.id.stat_player_grade)).setText("Grade");
        ((TextView) findViewById(R.id.stat_success)).setText("Success");
        ((TextView) findViewById(R.id.stat_games_count)).setText("Games");
        ((TextView) findViewById(R.id.stat_wins_percentage)).setText("Win rate");

        playersList = (ListView) findViewById(R.id.players_statistics_list);

        final ArrayList<Player> players = DbHelper.getPlayersStatistics(getApplicationContext());

        updateList(players);

        sortByNameHandler(players);
        sortByGradeHandler(players);
        sortBySuccessHandler(players);
        sortByGamesHandler(players);
        sortByWinRateHandler(players);

        playersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO edit player (NewPlayerActivity) / show games history
            }
        });

        playersList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
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
