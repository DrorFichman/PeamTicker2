package com.teampicker.drorfichman.teampicker.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.teampicker.drorfichman.teampicker.Controller.PreferenceHelper;
import com.teampicker.drorfichman.teampicker.Controller.TeamData;
import com.teampicker.drorfichman.teampicker.Controller.TeamDivision;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private Button team1Score;
    private Button team2Score;
    private View saveView;

    private boolean mSetResult;

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
                hideSelection();
            }
        });

        AdapterView.OnItemClickListener selected = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (!mSetResult && moveView.isChecked()) {
                    switchPlayer((Player) adapterView.getItemAtPosition(i));
                } else {

                    // Switch player NA/Missed status
                    Player player = (Player) adapterView.getItemAtPosition(i);

                    ResultEnum newResult = player.isMissed() ? ResultEnum.NA : ResultEnum.Missed;
                    player.switchMissed();

                    DbHelper.updatePlayerResult(MakeTeamsActivity.this, PreferenceHelper.getCurrGame(MakeTeamsActivity.this), player.mName, newResult);

                    updateLists();
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

        sendView = findViewById(R.id.send);
        sendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("teams", "Enter send mode");
                saveTeams();
                enterSendMode();

                // TODO runnable 0.5 second?
                Handler handler = new Handler();

                final Runnable r = new Runnable() {
                    public void run() {
                        takeScreenShot();
                        Log.d("teams", "Exit send mode - Shot taken");
                        exitSendMode();
                    }
                };

                handler.postDelayed(r, 200);
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
            if (PreferenceHelper.getCurrGame(this) > 0) {
                setResultInit();
            } else {
                Toast.makeText(this, "No saved teams found, \n" +
                        "Make teams first", Toast.LENGTH_LONG).show();
            }
        }

        initialData();
    }

    private void saveResults() {
        int currGame = PreferenceHelper.getCurrGame(this);
        DbHelper.insertGame(this, currGame, getScoreValue(team1Score), getScoreValue(team2Score));
        PreferenceHelper.clearCurrGame(this);

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

        int currGame = PreferenceHelper.getCurrGame(this);
        if (currGame < 0) {
            Log.d("teams", "Initial data curr game = 0 - so shuffling");
            initialDivision();
        } else {
            Log.d("teams", "Initial data curr game > 0 - so getting from DB");
            players1 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team1, STARS_COUNT);
            players2 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team2, STARS_COUNT);

            if (players1 != null && players1.size() > 0 && players2 != null && players2.size() > 0) {
                updateLists();
            } else {
                Log.e("TEAMS", "Unable to find teams for curr game " + currGame);
                initialDivision();
            }

            // TODO no need for this message?
//            if (!mSetResult) {
//                Toast.makeText(this, "Displaying saved teams, \n" +
//                        "Reshuffle if needed", Toast.LENGTH_LONG).show();
//            }
        }
    }

    private void saveTeams() {

        DbHelper.clearOldGameTeams(this);

        int currGame = PreferenceHelper.getMaxGame(this) + 1;
        PreferenceHelper.setMaxGame(this, currGame);
        PreferenceHelper.setCurrGame(this, currGame);

        for (Player a : players1) {
            DbHelper.insertPlayerGame(this, a, currGame, TeamEnum.Team1);
        }
        for (Player b : players2) {
            DbHelper.insertPlayerGame(this, b, currGame, TeamEnum.Team2);
        }
    }

    private void enterSendMode() {

        sendView.setVisibility(View.GONE);
        moveView.setVisibility(View.GONE);
        shuffleView.setVisibility(View.GONE);

        hideSelection();
        showGrade(false);
        showStats(false);

        updateLists();
    }

    private void exitSendMode() {

        sendView.setVisibility(View.VISIBLE);
        moveView.setVisibility(View.VISIBLE);
        shuffleView.setVisibility(View.VISIBLE);

        showGrade(true);
        showStats(true);

        updateLists();
    }

    private void initialDivision() {

        players1.clear();
        players2.clear();

        PreferenceHelper.clearCurrGame(this);

        ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(this, STARS_COUNT);
        int totalPlayers = comingPlayers.size();
        int teamSize = totalPlayers / 2;
        Log.d("teams", "Total " + totalPlayers + ", team " + teamSize);

        if (totalPlayers == 0) {
            Toast.makeText(this, "Why you wanna play alone?!?", Toast.LENGTH_LONG).show();
        }

        TeamDivision.dividePlayers(comingPlayers, players1, players2);

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

    private void openScreenshot(File imageFile) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Share teams"));
    }

    public void takeScreenShot() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            takeScreenshotPermitted();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exitSendMode();
                Toast.makeText(this, "We're ready! you can now share your teams :)", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void takeScreenshotPermitted() {

        try {

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imagePath = new File(Environment.getExternalStorageDirectory().toString() + "/TeamPicker/");
            imagePath.mkdirs();
            File imageFile = new File(imagePath, DbHelper.getNow() + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 50;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);

        } catch (Throwable e) {

            // Several error may come out with file handling or OOM
            Toast.makeText(this, "Failed to take screenshot " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void updateLists() {

        sortPlayerNames(players1);
        sortPlayerNames(players2);

        list1.setAdapter(newArrayAdapter(players1));
        list2.setAdapter(newArrayAdapter(players2));

        updateTeamData(teamData1, players1);
        updateTeamData(teamData2, players2);
    }

    private void updateTeamData(TextView view, List<Player> players) {
        TeamData teamData = new TeamData(players);
        view.setText(getString(R.string.team_data,
                String.valueOf(teamData.getCount()),
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
