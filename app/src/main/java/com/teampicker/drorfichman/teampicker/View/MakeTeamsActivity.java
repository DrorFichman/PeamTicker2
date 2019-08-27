package com.teampicker.drorfichman.teampicker.View;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapter;
import com.teampicker.drorfichman.teampicker.Controller.CollaborationHelper;
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
import java.util.Random;

public class MakeTeamsActivity extends AppCompatActivity {

    private static final int RECENT_GAMES = 50;

    public static String INTENT_SET_RESULT = "INTENT_SET_RESULT";

    private ArrayList<Player> players1 = new ArrayList<>();
    private ArrayList<Player> players2 = new ArrayList<>();
    private CollaborationHelper.Collaboration collaborationResult;
    private String collaborationSelectedPlayer;

    private ListView list2;
    private ListView list1;

    ArrayList<Player> movedPlayers = new ArrayList<>();
    ArrayList<Player> missedPlayers = new ArrayList<>();

    private View totalData;
    private TextView teamData2;
    private TextView teamData1;

    private View sendView;
    private ToggleButton moveView;
    private View shuffleView;
    private View moveViewDescription;
    private ImageView predictView;

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
                if (!moveView.isChecked()) {
                    movedPlayers.clear();
                    refreshPlayers();
                }
            }
        });

        list1.setOnItemClickListener(playerSelected);
        list2.setOnItemClickListener(playerSelected);

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
                        ScreenshotHelper.takeScreenshot(MakeTeamsActivity.this, teamsScreenArea);
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

        predictView = (ImageView) findViewById(R.id.game_prediction_button);
        predictView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (collaborationSelectedPlayer != null) { // cancel player analysis selection
                    collaborationSelectedPlayer = null;
                    predictView.setImageResource(R.drawable.analysis_selected);
                    refreshPlayers();
                } else if (collaborationResult != null) { // cancel analysis
                    collaborationResult = null;
                    predictView.setImageResource(R.drawable.analysis);
                    refreshPlayers();
                } else { // enter analysis mode
                    predictView.setImageResource(R.drawable.analysis_selected);
                    initCollaboration();
                }
            }
        });

        if (getIntent().getBooleanExtra(INTENT_SET_RESULT, false)) {
            if (DbHelper.getActiveGame(this) > 0) {
                InitSetResults();
            } else {
                Toast.makeText(this, "No saved teams found, \n" +
                        "Make teams first", Toast.LENGTH_LONG).show();
            }
        }

        initialData();
    }

    private void initCollaboration() {
        collaborationResult = CollaborationHelper.getCollaborationData(MakeTeamsActivity.this, players1, players2);
        collaborationSelectedPlayer = null;
        refreshPlayers();
    }

    private void saveResults() {
        int currGame = DbHelper.getActiveGame(this);
        DbHelper.insertGame(this, currGame, getScoreValue(team1Score), getScoreValue(team2Score));

        Toast.makeText(this, "Results saved", Toast.LENGTH_LONG).show();
        finish();
    }

    private void InitSetResults() {

        mSetResult = true;

        moveView.setVisibility(View.GONE);
        predictView.setVisibility(View.VISIBLE);

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
            Toast.makeText(this, "Initial teams", Toast.LENGTH_SHORT).show();
            Log.d("teams", "Initial shuffled teams");
            initialDivision();
        } else {
            Log.d("teams", "Initial data curr game > 0 - so getting from DB");
            players1 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team1, RECENT_GAMES);
            players2 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team2, RECENT_GAMES);

            ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(this, RECENT_GAMES);

            if (players1 != null && players1.size() > 0 && players2 != null && players2.size() > 0) {
                boolean changed = handleComingChanges(comingPlayers, players1, players2);
                refreshPlayers();
                if (changed)
                    Toast.makeText(this, "Changes in coming players applied", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("TEAMS", "Unable to find teams for curr game " + currGame);
                initialDivision();
            }
        }
    }

    private boolean handleComingChanges(ArrayList<Player> comingPlayers,
                                        ArrayList<Player> players1, ArrayList<Player> players2) {
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

        clearMovedPlayers();
        clearTeamAnalysis();
        totalData.setVisibility(View.INVISIBLE);

        refreshPlayers(false);
    }

    private void exitSendMode() {

        totalData.setVisibility(View.VISIBLE);

        refreshPlayers(true);
    }

    private void initialDivision() {

        ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(this, RECENT_GAMES);

        int totalPlayers = comingPlayers.size();
        int teamSize = totalPlayers / 2;
        Log.d("teams", "Total " + totalPlayers + ", team " + teamSize);

        if (totalPlayers == 0) {
            Toast.makeText(this, "Why you wanna play alone?!?", Toast.LENGTH_LONG).show();
        }

        TeamDivision.dividePlayers(this, comingPlayers, players1, players2);

        scramble();

        clearMovedPlayers();
        refreshPlayers();

        if (collaborationResult != null) {
            initCollaboration();
        }
    }

    @Override
    public void onBackPressed() {
        saveTeams();
        super.onBackPressed();
    }

    private void clearMovedPlayers() {
        movedPlayers.clear();
        moveView.setChecked(false);
    }

    private void clearTeamAnalysis() {
        collaborationResult = null;
        collaborationSelectedPlayer = null;
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

    private void refreshPlayers(boolean showInternalData) {
        sortPlayerNames(players1);
        sortPlayerNames(players2);
        list1.setAdapter(new PlayerTeamAdapter(this, players1, movedPlayers, missedPlayers, collaborationResult, collaborationSelectedPlayer, showInternalData));
        list2.setAdapter(new PlayerTeamAdapter(this, players2, movedPlayers, missedPlayers, collaborationResult, collaborationSelectedPlayer, showInternalData));

        updateStats();
    }

    private void refreshPlayers() {
        refreshPlayers(true);
    }

    private void updateStats() {
        int count = Math.min(players1.size(), players2.size());
        TeamData team1Data = new TeamData(players1, count);
        TeamData team2Data = new TeamData(players2, count);

        updateTeamData(teamData1, (TextView) findViewById(R.id.team1_public_stats), team1Data);
        updateTeamData(teamData2, (TextView) findViewById(R.id.team2_public_stats), team2Data);
    }

    private void scramble() {
        if (new Random().nextInt(3) % 2 == 1) {
            Log.d("Team", "Scramble");
            ArrayList<Player> temp = players1;
            players1 = players2;
            players2 = temp;
        }
    }

    private void updateTeamData(TextView stats, TextView publicStats, TeamData players) {

        String collaborationWinRate = "";
        if (collaborationResult != null) {
            int winRate = collaborationResult.getCollaborationWinRate(players.players);
            if (winRate != 0) {
                collaborationWinRate = " (" + winRate + "%)";
            }
        }

        stats.setText(getString(R.string.team_data,
                String.valueOf(players.getAllCount()),
                String.valueOf(players.getAverage()),
                String.valueOf(players.getWinRate() + collaborationWinRate),
                String.valueOf(players.getSuccess()),
                String.valueOf(players.getStdDev())));

        publicStats.setText(getString(R.string.team_public_stats,
                String.valueOf(players.getAge())));
    }

    private void sortPlayerNames(ArrayList<Player> playersList) {
        Collections.sort(playersList, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player t1) {
                return p1.mName.compareTo(t1.mName);
            }
        });
    }

    //region player clicked
    AdapterView.OnItemClickListener playerSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            Player player = (Player) adapterView.getItemAtPosition(i);

            if (moveView.isChecked()) { // Moving when making teams

                switchPlayer(player);

                // After a player is moved - recalculate team's collaboration
                if (collaborationResult != null) {
                    initCollaboration();
                }
            } else if (collaborationResult != null) { // Seeing extra data for collaboration

                if (player.mName.equals(collaborationSelectedPlayer)) {
                    collaborationSelectedPlayer = null;
                } else {
                    CollaborationHelper.PlayerCollaboration playerStats = collaborationResult.getPlayer(player.mName);
                    if (playerStats != null) {
                        collaborationSelectedPlayer = player.mName;
                    }
                }
                refreshPlayers();
            } else if (mSetResult) { // Setting "Missed" when setting results

                // Switch player NA/Missed status
                if (missedPlayers.contains(player)) {
                    missedPlayers.remove(player);
                    DbHelper.setPlayerResult(MakeTeamsActivity.this,
                            DbHelper.getActiveGame(MakeTeamsActivity.this), player.mName, ResultEnum.NA);
                } else {
                    missedPlayers.add(player);
                    DbHelper.setPlayerResult(MakeTeamsActivity.this,
                            DbHelper.getActiveGame(MakeTeamsActivity.this), player.mName, ResultEnum.Missed);
                }

                refreshPlayers();
            }
        }
    };

    private void switchPlayer(Player movedPlayer) {

        if (movedPlayers.contains(movedPlayer)) {
            movedPlayers.remove(movedPlayer);
        } else {
            movedPlayers.add(movedPlayer);
        }

        if (players1.contains(movedPlayer)) {
            players1.remove(movedPlayer);
            players2.add(movedPlayer);
        } else if (players2.contains(movedPlayer)) {
            players1.add(movedPlayer);
            players2.remove(movedPlayer);
        }

        refreshPlayers();
    }
    //endregion
}
