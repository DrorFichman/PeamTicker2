package com.teampicker.drorfichman.teampicker.Data;

import java.io.Serializable;

/**
 * Created by drorfichman on 7/27/16.
 */
public class Game implements Serializable {

    public int gameId;
    public String date;
    public TeamEnum result;
    public int team1Score;
    public int team2Score;

    public Game(int gameId, String date) {
        this.gameId = gameId;
        this.date = date;
    }
}
