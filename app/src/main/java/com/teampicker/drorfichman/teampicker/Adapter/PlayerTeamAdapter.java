package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Controller.CollaborationHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerTeamAdapter extends ArrayAdapter<Player> {
    static final int SUCCESS_DIFF_ISSUE = 5;
    static final int SUCCESS_DIFF_EXCESSIVE = 10;
    static final int LOW_WIN_RATE = 40;
    static final int HIGH_WIN_RATE = 60;

    private Context context;
    private List<Player> mPlayers;
    private List<Player> mMovedPlayers;
    private List<Player> mMarkedPlayers;
    private List<String> mHighPlayers = new ArrayList<>();
    private List<String> mLowPlayers = new ArrayList<>();
    private String mSelectedPlayer;
    private CollaborationHelper.Collaboration mCollaboration;

    boolean isAttributesVisible;
    boolean isGameHistoryVisible;
    boolean isGradeVisible;

    public PlayerTeamAdapter(Context ctx, List<Player> players,
                             List<Player> coloredPlayers, List<Player> markedPlayers,
                             CollaborationHelper.Collaboration collaboration, String selectedPlayer,
                             boolean showInternalData) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
        mMovedPlayers = coloredPlayers != null ? coloredPlayers : new ArrayList<Player>();
        mMarkedPlayers = markedPlayers != null ? markedPlayers : new ArrayList<Player>();
        mSelectedPlayer = selectedPlayer;

        initCollaboration(collaboration);

        isGradeVisible = showInternalData;
        isAttributesVisible = showInternalData && mCollaboration == null;
        isGameHistoryVisible = mCollaboration == null;
    }

    private void initCollaboration(CollaborationHelper.Collaboration collaboration) {
        if (collaboration != null) {
            mCollaboration = collaboration;
            for (CollaborationHelper.PlayerCollaboration effect : collaboration.players.values()) {

                if (effect.games > CollaborationHelper.MIN_GAMES_ANALYSIS) {
                    if (effect.winRate > HIGH_WIN_RATE) {
                        mHighPlayers.add(effect.name);
                    } else if (effect.winRate < LOW_WIN_RATE) {
                        mLowPlayers.add(effect.name);
                    }
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.player_team_item, parent, false);

        Player player = mPlayers.get(position);

        TextView name = (TextView) rowView.findViewById(R.id.player_team_name);
        setName(player, name);

        setAttributes(rowView, player);

        setGamesHistory(rowView, player);

        TextView grade = (TextView) rowView.findViewById(R.id.player_team_grade);
        setGrade(player, grade);

        TextView playerMarker = (TextView) rowView.findViewById(R.id.player_moved_marker);
        playerMarker.setVisibility(mMovedPlayers.contains(player) ? View.VISIBLE : View.GONE);

        TextView analysis = (TextView) rowView.findViewById(R.id.player_analysis);
        ImageView suggestion = (ImageView) rowView.findViewById(R.id.player_analysis_suggestion);
        setCollaborationAnalysis(rowView, player, analysis);
        setSelectedPlayer(rowView, player);
        setColoredPlayers(rowView, suggestion, player);

        return rowView;
    }

    private void setName(Player player, TextView name) {
        if (mCollaboration != null) {
            name.setText(player.mName.substring(0, Math.min(5, player.mName.length())));
        } else {
            name.setText(player.mName + (mMarkedPlayers.contains(player) ? " **" : ""));
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
            int successDiff = mCollaboration.getPlayer(player.mName).getSuccessDiff();
            if (mHighPlayers.contains(player.mName) && successDiff > SUCCESS_DIFF_ISSUE) {
                rowView.setBackgroundColor(Color.YELLOW);
            } else if (successDiff > SUCCESS_DIFF_EXCESSIVE) {
                suggestion.setImageResource(R.drawable.increase);
                suggestion.setVisibility(View.VISIBLE);
            } else if (mLowPlayers.contains(player.mName) && successDiff < -SUCCESS_DIFF_ISSUE) {
                rowView.setBackgroundColor(Color.RED);
            } else if (successDiff < -SUCCESS_DIFF_EXCESSIVE) {
                suggestion.setImageResource(R.drawable.decrease);
                suggestion.setVisibility(View.VISIBLE);
            } else {
                rowView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private void setAttributes(View rowView, Player player) {
        rowView.findViewById(R.id.player_gk).setVisibility(isAttributesVisible && player.isGK ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_d).setVisibility(isAttributesVisible && player.isDefender ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_pm).setVisibility(isAttributesVisible && player.isPlaymaker ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_breaking).setVisibility(isAttributesVisible && player.isBreakable ? View.VISIBLE : View.GONE);
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
        starView.add((ImageView) rowView.findViewById(R.id.res_1));
        starView.add((ImageView) rowView.findViewById(R.id.res_2));
        starView.add((ImageView) rowView.findViewById(R.id.res_3));
        starView.add((ImageView) rowView.findViewById(R.id.res_4));
        starView.add((ImageView) rowView.findViewById(R.id.res_5));

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

            CollaborationHelper.PlayerCollaboration selectedPlayerData = mCollaboration.getPlayer(mSelectedPlayer);
            if (player.mName.equals(mSelectedPlayer)) { // selected player stats
                stats = context.getString(R.string.player_analysis_selected,
                        selectedPlayerData.winRate,
                        selectedPlayerData.games,
                        String.valueOf(selectedPlayerData.success),
                        selectedPlayerData.getSuccessDiffString());
            } else { // collaborator of selected player stats
                CollaborationHelper.EffectMargin collaboratorEffect = selectedPlayerData.getEffect(player.mName);
                if (collaboratorEffect != null) {
                    stats = context.getString(R.string.player_analysis,
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
                        default:
                            rowView.setBackgroundColor(Color.TRANSPARENT);
                            break;
                    }
                }
            }

            analysis.setVisibility(View.VISIBLE);
            analysis.setText(stats);
        } else if (mCollaboration != null) { // non-selected player view
            CollaborationHelper.PlayerCollaboration data = mCollaboration.getPlayer(player.mName);
            String stats = context.getString(R.string.player_analysis,
                    data.winRate,
                    data.games,
                    data.getSuccessDiffString());
            analysis.setVisibility(View.VISIBLE);
            analysis.setText(stats);
        } else {
            analysis.setVisibility(View.GONE);
        }
    }
}