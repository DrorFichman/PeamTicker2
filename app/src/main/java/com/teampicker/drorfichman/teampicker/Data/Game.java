package com.teampicker.drorfichman.teampicker.Data;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.tools.DateHelper;

import java.io.Serializable;

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

    public Game(int gameId, String date) {
        this.gameId = gameId;
        this.dateString = date;
    }

    public String getScore() {
        return team1Score + " - " + team2Score;
    }

    public String getDate(Context ctx) {
        return DateHelper.getDisplayDate(ctx, this.dateString);
    }
}
