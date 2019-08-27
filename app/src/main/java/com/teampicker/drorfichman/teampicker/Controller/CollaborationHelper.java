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

    private static final int RECENT_GAMES = 50;         // games back to look for
    private static final int MIN_GAMES_COUNT = 7;       // games for player to play over RECENT_GAMES to consider effect
    private static final int MIN_GAMES_TOGETHER = 5;    // games played with another player to consider effect
    private static final int WIN_RATE_MARGIN = 5;       // win rate exceeded by to count effect

    public enum EffectType {
        Positive,
        Negative,
        Equal,
        NoData
    }

    public static class Collaboration {
        public HashMap<String, PlayerCollaboration> players = new HashMap<>();

        public EffectType overallEffect() {
            return EffectType.NoData;
        }

        public PlayerCollaboration getPlayer(String name) {
            return players.get(name);
        }
    }

    public static class PlayerCollaboration {
        public String name;
        public int games;
        public int winRate;
        HashMap<String, EffectMargin> collaborators = new HashMap<>();

        int overallEffect = 0;

        void addCollaborator(String name, EffectMargin effectData) {
            collaborators.put(name, effectData);
            switch (effectData.effect) {
                case Positive: overallEffect++; break;
                case Negative: overallEffect--; break;
            }
        }

        public PlayerCollaboration(Player p) {
            name = p.mName;
            games =  p.statistics.gamesCount;
            winRate =  p.statistics.getWinRate();
        }

        public EffectMargin getCollaboratorEffect(String name) {
            return collaborators.get(name);
        }

        public EffectType getOverallEffect() {
            if (collaborators.size() == 0) {
                return EffectType.NoData;
            } else if (overallEffect == 0) {
                return EffectType.Equal;
            } else if (overallEffect > 0) {
                return EffectType.Positive;
            } else {
                return EffectType.Negative;
            }
        }
    }

    public static class EffectMargin {
        public String player;
        public String collaborator;
        public EffectType effect;
        public int games;
        public int winRate;
        public int winRateMargin;

        public EffectMargin(Player player, PlayerParticipation with) {
            this.player = player.mName;
            collaborator = with.mName;
            games = with.statisticsWith.gamesCount;
            winRate = with.statisticsWith.getWinRate();
            winRateMargin = with.statisticsWith.getWinRate() - player.statistics.getWinRate();

            if (with.statisticsWith.gamesCount < MIN_GAMES_TOGETHER) {
                effect = EffectType.NoData;
            } else if (winRateMargin > WIN_RATE_MARGIN) {
                effect = EffectType.Positive;
            } else if (winRateMargin < -WIN_RATE_MARGIN) {
                effect = EffectType.Negative;
            } else {
                effect = EffectType.Equal;
            }
        }
    }

    public static Collaboration predictWinner(Context context, ArrayList<Player> team1, ArrayList<Player> team2) {
        Collaboration result = new Collaboration();
        processTeam(context, result, team1);
        processTeam(context, result, team2);

        return result;
    }

    private static void processTeam(Context context, Collaboration result, ArrayList<Player> team) {

        for (Player p : team) {
            HashMap<String, PlayerParticipation> ps = DbHelper.getPlayersParticipationsStatistics(context, RECENT_GAMES, p.mName);
            PlayerCollaboration pcoll = new PlayerCollaboration(p);
            if (p.statistics.gamesCount >= MIN_GAMES_COUNT) {
                for (Player with : team) {
                    PlayerParticipation pp = ps.get(with.mName);
                    if (pp != null) {
                        EffectMargin effect = new EffectMargin(p, pp);
                        pcoll.addCollaborator(with.mName, effect);
                    }
                }
            }
            result.players.put(p.mName, pcoll);
        }
    }
}
