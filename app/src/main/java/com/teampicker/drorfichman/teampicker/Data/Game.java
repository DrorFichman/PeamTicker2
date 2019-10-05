package com.teampicker.drorfichman.teampicker.Data;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by drorfichman on 7/27/16.
 */
public class Game implements Serializable {

    public int gameId;
    public String dateString;
    public TeamEnum winningTeam;
    public int team1Score;
    public int team2Score;

    public ResultEnum playerResult;
    public int playerGrade;

    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY", Locale.getDefault());

    public Game(int gameId, String date) {
        this.gameId = gameId;
        this.dateString = date;
    }

    public String getScore() {
        return team1Score + " - " + team2Score;
    }

    public Date getDate() {
        try {
            return dateFormat.parse(this.dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
