package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.tools.DateHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by drorfichman on 10/3/16.
 */
public class PlayerGamesDbHelper {

    public static final int EMPTY_RESULT = -10;
    public static final int MISSED_GAME = -9;
    public static final int TIE = 0;
    public static final int WIN = 1;
    public static final int LOSE = -1;

    private static final String SQL_CREATE_PLAYERS_GAMES =
            "CREATE TABLE " + PlayerContract.PlayerGameEntry.TABLE_NAME + " (" +
                    PlayerContract.PlayerGameEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerContract.PlayerGameEntry.NAME + " TEXT, " +
                    PlayerContract.PlayerGameEntry.GAME + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.DATE + " TEXT, " +
                    PlayerContract.PlayerGameEntry.PLAYER_GRADE + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.PLAYER_AGE + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.GAME_GRADE + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.TEAM + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.GOALS + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.ASSISTS + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.DID_WIN + " INTEGER, " +
                    PlayerContract.PlayerGameEntry.PLAYER_RESULT + " INTEGER DEFAULT " + EMPTY_RESULT + ", " +
                    PlayerContract.PlayerGameEntry.ATTRIBUTES + " TEXT DEFAULT '' " +
                    ")";

    public static final String SQL_DROP_PLAYER_GAMES_TABLE =
            "DELETE FROM " + PlayerContract.PlayerGameEntry.TABLE_NAME;

    public static String getSqlCreate() {
        return SQL_CREATE_PLAYERS_GAMES;
    }

    public static void addPlayerGame(SQLiteDatabase db, Player player, int currGame, TeamEnum team) {

        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.GAME, currGame);
        values.put(PlayerContract.PlayerGameEntry.DATE, DateHelper.getNow());
        values.put(PlayerContract.PlayerGameEntry.NAME, player.mName);
        values.put(PlayerContract.PlayerGameEntry.PLAYER_GRADE, player.mGrade);
        values.put(PlayerContract.PlayerGameEntry.TEAM, team.ordinal());
        values.put(PlayerContract.PlayerGameEntry.PLAYER_AGE, player.getAge());
        values.put(PlayerContract.PlayerGameEntry.ATTRIBUTES, player.getAttributes());

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
                PlayerContract.PlayerGameEntry.PLAYER_AGE,
                PlayerContract.PlayerGameEntry.GAME_GRADE,
                PlayerContract.PlayerGameEntry.GOALS,
                PlayerContract.PlayerGameEntry.ASSISTS,
                PlayerContract.PlayerGameEntry.TEAM,
                PlayerContract.PlayerGameEntry.PLAYER_RESULT,
                PlayerContract.PlayerGameEntry.ATTRIBUTES
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
                    Player p = PlayerDbHelper.createPlayerFromCursor(c,
                            PlayerContract.PlayerGameEntry.NAME,
                            null,
                            null,
                            PlayerContract.PlayerGameEntry.PLAYER_GRADE,
                            null, null, PlayerContract.PlayerGameEntry.ATTRIBUTES);

