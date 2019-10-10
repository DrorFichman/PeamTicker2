package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.tools.DateHelper;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Created by drorfichman on 10/3/16.
 */
public class GameDbHelper {

    private static final String SQL_CREATE_GAMES =
            "CREATE TABLE " + PlayerContract.GameEntry.TABLE_NAME + " (" +
                    PlayerContract.GameEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerContract.GameEntry.GAME + " INTEGER, " +
                    PlayerContract.GameEntry.DATE + " TEXT, " + // TODO change to date (sortable)
                    PlayerContract.GameEntry.TEAM_RESULT + " INTEGER DEFAULT -1, " +
                    PlayerContract.GameEntry.TEAM1_SCORE + " INTEGER, " +
                    PlayerContract.GameEntry.TEAM2_SCORE + " INTEGER )";

    public static final String SQL_DROP_GAMES_TABLE =
            "DELETE FROM " + PlayerContract.GameEntry.TABLE_NAME;

    public static String getSqlCreate() {
        return SQL_CREATE_GAMES;
    }

    public static void insertGameResults(SQLiteDatabase db, int gameId,
                                         String gameDate, int team1Score, int team2Score) {

        Log.d("TEAMS", "Saving result " + team1Score + " - " + team2Score);

        ContentValues values = new ContentValues();
        values.put(PlayerContract.GameEntry.GAME, gameId);
        values.put(PlayerContract.GameEntry.DATE, gameDate != null ? gameDate : DateHelper.getNow());
        values.put(PlayerContract.GameEntry.TEAM1_SCORE, team1Score);
        values.put(PlayerContract.GameEntry.TEAM2_SCORE, team2Score);
        values.put(PlayerContract.GameEntry.TEAM_RESULT, TeamEnum.getResult(team1Score, team2Score).ordinal());

        // Insert the new row, returning the primary key value of the new row
        db.insertWithOnConflict(PlayerContract.GameEntry.TABLE_NAME,
                null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static Game getGame(SQLiteDatabase db, int gameId) {

        String[] projection = {
                PlayerContract.GameEntry.ID,
                PlayerContract.GameEntry.GAME,
                PlayerContract.GameEntry.DATE,
                PlayerContract.GameEntry.TEAM_RESULT,
                PlayerContract.GameEntry.TEAM1_SCORE,
                PlayerContract.GameEntry.TEAM2_SCORE,
        };

        String where = PlayerContract.GameEntry.GAME + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(gameId)};

        String sortOrder = PlayerContract.GameEntry.ID + " DESC";

        Cursor c = db.query(
                PlayerContract.GameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return getGames(c, -1).get(0);
    }

    public static ArrayList<Game> getGames(SQLiteDatabase db, String name) {

        ArrayList<Game> results = new ArrayList<>();

        Cursor c = db.rawQuery("select " +
                PlayerContract.GameEntry.ID + ", " +
                PlayerContract.GameEntry.GAME + ", " +
                        PlayerContract.GameEntry.DATE + ", " +
                        " res."  + PlayerContract.PlayerGameEntry.PLAYER_RESULT + " as " + PlayerContract.PlayerGameEntry.PLAYER_RESULT + ", " +
                        " res."  + PlayerContract.PlayerGameEntry.PLAYER_GRADE + " as " + PlayerContract.PlayerGameEntry.PLAYER_GRADE + ", " +
                        PlayerContract.GameEntry.TEAM1_SCORE + ", " +
                        PlayerContract.GameEntry.TEAM2_SCORE +
                        " from " + PlayerContract.GameEntry.TABLE_NAME +
                        " , (select game, result, player_grade from player_game where name = ?) as res " +
                        " where " + PlayerContract.GameEntry.GAME + " = res.game " +
                        " order by " + PlayerContract.GameEntry.GAME + " DESC ",
                new String[]{name}, null);

        return getGames(c, -1);
    }

    public static ArrayList<Game> getGames(SQLiteDatabase db) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.GameEntry.ID,
                PlayerContract.GameEntry.GAME,
                PlayerContract.GameEntry.DATE,
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
                    g.winningTeam = TeamEnum.getResult(g.team1Score, g.team2Score);

                    if (c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_RESULT) > 0)
                        g.playerResult = ResultEnum.getResultFromOrdinal(c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_RESULT)));
                    if (c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_GRADE) > 0)
                        g.playerGrade = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_GRADE));

                    games.add(g);
                    i++;
                } while (c.moveToNext() && (i < count || count == -1));
            }
        } finally {
            c.close();
        }
        return games;
    }

    public static void deleteGame(SQLiteDatabase db, int gameId) {
        int delete = db.delete(PlayerContract.GameEntry.TABLE_NAME,
                PlayerContract.GameEntry.GAME + " = ? ",
                new String[]{String.valueOf(gameId)});
        Log.d("TEAMS", delete + " game was deleted");
    }
}
