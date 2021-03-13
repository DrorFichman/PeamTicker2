package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.tools.FirebaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;

/**
 * Created by drorfichman on 7/30/16.
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "Players.db";

    private static SQLiteDatabase writableDatabase;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void saveTeams(Context ctx, ArrayList<Player> firstTeam, ArrayList<Player> secondTeam) {
        DbHelper.clearOldGameTeams(ctx);

        int currGame = DbHelper.getMaxGame(ctx) + 1;

        for (Player a : firstTeam) {
            PlayerGame pg = new PlayerGame(currGame, a.mName, a.mGrade, TeamEnum.Team1, a.getAge());
            DbHelper.insertPlayerGame(ctx, pg);
        }
        for (Player b : secondTeam) {
            PlayerGame pg = new PlayerGame(currGame, b.mName, b.mGrade, TeamEnum.Team2, b.getAge());
            DbHelper.insertPlayerGame(ctx, pg);
        }
    }

    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    public void createTables(SQLiteDatabase db) {
        try {
            db.execSQL(PlayerDbHelper.getSqlCreate());
            db.execSQL(PlayerGamesDbHelper.getSqlCreate());
            db.execSQL(GameDbHelper.getSqlCreate());
        } catch (SQLiteException e) {
            Log.w("Create", "Tables already exist " + e.getMessage());
        }

        Log.d("Create", "Create new tables called");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);

        addColumns(db);

        // TODO sanitize player names for firebase, not mandatory - sanitized when synced to cloud
    }

    /*
    This content can be removed for official initial release - DDL done with the table
     */
    private void addColumns(SQLiteDatabase db) {
        addColumn(db, PlayerContract.PlayerEntry.TABLE_NAME, PlayerContract.PlayerEntry.BIRTH_YEAR, "INTEGER", null);
        addColumn(db, PlayerContract.PlayerEntry.TABLE_NAME, PlayerContract.PlayerEntry.BIRTH_MONTH, "INTEGER", null);
        addColumn(db, PlayerContract.PlayerGameEntry.TABLE_NAME, PlayerContract.PlayerGameEntry.PLAYER_AGE, "INTEGER", null);
        addColumn(db, PlayerContract.PlayerEntry.TABLE_NAME, PlayerContract.PlayerEntry.ARCHIVED, "INTEGER", "0");
        addColumn(db, PlayerContract.PlayerEntry.TABLE_NAME, PlayerContract.PlayerEntry.ATTRIBUTES, "TEXT", "''");
        addColumn(db, PlayerContract.PlayerGameEntry.TABLE_NAME, PlayerContract.PlayerGameEntry.ATTRIBUTES, "TEXT", "''");
        modifyGameDates(db);
    }

    private void addColumn(SQLiteDatabase db, String table, String column, String type, String def) {
        try {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type + " default " + def);
            Log.i("Upgrade", "Altering " + table + ": " + column);
        } catch (SQLiteException ex) {
            Log.e("Upgrade", "Altering " + table + ": " + ex.getMessage(), ex);
        }
    }

    /*
    Migrate existing data from date format that can't be ordered using sqlite
    This method can be removed for official initial release
     */
    private void modifyGameDates(SQLiteDatabase db) {
        // From dd-MM-YYYY to yyyy-MM-dd
        try {
            db.execSQL("UPDATE " + PlayerContract.GameEntry.TABLE_NAME +
                    " SET date = " +
                    " (substr(date,7,4) || '-' || substr(date, 4,2) || '-' || substr(date,1,2)) " +
                    " where substr(date,3,1) = '-' and substr(date,6,1) = '-' ");

            db.execSQL("UPDATE " + PlayerContract.PlayerGameEntry.TABLE_NAME +
                    " SET date = " +
                    " (substr(date,7,4) || '-' || substr(date, 4,2) || '-' || substr(date,1,2)) " +
                    " where substr(date,3,1) = '-' and substr(date,6,1) = '-' ");

        } catch (Exception e) {
            Log.e("modifyGameDates", e.getMessage(), e);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private static SQLiteDatabase getSqLiteDatabase(Context context) {

        // Gets the data repository in write mode
        if (writableDatabase == null) {
            DbHelper mDbHelper = new DbHelper(context.getApplicationContext());
            writableDatabase = mDbHelper.getWritableDatabase();
        }
        return writableDatabase;
    }

    public static void deleteTableContents(Context context) {
        getSqLiteDatabase(context).execSQL(PlayerDbHelper.SQL_DROP_PLAYER_TABLE);
        getSqLiteDatabase(context).execSQL(GameDbHelper.SQL_DROP_GAMES_TABLE);
        getSqLiteDatabase(context).execSQL(PlayerGamesDbHelper.SQL_DROP_PLAYER_GAMES_TABLE);
    }

    public static void setPlayerComing(Context context, ArrayList<Player> team) {
        for (Player p : team) {
            PlayerDbHelper.updatePlayerComing(getSqLiteDatabase(context), p.mName, true);
        }
    }

    public static void updatePlayerComing(Context context, String name, boolean isComing) {
        PlayerDbHelper.updatePlayerComing(getSqLiteDatabase(context), name, isComing);
    }

    public static void updatePlayerGrade(Context context, String name, int grade) {
        PlayerDbHelper.updatePlayerGrade(getSqLiteDatabase(context), name, grade);
    }

    public static boolean updatePlayerName(Context context, Player player, String newName) {
        if (getPlayer(context, newName) != null) // new name already exists
            return false;

        PlayerDbHelper.updatePlayerName(getSqLiteDatabase(context), player.mName, newName);
        player.mName = newName;

        return true;
    }

    public static void updatePlayerAttributes(Context context, Player p) {
        PlayerDbHelper.setPlayerAttributes(getSqLiteDatabase(context), p.mName, p.getAttributes());
    }

    public static void updatePlayerBirth(Context context, String name, int year, int month) {
        PlayerDbHelper.updatePlayerBirth(getSqLiteDatabase(context), name, year, month);
    }

    public static boolean insertPlayer(Context context, Player p) {
        p.mName = FirebaseHelper.sanitizeKey(p.mName);
        return PlayerDbHelper.insertPlayer(getSqLiteDatabase(context), p);
    }

    public static Player getPlayer(Context context, String name) {
        return getPlayer(context, name, -1);
    }

    public static Player getPlayer(Context context, String name, int gameCount) {
        final Player player = PlayerDbHelper.getPlayer(getSqLiteDatabase(context), name);
        if (player != null) {
            ArrayList<Player> players = new ArrayList<>(Arrays.asList(player));
            addLastGameStats(context, gameCount, players, true);
        }
        return player;
    }

    public static ArrayList<Player> getPlayersStatistics(Context context, int games) {
        return PlayerGamesDbHelper.getPlayersStatistics(getSqLiteDatabase(context), games);
    }

    public static HashMap<String, PlayerParticipation> getPlayersParticipationStatistics(Context context, String name, BuilderPlayerCollaborationStatistics params) {
        return getPlayersParticipationStatistics(context, params.games, params.upTo, params.cache, name);
    }

    public static HashMap<String, PlayerParticipation> getPlayersParticipationStatistics(Context context, int games, Date upTo, GamesPlayersCache cache, String name) {
        return PlayerGamesDbHelper.getParticipationStatistics(getSqLiteDatabase(context), games, cache, upTo, name);
    }

    public static ArrayList<Player> getPlayers(Context context) {
        ArrayList<Player> players = PlayerDbHelper.getPlayers(getSqLiteDatabase(context));
        return players;
    }

    @NonNull
    public static ArrayList<Player> getPlayers(Context context, int gamesCount, boolean showArchived) {
        ArrayList<Player> players = PlayerDbHelper.getPlayers(getSqLiteDatabase(context), showArchived);
        DbHelper.addLastGameStats(context, gamesCount, players, false);
        return players;
    }

    public static int getComingPlayersCount(Context context) {
        return PlayerDbHelper.getComingPlayersCount(getSqLiteDatabase(context));
    }

    public static ArrayList<Player> getComingPlayers(Context context, int countLastGames) {
        ArrayList<Player> comingPlayers = PlayerDbHelper.getComingPlayers(getSqLiteDatabase(context));
        addLastGameStats(context, countLastGames, comingPlayers, countLastGames > 0);
        return comingPlayers;
    }

    public static void deletePlayer(Context context, String playerName) {
        PlayerDbHelper.deletePlayer(getSqLiteDatabase(context), playerName);
    }

    public static void archivePlayer(Context context, String name, boolean archiveValue) {
        PlayerDbHelper.setPlayerArchive(getSqLiteDatabase(context), name, archiveValue);
    }

    public static void clearOldGameTeams(Context context) {
        PlayerGamesDbHelper.clearOldGameTeams(getSqLiteDatabase(context));
    }

    public static void insertPlayerGame(Context context, PlayerGame pg) {
        pg.playerName = FirebaseHelper.sanitizeKey(pg.playerName);
        PlayerGamesDbHelper.addPlayerGame(getSqLiteDatabase(context), pg);
    }

    public static ArrayList<PlayerGame> getPlayersGames(Context ctx) {
        return PlayerGamesDbHelper.getPlayersGames(getSqLiteDatabase(ctx));
    }

    public static ArrayList<Player> getCurrTeam(Context context, int currGame, TeamEnum team, int countLastGames) {
        ArrayList<Player> currTeam = PlayerGamesDbHelper.getCurrTeam(getSqLiteDatabase(context), currGame, team);

        addLastGameStats(context, countLastGames, currTeam, countLastGames > 0);

        return currTeam;
    }

    private static void addLastGameStats(Context context, int countLastGames, ArrayList<Player> currTeam, boolean statistics) {

        for (Player p : currTeam) {
            p.results = PlayerGamesDbHelper.getPlayerLastGames(getSqLiteDatabase(context), p, countLastGames);
            if (statistics) {
                p.statistics = PlayerGamesDbHelper.getPlayerStatistics(getSqLiteDatabase(context), countLastGames, p.mName);
            }
        }
    }

    public static ArrayList<Game> getGames(Context context) {
        return GameDbHelper.getGames(getSqLiteDatabase(context));
    }

    public static ArrayList<Game> getGames(Context context, String name) {
        return GameDbHelper.getGames(getSqLiteDatabase(context), name);
    }

    public static ArrayList<Game> getGames(Context context, String name, String another) {
        return GameDbHelper.getGames(getSqLiteDatabase(context), name, another);
    }

    public static void insertGame(Context context, Game game) {
        GameDbHelper.insertGameResults(getSqLiteDatabase(context), game);
        PlayerGamesDbHelper.setPlayerGameResult(getSqLiteDatabase(context),
                game.gameId, game.dateString, game.winningTeam);
    }

    public static void updateGameDate(Context context, Game game, String gameDate) {
        GameDbHelper.updateGameDate(getSqLiteDatabase(context), game.gameId, gameDate);
        PlayerGamesDbHelper.updateGameDate(getSqLiteDatabase(context), game.gameId, gameDate);
    }

    public static void setPlayerResult(Context context, int gameId, String name, ResultEnum res) {
        PlayerGamesDbHelper.updatePlayerResult(getSqLiteDatabase(context), gameId, name, res, -1);
    }

    public static void modifyPlayerResult(Context context, int gameId, String name) {
        PlayerGamesDbHelper.PlayerGameResult pg = PlayerGamesDbHelper.getPlayerResult(getSqLiteDatabase(context), gameId, name);

        ResultEnum newRes = ResultEnum.Missed;
        int newTeam = -1;

        if (pg.result == ResultEnum.Missed) {

            // TODO move this option to another place // TODO check with Uri
            Game game = GameDbHelper.getGame(getSqLiteDatabase(context), gameId);
            newRes = TeamEnum.getTeamResultInGame(game.winningTeam, pg.team);

        } else {

            if (pg.result == ResultEnum.Tie) newRes = ResultEnum.Tie;
            if (pg.result == ResultEnum.Win) newRes = ResultEnum.Lose;
            if (pg.result == ResultEnum.Lose) newRes = ResultEnum.Win;

            if (pg.team == 1) newTeam = 0;
            if (pg.team == 0) newTeam = 1;
        }

        PlayerGamesDbHelper.updatePlayerResult(getSqLiteDatabase(context), gameId, name, newRes, newTeam);
    }

    public static int updateRecord(SQLiteDatabase db, ContentValues values, String where, String[] whereArgs, String tableName) {

        return db.updateWithOnConflict(tableName,
                values,
                where, whereArgs, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void deleteGame(Context context, int game) {

        GameDbHelper.deleteGame(getSqLiteDatabase(context), game);
        PlayerGamesDbHelper.deleteGame(getSqLiteDatabase(context), game);
    }

    public static void clearComingPlayers(Context context) {
        PlayerDbHelper.clearAllComing(getSqLiteDatabase(context));
    }

    public static int getMaxGame(Context context) {
        return PlayerGamesDbHelper.getMaxGame(getSqLiteDatabase(context));
    }

    public static int getActiveGame(Context context) {
        return PlayerGamesDbHelper.getActiveGame(getSqLiteDatabase(context));
    }
}
