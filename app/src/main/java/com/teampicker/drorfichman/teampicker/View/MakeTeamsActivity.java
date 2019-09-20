package com.teampicker.drorfichman.teampicker.View;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MakeTeamsActivity extends AppCompatActivity {

    private static final int RECENT_GAMES = 50;

    public static String INTENT_SET_RESULT = "INTENT_SET_RESULT";

    private ArrayList<Player> players1 = new ArrayList<>();
    private ArrayList<Player> players2 = new ArrayList<>();
    private CollaborationHelper.Collaboration analysisResult;
    private String analysisSelectedPlayer;

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
    private ImageView analysisView;

    private Button team1Score;
    private Button team2Score;
    private View saveView;

    private boolean mSetResult;
    private View teamsScreenArea;

    private AlertDialog makeTeamsDialog;
    View progressBarTeamDivision;
    private View teamStatsLayout;
    private View buttonsLayout;

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

        moveView = (ToggleButton) findViewById(R.id.move);
        moveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMoveMode()) {
                    Toast.makeText(MakeTeamsActivity.this, R.string.operation_move, Toast.LENGTH_LONG).show();
                } else {
                    movedPlayers.clear();
                    refreshPlayers();
                }
            }
        });
        moveView.setOnLongClickListener(explainOperation);

        list1.setOnItemClickListener(playerClicked);
        list2.setOnItemClickListener(playerClicked);

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
                DbHelper.saveTeams(MakeTeamsActivity.this, players1, players2);
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
        sendView.setOnLongClickListener(explainOperation);

        shuffleView = findViewById(R.id.shuffle);
        shuffleView.setOnClickListener(view -> divideComingPlayers(TeamDivision.DivisionStrategy.Grade));
        shuffleView.setOnLongClickListener(showShuffleDialog);

        team1Score = (Button) findViewById(R.id.team_1_score);
        team2Score = (Button) findViewById(R.id.team_2_score);

        analysisView = (ImageView) findViewById(R.id.game_prediction_button);
        analysisView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnalysisPlayerSelectedMode()) { // cancel player analysis selection
                    analysisSelectedPlayer = null;
                    analysisView.setImageResource(R.drawable.analysis_selected);
                    refreshPlayers();
                } else if (isAnalysisMode()) { // cancel analysis
                    analysisResult = null;
                    analysisSelectedPlayer = null;
                    analysisView.setImageResource(R.drawable.analysis);
                    refreshPlayers();
                } else { // enter analysis mode
                    if (!isMoveMode())
                        Toast.makeText(MakeTeamsActivity.this, R.string.operation_analysis, Toast.LENGTH_LONG).show();
                    analysisView.setImageResource(R.drawable.analysis_selected);

                    analysisSelectedPlayer = null;
                    initCollaboration();
                }
            }
        });
        analysisView.setOnLongClickListener(explainOperation);

        if (getIntent().getBooleanExtra(INTENT_SET_RESULT, false)) {
            if (DbHelper.getActiveGame(this) > 0) {
                InitSetResults();
            } else {
                Toast.makeText(this, "No saved teams found, \n" +
                        "Make teams first", Toast.LENGTH_LONG).show();
            }
        }

        progressBarTeamDivision = findViewById(R.id.calculating_teams_progress);

        teamStatsLayout = findViewById(R.id.total_scores);
        buttonsLayout = findViewById(R.id.buttons_layout);

        initialData(TeamDivision.DivisionStrategy.Grade);
    }

    private void initCollaboration() {
        analysisResult = CollaborationHelper.getCollaborationData(MakeTeamsActivity.this, players1, players2);
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
        analysisView.setVisibility(View.VISIBLE);

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

    private void initialData(TeamDivision.DivisionStrategy selectedDivision) {

        int currGame = DbHelper.getActiveGame(this);
        if (currGame < 0) {
            Toast.makeText(this, "Initial teams", Toast.LENGTH_SHORT).show();
            Log.d("teams", "Initial shuffled teams");
            divideComingPlayers(selectedDivision);
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
                divideComingPlayers(selectedDivision);
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

    private void enterSendMode() {

        clearMovedPlayers();
        totalData.setVisibility(View.INVISIBLE);

        refreshPlayers(false);
    }

    private void exitSendMode() {

        totalData.setVisibility(View.VISIBLE);

        refreshPlayers(true);
    }

    private void divideComingPlayers(TeamDivision.DivisionStrategy selectedDivision) {
        divideComingPlayers(selectedDivision, true);
    }

    private void divideComingPlayers(TeamDivision.DivisionStrategy selectedDivision, boolean refreshPlayersView) {

        ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(this, RECENT_GAMES);

        int totalPlayers = comingPlayers.size();
        int teamSize = totalPlayers / 2;
        Log.d("teams", "Total " + totalPlayers + ", team " + teamSize);

        if (totalPlayers == 0) {
            Toast.makeText(this, "Why you wanna play alone?!?", Toast.LENGTH_LONG).show();
        }

        TeamDivision.dividePlayers(this, comingPlayers, players1, players2, selectedDivision);

        scramble();

        if (refreshPlayersView) {
            postDividePlayers();
        }
    }

    private void postDividePlayers() {
        if (isAnalysisMode()) {
            analysisSelectedPlayer = null;
            initCollaboration();
        }

        clearMovedPlayers();
        refreshPlayers();
    }

    @Override
    public void onBackPressed() {
        DbHelper.saveTeams(this, players1, players2);
        super.onBackPressed();
    }

    private void clearMovedPlayers() {
        movedPlayers.clear();
        moveView.setChecked(false);
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
        list1.setAdapter(new PlayerTeamAdapter(this, players1, movedPlayers, missedPlayers, analysisResult, analysisSelectedPlayer, showInternalData));
        list2.setAdapter(new PlayerTeamAdapter(this, players2, movedPlayers, missedPlayers, analysisResult, analysisSelectedPlayer, showInternalData));

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
        if (isAnalysisMode()) {
            int winRate = analysisResult.getCollaborationWinRate(players.players);
            int stdDev = analysisResult.getExpectedWinRateStdDiv(players.players);
            if (winRate != 0) {
                collaborationWinRate = "Forecast: " + winRate + "% (+/- " + stdDev + ")";
            }
        }

        stats.setText(getString(R.string.team_data,
                String.valueOf(players.getAllCount()),
                String.valueOf(players.getAverage()),
                String.valueOf(players.getWinRate()),
                String.valueOf(players.getSuccess()),
                String.valueOf(players.getStdDev()),
                collaborationWinRate));

        publicStats.setText(getString(R.string.team_public_stats,
                String.valueOf(players.getAge())));

        publicStats.setVisibility(View.VISIBLE);
    }

    private void sortPlayerNames(ArrayList<Player> playersList) {
        Collections.sort(playersList, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player t1) {
                return p1.mName.compareTo(t1.mName);
            }
        });
    }

    private boolean isAnalysisMode() {
        return analysisResult != null;
    }

    private boolean isAnalysisPlayerSelectedMode() {
        return analysisResult != null && analysisSelectedPlayer != null;
    }

    private boolean isMoveMode() {
        return moveView != null && moveView.isChecked();
    }

    View.OnLongClickListener showShuffleDialog = v -> {
        showMakeTeamOptionsDialog();
        return true;
    };

    View.OnLongClickListener explainOperation = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.game_prediction_button:
                    Toast.makeText(MakeTeamsActivity.this, "Enter players collaboration analysis mode", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.send:
                    Toast.makeText(MakeTeamsActivity.this, "Share teams", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.shuffle:
                    Toast.makeText(MakeTeamsActivity.this, "Shuffle teams", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.move:
                    Toast.makeText(MakeTeamsActivity.this, "Enter manual players moving mode", Toast.LENGTH_LONG).show();
                    return true;
            }
            return false;
        }
    };

    AdapterView.OnItemClickListener playerClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            Player player = (Player) adapterView.getItemAtPosition(i);

            if (isAnalysisMode() && player.mName.equals(analysisSelectedPlayer)) { // cancel analysis player selection

                analysisSelectedPlayer = null;
                refreshPlayers();

            } else if (isMoveMode()) { // Moving when making teams

                switchPlayer(player);

                // After a player is moved - recalculate team's collaboration
                if (isAnalysisMode()) initCollaboration();

            } else if (isAnalysisMode() && !player.mName.equals(analysisSelectedPlayer)) { // set analysis player selection

                CollaborationHelper.PlayerCollaboration playerStats = analysisResult.getPlayer(player.mName);
                if (playerStats != null) analysisSelectedPlayer = player.mName;

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

            } else {

                Intent intent = PlayerParticipationActivity.getPlayerParticipationActivity(MakeTeamsActivity.this, player.mName, players2, players1);
                startActivity(intent);
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

    private void showMakeTeamOptionsDialog() {

        if (makeTeamsDialog != null && makeTeamsDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Shake things up?");

        alertDialogBuilder
                .setCancelable(true)
                .setItems(new CharSequence[]
                                {"Divide by grade (default)", "Divide by age", "AI - beep boop beep"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    divideComingPlayers(TeamDivision.DivisionStrategy.Grade);
                                    break;
                                case 1:
                                    divideComingPlayers(TeamDivision.DivisionStrategy.Age);
                                    break;
                                case 2:
                                    Toast.makeText(this, R.string.operation_divide_by_collaboration, Toast.LENGTH_LONG).show();
                                    dividePlayersAsync();
                            }
                        });

        makeTeamsDialog = alertDialogBuilder.create();

        makeTeamsDialog.show();
    }

    private static class DivideCollaborationAsync extends AsyncTask<Void, Void, String> {

        private WeakReference<MakeTeamsActivity> ref;

        DivideCollaborationAsync(MakeTeamsActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(Void... params) {
            MakeTeamsActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) return null;

            activity.divideComingPlayers(TeamDivision.DivisionStrategy.Optimize, false);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MakeTeamsActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) return;

            activity.progressBarTeamDivision.setVisibility(View.VISIBLE);
            activity.teamStatsLayout.setVisibility(View.INVISIBLE);
            activity.buttonsLayout.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MakeTeamsActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) return;

            activity.postDividePlayers();
            activity.progressBarTeamDivision.setVisibility(View.GONE);
            activity.teamStatsLayout.setVisibility(View.VISIBLE);
            activity.buttonsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void dividePlayersAsync() {
        DivideCollaborationAsync divide = new DivideCollaborationAsync(this);
        divide.execute();
    }
}
