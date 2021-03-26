package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.List;

import static com.teampicker.drorfichman.teampicker.tools.ColorHelper.setColorAlpha;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerStatisticsAdapter extends ArrayAdapter<Player> {

    private final Context context;
    private final List<Player> mPlayers;
    int maxSuccess = 0;
    int maxGames = 0;

    boolean isGradeVisible;

    public PlayerStatisticsAdapter(Context ctx, List<Player> players, boolean showGrades) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
        isGradeVisible = showGrades;

        for (Player p : players) {
            if (Math.abs(p.statistics.successRate) > maxSuccess) {
                maxSuccess = p.statistics.successRate;
            }
            if (p.statistics.gamesCount > maxGames) {
                maxGames = p.statistics.gamesCount;
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_statistics_item, parent, false);

        TextView name = view.findViewById(R.id.player_name);
        TextView grade = view.findViewById(R.id.stat_player_grade);
        TextView gamesCount = view.findViewById(R.id.stat_games_count);
        TextView success = view.findViewById(R.id.stat_success);
        TextView winRate = view.findViewById(R.id.stat_wins_percentage);

        Player p = mPlayers.get(position);

        name.setText(p.mName);

        if (isGradeVisible) {
            grade.setText(String.valueOf(p.mGrade));
        } else {
            grade.setVisibility(View.INVISIBLE);
        }

        if (p.statistics != null) {
            success.setText(String.valueOf(p.statistics.successRate));
            setColorAlpha(context, success, p.statistics.successRate, maxSuccess);

            gamesCount.setText(String.valueOf(p.statistics.gamesCount));
            setColorAlpha(context, gamesCount, p.statistics.gamesCount, maxGames);

            winRate.setText(String.valueOf(p.statistics.getWinRateDisplay()));
        }

        view.setTag(R.id.player_id, p.mName);

        return view;
    }
}