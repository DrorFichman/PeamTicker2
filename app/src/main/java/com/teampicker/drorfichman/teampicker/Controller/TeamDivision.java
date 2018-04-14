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
                                     @NonNull List<Player> players1,
                                     @NonNull List<Player> players2) {

        players1.clear();
        players2.clear();

        Collections.sort(comingPlayers);

        maxOptionsDivision(ctx, cloneList(comingPlayers), players1, players2);
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

        OptionalDivision selected = getOption(ctx, cloneList(Others), cloneList(GKs), cloneList(Defenders), cloneList(Playmakers));
        for (int option = 0; option < OPTIONS; ++option) {
            OptionalDivision another = getOption(ctx, cloneList(Others), cloneList(GKs), cloneList(Defenders), cloneList(Playmakers));
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

    static ArrayList<PreferenceAttributesHelper.PlayerAttribute> specials =
            new ArrayList<PreferenceAttributesHelper.PlayerAttribute>() {{
        add(PreferenceAttributesHelper.PlayerAttribute.isDefender);
        add(PreferenceAttributesHelper.PlayerAttribute.isGK);
    }};

    private static TeamData getNextTeam(Context ctx,
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
                                              ArrayList<Player> others,
                                              ArrayList<Player> GKs,
                                              ArrayList<Player> Defenders,
                                              ArrayList<Player> Playmakers) {
        OptionalDivision option = new OptionalDivision();

        Random r = new Random();

        while (GKs.size() > 0) {
            int a = r.nextInt(GKs.size());
            getNextTeam(ctx, option, PreferenceAttributesHelper.PlayerAttribute.isGK).players.add(GKs.get(a));
            GKs.remove(a);
        }

        while (Defenders.size() > 0) {
            int a = r.nextInt(Defenders.size());
            getNextTeam(ctx, option, PreferenceAttributesHelper.PlayerAttribute.isDefender).players.add(Defenders.get(a));
            Defenders.remove(a);
        }

        while (Playmakers.size() > 0) {
            int a = r.nextInt(Playmakers.size());
            getNextTeam(ctx, option, PreferenceAttributesHelper.PlayerAttribute.isPlaymaker).players.add(Playmakers.get(a));
            Playmakers.remove(a);
        }

        while (others.size() > 0) {
            int a = r.nextInt(others.size());
            getNextTeam(ctx, option, null).players.add(others.get(a));
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
