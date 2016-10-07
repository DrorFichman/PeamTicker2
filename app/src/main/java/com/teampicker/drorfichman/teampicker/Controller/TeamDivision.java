package com.teampicker.drorfichman.teampicker.Controller;

import android.support.annotation.NonNull;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.OptionalDivision;
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by drorfichman on 9/16/16.
 */
public class TeamDivision {

    public static void dividePlayers(@NonNull List<Player> comingPlayers,
                                     @NonNull List<Player> players1,
                                     @NonNull List<Player> players2) {

        players1.clear();
        players2.clear();

        Collections.sort(comingPlayers, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                if (p1.mGrade > p2.mGrade) {
                    return -1;
                } else if (p1.mGrade == p2.mGrade) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        maxOptionsDivision(cloneList(comingPlayers), players1, players2);
    }

    private static ArrayList<Player> cloneList(List<Player> players) {
        ArrayList<Player> clone = new ArrayList<>(players.size());
        clone.addAll(players);
        return clone;
    }

    private static void maxOptionsDivision(@NonNull ArrayList<Player> comingPlayers,
                                           @NonNull List<Player> players1,
                                           @NonNull List<Player> players2) {

        int OPTIONS = 20;

        Player extraPlayer = null;
        if (comingPlayers.size() % 2 == 1) {
            extraPlayer = comingPlayers.get(comingPlayers.size() - 1);
            comingPlayers.remove(comingPlayers.size() - 1);
        }

        OptionalDivision selected = getOption(cloneList(comingPlayers));
        for (int option = 0; option < OPTIONS; ++option) {
            OptionalDivision another = getOption(cloneList(comingPlayers));
            if (another.score() < selected.score()) {
                selected = another;
            }
        }

        players1.addAll(selected.players1.players);
        players2.addAll(selected.players2.players);

        if (extraPlayer != null) {
            Log.d("teams", "adding extra player " + extraPlayer.mName);
            players1.add(extraPlayer);
        }
    }

    private static OptionalDivision getOption(List<Player> players) {
        OptionalDivision option = new OptionalDivision();

        Random r = new Random();

        while (players.size() > 0) {
            int a = r.nextInt(players.size());
            option.players1.players.add(players.get(a));
            players.remove(a);
            int b = r.nextInt(players.size());
            option.players2.players.add(players.get(b));
            players.remove(b);
        }

        return option;
    }

    private static void simpleDivision(@NonNull List<Player> comingPlayers,
                                       @NonNull List<Player> players1,
                                       @NonNull List<Player> players2) {

        for (Player currPlayer : comingPlayers) {
            if (players1.size() == players2.size()) {
                players1.add(currPlayer);
            } else {
                players2.add(currPlayer);
            }
        }
    }
}
