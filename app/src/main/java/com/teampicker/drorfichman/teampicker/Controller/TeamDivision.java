package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by drorfichman on 9/16/16.
 */
public class TeamDivision {

    public static void dividePlayers(Context ctx,
                                     @NonNull List<Player> comingPlayers,
                                     @NonNull List<Player> resultPlayers1,
                                     @NonNull List<Player> resultPlayers2) {

        resultPlayers1.clear();
        resultPlayers2.clear();

        ArrayList<Player> players = cloneList(comingPlayers);
        Collections.sort(players);

        maxOptionsDivision(ctx, players, resultPlayers1, resultPlayers2);
    }

    public static ArrayList<Player> cloneList(List<Player> players) {
        ArrayList<Player> clone = new ArrayList<>(players.size());
        clone.addAll(players);
        return clone;
    }

    private static void maxOptionsDivision(Context ctx,
                                           @NonNull ArrayList<Player> comingPlayers,
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

        OptionalDivision selected = getOption(ctx, Others, GKs, Defenders, Playmakers, Divs);
        for (int option = 0; option < OPTIONS; ++option) {
            OptionalDivision another = getOption(ctx, Others, GKs, Defenders, Playmakers, Divs);
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

    static ArrayList<PreferenceAttributesHelper.PlayerAttribute> specials =
            new ArrayList<PreferenceAttributesHelper.PlayerAttribute>() {{
        add(PreferenceAttributesHelper.PlayerAttribute.isDefender);
        add(PreferenceAttributesHelper.PlayerAttribute.isGK);
    }};

    private static TeamData nextTeamToAdd(Context ctx,
                                          OptionalDivision option,
                                          PreferenceAttributesHelper.PlayerAttribute attribute) {

        int team1Players = option.players1.getCount(ctx, attribute);
        int team2Players = option.players2.getCount(ctx, attribute);
        if (team1Players > team2Players) {
            return option.players2;
        } else if (team2Players > team1Players) {
            return option.players1;
        } else {
            int team1Specials = option.players1.getCount(ctx, specials);
            int team2Specials = option.players2.getCount(ctx, specials);
            if (team1Specials > team2Specials) {
                return option.players2;
            } else {
                return option.players1;
            }
        }
    }

    private static OptionalDivision getOption(Context ctx,
                                              ArrayList<Player> iOthers,
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

        addSpecialPlayers(ctx, option, GKs, PreferenceAttributesHelper.PlayerAttribute.isGK);
        addSpecialPlayers(ctx, option, Defenders, PreferenceAttributesHelper.PlayerAttribute.isDefender);
        addSpecialPlayers(ctx, option, Playmakers, PreferenceAttributesHelper.PlayerAttribute.isPlaymaker);
        addSpecialPlayers(ctx, option, Divs, PreferenceAttributesHelper.PlayerAttribute.isUnbreakable);
        addSpecialPlayers(ctx, option, Others, null);

        return option;
    }

    private static void addSpecialPlayers(Context ctx, OptionalDivision option,
                                          ArrayList<Player> attributePlayers,
                                          PreferenceAttributesHelper.PlayerAttribute attribute) {
        Random r = new Random();
        while (attributePlayers.size() > 0) {
            int a = r.nextInt(attributePlayers.size());
            nextTeamToAdd(ctx, option, attribute).players.add(attributePlayers.get(a));
            attributePlayers.remove(a);
        }
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
