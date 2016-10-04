package com.teampicker.drorfichman.teampicker.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by drorfichman on 7/30/16.
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Players.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PlayerDbHelper.getSqlCreate());
        db.execSQL(PlayerGamesDbHelper.getSqlCreate());
        db.execSQL(GameDbHelper.getSqlCreate());

        // TODO debug data
        PlayerDbHelper.insertPlayer(db, "דרור", 85);
        PlayerDbHelper.insertPlayer(db, "אורי", 91);
        PlayerDbHelper.insertPlayer(db, "נדב", 87);
        PlayerDbHelper.insertPlayer(db, "גלעד", 88);
        PlayerDbHelper.insertPlayer(db, "מתן", 85);
        PlayerDbHelper.insertPlayer(db, "שי", 60);
        PlayerDbHelper.insertPlayer(db, "גדי", 75);
        PlayerDbHelper.insertPlayer(db, "פלאיני", 50);
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
        DbHelper mDbHelper = new DbHelper(context);
        return mDbHelper.getWritableDatabase();
    }

    public static void updatePlayerComing(Context context, String name, boolean coming) {
        PlayerDbHelper.updatePlayerComing(getSqLiteDatabase(context), name, coming);
    }

    public static void updatePlayer(Context context, String name, int grade) {
        PlayerDbHelper.updatePlauer(getSqLiteDatabase(context), name, grade);
    }

    public static boolean insertPlayer(Context context, String name, int grade) {
        return PlayerDbHelper.insertPlayer(getSqLiteDatabase(context), name, grade);
    }

    public static Cursor getPlayers(Context context) {
        return PlayerDbHelper.getPlayers(getSqLiteDatabase(context));
    }

    public static
    @NonNull
    ArrayList<Player> getComingPlayers(Context context, int countLastGames) {
        ArrayList<Player> comingPlayers = PlayerDbHelper.getComingPlayers(getSqLiteDatabase(context));
        addLastGameStats(context, countLastGames, comingPlayers);
        return comingPlayers;
    }

    public static void deletePlayer(Context context, String name) {
        PlayerDbHelper.deletePlayer(getSqLiteDatabase(context), name);
    }

    public static void clearOldGameTeams(Context context) {
        PlayerGamesDbHelper.clearOldGameTeams(getSqLiteDatabase(context));
    }

    public static void insertPlayerGame(Context context, Player player, int currGame, TeamEnum team) {
        PlayerGamesDbHelper.insertPlayerGame(getSqLiteDatabase(context), player, currGame, team);
    }

    public static ArrayList<Player> getCurrTeam(Context context, int currGame, TeamEnum team, int countLastGames) {
        ArrayList<Player> currTeam = PlayerGamesDbHelper.getCurrTeam(getSqLiteDatabase(context), currGame, team);

        addLastGameStats(context, countLastGames, currTeam);

        return currTeam;
    }

    private static void addLastGameStats(Context context, int countLastGames, ArrayList<Player> currTeam) {
        if (countLastGames > 0) {
            Log.d("STATS", "count " + countLastGames);
            ArrayList<Game> lastGames = GameDbHelper.getLastGames(getSqLiteDatabase(context), countLastGames);
            Log.d("STATS", "found " + lastGames.size());
            for (int game = 0; game < lastGames.size(); ++game) {
                Game g = lastGames.get(game);
                ArrayList<Player> team1 = PlayerGamesDbHelper.getCurrTeam(getSqLiteDatabase(context), g.gameId, TeamEnum.Team1);
                ArrayList<Player> team2 = PlayerGamesDbHelper.getCurrTeam(getSqLiteDatabase(context), g.gameId, TeamEnum.Team2);
                for (Player t1 : team1) {
                    setResult(currTeam, t1, game, TeamEnum.getTeam1Result(g.result));
                }
                for (Player t2 : team2) {
                    setResult(currTeam, t2, game, TeamEnum.getTeam2Result(g.result));
                }
            }
        }
    }

    private static boolean setResult(ArrayList<Player> players, Player search, int gameIndex, ResultEnum result) {
        for (Player p : players) {
            if (p.mName.equals(search.mName)) {
                p.results.add(gameIndex, result);
                Log.d("STATS", "set " + p.mName + " with " + result + " in " + gameIndex);
                return true;
            }
        }
        return false;
    }

    public static Cursor getGames(Context context) {
        return GameDbHelper.getGames(getSqLiteDatabase(context));
    }

    public static void insertGame(Context context, int gameId, int score1, int score2) {
        GameDbHelper.insertGameResults(getSqLiteDatabase(context), gameId, score1, score2);
    }

    public static String getNow() {
        return DateFormat.format("dd-MM-yyyy", System.currentTimeMillis()).toString();
    }
}
