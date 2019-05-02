package com.teampicker.drorfichman.teampicker.View;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapter;
import com.teampicker.drorfichman.teampicker.Controller.ScreenshotHelper;
import com.teampicker.drorfichman.teampicker.Controller.TeamData;
import com.teampicker.drorfichman.teampicker.Controller.TeamDivision;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MakeTeamsActivity extends AppCompatActivity {

    private static final int STARS_COUNT = 5;
    public static String INTENT_SET_RESULT = "INTENT_SET_RESULT";

    private ArrayList<Player> players1 = new ArrayList<>();
    private ArrayList<Player> players2 = new ArrayList<>();
    private ListView list2;
    private ListView list1;

    private View totalData;
    private TextView teamData2;
    private TextView teamData1;

    private View sendView;
    private ToggleButton moveView;
    private View shuffleView;
    private View moveViewDescription;

    private Button team1Score;
    private Button team2Score;
    private View saveView;

    private boolean mSetResult;
    private View teamsScreenArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_make_teams_activity);
        Log.d("teams", "onCreate");

        totalData = findViewById(R.id.total_scores);
        teamData1 = (TextView) findViewById(R.id.total_list1);
        teamData2 = (TextView) findViewById(R.id.total_list2);

        list1 = (ListView) findViewById(R.id.team_1);
        list2 = (ListView) findViewById(R.id.team_2);

        moveViewDescription = findViewById(R.id.move_mode_how_to_exit);
        moveView = (ToggleButton) findViewById(R.id.move);
        moveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                moveViewDescription.setVisibility(moveView.isChecked() ? View.VISIBLE : View.INVISIBLE);

                hideSelection();
            }
        });

        AdapterView.OnItemClickListener selected = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (mSetResult) { // Setting "Missed"

                    // Switch player NA/Missed status
                    Player player = (Player) adapterView.getItemAtPosition(i);

                    ResultEnum newResult = player.isMissed() ? ResultEnum.NA : ResultEnum.Missed;
                    player.switchMissed();

                    DbHelper.setPlayerResult(MakeTeamsActivity.this, DbHelper.getActiveGame(MakeTeamsActivity.this), player.mName, newResult);
                    updateLists();

                } else if (moveView.isChecked()) { // Moving

                    switchPlayer((Player) adapterView.getItemAtPosition(i));
                }
            }
        };

        list1.setOnItemClickListener(selected);
        list2.setOnItemClickListener(selected);

        saveView = findViewById(R.id.save);
        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveResults();
            }
        });

        teamsScreenArea = findViewById(R.id.teams_list_area);

        sendView = findViewById(R.id.send);
        sendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("teams", "Enter send mode");
                saveTeams();
                enterSendMode();

                final Runnable r = new Runnable() {
                    public void run() {
                        ScreenshotHelper.takeScreenShot(MakeTeamsActivity.this, teamsScreenArea);
                        Log.d("teams", "Exit send mode - Shot taken");
                        exitSendMode();
                    }
                };

                new Handler().postDelayed(r, 200);
            }
        });

        shuffleView = findViewById(R.id.shuffle);
        shuffleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialDivision();
            }
        });

        team1Score = (Button) findViewById(R.id.team_1_score);
        team2Score = (Button) findViewById(R.id.team_2_score);

        // TODO curr game might not be needed since we're always saving teams - never auto-reshuffle
        if (getIntent().getBooleanExtra(INTENT_SET_RESULT, false)) {
            if (DbHelper.getActiveGame(this) > 0) {
                setResultInit();
            } else {
                Toast.makeText(this, "No saved teams found, \n" +
                        "Make teams first", Toast.LENGTH_LONG).show();
            }
        }

        initialData();
    }

    private void saveResults() {
        int currGame = DbHelper.getActiveGame(this);
        DbHelper.insertGame(this, currGame, getScoreValue(team1Score), getScoreValue(team2Score));

        Toast.makeText(this, "Results saved", Toast.LENGTH_LONG).show();
        finish();
    }

    private void setResultInit() {

        mSetResult = true;

        moveView.setVisibility(View.GONE);
        shuffleView.setVisibility(View.GONE);
        sendView.setVisibility(View.GONE);

        saveView.setVisibility(View.VISIBLE);

        team1Score.setVisibility(View.VISIBLE);
        team2Score.setVisibility(View.VISIBLE);

        team1Score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                team1Score.setText(String.valueOf((getScoreValue(team1Score) + 1) % 11));
            }
        });

        team2Score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                team2Score.setText(String.valueOf((getScoreValue(team2Score) + 1) % 11));
            }
        });
    }

    int getScoreValue(Button b) {
        return Integer.valueOf(b.getText().toString());
    }

    private void initialData() {

        int currGame = DbHelper.getActiveGame(this);
        if (currGame < 0) {
            Toast.makeText(this,"Initial teams", Toast.LENGTH_SHORT).show();
            Log.d("teams", "Initial shuffled teams");
            initialDivision();
        } else {
            // Toast.makeText(this,"Saved teams", Toast.LENGTH_SHORT).show();
            Log.d("teams", "Initial data curr game > 0 - so getting from DB");
            players1 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team1, STARS_COUNT);
            players2 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team2, STARS_COUNT);

            ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(this, STARS_COUNT);

            if (players1 != null && players1.size() > 0 && players2 != null && players2.size() > 0) {
                boolean changed = handleComingChanges(comingPlayers, players1, players2);
                updateLists();
                if (changed) Toast.makeText(this, "Changes in coming players applied", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("TEAMS", "Unable to find teams for curr game " + currGame);
                initialDivision();
            }
        }
    }

    private boolean handleComingChanges(ArrayList<Player> comingPlayers,
                                        ArrayList<Player> players1, ArrayList<Player> players2) {
        // TODO + toast
        Log.d("COMING", "coming " + comingPlayers.size() +
                " 1=" + players1.size() +
                " 2=" + players2.size());

        boolean isChanged = false;

        HashMap<String, Player> all = new HashMap<>();
        for (Player coming : comingPlayers) {
            all.put(coming.mName, coming);
        }

        isChanged = removeNonComingPlayers(players1, all) || isChanged;
        isChanged = removeNonComingPlayers(players2, all) || isChanged;

        isChanged = isChanged || all.values().size() > 0;
        players1.addAll(all.values());

        return isChanged;
    }

    private boolean removeNonComingPlayers(ArrayList<Player> players, HashMap<String, Player> all) {
        boolean isChanged = false;
        Iterator<Player> i = players.iterator();
        while (i.hasNext()) {
            Player p = i.next();
            if (all.containsKey(p.mName)) {
                all.remove(p.mName); // remove from coming player
            } else {
                i.remove(); // remove from team
                isChanged = true;
            }
        }
        return isChanged;
    }

    private void saveTeams() {

        DbHelper.clearOldGameTeams(this);

        int currGame = DbHelper.getMaxGame(this) + 1;

        for (Player a : players1) {
            DbHelper.insertPlayerGame(this, a, currGame, TeamEnum.Team1);
        }
        for (Player b : players2) {
            DbHelper.insertPlayerGame(this, b, currGame, TeamEnum.Team2);
        }
    }

    private void enterSendMode() {

        hideSelection();
        showGrade(false);
        showStats(false);

        updateLists();
    }

    private void exitSendMode() {

        showGrade(true);
        showStats(true);

        updateLists();
    }

    private void initialDivision() {

        players1.clear();
        players2.clear();

        ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(this, STARS_COUNT);
        int totalPlayers = comingPlayers.size();
        int teamSize = totalPlayers / 2;
        Log.d("teams", "Total " + totalPlayers + ", team " + teamSize);

        if (totalPlayers == 0) {
            Toast.makeText(this, "Why you wanna play alone?!?", Toast.LENGTH_LONG).show();
        }

        TeamDivision.dividePlayers(this, comingPlayers, players1, players2);

        scramble();

        updateLists();
    }

    @Override
    public void onBackPressed() {
        saveTeams();
        super.onBackPressed();
    }

    private void showStats(boolean show) {
        totalData.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showGrade(boolean show) {
        for (Player p : players1) {
            p.showGrade(show);
        }
        for (Player p : players2) {
            p.showGrade(show);
        }
    }

    private void hideSelection() {
        for (Player p : players1) {
            p.switchMoved(false);
        }
        for (Player p : players2) {
            p.switchMoved(false);
        }
        updateLists();
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

    private void updateLists() {

        sortPlayerNames(players1);
        sortPlayerNames(players2);
        list1.setAdapter(newArrayAdapter(players1));
        list2.setAdapter(newArrayAdapter(players2));

        updateStats();
    }

    private void updateStats() {
        ArrayList<Player> team1Stats = players1;
        ArrayList<Player> team2Stats = players2;
        if (players1.size() > players2.size()) {
            ArrayList<Player> players = TeamDivision.cloneList(players1);
            Collections.sort(players);
            players.remove(players.size() - 1);
            team1Stats = players;
        }
        if (players2.size() > players1.size()) {
            ArrayList<Player> players = TeamDivision.cloneList(players2);
            Collections.sort(players);
            players.remove(players.size() - 1);
            team2Stats = players;
        }

        updateTeamData(teamData1, team1Stats, players1.size());
        updateTeamData(teamData2, team2Stats, players2.size());
    }

    private void scramble() {
        if (new Random().nextInt(3) % 2 == 1) {
            Log.d("Team", "Scramble");
            ArrayList<Player> temp = players1;
            players1 = players2;
            players2 = temp;
        }
    }

    private void updateTeamData(TextView view, List<Player> players, int playersActualCount) {
        TeamData teamData = new TeamData(players);
        view.setText(getString(R.string.team_data,
                String.valueOf(playersActualCount),
                String.valueOf(teamData.getSum()),
                String.valueOf(teamData.getAverage()),
                String.valueOf(teamData.getSuccess()),
                String.valueOf(teamData.getStdDev())));
    }

    private void sortPlayerNames(ArrayList<Player> playersList) {
        Collections.sort(playersList, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player t1) {
                return p1.mName.compareTo(t1.mName);
            }
        });
    }

    private void switchPlayer(Player movedPlayer) {

        movedPlayer.switchMoved(true);

        for (Player curr : players1) {
            if (curr.mName.equals(movedPlayer.mName)) {
                players1.remove(curr);
                players2.add(curr);

                updateLists();
                return;
            }
        }

        for (Player curr : players2) {
            if (curr.mName.equals(movedPlayer.mName)) {
                players1.add(curr);
                players2.remove(curr);

                updateLists();
                return;
            }
        }
    }

    @NonNull
    private ArrayAdapter<Player> newArrayAdapter(List<Player> players) {

        return new PlayerTeamAdapter(this, players);
    }
}
