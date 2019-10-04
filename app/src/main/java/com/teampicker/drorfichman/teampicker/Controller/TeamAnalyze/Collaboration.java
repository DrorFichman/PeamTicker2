package com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze;

import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.tools.MathTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Collaboration {
    public HashMap<String, PlayerCollaboration> players = new HashMap<>();

    public PlayerCollaboration getPlayer(String name) {
        return players.get(name);
    }

    /**
     * Gets team's overall historical success when playing with each other
     */
    public int getCollaborationWinRate(List<Player> players) {
        int winsAndLoses = 0;
        int wins = 0;
        for (Player p : players) {
            PlayerCollaboration player = getPlayer(p.mName);
            if (player != null) {
                winsAndLoses += player.overallCollaboratorsWinsAndLoses;
                wins += player.overallCollaboratorsWins;
            }
        }
        if (winsAndLoses > 0)
            return (wins * 100 / winsAndLoses);
        else
            return 0;
    }

    public int getExpectedWinRateStdDiv(List<Player> players) {
        ArrayList<Integer> diffs = new ArrayList<>();
        for (Player p : players) {
            PlayerCollaboration player = getPlayer(p.mName);
            if (player != null) {
                int expectedWinRate = player.getExpectedWinRate();
                if (expectedWinRate != -1) {
                    diffs.add(Math.abs(50 - expectedWinRate));
                }
            }
        }

        if (diffs.size() > 0) return (int) MathTools.getStdDevFromDiffs(diffs);
        else return -1;
    }
}
