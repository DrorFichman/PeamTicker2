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

        Collections.sort(comingPlayers);

        maxOptionsDivision(cloneList(comingPlayers), players1, players2);
    }

    public static ArrayList<Player> cloneList(List<Player> players) {
        ArrayList<Player> clone = new ArrayList<>(players.size());
        clone.addAll(players);
        return clone;
    }

    private static void maxOptionsDivision(@NonNull ArrayList<Player> comingPlayers,
                                           @NonNull List<Player> players1,
                                           @NonNull List<Player> players2) {

        int OPTIONS = 20;

        List<Player> GKs = new ArrayList<>();
        List<Player> Defenders = new ArrayList<>();
        List<Player> Playmakers = new ArrayList<>();
        List<Player> Others = new ArrayList<>();
        for (int curr = 0; curr < comingPlayers.size(); ++curr) {
            Player currPlayer = comingPlayers.get(curr);
            if (currPlayer.isGK) {
                GKs.add(currPlayer);
            } else if (currPlayer.isDefender) {
                Defenders.add(currPlayer);
            } else if (currPlayer.isPlaymaker) {
                Playmakers.add(currPlayer);
            } else {
                Others.add(currPlayer);
            }
        }

        Collections.sort(GKs);
        Collections.sort(Defenders);
        Collections.sort(Playmakers);
        Collections.sort(Others);

        Player extraPlayer = null;
        if (comingPlayers.size() % 2 == 1 && Others.size() > 0) {
            extraPlayer = Others.get(Others.size() - 1);
            Others.remove(extraPlayer);
        }

        OptionalDivision selected = getOption(cloneList(Others), cloneList(GKs), cloneList(Defenders), cloneList(Playmakers));
        for (int option = 0; option < OPTIONS; ++option) {
            OptionalDivision another = getOption(cloneList(Others), cloneList(GKs), cloneList(Defenders), cloneList(Playmakers));
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

    private static TeamData getNext(OptionalDivision option) {
        if (option.players1.getCount() > option.players2.getCount()) {
            return option.players2;
        } else {
            return option.players1;
        }
    }

    private static OptionalDivision getOption(ArrayList<Player> others,
                                              ArrayList<Player> GKs,
                                              ArrayList<Player> Defenders,
                                              ArrayList<Player> Playmakers) {
        OptionalDivision option = new OptionalDivision();

        Random r = new Random();

        while (GKs.size() > 0) {
            int a = r.nextInt(GKs.size());
            getNext(option).players.add(GKs.get(a));
            GKs.remove(a);
        }

        while (Defenders.size() > 0) {
            int a = r.nextInt(Defenders.size());
            getNext(option).players.add(Defenders.get(a));
            Defenders.remove(a);
        }

        while (Playmakers.size() > 0) {
            int a = r.nextInt(Playmakers.size());
            getNext(option).players.add(Playmakers.get(a));
            Playmakers.remove(a);
        }

        while (others.size() > 0) {
            int a = r.nextInt(others.size());
            getNext(option).players.add(others.get(a));
            others.remove(a);
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
