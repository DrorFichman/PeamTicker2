package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.content.Context;
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
    public static final int MISSED_GAME = -9;

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
                    PlayerContract.PlayerGameEntry.DID_WIN + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.PLAYER_RESULT + " INTEGER DEFAULT " + EMPTY_RESULT + ")";

    public static final String SQL_DROP_PLAYER_GAMES_TABLE =
            "DELETE FROM " + PlayerContract.PlayerGameEntry.TABLE_NAME;

    public static String getSqlCreate() {
        return SQL_CREATE_PLAYERS_GAMES;
    }

    public static void addPlayerGame(SQLiteDatabase db, Player player, int currGame, TeamEnum team) {

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

    public static ArrayList<Player> getCurrTeam(Context context, SQLiteDatabase db, int currGame, TeamEnum team) {

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
                    Player p = PlayerDbHelper.createPlayerFromCursor(c, context,
                            PlayerContract.PlayerGameEntry.NAME, PlayerContract.PlayerGameEntry.PLAYER_GRADE, null);

                    int res = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_RESULT));
                    if (ResultEnum.Missed.getValue() == res) {
                        p.switchMissed();
                    }
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

        ArrayList<ResultEnum> results = new ArrayList<>();
        if (countLastGames == 0) {
            return results;
        }

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

        try {
            if (c.moveToFirst()) {
                int i = 0;
                do {
                    int res = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_RESULT));
                    results.add(ResultEnum.getResultFromOrdinal(res));
                    i++;
                } while (c.moveToNext() && (i < countLastGames || countLastGames == -1));
            }
        } finally {
            c.close();
        }

        return results;
    }

    public static void setPlayerGameResult(SQLiteDatabase db, int gameId, TeamEnum winningTeam) {

        updateTeamGameResult(db, gameId, TeamEnum.Team1, TeamEnum.getTeam1Result(winningTeam));
        updateTeamGameResult(db, gameId, TeamEnum.Team2, TeamEnum.getTeam2Result(winningTeam));
    }

    public static void updatePlayerResult(SQLiteDatabase db, int gameId, String name, ResultEnum res) {
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.PLAYER_RESULT, res.getValue());

        String where = PlayerContract.PlayerGameEntry.GAME + " = ? AND " +
                PlayerContract.PlayerGameEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(gameId), name};

        // Update the new row, returning the primary key value of the new row
        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerGameEntry.TABLE_NAME);
    }

    private static void updateTeamGameResult(SQLiteDatabase db, int gameId, TeamEnum team, ResultEnum result) {

        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.PLAYER_RESULT, result.getValue());
        values.put(PlayerContract.PlayerGameEntry.DID_WIN, result == ResultEnum.Win ? 1 : 0);

        String where = PlayerContract.PlayerGameEntry.GAME + " = ? AND " +
                PlayerContract.PlayerGameEntry.TEAM + " = ? AND " +
                PlayerContract.PlayerGameEntry.PLAYER_RESULT + " != ? ";
        String[] whereArgs = new String[]{String.valueOf(gameId),
                String.valueOf(team.ordinal()),
                String.valueOf(ResultEnum.Missed.getValue())};

        // Update the new row, returning the primary key value of the new row
        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerGameEntry.TABLE_NAME);
    }

    public static ArrayList<Player> getPlayersStatistics(Context context, SQLiteDatabase db, int gameCount) {

        String limitGamesCount = "";
        Log.d("teams", "Game count " + gameCount);
        if (gameCount > 0) {
            limitGamesCount = " AND game in (select game_index from game order by game_index DESC LIMIT " + gameCount + " ) ";
        }

        Cursor c = db.rawQuery("select player.name as player_name, player.grade as player_grade, " +
                        " sum(result) as results_sum, " +
                        " sum(did_win) as results_wins, " +
                        " count(result) as results_count " +
                        " from player_game, player " +
                        " where " +
                        " player.name = player_game.name " +
                        " AND result NOT IN ( " +
                        PlayerGamesDbHelper.EMPTY_RESULT + ", " +
                        PlayerGamesDbHelper.MISSED_GAME + " ) " +
                        limitGamesCount +
                        " group by player_name " +
                        " order by results_sum DESC",
                null, null);

        ArrayList<Player> players = new ArrayList<>();
        try {
            if (c.moveToFirst()) {
                do {
                    Player p = PlayerDbHelper.createPlayerFromCursor(c, context, "player_name", "player_grade", null);

                    int games = c.getInt(c.getColumnIndex("results_count"));
                    int success = c.getInt(c.getColumnIndex("results_sum"));
                    int wins = c.getInt(c.getColumnIndex("results_wins"));
                    StatisticsData s = new StatisticsData(games, success, wins);
                    p.setStatistics(s);

                    players.add(p);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return players;
    }

    public static void deleteGame(SQLiteDatabase db, String gameId) {
        int delete = db.delete(PlayerContract.PlayerGameEntry.TABLE_NAME,
                PlayerContract.PlayerGameEntry.GAME + " = ? ",
                new String[]{gameId});
        Log.d("TEAMS", delete + " game players were deleted");
    }
}