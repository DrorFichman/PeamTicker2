package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;
import com.teampicker.drorfichman.teampicker.R;

import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerParticipationAdapter extends ArrayAdapter<PlayerParticipation> {

    private final Context context;
    private final List<PlayerParticipation> mPlayers;

    public PlayerParticipationAdapter(Context ctx, List<PlayerParticipation> players) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_participation_item, parent, false);

        TextView name = (TextView) view.findViewById(R.id.player_name);

        TextView countWith = (TextView) view.findViewById(R.id.part_games_count_with);
        TextView winRateWith = (TextView) view.findViewById(R.id.part_wins_percentage_with);

        TextView countVs = (TextView) view.findViewById(R.id.part_games_count_against);
        TextView winRateVs = (TextView) view.findViewById(R.id.part_wins_percentage_against);

        PlayerParticipation p = mPlayers.get(position);

        name.setText(p.mName);

        countWith.setText(String.valueOf(p.statisticsWith.gamesCount));
        if (p.statisticsWith.gamesCount > 0)
            winRateWith.setText(context.getString(R.string.player_wins_participation, p.statisticsWith.getWinRateDisplay(), String.valueOf(p.statisticsWith.successRate)));
        else
            winRateWith.setText(p.statisticsWith.getWinRateDisplay());

        countVs.setText(String.valueOf(p.statisticsVs.gamesCount));
        if (p.statisticsVs.gamesCount > 0)
            winRateVs.setText(context.getString(R.string.player_wins_participation, p.statisticsVs.getWinRateDisplay(), String.valueOf(p.statisticsVs.successRate)));
        else
            winRateVs.setText(p.statisticsVs.getWinRateDisplay());

        view.setTag(R.id.player_id, p.mName);

        return view;
    }
}