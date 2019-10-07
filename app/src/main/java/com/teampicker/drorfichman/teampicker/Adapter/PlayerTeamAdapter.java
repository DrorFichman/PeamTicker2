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
import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.PlayerCollaboration;
import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.EffectMargin;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerTeamAdapter extends ArrayAdapter<Player> {
    public static final int HIGH_WIN_RATE = 60;
    public static final int WIN_RATE_DELTA = 2;
    public static final int ALMOST_HIGH_WIN_RATE = 55;
    public static final int HIGH_WIN_RATE_DIFF = 10;
    public static final int LOW_WIN_RATE_DIFF = 40;
    public static final int ALMOST_LOW_WIN_RATE = 45;

    private Context context;
    private List<Player> mPlayers;
    private List<Player> mMovedPlayers;
    private List<Player> mMarkedPlayers;
    private String mSelectedPlayer;
    private Collaboration mCollaboration;

    private boolean isAttributesVisible;
    private boolean isGameHistoryVisible;
    private boolean isGradeVisible;

    public PlayerTeamAdapter(Context ctx, List<Player> players,
                             List<Player> coloredPlayers, List<Player> markedPlayers,
                             Collaboration collaboration, String selectedPlayer,
                             boolean showInternalData) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
        mMovedPlayers = coloredPlayers != null ? coloredPlayers : new ArrayList<>();
        mMarkedPlayers = markedPlayers != null ? markedPlayers : new ArrayList<>();
        mCollaboration = collaboration;
        mSelectedPlayer = selectedPlayer;

        isGradeVisible = showInternalData;
        isAttributesVisible = showInternalData && mCollaboration == null;
        isGameHistoryVisible = mCollaboration == null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.player_team_item, parent, false);

        Player player = mPlayers.get(position);

        TextView name = rowView.findViewById(R.id.player_team_name);
        setName(rowView, player, name);

        setAttributes(rowView, player);

        setGamesHistory(rowView, player);

        TextView grade = rowView.findViewById(R.id.player_team_grade);
        setGrade(player, grade);

        TextView analysis = rowView.findViewById(R.id.player_analysis);
        ImageView suggestion = rowView.findViewById(R.id.player_analysis_suggestion);
        setCollaborationAnalysis(rowView, player, analysis);
        setSelectedPlayer(rowView, player);
        setColoredPlayers(rowView, suggestion, player);

        return rowView;
    }

    private void setName(View rowView, Player player, TextView name) {
        if (mCollaboration != null) {
            name.setText(player.mName.substring(0, Math.min(6, player.mName.length())));
        } else {
            name.setText(player.mName);
        }

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

    private void setColoredPlayers(View rowView, ImageView suggestion, Player player) {
        suggestion.setVisibility(View.GONE);
        if (mSelectedPlayer == null && mCollaboration != null) {
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
                        suggestion.setImageResource(R.drawable.increase_warn);
                        suggestion.setVisibility(View.VISIBLE);
                    } else if (expectedWinRateDiff > HIGH_WIN_RATE_DIFF) {
                        // Show high effect
                        suggestion.setImageResource(R.drawable.increase);
                        suggestion.setVisibility(View.VISIBLE);
                    }
                } else if (expectedWinRateDiff < 0) {
                    if (playerData.winRate < LOW_WIN_RATE_DIFF && expectedWinRateDiff < WIN_RATE_DELTA) {
                        // Show red high warning
                        rowView.setBackgroundColor(Color.RED);
                    } else if (playerData.winRate < ALMOST_LOW_WIN_RATE && expectedWinRateDiff < -WIN_RATE_DELTA) {
                        // Show low warning
                        suggestion.setImageResource(R.drawable.decrease_warn);
                        suggestion.setVisibility(View.VISIBLE);
                    } else if (expectedWinRateDiff < -HIGH_WIN_RATE_DIFF) {
                        // Show high effect
                        suggestion.setImageResource(R.drawable.decrease);
                        suggestion.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void setAttributes(View rowView, Player player) {
        rowView.findViewById(R.id.player_gk).setVisibility(isAttributesVisible && player.isGK ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_d).setVisibility(isAttributesVisible && player.isDefender ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_pm).setVisibility(isAttributesVisible && player.isPlaymaker ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_breaking).setVisibility(isAttributesVisible && player.isUnbreakable ? View.VISIBLE : View.GONE);
    }

    private void setGrade(Player player, TextView grade) {
        if (isGradeVisible) {
            grade.setText(String.valueOf(player.mGrade));
            grade.setVisibility(View.VISIBLE);
        } else {
            grade.setVisibility(View.GONE);
        }
    }

    private void setGamesHistory(View rowView, Player player) {
        ArrayList<ImageView> starView = new ArrayList();
        starView.add(rowView.findViewById(R.id.res_1));
        starView.add(rowView.findViewById(R.id.res_2));
        starView.add(rowView.findViewById(R.id.res_3));
        starView.add(rowView.findViewById(R.id.res_4));
        starView.add(rowView.findViewById(R.id.res_5));

        for (ImageView im : starView) {
            im.setVisibility(View.INVISIBLE);
        }

        if (isGameHistoryVisible) {
            for (int r = 0; r < player.results.size() && r < starView.size(); ++r) {
                ResultEnum res = player.results.get(r).result;
                if (res == ResultEnum.Win) {
                    starView.get(r).setImageResource(R.drawable.circle_win);
                } else if (res == ResultEnum.Lose) {
                    starView.get(r).setImageResource(R.drawable.circle_lose);
                } else if (res == ResultEnum.Tie) {
                    starView.get(r).setImageResource(R.drawable.circle_draw);
                } else if (res == ResultEnum.Missed) {
                    starView.get(r).setImageResource(R.drawable.circle_na);
                }
                starView.get(r).setVisibility(View.VISIBLE);
            }
        }
    }

    private void setCollaborationAnalysis(View rowView, Player player, TextView analysis) {
        if (mSelectedPlayer != null && mCollaboration != null) { // selected player view
            String stats = "";

            PlayerCollaboration selectedPlayerData = mCollaboration.getPlayer(mSelectedPlayer);
            if (player.mName.equals(mSelectedPlayer)) { // selected player stats
                stats = context.getString(R.string.player_analysis_selected,
                        selectedPlayerData.winRate,
                        selectedPlayerData.games,
                        selectedPlayerData.getExpectedWinRateString());
            } else { // collaborator of selected player stats
                EffectMargin collaboratorEffect = selectedPlayerData.getEffect(player.mName);
                if (collaboratorEffect != null) {
                    stats = context.getString(R.string.player_analysis_collaborator,
                            collaboratorEffect.winRateWith,
                            collaboratorEffect.gamesWith,
                            String.valueOf(collaboratorEffect.getSuccessWithString()));
                    switch (collaboratorEffect.effect) {
                        case Positive:
                            rowView.setBackgroundColor(Color.YELLOW);
                            break;
                        case Negative:
                            rowView.setBackgroundColor(Color.RED);
                            break;
                        case NotEnoughData:
                            rowView.setBackgroundColor(Color.TRANSPARENT);
                            break;
                        default:
                            rowView.setBackgroundColor(Color.GRAY);
                            break;
                    }
                }
            }

            analysis.setVisibility(View.VISIBLE);
            analysis.setText(stats);
        } else if (mCollaboration != null) { // non-selected player view
            PlayerCollaboration data = mCollaboration.getPlayer(player.mName);
            String stats = context.getString(R.string.player_analysis,
                    data.winRate,
                    data.games,
                    data.getExpectedWinRateString());
            analysis.setVisibility(View.VISIBLE);
            analysis.setText(stats);
        } else { // not analysis mode
            analysis.setVisibility(View.GONE);
        }
    }
}