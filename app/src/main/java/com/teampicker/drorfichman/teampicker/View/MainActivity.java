package com.teampicker.drorfichman.teampicker.View;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.navigation.NavigationView;
import com.teampicker.drorfichman.teampicker.Adapter.PlayerAdapter;
import com.teampicker.drorfichman.teampicker.Controller.Sorting;
import com.teampicker.drorfichman.teampicker.Controller.sortType;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.DBSnapshotUtils;
import com.teampicker.drorfichman.teampicker.tools.FileHelper;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Sorting.sortingCallbacks {

    private ListView playersList;
    private PlayerAdapter playersAdapter;

    FloatingActionsMenu fab;

    private static final int ACTIVITY_RESULT_PLAYER = 1;
    private static final int ACTIVITY_RESULT_IMPORT_FILE_SELECTED = 2;

    private static final int RECENT_GAMES_COUNT = 10;
    private boolean showArchivedPlayers = false;

    Sorting sorting = new Sorting(this, sortType.grade);

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

        sorting.setHeadlineSorting(this, R.id.player_name, "Name", sortType.name);
        sorting.setHeadlineSorting(this, R.id.player_age, "Age", sortType.age);
        sorting.setHeadlineSorting(this, R.id.player_attributes, "Attr", sortType.attributes);
        sorting.setHeadlineSorting(this, R.id.player_recent_performance, "+/-", sortType.suggestedGrade);
        sorting.setHeadlineSorting(this, R.id.player_grade, "Grade", sortType.grade);
        sorting.setHeadlineSorting(this, R.id.player_grade, "Grade", sortType.grade);
        sorting.setHeadlineSorting(this, R.id.player_coming, null, sortType.coming);

        ((CheckBox) findViewById(R.id.player_coming)).setTextColor(Color.BLACK);
        /*findViewById(R.id.player_coming).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CheckBox) view).setChecked(true);
            }
        })*/;
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
            case R.id.make_teams:
                startActivity(new Intent(MainActivity.this, MakeTeamsActivity.class));
                break;
            case R.id.enter_results:
                startEnterResultActivity();
                break;
            case R.id.add_player:
                startActivityForResult(new Intent(MainActivity.this, NewPlayerActivity.class), ACTIVITY_RESULT_PLAYER);
                break;
            case R.id.show_archived_players:
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

        sorting.sort(players);

        playersAdapter.clear();
        playersAdapter.addAll(players);

        setActivityTitle();
    }

    private void startEnterResultActivity() {
        Intent intent = new Intent(MainActivity.this, MakeTeamsActivity.class);
        intent.putExtra(MakeTeamsActivity.INTENT_SET_RESULT, true);
        startActivity(intent);
    }

    //region player archive & deletion
    private void checkPlayerDeletion(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        if (showArchivedPlayers) {
            alertDialogBuilder.setTitle("Do you want to remove the player?")
                    .setCancelable(true)
                    .setItems(new CharSequence[]
                                    {"Unarchive", "Remove", "Cancel"},
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            DbHelper.archivePlayer(MainActivity.this, player.mName, false);
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
        } else {
            alertDialogBuilder.setTitle("Do you want to archive the player?")
                    .setCancelable(true)
                    .setItems(new CharSequence[]
                                    {"Archive", "Cancel"},
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            DbHelper.archivePlayer(MainActivity.this, player.mName, true);
                                            refreshPlayers();
                                            break;
                                        case 2:
                                            break;
                                    }
                                }
                            });
        }


        alertDialogBuilder.create().show();
    }
    //endregion

    //region Tutorial
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
    //endregion

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
                Toast.makeText(MainActivity.this, "Import Failed : " + msg, Toast.LENGTH_LONG).show();
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
                Toast.makeText(MainActivity.this, "Data export failed " + msg, Toast.LENGTH_LONG).show();
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

                        DBSnapshotUtils.importDBSnapshotSelected(MainActivity.this, importPath, handler);

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

    @Override
    public void refresh() {
        refreshPlayers();
    }
}
