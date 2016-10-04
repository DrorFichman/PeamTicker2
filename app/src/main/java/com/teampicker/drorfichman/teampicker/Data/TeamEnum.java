package com.teampicker.drorfichman.teampicker.Data;

/**
 * Created by drorfichman on 10/3/16.
 */
public enum TeamEnum {
    Team1,
    Team2,
    Tie;

    public static TeamEnum getResult(int team1Score, int team2Score) {
        if (team1Score > team2Score) {
            return Team1;
        } else if (team2Score > team1Score) {
            return Team2;
        } else {
            return Tie;
        }
    }

    public static ResultEnum getTeam2Result(TeamEnum team) {
        if (team == Team2) {
            return ResultEnum.Win;
        } else if (team == Team1) {
            return ResultEnum.Lose;
        } else if (team == Tie) {
            return ResultEnum.Tie;
        } else {
            return ResultEnum.NA;
        }
    }

    public static ResultEnum getTeam1Result(TeamEnum team) {
        if (team == Team1) {
            return ResultEnum.Win;
        } else if (team == Team2) {
            return ResultEnum.Lose;
        } else if (team == Tie) {
            return ResultEnum.Tie;
        } else {
            return ResultEnum.NA;
        }
    }
}
