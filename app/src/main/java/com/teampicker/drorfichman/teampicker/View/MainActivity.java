package com.teampicker.drorfichman.teampicker.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Adapter.PlayerAdapter;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.DBSnapshotUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView playersList;
    private PlayerAdapter playersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setActivityTitle();

        final FloatingActionsMenu fab = (FloatingActionsMenu) findViewById(R.id.fab);

        FloatingActionButton makeTeamsButton = new FloatingActionButton(this);
        makeTeamsButton.setTitle("Teams");
        makeTeamsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                startActivity(new Intent(MainActivity.this, MakeTeamsActivity.class));
            }
        });

        FloatingActionButton enterResultsButton = new FloatingActionButton(this);
        enterResultsButton.setTitle("Results");
        enterResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                startEnterResultActivity();
            }
        });

        FloatingActionButton addPlayerButton = new FloatingActionButton(this);
        addPlayerButton.setTitle("New Player");
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                startActivityForResult(new Intent(MainActivity.this, NewPlayerActivity.class), 1);
            }
        });

        fab.addButton(makeTeamsButton);
        fab.addButton(enterResultsButton);
        fab.addButton(addPlayerButton);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        playersList = (ListView) findViewById(R.id.players_list);

        ArrayList<Player> players = DbHelper.getPlayers(getApplicationContext());
        playersAdapter = new PlayerAdapter(this, players);

        playersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Player p = (Player) view.getTag();
                Intent intent = EditPlayerActivity.getEditPlayerIntent(MainActivity.this, p.mName);
                startActivityForResult(intent, 1);
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
    }

    private void checkPlayerDeletion(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Delete");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to remove this player?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DbHelper.deletePlayer(MainActivity.this, player.mName);
                        refreshPlayers();
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("onActivityResult", "Result " + resultCode);

        if (resultCode == 1) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                                .setAction("Action", null).show();
        }

        refreshPlayers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshPlayers();
    }

    public void refreshPlayers() {
        ArrayList<Player> players = DbHelper.getPlayers(getApplicationContext());

        // Attach cursor adapter to the ListView
        playersAdapter.clear();
        playersAdapter.addAll(players);

        setActivityTitle();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
                startActivityForResult(new Intent(MainActivity.this, NewPlayerActivity.class), 1);
                break;
            case R.id.clear_all:
                DbHelper.clearComingPlayers(this);
                refreshPlayers();
                setActivityTitle();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startEnterResultActivity() {
        Intent intent = new Intent(MainActivity.this, MakeTeamsActivity.class);
        intent.putExtra(MakeTeamsActivity.INTENT_SET_RESULT, true);
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_players) {
            // nothing
        } else if (id == R.id.nav_games) {
            startActivity(new Intent(this, GamesActivity.class));
        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, StatisticsActivity.class));
        } else if (id == R.id.nav_save_snapshot) {
            DBSnapshotUtils.takeDBSnapshot(this);
        } else if (id == R.id.nav_import_snapshot) {
            DBSnapshotUtils.importDBSnapshot(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setActivityTitle() {
        setTitle(String.format("PeamTicker (%d)", DbHelper.getComingPlayersCount(this)));
    }
}
