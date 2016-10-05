package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;

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
        if (writableDatabase == null) {
            DbHelper mDbHelper = new DbHelper(context.getApplicationContext());
            writableDatabase = mDbHelper.getWritableDatabase();
        }
        return writableDatabase;
    }

    public static void updatePlayerComing(Context context, String name, boolean coming) {
        PlayerDbHelper.updatePlayerComing(getSqLiteDatabase(context), name, coming);
    }

    public static void updatePlayer(Context context, String name, int grade) {
        PlayerDbHelper.updatePlayerGrade(getSqLiteDatabase(context), name, grade);
    }

    public static boolean insertPlayer(Context context, String name, int grade) {
        return PlayerDbHelper.insertPlayer(getSqLiteDatabase(context), name, grade);
    }

    public static Player getPlayer(Context context, String name) {
        final Player player = PlayerDbHelper.getPlayer(getSqLiteDatabase(context), name);
        if (player != null) {
            ArrayList<Player> players = new ArrayList<>(Arrays.asList(player));
            addLastGameStats(context, 10, players);
        }
        return player;
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

        for (Player p : currTeam) {
            p.results = PlayerGamesDbHelper.getPlayerLastGames(getSqLiteDatabase(context), p, countLastGames);
        }
    }

    public static Cursor getGames(Context context) {
        return GameDbHelper.getGames(getSqLiteDatabase(context));
    }

    public static void insertGame(Context context, int gameId, int score1, int score2) {
        GameDbHelper.insertGameResults(getSqLiteDatabase(context), gameId, score1, score2);
        PlayerGamesDbHelper.setPlayerGameResult(getSqLiteDatabase(context), gameId, TeamEnum.getResult(score1, score2));
    }

    public static String getNow() {
        return DateFormat.format("dd-MM-yyyy", System.currentTimeMillis()).toString();
    }

    public static void updateRecord(SQLiteDatabase db, ContentValues values, String where, String[] whereArgs, String tableName) {

        // Insert the new row, returning the primary key value of the new row
        db.updateWithOnConflict(tableName,
                values,
                where, whereArgs, SQLiteDatabase.CONFLICT_IGNORE);
    }
}