                    // age is saved at the time the game was played
                    p.setAge(c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_AGE)));
                    p.gameResult = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_RESULT));

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

    public static ArrayList<PlayerGameStat> getPlayerLastGames(SQLiteDatabase db, Player player, int countLastGames) {

        ArrayList<PlayerGameStat> results = new ArrayList<>();
        if (countLastGames == 0) {
            return results;
        }

        String[] projection = {
                PlayerContract.PlayerGameEntry.NAME,
                PlayerContract.PlayerGameEntry.GAME,
                PlayerContract.PlayerGameEntry.DATE,
                PlayerContract.PlayerGameEntry.PLAYER_RESULT,
                PlayerContract.PlayerGameEntry.PLAYER_GRADE
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = "date(" + PlayerContract.PlayerGameEntry.DATE + ") DESC";

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
                    int grade = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_GRADE));
                    String date = c.getString(c.getColumnIndex(PlayerContract.PlayerGameEntry.DATE));
                    PlayerGameStat stat = new PlayerGameStat(ResultEnum.getResultFromOrdinal(res), grade, date);

                    results.add(stat);
                    i++;
                } while (c.moveToNext() && (i < countLastGames || countLastGames == -1));
            }
        } finally {
            c.close();
        }

        results.sort(Comparator.comparing(PlayerGameStat::getDate).reversed());
        return results;
    }

    public static void setPlayerGameResult(SQLiteDatabase db, int gameId, String date, TeamEnum winningTeam) {

        updateTeamGameResult(db, gameId, date, TeamEnum.Team1, TeamEnum.getTeam1Result(winningTeam));
        updateTeamGameResult(db, gameId, date, TeamEnum.Team2, TeamEnum.getTeam2Result(winningTeam));
    }

    static class PlayerGame {
        int team;
        ResultEnum result;
        PlayerGame(int t, ResultEnum r) {team = t; result = r;}
    }

    public static PlayerGame getPlayerResult(SQLiteDatabase db, int gameId, String name) {

        String[] projection = {
                PlayerContract.PlayerGameEntry.PLAYER_RESULT,
                PlayerContract.PlayerGameEntry.TEAM
        };

        String where = PlayerContract.PlayerGameEntry.NAME + " = ? AND "
                + PlayerContract.PlayerGameEntry.GAME + " = ? ";
        String[] whereArgs = new String[]{name, String.valueOf(gameId)};

        Cursor c = db.query(
                PlayerContract.PlayerGameEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        try {
            if (c.moveToFirst()) {
                int team = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.TEAM));
                int res = c.getInt(c.getColumnIndex(PlayerContract.PlayerGameEntry.PLAYER_RESULT));
                ResultEnum r = ResultEnum.getResultFromOrdinal(res);
                PlayerGame pg = new PlayerGame(team, r);
                return pg;
            }
        } finally {
            c.close();
        }

        return null;
    }

    public static void updatePlayerResult(SQLiteDatabase db, int gameId, String name, ResultEnum res, int newTeam) {
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.PLAYER_RESULT, res.getValue());
        values.put(PlayerContract.PlayerGameEntry.DID_WIN, res == ResultEnum.Win ? 1 : 0);
        if (newTeam >= 0) values.put(PlayerContract.PlayerGameEntry.TEAM, newTeam);

        String where = PlayerContract.PlayerGameEntry.GAME + " = ? AND " +
                PlayerContract.PlayerGameEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(gameId), name};

        // Update the new row, returning the primary key value of the new row
        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerGameEntry.TABLE_NAME);
    }

    private static void updateTeamGameResult(SQLiteDatabase db, int gameId, String date, TeamEnum team, ResultEnum result) {

        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.PLAYER_RESULT, result.getValue());
        values.put(PlayerContract.PlayerGameEntry.DID_WIN, result == ResultEnum.Win ? 1 : 0);
        values.put(PlayerContract.PlayerGameEntry.DATE, date);

        String where = PlayerContract.PlayerGameEntry.GAME + " = ? AND " +
                PlayerContract.PlayerGameEntry.TEAM + " = ? AND " +
                PlayerContract.PlayerGameEntry.PLAYER_RESULT + " != ? ";
        String[] whereArgs = new String[]{String.valueOf(gameId),
                String.valueOf(team.ordinal()),
                String.valueOf(ResultEnum.Missed.getValue())}; // avoid overwrite 'missed'

        // Update the new row, returning the primary key value of the new row
        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerGameEntry.TABLE_NAME);
    }

    public static boolean updateGameDate(SQLiteDatabase db, int gameId, String date) {

        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerGameEntry.DATE, date);

        String where = PlayerContract.PlayerGameEntry.GAME + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(gameId)};

        return 0 < DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerGameEntry.TABLE_NAME);
    }

    public static ArrayList<Player> getPlayersStatistics(SQLiteDatabase db, int gameCount) {
        return getStatistics(db, gameCount, null);
    }

    public static StatisticsData getPlayerStatistics(SQLiteDatabase db, int gameCount, String name) {
        ArrayList<Player> playersStatistics = getStatistics(db, gameCount, name);
        if (playersStatistics.size() > 0) {
            return playersStatistics.get(0).statistics;
        } else {
            Log.w("STAT", "Can't find player stats " + name);
            return new StatisticsData();
        }
    }

    private static ArrayList<Player> getStatistics(SQLiteDatabase db, int gameCount, String name) {

        String limitGamesCount = "";
        Log.d("stats", "Game count " + gameCount);
        if (gameCount > 0) {
            limitGamesCount = " AND game in (select game_index from game order by date(date) DESC LIMIT " + gameCount + " ) ";
        }

        String nameFilter = "";
        if (name != null) {
            nameFilter = " AND player.name =  \"" + name + "\"";
        }

        Cursor c = db.rawQuery("select player.name as player_name, " +
                        " player.birth_year as birth_year, " +
                        " player.birth_month as birth_month, " +
                        " player.grade as player_grade, " +
                        " player.attributes as player_attributes, " +
                        " sum(result) as results_sum, " +
                        " sum(did_win) as results_wins, " +
                        " count(result) as results_count " +
                        " from player_game, player " +
                        " where " +
                        " player.name = player_game.name " + nameFilter +
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
                    Player p = PlayerDbHelper.createPlayerFromCursor(c,
                            "player_name",
                            "birth_year",
                            "birth_month",
                            "player_grade",
                            null, null,
                            "player_attributes");

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

    public static void deleteGame(SQLiteDatabase db, int gameId) {
        int delete = db.delete(PlayerContract.PlayerGameEntry.TABLE_NAME,
                PlayerContract.PlayerGameEntry.GAME + " = ? ",
                new String[]{String.valueOf(gameId)});
        Log.d("TEAMS", delete + " game players were deleted");
    }

    private static PlayerParticipation getPlayer(HashMap<String, PlayerParticipation> result,
                                                 String currName, String collaborator) {

        PlayerParticipation p = result.get(currName);
        if (p == null) {
            p = new PlayerParticipation();
            p.mName = currName;
            p.mParticipatedWith = collaborator;
            result.put(currName, p);
        }

        return p;
    }

    public static HashMap<String, PlayerParticipation> getParticipationStatistics(SQLiteDatabase db, int gameCount, GamesPlayersCache cache, Date upTo, String name) {

        String limitGamesCount = "";
        if (gameCount > 0) {
            limitGamesCount = " AND game in (select game_index from game order by date(date) DESC LIMIT " + gameCount + " ) ";
        }

        String limitUpToDate = " ";
//        if (upTo != null) { // TODO add check valid time
//            limitUpToDate = " AND date < date(" + DateHelper.getDisplayDate(upTo.getTime()) + ") ";
//        }

        Cursor c = db.rawQuery("select player.name as player_name, " +
                        " game, team, result " +
                        " from player_game, player " +
                        " where " +
                        " player.name = player_game.name AND player.name =  \"" + name + "\"" +
                        limitUpToDate +
                        " AND result NOT IN ( " +
                        PlayerGamesDbHelper.EMPTY_RESULT + ", " +
                        PlayerGamesDbHelper.MISSED_GAME + " ) " +
                        limitGamesCount,
                null, null);

        HashMap<String, PlayerParticipation> result = new HashMap();

        // Get teams based on player active games, aggregate player wins with and against teammates
        try {
            if (c.moveToFirst()) {
                do {
                    int currGame = c.getInt(c.getColumnIndex("game"));
                    ResultEnum gameResult = ResultEnum.getResultFromOrdinal(c.getInt(c.getColumnIndex("result")));
                    int collaboratorTeam = c.getInt(c.getColumnIndex("team"));

                    ArrayList<Player> team1;
                    ArrayList<Player> team2;

                    if (cache != null) {
                        team1 = cache.team1s.get(currGame);
                        team2 = cache.team2s.get(currGame);

                        if (team1 == null || team2 == null) {
                            team1 = getCurrTeam(db, currGame, TeamEnum.Team1);
                            team2 = getCurrTeam(db, currGame, TeamEnum.Team2);
                            cache.team1s.put(currGame, team1);
                            cache.team2s.put(currGame, team2);
                        }
                    } else {
                        team1 = getCurrTeam(db, currGame, TeamEnum.Team1);
                        team2 = getCurrTeam(db, currGame, TeamEnum.Team2);
                    }

                    if (TeamEnum.Team1.ordinal() == collaboratorTeam) {
                        for (Player p1 : team1) { // same team
                            PlayerParticipation player = getPlayer(result, p1.mName, name);
                            addResult(player.statisticsWith, gameResult, ResultEnum.getResultFromOrdinal(p1.gameResult));
                        }
                        for (Player p2 : team2) { // opposite team
                            PlayerParticipation player = getPlayer(result, p2.mName, name);
                            addResult(player.statisticsVs, gameResult, ResultEnum.getResultFromOrdinal(p2.gameResult));
                        }
                    }

                    if (TeamEnum.Team2.ordinal() == collaboratorTeam) {
                        for (Player p2 : team2) { // same team
                            PlayerParticipation player = getPlayer(result, p2.mName, name);
                            addResult(player.statisticsWith, gameResult, ResultEnum.getResultFromOrdinal(p2.gameResult));
                        }
                        for (Player p1 : team1) { // opposite team
                            PlayerParticipation player = getPlayer(result, p1.mName, name);
                            addResult(player.statisticsVs, gameResult, ResultEnum.getResultFromOrdinal(p1.gameResult));
                        }
                    }

                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        // Remove the actual player
        result.remove(name);

        return result;
    }

    private static void addResult(StatisticsData playerStatistics,
                                  ResultEnum collaboratorRes, ResultEnum playerRes) {
        if (!ResultEnum.isActive(playerRes))
            return;

        playerStatistics.gamesCount++;
        if (ResultEnum.Win.equals(collaboratorRes)) {
            playerStatistics.wins++;
            playerStatistics.successRate++;
        }
        if (ResultEnum.Lose.equals(collaboratorRes)) {
            playerStatistics.successRate--;
        }
    }

    public static int getActiveGame(SQLiteDatabase db) {

        Cursor c = db.rawQuery("select game " +
                        " from player_game " +
                        " where " +
                        " result = " + PlayerGamesDbHelper.EMPTY_RESULT +
                        " order by game DESC ",
                null, null);

        try {
            if (c.moveToFirst()) {
                return c.getInt(c.getColumnIndex("game"));
            }
        } finally {
            c.close();
        }

        return -1;
    }

    public static int getMaxGame(SQLiteDatabase db) {

        Cursor c = db.rawQuery("select max(game) as curr_game " +
                        " from player_game ",
                null, null);

        try {
            if (c.moveToFirst()) {
                return c.getInt(c.getColumnIndex("curr_game"));
            }
        } finally {
            c.close();
        }

        return 1;
    }
}