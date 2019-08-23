package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.PreferenceAttributesHelper;

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
                    PlayerContract.PlayerEntry.IS_COMING + " INTEGER, " +
                    PlayerContract.PlayerEntry.BIRTH_YEAR + " INTEGER, " +
                    PlayerContract.PlayerEntry.BIRTH_MONTH + " INTEGER " +
                    " )";

    public static final String SQL_DROP_PLAYER_TABLE =
            "DELETE FROM " + PlayerContract.PlayerEntry.TABLE_NAME;

    public static String getSqlCreate() {
        return SQL_CREATE_PLAYERS;
    }

    public static int getComingPlayersCount(SQLiteDatabase db) {
        String[] projection = {
                PlayerContract.PlayerEntry.ID,
                PlayerContract.PlayerEntry.IS_COMING
        };

        String where = PlayerContract.PlayerEntry.IS_COMING + " = ? AND " +
                PlayerContract.PlayerEntry.ARCHIVED + " = ? ";
        String[] whereArgs = new String[]{"1", "0"}; // coming and not archived

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
        c.close();

        return count;
    }

    public static
    @NonNull
    ArrayList<Player> getComingPlayers(Context context, SQLiteDatabase db) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.PlayerEntry.ID,
                PlayerContract.PlayerEntry.NAME,
                PlayerContract.PlayerEntry.GRADE,
                PlayerContract.PlayerEntry.BIRTH_YEAR,
                PlayerContract.PlayerEntry.BIRTH_MONTH,
                PlayerContract.PlayerEntry.IS_COMING
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.PlayerEntry.GRADE + " DESC";

        String where = PlayerContract.PlayerEntry.IS_COMING + " = ? AND " +
                PlayerContract.PlayerEntry.ARCHIVED + " = ? ";
        String[] whereArgs = new String[]{"1", "0"}; // coming and not archived

        Cursor c = db.query(
                PlayerContract.PlayerEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return getPlayers(c, context);
    }

    @NonNull
    private static ArrayList<Player> getPlayers(Cursor c, Context ctx) {

        ArrayList<Player> players = new ArrayList<>();
        try {
            if (c.moveToFirst()) {
                do {
                    Player p = createPlayerFromCursor(c, ctx,
                            PlayerContract.PlayerEntry.NAME,
                            PlayerContract.PlayerEntry.BIRTH_YEAR,
                            PlayerContract.PlayerEntry.BIRTH_MONTH,
                            PlayerContract.PlayerEntry.GRADE,
                            PlayerContract.PlayerEntry.IS_COMING);
                    players.add(p);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return players;
    }

    @NonNull
    public static Player createPlayerFromCursor(Cursor c, Context ctx,
                                                String player_name,
                                                String year,
                                                String month,
                                                String player_grade,
                                                String is_coming) {

        Player p = new Player(c.getString(c.getColumnIndex(player_name)), c.getInt(c.getColumnIndex(player_grade)));
        p.isComing = (is_coming != null) ? c.getInt(c.getColumnIndex(is_coming)) == 1 : true;
        p.mBirthYear = (year != null && c.getColumnIndex(year) > 0) ? c.getInt(c.getColumnIndex(year)) : 0;
        p.mBirthMonth = (month != null && c.getColumnIndex(month) > 0) ? c.getInt(c.getColumnIndex(month)) : 0;
        p.isGK = isAttribute(ctx, p.mName, PreferenceAttributesHelper.PlayerAttribute.isGK);
        p.isDefender = isAttribute(ctx, p.mName, PreferenceAttributesHelper.PlayerAttribute.isDefender);
        p.isPlaymaker = isAttribute(ctx, p.mName, PreferenceAttributesHelper.PlayerAttribute.isPlaymaker);
        p.isBreakable = isAttribute(ctx, p.mName, PreferenceAttributesHelper.PlayerAttribute.isBreakable);
        return p;
    }

    public static boolean isAttribute(Context ctx, String playerName, PreferenceAttributesHelper.PlayerAttribute attribute) {
        return PreferenceAttributesHelper.getPlayerPreferences(ctx, playerName, attribute);
    }

    public static void setAttribute(Context ctx, String playerName,
                                       PreferenceAttributesHelper.PlayerAttribute attribute, boolean set) {
        PreferenceAttributesHelper.setPlayerPreferences(ctx, playerName, attribute, set);
    }

    public static void deletePlayer(SQLiteDatabase db, String name) {
        Log.d("DB", "delete player " + name);

        db.delete(PlayerContract.PlayerEntry.TABLE_NAME,
                PlayerContract.PlayerEntry.NAME + " = ? ",
                new String[]{name});
    }

    public static void archivePlayer(SQLiteDatabase db, String name, boolean archive) {
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.ARCHIVED, archive ? 1 : 0);

        updatePlayer(db, name, values);
    }

    @NonNull
    public static ArrayList<Player> getPlayers(Context context, SQLiteDatabase db, boolean showArchived) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PlayerContract.PlayerEntry.ID,
                PlayerContract.PlayerEntry.NAME,
                PlayerContract.PlayerEntry.GRADE,
                PlayerContract.PlayerEntry.BIRTH_YEAR,
                PlayerContract.PlayerEntry.BIRTH_MONTH,
                PlayerContract.PlayerEntry.IS_COMING
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlayerContract.PlayerEntry.GRADE + " DESC";
        String where = PlayerContract.PlayerEntry.ARCHIVED + " = ? ";
        String[] selectionArgs = {showArchived ? "1" : "0"};

        Cursor c = db.query(
                PlayerContract.PlayerEntry.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                where,                                    // where
                selectionArgs,                        // where values
                null,                            // don't group the rows
                null,                             // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return getPlayers(c, context);
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

    public static void updatePlayerComing(SQLiteDatabase db, String name, boolean coming) {
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.IS_COMING, coming ? 1 : 0);

        updatePlayer(db, name, values);
    }

    public static void updatePlayerBirth(SQLiteDatabase db, String name, int year, int month) {
        ContentValues values = new ContentValues();
        if (year > 0) values.put(PlayerContract.PlayerEntry.BIRTH_YEAR, year);
        if (month > 0) values.put(PlayerContract.PlayerEntry.BIRTH_MONTH, month);

        updatePlayer(db, name, values);
    }

    public static void updatePlayerGrade(SQLiteDatabase db, String name, int grade) {
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.GRADE, grade);

        updatePlayer(db, name, values);
    }

    public static void updatePlayerName(SQLiteDatabase db, String currentName, String newName) {
        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.NAME, newName);
        updatePlayer(db, currentName, values);

        String where = PlayerContract.PlayerGameEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{currentName};
        ContentValues values2 = new ContentValues();
        values2.put(PlayerContract.PlayerGameEntry.NAME, newName);
        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerGameEntry.TABLE_NAME);
    }

    private static void updatePlayer(SQLiteDatabase db, String name, ContentValues values) {
        String where = PlayerContract.PlayerEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{name};

        DbHelper.updateRecord(db, values, where, whereArgs, PlayerContract.PlayerEntry.TABLE_NAME);
    }

    public static Player getPlayer(Context ctx, SQLiteDatabase db, String name) {

        String[] projection = {
                PlayerContract.PlayerEntry.ID,
                PlayerContract.PlayerEntry.NAME,
                PlayerContract.PlayerEntry.GRADE,
                PlayerContract.PlayerEntry.IS_COMING,
                PlayerContract.PlayerEntry.BIRTH_YEAR,
                PlayerContract.PlayerEntry.BIRTH_MONTH
        };

        String where = PlayerContract.PlayerEntry.NAME + " = ? ";
        String[] whereArgs = new String[]{name};

        Cursor c = db.query(
                PlayerContract.PlayerEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );


        try {
            if (c.moveToFirst()) {
                return createPlayerFromCursor(c, ctx,
                        PlayerContract.PlayerEntry.NAME,
                        PlayerContract.PlayerEntry.BIRTH_YEAR,
                        PlayerContract.PlayerEntry.BIRTH_MONTH,
                        PlayerContract.PlayerEntry.GRADE,
                        PlayerContract.PlayerEntry.IS_COMING);
            }
        } finally {
            c.close();
        }

        Log.e("TEAMS", "player " + name + " is missing from the DB");
        return null;
    }

    public static void clearAllComing(SQLiteDatabase db) {

        ContentValues values = new ContentValues();
        values.put(PlayerContract.PlayerEntry.IS_COMING, 0);

        DbHelper.updateRecord(db, values, null, null, PlayerContract.PlayerEntry.TABLE_NAME);
    }
}
