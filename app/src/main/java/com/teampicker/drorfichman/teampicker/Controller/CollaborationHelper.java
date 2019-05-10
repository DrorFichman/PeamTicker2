package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by drorfichman on 9/17/16.
 */
public class CollaborationHelper {

    public static int predictWinner(Context context, ArrayList<Player> team1, ArrayList<Player> team2) {
        HashMap<String, PlayerParticipation> result =
                DbHelper.getPlayersParticipationsStatistics(context, 50, "דרור");
        ArrayList<PlayerParticipation> players = new ArrayList<>();
        players.addAll(result.values());

        return 0;
    }

    private static int calculateTeamCollaboration(ArrayList<Player> team) {
        return 0;
    }

    private static int calculateTeamAgainst(ArrayList<Player> team, ArrayList<Player> against) {
        return 0;
    }

    private int calculatePlayerCollaboration(Player player, ArrayList<Player> team) {
        return 0;
    }

    private int calculatePlayerAgainst(Player player, ArrayList<Player> against) {
        return 0;
    }
}
