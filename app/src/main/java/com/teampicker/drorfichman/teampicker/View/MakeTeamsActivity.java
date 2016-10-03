package com.teampicker.drorfichman.teampicker.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapter;
import com.teampicker.drorfichman.teampicker.Controller.PreferenceHelper;
import com.teampicker.drorfichman.teampicker.Data.PlayerGamesDbHelper;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.Controller.TeamData;
import com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MakeTeamsActivity extends AppCompatActivity {

    private ArrayList<Player> players1 = new ArrayList<>();
    private ArrayList<Player> players2 = new ArrayList<>();
    private Player player;
    private ListView list2;
    private ListView list1;

    private View totalData;
    private TextView teamData2;
    private TextView teamData1;

    private View sendView;
    private View doneView;
    private View moveView;
    private View shuffleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_teams);
        Log.d("teams", "onCreate");

        totalData = findViewById(R.id.total_scores);
        teamData1 = (TextView) findViewById(R.id.total_list1);
        teamData2 = (TextView) findViewById(R.id.total_list2);

        list1 = (ListView) findViewById(R.id.team_1);
        list2 = (ListView) findViewById(R.id.team_2);

        AdapterView.OnItemClickListener selected = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Player oldSelected = player;

                Player newSelected = (Player) adapterView.getItemAtPosition(i);
                if (player != newSelected) {
                    player = newSelected;
                    player.isSelected = true;
                    Log.d("teams", "selected " + player.mName);
                } else {
                    player = null;
                }

                if (oldSelected != null) {
                    oldSelected.isSelected = false;
                }

                updateLists();
            }
        };
        list1.setOnItemClickListener(selected);
        list2.setOnItemClickListener(selected);

        moveView = findViewById(R.id.move_to_1);
        moveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player == null) {

                    Toast.makeText(MakeTeamsActivity.this, "Select a player to be moved", Toast.LENGTH_LONG).show();

                } else {
                    for (Player curr : players1) {
                        if (curr.mName.equals(player.mName)) {
                            players1.remove(curr);
                            players2.add(curr);

                            updateLists();
                            return;
                        }
                    }

                    for (Player curr : players2) {
                        if (curr.mName.equals(player.mName)) {
                            players1.add(curr);
                            players2.remove(curr);

                            updateLists();
                            return;
                        }
                    }
                }
            }
        });

        doneView = findViewById(R.id.done);
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTeams();
                enterSendMode();
            }
        });

        sendView = findViewById(R.id.send);
        sendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                takeScreenShot();
                Log.d("teams", "Shot taken");

                exitSendMode();
            }
        });

        shuffleView = findViewById(R.id.shuffle);
        shuffleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialDivision();
            }
        });

        initialData();
    }

    private void initialData() {

        int currGame = PreferenceHelper.getCurrGame(this);
        if (currGame < 0) {
            Log.d("teams", "Initial data curr game = 0 - so shuffling");
            initialDivision();
        } else {
            Log.d("teams", "Initial data curr game > 0 - so getting from DB");
            players1 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team1);
            players2 = DbHelper.getCurrTeam(this, currGame, TeamEnum.Team2);
            updateLists();
            Toast.makeText(this, "Displaying saved teams, \n" +
                    "Reshuffle if needed", Toast.LENGTH_LONG).show();
        }
    }

    private void saveTeams() {

        DbHelper.clearOldGameTeams(this);

        int currGame = PreferenceHelper.getMaxGame(this) + 1;
        PreferenceHelper.setMaxGame(this, currGame);
        PreferenceHelper.setCurrGame(this, currGame);

        // TODO : PREF_CURR_GAME_INDEX should be cleared after game results are entered, so new games will be created

        for (Player a : players1) {
            DbHelper.insertPlayerGame(this, a, currGame, TeamEnum.Team1);
        }
        for (Player b : players2) {
            DbHelper.insertPlayerGame(this, b, currGame, TeamEnum.Team2);
        }
    }

    private void enterSendMode() {
        sendView.setVisibility(View.VISIBLE);

        doneView.setVisibility(View.GONE);
        moveView.setVisibility(View.GONE);
        shuffleView.setVisibility(View.GONE);

        hideSelection();
        showGrade(false);
        showStats(false);
        updateLists();
    }

    private void exitSendMode() {
        sendView.setVisibility(View.GONE);

        doneView.setVisibility(View.VISIBLE);
        moveView.setVisibility(View.VISIBLE);
        shuffleView.setVisibility(View.VISIBLE);

        showGrade(true);
        showStats(true);
        updateLists();
    }

    private void initialDivision() {

        players1.clear();
        players2.clear();

        PreferenceHelper.setCurrGame(this, -1);

        ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(this);
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

        if (doneView != null && doneView.getVisibility() != View.VISIBLE) {
            exitSendMode();
        } else {
            super.onBackPressed();
        }
    }

    private void showStats(boolean show) {
        totalData.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void hideSelection() {
        if (player != null) {
            player.isSelected = false;
        }
        player = null;
    }

    private void showGrade(boolean show) {
        for (Player p : players1) {
            p.showGrade(show);
        }
        for (Player p : players2) {
            p.showGrade(show);
        }
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
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exitSendMode();
                Toast.makeText(this, "We're ready! you can now share your teams :)", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void takeScreenshotPermitted() {

        String now = DbHelper.getNow();

        try {
            // image naming and path  to include sd card  appending name you choose for file
            // TODO app folder

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imagePath = new File(Environment.getExternalStorageDirectory().toString() + "/TeamPicker/");
            imagePath.mkdirs();
            File imageFile = new File(imagePath, now + ".jpg");

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
        view.setText(getString(R.string.team_data,
                String.valueOf(players.size()),
                String.valueOf(new TeamData(players).getSum()),
                new TeamData(players).getStdDev()));
    }

    private void sortPlayerNames(ArrayList<Player> playersList) {
        Collections.sort(playersList, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player t1) {
                return p1.mName.compareTo(t1.mName);
            }
        });
    }

    @NonNull
    private ArrayAdapter<Player> newArrayAdapter(List<Player> players) {

        return new PlayerTeamAdapter(this, players);
    }
}
