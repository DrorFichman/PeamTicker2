package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by drorfichman on 9/16/16.
 */
public class TeamDivision {

    public static void dividePlayers(@NonNull List<Player> comingPlayers,
                                     @NonNull List<Player> resultPlayers1,
                                     @NonNull List<Player> resultPlayers2) {

        resultPlayers1.clear();
        resultPlayers2.clear();

        ArrayList<Player> players = cloneList(comingPlayers);
        Collections.sort(players);

        maxOptionsDivision(players, resultPlayers1, resultPlayers2);
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

        ArrayList<Player> GKs = new ArrayList<>();
        ArrayList<Player> Defenders = new ArrayList<>();
        ArrayList<Player> Divs = new ArrayList<>();
        ArrayList<Player> Playmakers = new ArrayList<>();
        ArrayList<Player> Others = new ArrayList<>();
        extractSpecialPlayers(comingPlayers, GKs, Defenders, Divs, Playmakers, Others);

        Player extraPlayer = null;
        if (comingPlayers.size() % 2 == 1 && Others.size() > 0) {
            extraPlayer = Others.get(Others.size() - 1);
            Others.remove(extraPlayer);
        }

        OptionalDivision selected = getOption(Others, GKs, Defenders, Playmakers, Divs);
        for (int option = 0; option < OPTIONS; ++option) {
            OptionalDivision another = getOption(Others, GKs, Defenders, Playmakers, Divs);
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

    private static void extractSpecialPlayers(@NonNull ArrayList<Player> comingPlayers,
                                              List<Player> GKs,
                                              List<Player> defenders,
                                              List<Player> divs,
                                              List<Player> playmakers,
                                              List<Player> others) {

        for (int n = 0; n < comingPlayers.size(); ++n) {
            Player currPlayer = comingPlayers.get(n);
            if (currPlayer.isGK) {
                GKs.add(currPlayer);
            } else if (currPlayer.isDefender) {
                defenders.add(currPlayer);
            } else if (currPlayer.isUnbreakable) {
                divs.add(currPlayer);
            } else if (currPlayer.isPlaymaker) {
                playmakers.add(currPlayer);
            } else {
                others.add(currPlayer);
            }
        }


        Collections.sort(GKs);
        Collections.sort(defenders);
        Collections.sort(divs);
        Collections.sort(playmakers);
        Collections.sort(others);
    }

    static ArrayList<PlayerAttribute> specials =
            new ArrayList<PlayerAttribute>() {{
        add(PlayerAttribute.isDefender);
        add(PlayerAttribute.isGK);
    }};

    private static TeamData nextTeamToAdd(OptionalDivision option,
                                          PlayerAttribute attribute) {

        int team1Players = option.players1.getCount(attribute);
        int team2Players = option.players2.getCount(attribute);
        if (team1Players > team2Players) { // more players of this type
            return option.players2;
        } else if (team2Players > team1Players) {
            return option.players1;
        } else {
            int team1Specials = option.players1.getCount(specials);
            int team2Specials = option.players2.getCount(specials);
            if (team1Specials > team2Specials) { // more special players
                return option.players2;
            } else if (team2Specials > team1Specials) {
                return option.players1;
            } else { // randomly add
                if (getRandom(new Random(), 2) == 0) {
                    return option.players2;
                } else {
                    return option.players1;
                }
            }
        }
    }

    private static OptionalDivision getOption(ArrayList<Player> iOthers,
                                              ArrayList<Player> iGKs,
                                              ArrayList<Player> iDefenders,
                                              ArrayList<Player> iPlaymakers,
                                              ArrayList<Player> iDivs) {
        OptionalDivision option = new OptionalDivision();
        ArrayList<Player> Others = cloneList(iOthers);
        ArrayList<Player> Divs = cloneList(iDivs);
        ArrayList<Player> GKs = cloneList(iGKs);
        ArrayList<Player> Defenders = cloneList(iDefenders);
        ArrayList<Player> Playmakers = cloneList(iPlaymakers);

        addSpecialPlayers(option, GKs, PlayerAttribute.isGK);
        addSpecialPlayers(option, Defenders, PlayerAttribute.isDefender);
        addSpecialPlayers(option, Playmakers, PlayerAttribute.isPlaymaker);
        addSpecialPlayers(option, Divs, PlayerAttribute.isUnbreakable);
        addSpecialPlayers(option, Others, null);

        return option;
    }

    private static void addSpecialPlayers(OptionalDivision option,
                                          ArrayList<Player> attributePlayers,
                                          PlayerAttribute attribute) {
        Random r = new Random();
        while (attributePlayers.size() > 0) {
            int a = getRandom(r, attributePlayers.size());
            nextTeamToAdd(option, attribute).players.add(attributePlayers.get(a));
            attributePlayers.remove(a);
        }
    }

    private static int getRandom(Random r, int value) {
        return r.nextInt(value);
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
