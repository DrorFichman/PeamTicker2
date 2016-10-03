package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by drorfichman on 10/3/16.
 */
public class PlayerGamesDbHelper {

    private static final String SQL_CREATE_PLAYERS_GAMES =
            "CREATE TABLE " + PlayerContract.PlayerGameEntry.TABLE_NAME + " (" +
                    PlayerContract.PlayerGameEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerContract.PlayerGameEntry.NAME + " TEXT, " +
                    PlayerContract.PlayerGameEntry.GAME + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.DATE + " TEXT, " +
                    PlayerContract.PlayerGameEntry.PLAYER_GRADE + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.GAME_GRADE + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.TEAM + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.GOALS + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.ASSISTS + " INTEGER )";

    public static String getSqlCreate() {
        return SQL_CREATE_PLAYERS_GAMES;
    }

    public static void insertPlayerGame(SQLiteDatabase db, Player player, int currGame, TeamEnum team) {

        String now = DbHelper.getNow();

        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.GAME, currGame);
        values.put(PlayerContract.PlayerGameEntry.DATE, now);
        values.put(PlayerContract.PlayerGameEntry.NAME, player.mName);
        values.put(PlayerContract.PlayerGameEntry.PLAYER_GRADE, player.mGrade);
        values.put(PlayerContract.PlayerGameEntry.TEAM, team.ordinal());

        // Insert the new row, returning the primary key value of the new row
        db.insert(PlayerContract.PlayerGameEntry.TABLE_NAME,
                null,
                values);
    }

    public static Cursor getGames(SQLiteDatabase db) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.PlayerGameEntry.ID,
                PlayerContract.PlayerGameEntry.GAME,
                PlayerContract.PlayerGameEntry.DATE
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.PlayerGameEntry.DATE + " DESC";

        Cursor c = db.query(
                PlayerContract.PlayerGameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                PlayerContract.PlayerGameEntry.GAME,
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();

        return c;
    }

    public static ArrayList<Player> getCurrTeam(SQLiteDatabase db, int currGame, TeamEnum team) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.PlayerGameEntry.ID,
                PlayerContract.PlayerGameEntry.NAME,
                PlayerContract.PlayerGameEntry.GAME,
                PlayerContract.PlayerGameEntry.PLAYER_GRADE,
                PlayerContract.PlayerGameEntry.GAME_GRADE,
                PlayerContract.PlayerGameEntry.GOALS,
                PlayerContract.PlayerGameEntry.ASSISTS,
                PlayerContract.PlayerGameEntry.TEAM // TODO add other fields
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.PlayerGameEntry.PLAYER_GRADE + " DESC";

        String where = PlayerContract.PlayerGameEntry.TEAM + " = ? AND " +
                PlayerContract.PlayerGameEntry.GAME + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(team.ordinal()),
                String.valueOf(currGame)};

        Cursor c = db.query(
                PlayerContract.PlayerGameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<Player> players = new ArrayList<>();
        try {
            if (c.moveToFirst()) {
                do {
                    // TODO set rest of the fields - goals, grades...
                    Player p = new Player(c.getString(c.getColumnIndex(PlayerContract.PlayerGameEntry.NAME)),
                            c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_GRADE)));
                    p.isComing = true;
                    players.add(p);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return players;
    }

    public static void clearOldGameTeams(SQLiteDatabase db) {
        Log.d("teams", "Clear old Game teams ");

        int n = db.delete(PlayerContract.PlayerGameEntry.TABLE_NAME,
                PlayerContract.PlayerGameEntry.GAME_GRADE + " IS NULL ", null);

        Log.d("teams", "deleted games players " + n);
    }
}
