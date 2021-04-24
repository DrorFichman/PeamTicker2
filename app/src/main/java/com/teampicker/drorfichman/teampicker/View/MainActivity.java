package com.teampicker.drorfichman.teampicker.View;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teampicker.drorfichman.teampicker.Adapter.PlayerAdapter;
import com.teampicker.drorfichman.teampicker.Controller.Sort.Sorting;
import com.teampicker.drorfichman.teampicker.Controller.Sort.sortType;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.AuthHelper;
import com.teampicker.drorfichman.teampicker.tools.DBSnapshotUtils;
import com.teampicker.drorfichman.teampicker.tools.FileHelper;
import com.teampicker.drorfichman.teampicker.tools.cloud.DataCallback;
import com.teampicker.drorfichman.teampicker.tools.cloud.FirebaseHelper;
import com.teampicker.drorfichman.teampicker.tools.PermissionTools;
import com.teampicker.drorfichman.teampicker.tools.SnapshotHelper;
import com.teampicker.drorfichman.teampicker.tools.cloud.SyncProgress;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Sorting.sortingCallbacks,
        SyncProgress {

    private static final int ACTIVITY_RESULT_PLAYER = 1;
    private static final int ACTIVITY_RESULT_IMPORT_FILE_SELECTED = 2;
    private static final int ACTIVITY_RESULT_SIGN_IN = 3;
    private static final int RECENT_GAMES_COUNT = 10; // for +/- grade suggestion

    private PlayerAdapter playersAdapter;

    FloatingActionsMenu fab;

    View syncInProgress;
    TextView syncProgressStatus;

    private boolean showArchivedPlayers = false;

    Sorting sorting = new Sorting(this::sortingChanged, sortType.grade);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setActivityTitle();

        setFloatingActionButton();

        setNavigationDrawer(toolbar);

        setPlayersList();

        syncInProgress = findViewById(R.id.sync_progress);
        syncProgressStatus = findViewById(R.id.sync_progress_status);

        AuthHelper.requireLogin(this, ACTIVITY_RESULT_SIGN_IN);
    }

    private void setPlayersList() {
        setHeadlines();

        ListView playersList = findViewById(R.id.players_list);

        ArrayList<Player> players = DbHelper.getPlayers(getApplicationContext(), RECENT_GAMES_COUNT, showArchivedPlayers);
        playersAdapter = new PlayerAdapter(this, players, this::setActivityTitle);

        playersList.setOnItemClickListener((adapterView, view, i, l) -> {
            Player p = (Player) view.getTag();
            Intent intent = PlayerDetailsActivity.getDetailsPlayerIntent(MainActivity.this, p.mName);
            startActivityForResult(intent, ACTIVITY_RESULT_PLAYER);
        });

        playersList.setOnItemLongClickListener((adapterView, view, i, l) -> {
            checkPlayerDeletion((Player) view.getTag());
            return true;
        });

        playersList.setAdapter(playersAdapter);

        if (players.size() == 0) {
            showTutorialDialog();
        }
    }

    private void setFloatingActionButton() {
        fab = findViewById(R.id.fab);

        FloatingActionButton enterResultsButton = new FloatingActionButton(this);
        enterResultsButton.setTitle("Results");
        enterResultsButton.setOnClickListener(v -> {
            fab.collapse();
            startEnterResultActivity();
        });

        FloatingActionButton makeTeamsButton = new FloatingActionButton(this);
        makeTeamsButton.setTitle("Teams");
        makeTeamsButton.setOnClickListener(v -> {
            fab.collapse();
            ArrayList<Player> comingPlayers = DbHelper.getComingPlayers(MainActivity.this, 0);
            if (comingPlayers.size() == 0) {
                Toast.makeText(MainActivity.this, "Why you wanna play alone?!?", Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(MainActivity.this, MakeTeamsActivity.class));
            }
        });

        FloatingActionButton addPlayerButton = new FloatingActionButton(this);
        addPlayerButton.setTitle("New Player");
        addPlayerButton.setOnClickListener(v -> {
            fab.collapse();
            startActivityForResult(new Intent(MainActivity.this, NewPlayerActivity.class), ACTIVITY_RESULT_PLAYER);
        });

        fab.addButton(enterResultsButton);
        fab.addButton(makeTeamsButton);
        fab.addButton(addPlayerButton);
    }

    private void setHeadlines() {

        sorting.setHeadlineSorting(this, R.id.player_name, this.getString(R.string.name), sortType.name);
        sorting.setHeadlineSorting(this, R.id.player_age, this.getString(R.string.age), sortType.age);
        sorting.setHeadlineSorting(this, R.id.player_attributes, this.getString(R.string.attributes), sortType.attributes);
        sorting.setHeadlineSorting(this, R.id.player_recent_performance, this.getString(R.string.plus_minus), sortType.suggestedGrade);
        sorting.setHeadlineSorting(this, R.id.player_grade, this.getString(R.string.grade), sortType.grade);
        sorting.setHeadlineSorting(this, R.id.player_coming, null, sortType.coming);

        ((CheckBox) findViewById(R.id.player_coming)).setTextColor(Color.BLACK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.i("AccountFB", "User success " + user);
                Toast.makeText(this, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();

                FirebaseHelper.getInstance().storeAccountData();

            } else {
                // Sign in failed. Either user canceled the sign-in flow using the back button.
                // Or response.getError().getErrorCode() with additional details
                Log.w("AccountFB", "Failed login - " + response);
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                // TODO finish? enforce authentication (and connectivity)
            }

        } else if (requestCode == ACTIVITY_RESULT_IMPORT_FILE_SELECTED &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            // Import data result
            SnapshotHelper.checkImportApproved(this, getImportListener(),
                    FileHelper.getPath(this, data.getData()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshPlayers();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) { // close drawer
            drawer.closeDrawer(GravityCompat.START);
        } else if (showArchivedPlayers) { // return from archived players
            showArchivedPlayers = false;
            refreshPlayers();
        } else if (fab != null && fab.isExpanded()) { // collapse floating button
            fab.collapse();
        } else {
            super.onBackPressed();
        }
    }

    //region Toolbar and Navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.make_teams:
                startActivity(MakeTeamsActivity.getInstance(this, false));
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

    private void setNavigationDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(ColorStateList.valueOf(Color.BLUE));
        navigationView.setItemTextColor(null);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_players) {
            // nothing
        } else if (id == R.id.nav_games) {
            startActivity(GamesActivity.getGameActivityIntent(this, null, null, true));
        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, StatisticsActivity.class));
        } else if (id == R.id.nav_save_snapshot) {
            DBSnapshotUtils.takeDBSnapshot(this, getExportListener(), null);
        } else if (id == R.id.nav_import_snapshot) {
            selectFileForImport();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_getting_started) {
            showTutorialDialog();
        } else if (id == R.id.nav_data_sync) {
            FirebaseHelper.getInstance().syncToCloud(this, this::showSyncStatus);
        } else if (id == R.id.nav_data_pull) {
            FirebaseHelper.getInstance().pullFromCloud(this, this::showSyncStatus);
        } else if (id == R.id.nav_auth_logout) {
            Log.i("AccountFB", "Log out user " + AuthHelper.getUser());
            AuthUI.getInstance().signOut(this);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion

    public void refreshPlayers() {
        ArrayList<Player> players = DbHelper.getPlayers(getApplicationContext(), RECENT_GAMES_COUNT, showArchivedPlayers);

        sorting.sort(players);

        playersAdapter.clear();
        playersAdapter.addAll(players);

        setActivityTitle();
    }

    private void startEnterResultActivity() {
        startActivity(MakeTeamsActivity.getInstance(this, true));
    }

    public void setActivityTitle() {
        if (showArchivedPlayers) {
            setTitle("Archived players");
        } else {
            setTitle(getString(R.string.main_title, DbHelper.getComingPlayersCount(this)));
        }
    }

    @Override
    public void showSyncStatus(String status) {
        if (status != null) {
            syncInProgress.setVisibility(View.VISIBLE);
            syncProgressStatus.setText(status);
        } else {
            syncInProgress.setVisibility(View.GONE);
            syncProgressStatus.setText("");
            refreshPlayers();
        }
    }

    //region player archive & deletion
    private void checkPlayerDeletion(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        if (showArchivedPlayers) {
            alertDialogBuilder.setTitle("Do you want to remove the player?")
                    .setCancelable(true)
                    .setItems(new CharSequence[]
                                    {"Unarchive", "Remove", "Cancel"},
                            (dialog, which) -> {
                                switch (which) {
                                    case 0: // Unarchive
                                        DbHelper.archivePlayer(MainActivity.this, player.mName, false);
                                        refreshPlayers();
                                        break;
                                    case 1: // Remove
                                        DbHelper.deletePlayer(MainActivity.this, player.mName);
                                        refreshPlayers();
                                        break;
                                    case 2: // Cancel
                                        break;
                                }
                            });
        } else {
            alertDialogBuilder.setTitle("Do you want to archive the player?")
                    .setCancelable(true)
                    .setItems(new CharSequence[]
                                    {"Archive", "Cancel"},
                            (dialog, which) -> {
                                switch (which) {
                                    case 0: // Archive
                                        DbHelper.archivePlayer(MainActivity.this, player.mName, true);
                                        refreshPlayers();
                                        break;
                                    case 2: // Cancel
                                        break;
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
                .setPositiveButton("Got it", (dialog, id) -> {
                    fab.expand();
                    dialog.dismiss();
                });

        alertDialogBuilder.create().show();
    }
    //endregion

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

                SnapshotHelper.sendSnapshot(MainActivity.this, snapshot);
            }

            @Override
            public void exportError(String msg) {
                Toast.makeText(MainActivity.this, "Data export failed " + msg, Toast.LENGTH_LONG).show();
            }
        };
    }

    public void selectFileForImport() {

        PermissionTools.checkPermissionsForExecution(this, 2, () -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*"); // TODO xls?
            startActivityForResult(
                    Intent.createChooser(chooseFile, "Select xls snapshot file to import"),
                    MainActivity.ACTIVITY_RESULT_IMPORT_FILE_SELECTED
            );
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    //endregion

    @Override
    public void sortingChanged() {
        refreshPlayers();
    }
}
