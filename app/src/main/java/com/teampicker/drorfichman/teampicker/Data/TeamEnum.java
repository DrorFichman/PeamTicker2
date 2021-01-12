package com.teampicker.drorfichman.teampicker.Data;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.ColorHelper;

import androidx.annotation.DrawableRes;

/**
 * Created by drorfichman on 10/3/16.
 */
public enum TeamEnum {
    Team1(0, -1),
    Team2(1, -1),
    Tie(-1, R.drawable.circle_draw);

    private int drawable;
    private int drawableIndex;

    TeamEnum(int index, int draw) {
        drawableIndex = index;
        drawable = draw;
    }

    @DrawableRes
    public int getDrawable(Context ctx) {
        if (drawableIndex == 0 || drawableIndex == 1) {
            int[] colors = ColorHelper.getTeamsIcons(ctx);
            return colors[drawableIndex];
        } else {
            return drawable;
        }
    }

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

    public static ResultEnum getTeamResultInGame(Game game, int team) {
        if (game.winningTeam == Tie) {
            return ResultEnum.Tie;
        } else if (game.winningTeam == Team1) {
            return team == Team1.ordinal() ? ResultEnum.Win : ResultEnum.Lose;
        } else if (game.winningTeam == Team2) {
            return team == Team2.ordinal() ? ResultEnum.Win : ResultEnum.Lose;
        }
        return ResultEnum.Missed;
    }
}
