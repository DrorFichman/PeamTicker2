package com.teampicker.drorfichman.teampicker.View;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Adapter.PlayerAdapter;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.DBSnapshotUtils;
import com.teampicker.drorfichman.teampicker.tools.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView playersList;
    private PlayerAdapter playersAdapter;

    FloatingActionsMenu fab;

    private static final int ACTIVITY_RESULT_PLAYER = 1;
    private static final int ACTIVITY_RESULT_IMPORT_FILE_SELECTED = 2;

    private static final int RECENT_GAMES_COUNT = 10;
    private sortType sort = sortType.grade; // TOOO sort by games
    private boolean showArchivedPlayers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setActivityTitle();

        setFloatingActionButton();

        setNavigationDrawer(toolbar);

        setPlayersList();
    }

    private void setPlayersList() {
        setHeadlines();

        playersList = (ListView) findViewById(R.id.players_list);

        ArrayList<Player> players = DbHelper.getPlayers(getApplicationContext(), RECENT_GAMES_COUNT, showArchivedPlayers);
        playersAdapter = new PlayerAdapter(this, players);

        playersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Player p = (Player) view.getTag();
                Intent intent = EditPlayerActivity.getEditPlayerIntent(MainActivity.this, p.mName);
                startActivityForResult(intent, ACTIVITY_RESULT_PLAYER);
            }
        });

        playersList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                checkPlayerDeletion((Player) view.getTag());
                return true;
            }
        });

        playersList.setAdapter(playersAdapter);

        if (players == null || players.size() == 0) {
            showTutorialDialog();
        }
    }

    private void setNavigationDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(ColorStateList.valueOf(Color.BLUE));
        navigationView.setItemTextColor(null);
    }

    private void setFloatingActionButton() {
        fab = (FloatingActionsMenu) findViewById(R.id.fab);

        FloatingActionButton enterResultsButton = new FloatingActionButton(this);
        enterResultsButton.setTitle("Results");
        enterResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                startEnterResultActivity();
            }
        });

        FloatingActionButton makeTeamsButton = new FloatingActionButton(this);
        makeTeamsButton.setTitle("Teams");
        makeTeamsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(MainActivity.this, 0);
                if (comingPlayers.size() == 0) {
                    Toast.makeText(MainActivity.this, "Why you wanna play alone?!?", Toast.LENGTH_LONG).show();
                } else {
                    startActivity(new Intent(MainActivity.this, MakeTeamsActivity.class));
                }
            }
        });

        FloatingActionButton addPlayerButton = new FloatingActionButton(this);
        addPlayerButton.setTitle("New Player");
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                startActivityForResult(new Intent(MainActivity.this, NewPlayerActivity.class), ACTIVITY_RESULT_PLAYER);
            }
        });

        fab.addButton(enterResultsButton);
        fab.addButton(makeTeamsButton);
        fab.addButton(addPlayerButton);
    }

    private void setHeadlines() {
        ((TextView) findViewById(R.id.player_name)).setText("Name");
        setHeadlineSorting(R.id.player_name, sortType.name);

        ((TextView) findViewById(R.id.player_age)).setText("Age");
        setHeadlineSorting(R.id.player_age, sortType.age);

        ((TextView) findViewById(R.id.player_attributes)).setText("Att");
        setHeadlineSorting(R.id.player_attributes, sortType.attributes);

        ((TextView) findViewById(R.id.player_recent_performance)).setText("+/-");
        setHeadlineSorting(R.id.player_recent_performance, sortType.suggestedGrade);

        ((TextView) findViewById(R.id.player_grade)).setText("Grade");
        setHeadlineSorting(R.id.player_grade, sortType.grade);

        ((CheckBox)findViewById(R.id.player_coming)).setTextColor(Color.BLACK);
        findViewById(R.id.player_coming).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CheckBox)view).setChecked(true);
                sort = sortType.coming;
                refreshPlayers();
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("onActivityResult", "Result " + resultCode);

        if (requestCode == ACTIVITY_RESULT_IMPORT_FILE_SELECTED && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            checkImportApproved(getImportListener(), FileHelper.getPath(this, data.getData()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshPlayers();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (showArchivedPlayers) {
            showArchivedPlayers = false;
            refreshPlayers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.make_teams :
                startActivity(new Intent(MainActivity.this, MakeTeamsActivity.class));
                break;
            case R.id.enter_results :
                startEnterResultActivity();
                break;
            case R.id.add_player :
                startActivityForResult(new Intent(MainActivity.this, NewPlayerActivity.class), ACTIVITY_RESULT_PLAYER);
                break;
            case R.id.show_archived_players :
                showArchivedPlayers = !showArchivedPlayers;
                if (showArchivedPlayers) {
                    ArrayList<Player> players = DbHelper.getPlayers(getApplicationContext(), RECENT_GAMES_COUNT, showArchivedPlayers);
                    if (players.size() == 0) {
                        Toast.makeText(MainActivity.this, "No archived players found", Toast.LENGTH_LONG).show();
                        showArchivedPlayers = false;
                        break;
                    }
                }
                refreshPlayers();
                break;
            case R.id.clear_all:
                DbHelper.clearComingPlayers(this);
                refreshPlayers();
                setActivityTitle();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_players) {
            // nothing
        } else if (id == R.id.nav_games) {
            startActivity(GamesActivity.getGameActivityIntent(this, null));
        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, StatisticsActivity.class));
        } else if (id == R.id.nav_save_snapshot) {
            DBSnapshotUtils.takeDBSnapshot(this, getExportListener());
        } else if (id == R.id.nav_import_snapshot) {
            selectFileForImport();
        } else if (id == R.id.nav_getting_started) {
            showTutorialDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void refreshPlayers() {
        ArrayList<Player> players = DbHelper.getPlayers(getApplicationContext(), RECENT_GAMES_COUNT, showArchivedPlayers);

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
                } else {
                    int i = Boolean.compare(p2.isComing, p1.isComing);
                    return (i != 0) ? i : p1.mName.compareTo(p2.mName);
                }
            }
        });

        playersAdapter.clear();
        playersAdapter.addAll(players);

        setActivityTitle();
    }

    private void startEnterResultActivity() {
        Intent intent = new Intent(MainActivity.this, MakeTeamsActivity.class);
        intent.putExtra(MakeTeamsActivity.INTENT_SET_RESULT, true);
        startActivity(intent);
    }

    private void checkPlayerDeletion(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String archiveAction = showArchivedPlayers ? "Unarchive" : "Archive";
        alertDialogBuilder.setTitle("Do you want to remove the player?")
                .setCancelable(true)
                .setItems(new CharSequence[]
                        {archiveAction, "Remove", "Cancel"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                DbHelper.archivePlayer(MainActivity.this, player.mName, !showArchivedPlayers);
                                refreshPlayers();
                                break;
                            case 1:
                                DbHelper.deletePlayer(MainActivity.this, player.mName);
                                refreshPlayers();
                                break;
                            case 2:
                                break;
                        }
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void showTutorialDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Getting Started");

        alertDialogBuilder
                .setMessage("Welcome! \n" +
                        "1. Use the \"+\" New player - to create players \n" +
                        "2. Mark the arriving players \n" +
                        "3. Use the \"+\" Teams - to divide your teams \n" +
                        "4. Use the \"+\" Results - once the game is over \n" +
                        "\n" +
                        "And don't forget to be awesome :)")
                .setCancelable(true)
                .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fab.expand();
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }


    public void setActivityTitle() {
        if (showArchivedPlayers) {
            setTitle("Archived players");
        } else {
            setTitle(String.format("PeamTicker (%d)", DbHelper.getComingPlayersCount(this)));
        }
    }

    //region snapshot
    private DBSnapshotUtils.ImportListener getImportListener() {
        return new DBSnapshotUtils.ImportListener() {
            @Override
            public void preImport() {
                refreshPlayers();
            }

            @Override
            public void importStarted() {
                Toast.makeText(MainActivity.this, "Import Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void importCompleted() {
                Toast.makeText(MainActivity.this, "Import Completed", Toast.LENGTH_SHORT).show();
                refreshPlayers();
            }

            @Override
            public void importError(String msg) {
                Toast.makeText(MainActivity.this, "Import Failed " + msg, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private DBSnapshotUtils.ExportListener getExportListener() {
        return new DBSnapshotUtils.ExportListener() {

            @Override
            public void exportStarted() {
                Toast.makeText(MainActivity.this, "Export Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void exportCompleted(File snapshot) {
                Toast.makeText(MainActivity.this, "Export Completed " + snapshot, Toast.LENGTH_SHORT).show();

                sendSnapshot(snapshot);
            }

            @Override
            public void exportError(String msg) {
                Toast.makeText(MainActivity.this, "Data export failed " + msg, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void sendSnapshot(File snapshotFile) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri snapshotURI = FileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".team.picker.share.screenshot",
                snapshotFile);

        intent.putExtra(Intent.EXTRA_STREAM, snapshotURI);
        intent.setType("*/*");
        startActivity(Intent.createChooser(intent, "Send snapshot"));
    }

    private void checkImportApproved(final DBSnapshotUtils.ImportListener handler, final String importPath) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Import Data Warning");

        alertDialogBuilder
                .setMessage("Delete local data and import selected file?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        DBSnapshotUtils.importDBSnapshotSelected(MainActivity.this,importPath, handler);

                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }

    public void selectFileForImport() {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        } else {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*"); // TODO xls?
            startActivityForResult(
                    Intent.createChooser(chooseFile, "Select xls snapshot file to import"),
                    MainActivity.ACTIVITY_RESULT_IMPORT_FILE_SELECTED
            );
        }
    }
    //endregion
}
