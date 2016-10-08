package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.StatisticsData;

import java.util.ArrayList;

/**
 * Created by drorfichman on 10/3/16.
 */
public class PlayerGamesDbHelper {

    public static final int EMPTY_RESULT = -10;

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
                    PlayerContract.PlayerGameEntry.ASSISTS + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.PLAYER_RESULT + " INTEGER DEFAULT " + EMPTY_RESULT + ")";

    public static String getSqlCreate() {
        return SQL_CREATE_PLAYERS_GAMES;
    }

    public static void insertPlayerGame(SQLiteDatabase db, Player player, int currGame, TeamEnum team) {

        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.GAME, currGame);
        values.put(PlayerContract.PlayerGameEntry.DATE, DbHelper.getNow());
        values.put(PlayerContract.PlayerGameEntry.NAME, player.mName);
        values.put(PlayerContract.PlayerGameEntry.PLAYER_GRADE, player.mGrade);
        values.put(PlayerContract.PlayerGameEntry.TEAM, team.ordinal());

        // Insert the new row, returning the primary key value of the new row
        db.insert(PlayerContract.PlayerGameEntry.TABLE_NAME,
                null,
                values);
    }

    public static ArrayList<Player> getCurrTeam(SQLiteDatabase db, int currGame, TeamEnum team) {

        String[] projection = {
                PlayerContract.PlayerGameEntry.ID,
                PlayerContract.PlayerGameEntry.NAME,
                PlayerContract.PlayerGameEntry.GAME,
                PlayerContract.PlayerGameEntry.PLAYER_GRADE,
                PlayerContract.PlayerGameEntry.GAME_GRADE,
                PlayerContract.PlayerGameEntry.GOALS,
                PlayerContract.PlayerGameEntry.ASSISTS,
                PlayerContract.PlayerGameEntry.TEAM,
                PlayerContract.PlayerGameEntry.PLAYER_RESULT
        };

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
                PlayerContract.PlayerGameEntry.GAME + " NOT IN " +
                        " ( SELECT " + PlayerContract.GameEntry.GAME +
                        " FROM " + PlayerContract.GameEntry.TABLE_NAME + " )", null);

        Log.d("teams", "deleted games players " + n);
    }

    public static ArrayList<ResultEnum> getPlayerLastGames(SQLiteDatabase db, Player player, int countLastGames) {

        String[] projection = {
                PlayerContract.PlayerGameEntry.NAME,
                PlayerContract.PlayerGameEntry.GAME,
                PlayerContract.PlayerGameEntry.PLAYER_RESULT
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.PlayerGameEntry.GAME + " DESC";

        String where = PlayerContract.PlayerGameEntry.NAME + " = ? AND "
                + PlayerContract.PlayerGameEntry.PLAYER_RESULT + " > ? ";
        String[] whereArgs = new String[]{player.mName, String.valueOf(EMPTY_RESULT)};

        Cursor c = db.query(
                PlayerContract.PlayerGameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<ResultEnum> results = new ArrayList<>();
        try {
            if (c.moveToFirst()) {
                int i = 0;
                do {
                    int res = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_RESULT));
                    results.add(ResultEnum.getResultFromOrdinal(res));
                    i++;
                } while (c.moveToNext() && i < countLastGames);
            }
        } finally {
            c.close();
        }

        return results;
    }

    public static void setPlayerGameResult(SQLiteDatabase db, int gameId, TeamEnum result) {

        // Create a new map of values, where column names are the keys
        ContentValues values1 = new ContentValues();
        values1.put(PlayerContract.PlayerGameEntry.PLAYER_RESULT, TeamEnum.getTeam1Result(result).getValue());

        String where1 = PlayerContract.PlayerGameEntry.GAME + " = ? AND " +
                PlayerContract.PlayerGameEntry.TEAM + " = ? ";
        String[] whereArgs1 = new String[]{String.valueOf(gameId), String.valueOf(TeamEnum.Team1.ordinal())};

        // Update the new row, returning the primary key value of the new row
        DbHelper.updateRecord(db, values1, where1, whereArgs1, PlayerContract.PlayerGameEntry.TABLE_NAME);

        // Create a new map of values, where column names are the keys
        ContentValues values2 = new ContentValues();
        values2.put(PlayerContract.PlayerGameEntry.PLAYER_RESULT, TeamEnum.getTeam2Result(result).getValue());

        String where2 = PlayerContract.PlayerGameEntry.GAME + " = ? AND " +
                PlayerContract.PlayerGameEntry.TEAM + " = ? ";
        String[] whereArgs2 = new String[]{String.valueOf(gameId), String.valueOf(TeamEnum.Team2.ordinal())};

        // Insert the new row, returning the primary key value of the new row
        DbHelper.updateRecord(db, values2, where2, whereArgs2, PlayerContract.PlayerGameEntry.TABLE_NAME);
    }

    public static ArrayList<Player> getPlayersStatistics(SQLiteDatabase db) {

        Cursor c = db.rawQuery("select player.name as player_name, player.grade as player_grade, " +
                " sum(result) as results_sum, " +
                " count(result) as results_count " +
                " from player_game, player " +
                " where result != " + PlayerGamesDbHelper.EMPTY_RESULT +
                " AND player.name = player_game.name " +
                " group by player_name " +
                " order by results_sum DESC",
                null, null);

        ArrayList<Player> players = new ArrayList<>();
        try {
            if (c.moveToFirst()) {
                do {
                    Player p = new Player(c.getString(c.getColumnIndex("player_name")),
                            c.getInt(c.getColumnIndex("player_grade")));
                    p.isComing = true;

                    int games = c.getInt(c.getColumnIndex("results_count"));
                    int success = c.getInt(c.getColumnIndex("results_sum"));
                    StatisticsData s = new StatisticsData(games, success);
                    p.setStatistics(s);

                    players.add(p);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return players;
    }
}