package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.PreferenceAttributesHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by drorfichman on 7/30/16.
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Players.db";

    private static SQLiteDatabase writableDatabase;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PlayerDbHelper.getSqlCreate());
        db.execSQL(PlayerGamesDbHelper.getSqlCreate());
        db.execSQL(GameDbHelper.getSqlCreate());

        Log.d("IMPORT", "No new data");
        // TODO debug data
        /*
        PlayerDbHelper.insertPlayer(db, "הדר יוסיפון", 88);
        PlayerDbHelper.insertPlayer(db, "אורי ש", 88);
        PlayerDbHelper.insertPlayer(db, "גלעד ב.א.", 88);
        PlayerDbHelper.insertPlayer(db, "תומר ל", 88);
        PlayerDbHelper.insertPlayer(db, "אלון ב", 88);
        PlayerDbHelper.insertPlayer(db, "בנקיר", 88);
        PlayerDbHelper.insertPlayer(db, "עומרי", 88);
        PlayerDbHelper.insertPlayer(db, "שיר הסוללים", 88);
        PlayerDbHelper.insertPlayer(db, "נדב", 85);
        PlayerDbHelper.insertPlayer(db, "גלעד", 85);
        PlayerDbHelper.insertPlayer(db, "שחר", 85);
        PlayerDbHelper.insertPlayer(db, "אלעד שניידר", 84);
        PlayerDbHelper.insertPlayer(db, "אורי גבאי", 84);
        PlayerDbHelper.insertPlayer(db, "אורן", 84);
        PlayerDbHelper.insertPlayer(db, "סער", 82);
        PlayerDbHelper.insertPlayer(db, "טל הדרי", 82);
        PlayerDbHelper.insertPlayer(db, "לירן", 81);
        PlayerDbHelper.insertPlayer(db, "רונן", 81);
        PlayerDbHelper.insertPlayer(db, "דרור", 80);
        PlayerDbHelper.insertPlayer(db, "שי חדד", 80);
        PlayerDbHelper.insertPlayer(db, "ריקי", 80);
        PlayerDbHelper.insertPlayer(db, "נועם", 79);
        PlayerDbHelper.insertPlayer(db, "בר", 79);
        PlayerDbHelper.insertPlayer(db, "גבי", 78);
        PlayerDbHelper.insertPlayer(db, "עדן", 78);
        PlayerDbHelper.insertPlayer(db, "רותם", 76);
        PlayerDbHelper.insertPlayer(db, "גדי", 75);
        PlayerDbHelper.insertPlayer(db, "מתן הורביץ", 75);
        PlayerDbHelper.insertPlayer(db, "שי פ", 73);
        PlayerDbHelper.insertPlayer(db, "בועז", 73);
        PlayerDbHelper.insertPlayer(db, "שי חן", 73);
        PlayerDbHelper.insertPlayer(db, "הדר ל", 73);
        PlayerDbHelper.insertPlayer(db, "תומר מן", 73);
        PlayerDbHelper.insertPlayer(db, "בוריס", 73);
        PlayerDbHelper.insertPlayer(db, "ערן", 73);
        PlayerDbHelper.insertPlayer(db, "שרון ב", 73);
        PlayerDbHelper.insertPlayer(db, "שרון ר", 73);
        */

        /*
        PlayerDbHelper.insertPlayer(db, "דרור", 85);
        PlayerDbHelper.insertPlayer(db, "אורי", 91);
        PlayerDbHelper.insertPlayer(db, "נדב", 87);
        PlayerDbHelper.insertPlayer(db, "גלעד", 88);
        PlayerDbHelper.insertPlayer(db, "מתן", 85);
        PlayerDbHelper.insertPlayer(db, "שי", 60);
        PlayerDbHelper.insertPlayer(db, "גדי", 75);
        PlayerDbHelper.insertPlayer(db, "פלאיני", 50);
        */
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // TODO ? db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
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
        getSqLiteDatabase(context).execSQL(GameDbHelper.SQL_DROP_GAMES_TABLE);
        getSqLiteDatabase(context).execSQL(PlayerDbHelper.SQL_DROP_PLAYER_TABLE);
        getSqLiteDatabase(context).execSQL(PlayerGamesDbHelper.SQL_DROP_PLAYER_GAMES_TABLE);
    }

    public static void updatePlayerComing(Context context, String name, boolean isComing) {
        PlayerDbHelper.updatePlayerComing(getSqLiteDatabase(context), name, isComing);
    }

    public static void updatePlayer(Context context, String name, int grade) {
        PlayerDbHelper.updatePlayerGrade(getSqLiteDatabase(context), name, grade);
    }

    public static boolean insertPlayer(Context context, String name, int grade) {
        return PlayerDbHelper.insertPlayer(getSqLiteDatabase(context), name, grade);
    }

    public static Player getPlayer(Context context, String name) {
        final Player player = PlayerDbHelper.getPlayer(context, getSqLiteDatabase(context), name);
        if (player != null) {
            ArrayList<Player> players = new ArrayList<>(Arrays.asList(player));
            addLastGameStats(context, -1, players, true);
        }
        return player;
    }

    public static ArrayList<Player> getPlayersStatistics(Context context, int games) {
        return PlayerGamesDbHelper.getPlayersStatistics(context, getSqLiteDatabase(context), games);
    }

    public static HashMap<String, PlayerParticipation> getPlayersParticipationsStatistics(Context context, int games, String name) {
        return PlayerGamesDbHelper.getParticipationStatistics(context, getSqLiteDatabase(context), games, name);
    }

    public static ArrayList<Player> getPlayers(Context context, int gamesCount) {
        ArrayList<Player> players = PlayerDbHelper.getPlayers(context, getSqLiteDatabase(context));
        DbHelper.addLastGameStats(context, gamesCount, players, false);
        return players;
    }

    public static int getComingPlayersCount(Context context) {
        return PlayerDbHelper.getComingPlayersCount(getSqLiteDatabase(context));
    }

    public static ArrayList<Player> getComingPlayers(Context context, int countLastGames) {
        ArrayList<Player> comingPlayers = PlayerDbHelper.getComingPlayers(context, getSqLiteDatabase(context));
        addLastGameStats(context, countLastGames, comingPlayers, false);
        return comingPlayers;
    }

    public static void deletePlayer(Context context, String playerName) {
        PlayerDbHelper.deletePlayer(getSqLiteDatabase(context), playerName);
        PreferenceAttributesHelper.deletePlayerAttributes(context, playerName);
    }

    public static void clearOldGameTeams(Context context) {
        PlayerGamesDbHelper.clearOldGameTeams(getSqLiteDatabase(context));
    }

    public static void insertPlayerGame(Context context, Player player, int currGame, TeamEnum team) {
        PlayerGamesDbHelper.addPlayerGame(getSqLiteDatabase(context), player, currGame, team);
    }

    public static ArrayList<Player> getCurrTeam(Context context, int currGame, TeamEnum team, int countLastGames) {
        ArrayList<Player> currTeam = PlayerGamesDbHelper.getCurrTeam(context, getSqLiteDatabase(context), currGame, team);

        addLastGameStats(context, countLastGames, currTeam, false);

        return currTeam;
    }

    private static void addLastGameStats(Context context, int countLastGames, ArrayList<Player> currTeam, boolean statistics) {

        for (Player p : currTeam) {
            p.results = PlayerGamesDbHelper.getPlayerLastGames(getSqLiteDatabase(context), p, countLastGames);
            if (statistics) {
                p.statistics = PlayerGamesDbHelper.getPlayerStatistics(context, getSqLiteDatabase(context), countLastGames, p.mName);
            }
        }
    }

    public static ArrayList<Game> getGames(Context context) {
        return GameDbHelper.getGames(getSqLiteDatabase(context));
    }

    public static ArrayList<Game> getGames(Context context, String name) {
        return GameDbHelper.getGames(getSqLiteDatabase(context), name);
    }

    public static void insertGame(Context context, int gameId, int score1, int score2) {
        GameDbHelper.insertGameResults(getSqLiteDatabase(context), gameId, score1, score2);
        PlayerGamesDbHelper.setPlayerGameResult(getSqLiteDatabase(context), gameId, TeamEnum.getResult(score1, score2));
    }

    public static void setPlayerResult(Context context, int gameId, String name, ResultEnum res) {
        PlayerGamesDbHelper.updatePlayerResult(getSqLiteDatabase(context), gameId, name, res, -1);
    }

    public static void modifyPlayerResult(Context context, int gameId, String name) {
        PlayerGamesDbHelper.PlayerGame pg = PlayerGamesDbHelper.getPlayerResult(getSqLiteDatabase(context), gameId, name);

        ResultEnum newRes = ResultEnum.Missed;
        int newTeam = -1;

        if (pg.result == ResultEnum.Missed) {

            // TODO move this option to another place
            Game game = GameDbHelper.getGame(getSqLiteDatabase(context), gameId);
            newRes = TeamEnum.getTeamResultInGame(game, pg.team);

        } else {

            if (pg.result == ResultEnum.Tie) newRes = ResultEnum.Tie;
            if (pg.result == ResultEnum.Win) newRes = ResultEnum.Lose;
            if (pg.result == ResultEnum.Lose) newRes = ResultEnum.Win;

            if (pg.team == 1) newTeam = 0;
            if (pg.team == 0) newTeam = 1;
        }

        PlayerGamesDbHelper.updatePlayerResult(getSqLiteDatabase(context), gameId, name, newRes, newTeam);
    }

    public static String getNow() {
        return DateFormat.format("dd-MM-yyyy", System.currentTimeMillis()).toString();
    }

    public static void updateRecord(SQLiteDatabase db, ContentValues values, String where, String[] whereArgs, String tableName) {

        db.updateWithOnConflict(tableName,
                values,
                where, whereArgs, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void deleteGame(Context context, String game) {

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
