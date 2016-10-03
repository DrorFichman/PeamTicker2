package com.teampicker.drorfichman.teampicker.View;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Adapter.GameAdapter;
import com.teampicker.drorfichman.teampicker.R;

public class GamesActivity extends AppCompatActivity {

    private ListView gamesList;
    private GameAdapter gamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_games_activity);

        gamesList = (ListView) findViewById(R.id.games_list);

        Cursor games = DbHelper.getGames(getApplicationContext());
        gamesAdapter = new GameAdapter(this, games, 0);

        gamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("DB", "TODO : Game clicked row");
            }
        });

        gamesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                checkGameDeletion((String)view.getTag());
                return true;
            }
        });

        gamesList.setAdapter(gamesAdapter);
    }

    private void checkGameDeletion(final String game) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Delete");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to remove this game?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("teams", "TODO delete game");
                        refreshGames();
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void refreshGames() {
        Cursor games = DbHelper.getGames(getApplicationContext());

        // Attach cursor adapter to the ListView
        gamesAdapter.changeCursor(games);
    }
}
