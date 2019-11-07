package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.Collaboration;
import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.CollaborationHelper;
import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.EffectMargin;
import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.PlayerCollaboration;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerTeamAnalysisAdapter extends ArrayAdapter<Player> {
    public static final int HIGH_WIN_RATE = 60;
    public static final int ALMOST_HIGH_WIN_RATE = 55;
    public static final int ALMOST_LOW_WIN_RATE = 45;
    public static final int LOW_WIN_RATE_DIFF = 40;
    public static final int WIN_RATE_DELTA = 2;
    public static final int HIGH_WIN_RATE_DIFF = 10;

    private Context context;
    private List<Player> mPlayers;
    private List<Player> mMovedPlayers;
    private List<Player> mMarkedPlayers;
    private String mSelectedPlayer;
    private Collaboration mCollaboration;

    public PlayerTeamAnalysisAdapter(Context ctx, List<Player> players,
                                     List<Player> coloredPlayers, List<Player> markedPlayers,
                                     @NonNull Collaboration collaboration, String selectedPlayer) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
        mMovedPlayers = coloredPlayers != null ? coloredPlayers : new ArrayList<>();
        mMarkedPlayers = markedPlayers != null ? markedPlayers : new ArrayList<>();
        mCollaboration = collaboration;
        mSelectedPlayer = selectedPlayer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.player_team_analysis_item, parent, false);

        Player player = mPlayers.get(position);

        TextView name = rowView.findViewById(R.id.player_team_name);
        setName(rowView, player, name);

        TextView games = rowView.findViewById(R.id.player_analysis_games);
        TextView winRate = rowView.findViewById(R.id.player_analysis_win_rate);
        TextView collaboration = rowView.findViewById(R.id.player_analysis_collaboration_win_rate);
        ImageView indicator = rowView.findViewById(R.id.player_analysis_indicator);

        setCollaborationAnalysis(player, games, winRate, collaboration);
        setSelectedPlayer(rowView, player);
        setColoredPlayers(rowView, player, indicator);

        return rowView;
    }

    private void setName(View rowView, Player player, TextView name) {
        name.setText(player.mName.substring(0, Math.min(6, player.mName.length())));

        if (mMarkedPlayers.contains(player)) {
            rowView.setAlpha(0.4F);
        } else if (mMovedPlayers.contains(player)) {
            rowView.setAlpha(0.4F);
        } else {
            rowView.setAlpha(1F);
        }
    }

    private void setSelectedPlayer(View rowView, Player player) {

        if (player.mName.equals(mSelectedPlayer)) {
            rowView.setBackgroundColor(Color.WHITE);
        }
    }

    private void setColoredPlayers(View rowView, Player player, ImageView indicator) {
        indicator.setVisibility(View.GONE);
        if (mSelectedPlayer == null) {
            PlayerCollaboration playerData = mCollaboration.getPlayer(player.mName);

            int games = playerData.games;
            int expectedWinRateDiff = playerData.getExpectedWinRateDiff();
            rowView.setBackgroundColor(Color.TRANSPARENT);

            if (games > CollaborationHelper.MIN_GAMES_ANALYSIS) {
                if (expectedWinRateDiff > 0) {
                    if (playerData.winRate > HIGH_WIN_RATE && expectedWinRateDiff > WIN_RATE_DELTA) {
                        // Show yellow high warning
                        rowView.setBackgroundColor(Color.GREEN);
                    } else if (playerData.winRate > ALMOST_HIGH_WIN_RATE && expectedWinRateDiff > WIN_RATE_DELTA) {
                        // Show high warning
                        indicator.setImageResource(R.drawable.increase_warn);
                        indicator.setVisibility(View.VISIBLE);
                    } else if (expectedWinRateDiff > HIGH_WIN_RATE_DIFF) {
                        // Show high effect
                        indicator.setImageResource(R.drawable.increase);
                        indicator.setVisibility(View.VISIBLE);
                    }
                } else if (expectedWinRateDiff < 0) {
                    if (playerData.winRate < LOW_WIN_RATE_DIFF && expectedWinRateDiff < -WIN_RATE_DELTA) {
                        // Show red high warning
                        rowView.setBackgroundColor(Color.RED);
                    } else if (playerData.winRate < ALMOST_LOW_WIN_RATE && expectedWinRateDiff < -WIN_RATE_DELTA) {
                        // Show low warning
                        indicator.setImageResource(R.drawable.decrease_warn);
                        indicator.setVisibility(View.VISIBLE);
                    } else if (expectedWinRateDiff < -HIGH_WIN_RATE_DIFF) {
                        // Show high effect
                        indicator.setImageResource(R.drawable.decrease);
                        indicator.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void setCollaborationAnalysis(Player player, TextView games, TextView winRate, TextView collaboration) {
        if (mSelectedPlayer != null) { // selected player mode

            PlayerCollaboration selectedPlayerData = mCollaboration.getPlayer(mSelectedPlayer);

            if (player.mName.equals(mSelectedPlayer)) { // selected player stats
                winRate.setText(context.getString(R.string.player_analysis_selected_win_rate, selectedPlayerData.winRate));
                games.setText(context.getString(R.string.player_analysis_selected_games, selectedPlayerData.games));
                collaboration.setText(selectedPlayerData.getExpectedWinRateString());
                collaboration.setTextColor(Color.BLACK);

            } else { // collaborator of selected player stats
                EffectMargin collaboratorEffect = selectedPlayerData.getEffect(player.mName);
                if (collaboratorEffect != null) {

                    winRate.setText(context.getString(R.string.player_analysis_selected_win_rate, mCollaboration.getPlayer(player.mName).winRate));
                    games.setText(context.getString(R.string.player_analysis_selected_games, collaboratorEffect.gamesWith));
                    collaboration.setText(context.getString(R.string.player_analysis_selected_win_rate, collaboratorEffect.winRateWith));
                    collaboration.setTextColor(collaboratorEffect.effect.color);
                }
            }

        } else { // non-selected player mode
            PlayerCollaboration data = mCollaboration.getPlayer(player.mName);

            winRate.setText(context.getString(R.string.player_analysis_selected_win_rate, data.winRate));
            games.setText(context.getString(R.string.player_analysis_selected_games, data.games));
            collaboration.setText(data.getExpectedWinRateString());
            collaboration.setTextColor(Color.BLACK);
        }
    }
}