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
public class PlayerDbHelper {

    private static final String SQL_CREATE_PLAYERS =
            "CREATE TABLE " + PlayerContract.PlayerEntry.TABLE_NAME + " (" +
                    PlayerContract.PlayerEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerContract.PlayerEntry.NAME + " TEXT, " +
                    PlayerContract.PlayerEntry.GRADE + " INTEGER, " +
                    PlayerContract.PlayerEntry.IS_COMING + " INTEGER )";

    public static String getSqlCreate() {
        return SQL_CREATE_PLAYERS;
    }

    public static
    @NonNull
    ArrayList<Player> getComingPlayers(SQLiteDatabase db) {

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

    public static void deletePlayer(SQLiteDatabase db, String name) {
        Log.d("DB", "delete player " + name);

        db.delete(PlayerContract.PlayerEntry.TABLE_NAME,
                PlayerContract.PlayerEntry.NAME + " = ? ",
                new String[]{name});
    }

    public static Cursor getPlayers(SQLiteDatabase db) {

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

    public static boolean insertPlayer(SQLiteDatabase db, String name, int grade) {

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

    public static void updatePlayerGrade(SQLiteDatabase db, String name, int grade) {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.NAME, name);
        values.put(PlayerContract.PlayerEntry.GRADE, grade);

        String where = PlayerContract.PlayerEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{name};

        // Insert the new row, returning the primary key value of the new row
        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerEntry.TABLE_NAME);
    }

    public static void updatePlayerComing(SQLiteDatabase db, String name, boolean coming) {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.NAME, name);
        values.put(PlayerContract.PlayerEntry.IS_COMING, coming ? 1 : 0);

        Log.d("DB", " updated " + name + " coming " + coming);

        String where = PlayerContract.PlayerEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{name};

        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerEntry.TABLE_NAME);
    }
}
