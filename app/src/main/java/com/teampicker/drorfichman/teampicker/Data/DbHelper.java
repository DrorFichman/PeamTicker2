package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
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

    private static final String SQL_CREATE_PLAYERS =
            "CREATE TABLE " + PlayerContract.PlayerEntry.TABLE_NAME + " (" +
                    PlayerContract.PlayerEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerContract.PlayerEntry.NAME + " TEXT, " +
                    PlayerContract.PlayerEntry.GRADE + " INTEGER, " +
                    PlayerContract.PlayerEntry.IS_COMING + " INTEGER )";

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

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlayerContract.PlayerEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Players.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PLAYERS);
        db.execSQL(SQL_CREATE_PLAYERS_GAMES);

        // TODO debug data
        insertPlayer(db, "דרור", 85);
        insertPlayer(db, "אורי", 91);
        insertPlayer(db, "נדב", 87);
        insertPlayer(db, "גלעד", 88);
        insertPlayer(db, "מתן", 85);
        insertPlayer(db, "שי", 60);
        insertPlayer(db, "גדי", 75);
        insertPlayer(db, "פלאיני", 50);
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

    private static void updateRecord(SQLiteDatabase db, ContentValues values, String where, String[] whereArgs) {
        // Insert the new row, returning the primary key value of the new row
        db.updateWithOnConflict(PlayerContract.PlayerEntry.TABLE_NAME,
                values,
                where, whereArgs, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void updatePlayerComing(Context context, String name, boolean coming) {

        SQLiteDatabase db = getSqLiteDatabase(context);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.NAME, name);
        values.put(PlayerContract.PlayerEntry.IS_COMING, coming ? 1 : 0);

        Log.d("DB", " updated " + name + " coming " + coming);

        String where = PlayerContract.PlayerEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{name};

        updateRecord(db, values, where, whereArgs);
    }

    public static void updatePlayer(Context context, String name, int grade) {
        SQLiteDatabase db = getSqLiteDatabase(context);


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.NAME, name);
        values.put(PlayerContract.PlayerEntry.GRADE, grade);

        String where = PlayerContract.PlayerEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{name};

        // Insert the new row, returning the primary key value of the new row
        updateRecord(db, values, where, whereArgs);
    }

    private static boolean insertPlayer(SQLiteDatabase db, String name, int grade) {

        String where = PlayerContract.PlayerEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{name};
        String[] projection = {
                PlayerContract.PlayerEntry.ID};
        Cursor c = db.query(
                PlayerContract.PlayerEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        int count = c.getCount();
        Log.d("DB", "Found " + count + " players with " + name);
        c.close();
        if (count > 0) {
            return false;
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.NAME, name);
        values.put(PlayerContract.PlayerEntry.GRADE, grade);

        // Insert the new row, returning the primary key value of the new row
        db.insert(PlayerContract.PlayerEntry.TABLE_NAME,
                null,
                values);

        return true;
    }

    public static boolean insertPlayer(Context context, String name, int grade) {

        return insertPlayer(getSqLiteDatabase(context), name, grade);
    }

    public static Cursor getPlayers(Context context) {

        SQLiteDatabase db = getSqLiteDatabase(context);

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.PlayerEntry.ID,
                PlayerContract.PlayerEntry.NAME,
                PlayerContract.PlayerEntry.GRADE,
                PlayerContract.PlayerEntry.IS_COMING
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.PlayerEntry.GRADE + " DESC";

        Cursor c = db.query(
                PlayerContract.PlayerEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();

        return c;
    }

    public static
    @NonNull
    ArrayList<Player> getComingPlayers(Context context) {

        SQLiteDatabase db = getSqLiteDatabase(context);

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.PlayerEntry.ID,
                PlayerContract.PlayerEntry.NAME,
                PlayerContract.PlayerEntry.GRADE,
                PlayerContract.PlayerEntry.IS_COMING
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.PlayerEntry.GRADE + " DESC";

        String where = PlayerContract.PlayerEntry.IS_COMING + " = ? ";
        String[] whereArgs = new String[]{"1"};

        Cursor c = db.query(
                PlayerContract.PlayerEntry.TABLE_NAME,  // The table to query
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
                    Player p = new Player(c.getString(c.getColumnIndex(PlayerContract.PlayerEntry.NAME)),
                            c.getInt(c.getColumnIndex(PlayerContract.PlayerEntry.GRADE)));
                    p.isComing = true;
                    players.add(p);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return players;
    }

    public static void deletePlayer(Context context, String name) {
        Log.d("DB", "delete player " + name);

        SQLiteDatabase db = getSqLiteDatabase(context);

        db.delete(PlayerContract.PlayerEntry.TABLE_NAME,
                PlayerContract.PlayerEntry.NAME + " = ? ",
                new String[]{name});
    }

    public static void clearOldGameTeams(Context context) {
        Log.d("teams", "Clear old Game teams ");
        SQLiteDatabase db = getSqLiteDatabase(context);

        int n = db.delete(PlayerContract.PlayerGameEntry.TABLE_NAME,
                PlayerContract.PlayerGameEntry.GAME_GRADE + " IS NULL ", null);

        Log.d("teams", "deleted games players " + n);
    }

    public static void insertPlayerGame(Context context, Player player, int currGame, PlayerContract.teamEnum team) {
        SQLiteDatabase db = getSqLiteDatabase(context);

        String now = DateFormat.format("dd-MM-yyyy", System.currentTimeMillis()).toString();

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

    public static ArrayList<Player> getCurrTeam(Context context, int currGame, PlayerContract.teamEnum team) {
        SQLiteDatabase db = getSqLiteDatabase(context);

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

    public static Cursor getGames(Context context) {

        SQLiteDatabase db = getSqLiteDatabase(context);

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
}
