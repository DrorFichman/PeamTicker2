package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by drorfichman on 10/3/16.
 */
public class GameDbHelper {

    private static final String SQL_CREATE_GAMES =
            "CREATE TABLE " + PlayerContract.GameEntry.TABLE_NAME + " (" +
                    PlayerContract.GameEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerContract.GameEntry.GAME + " INTEGER, " +
                    PlayerContract.GameEntry.DATE + " TEXT, " +
                    PlayerContract.GameEntry.TEAM_RESULT + " INTEGER DEFAULT -1, " +
                    PlayerContract.GameEntry.TEAM1_SCORE + " INTEGER, " +
                    PlayerContract.GameEntry.TEAM2_SCORE + " INTEGER )";

    public static final String SQL_DROP_GAMES_TABLE =
            "DELETE FROM " + PlayerContract.GameEntry.TABLE_NAME;

    public static String getSqlCreate() {
        return SQL_CREATE_GAMES;
    }

    public static void insertGameResults(SQLiteDatabase db, int gameId,
                                         int team1Score, int team2Score) {

        Log.d("TEAMS", "Saving result " + team1Score + " - " + team2Score);

        ContentValues values = new ContentValues();
        values.put(PlayerContract.GameEntry.GAME, gameId);
        values.put(PlayerContract.GameEntry.DATE, DbHelper.getNow());
        values.put(PlayerContract.GameEntry.TEAM1_SCORE, team1Score);
        values.put(PlayerContract.GameEntry.TEAM2_SCORE, team2Score);
        values.put(PlayerContract.GameEntry.TEAM_RESULT, TeamEnum.getResult(team1Score, team2Score).ordinal());

        // Insert the new row, returning the primary key value of the new row
        db.insertWithOnConflict(PlayerContract.GameEntry.TABLE_NAME,
                null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static ArrayList<Game> getGames(SQLiteDatabase db) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.GameEntry.ID,
                PlayerContract.GameEntry.GAME,
                PlayerContract.GameEntry.DATE,
                PlayerContract.GameEntry.TEAM_RESULT,
                PlayerContract.GameEntry.TEAM1_SCORE,
                PlayerContract.GameEntry.TEAM2_SCORE,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.GameEntry.ID + " DESC";

        Cursor c = db.query(
                PlayerContract.GameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return getGames(c, -1);
    }

    public static ArrayList<Game> getLastGames(SQLiteDatabase db, int count) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.GameEntry.ID,
                PlayerContract.GameEntry.GAME,
                PlayerContract.GameEntry.DATE,
                PlayerContract.GameEntry.TEAM_RESULT,
                PlayerContract.GameEntry.TEAM1_SCORE,
                PlayerContract.GameEntry.TEAM2_SCORE,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.GameEntry.ID + " DESC";

        Cursor c = db.query(
                PlayerContract.GameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return getGames(c, count);
    }

    @NonNull
    private static ArrayList<Game> getGames(Cursor c, int count) {

        ArrayList<Game> games = new ArrayList<>();
        try {
            if (c.moveToFirst()) {
                int i = 0;
                do {
                    Game g = new Game(c.getInt(c.getColumnIndex(PlayerContract.GameEntry.GAME)),
                            c.getString(c.getColumnIndex(PlayerContract.GameEntry.DATE)));
                    g.team1Score = c.getInt(c.getColumnIndex(PlayerContract.GameEntry.TEAM1_SCORE));
                    g.team2Score = c.getInt(c.getColumnIndex(PlayerContract.GameEntry.TEAM2_SCORE));
                    g.result = TeamEnum.getResult(g.team1Score, g.team2Score);
                    games.add(g);
                    i++;
                } while (c.moveToNext() && (i < count || count == -1));
            }
        } finally {
            c.close();
        }
        return games;
    }

    public static void deleteGame(SQLiteDatabase db, String gameId) {
        int delete = db.delete(PlayerContract.GameEntry.TABLE_NAME,
                PlayerContract.GameEntry.GAME + " = ? ",
                new String[]{gameId});
        Log.d("TEAMS", delete + " game was deleted");
    }
}
