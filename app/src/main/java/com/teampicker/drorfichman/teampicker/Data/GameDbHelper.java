package com.teampicker.drorfichman.teampicker.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by drorfichman on 10/3/16.
 */
public class GameDbHelper {

    private static final String SQL_CREATE_GAMES =
            "CREATE TABLE " + PlayerContract.GameEntry.TABLE_NAME + " (" +
                    PlayerContract.GameEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerContract.GameEntry.GAME + " INTEGER, " +
                    PlayerContract.GameEntry.DATE + " TEXT, " +
                    PlayerContract.GameEntry.RESULT + " INTEGER, " +
                    PlayerContract.GameEntry.TEAM1_SCORE + " INTEGER, " +
                    PlayerContract.GameEntry.TEAM2_SCORE + " INTEGER )";

    public static String getSqlCreate() {
        return SQL_CREATE_GAMES;
    }

    public static void insertGameResults(SQLiteDatabase db, int gameId,
                                    int team1Score, int team2Score) {

        ContentValues values = new ContentValues();
        values.put(PlayerContract.GameEntry.GAME, gameId);
        values.put(PlayerContract.GameEntry.DATE, DbHelper.getNow());
        values.put(PlayerContract.GameEntry.TEAM1_SCORE, team1Score);
        values.put(PlayerContract.GameEntry.TEAM2_SCORE, team2Score);
        values.put(PlayerContract.GameEntry.RESULT, TeamEnum.getResult(team1Score, team2Score).ordinal());

        // Insert the new row, returning the primary key value of the new row
        db.insert(PlayerContract.PlayerGameEntry.TABLE_NAME,
                null,
                values);
    }
}
