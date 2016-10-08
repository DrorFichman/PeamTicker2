package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerContract;
import com.teampicker.drorfichman.teampicker.R;

import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerStatisticsAdapter extends ArrayAdapter<Player> {

    private final Context context;
    private final List<Player> mPlayers;

    public PlayerStatisticsAdapter(Context ctx, List<Player> players) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_statistics_item, parent, false);

        TextView name = (TextView) view.findViewById(R.id.player_name);
        TextView grade = (TextView) view.findViewById(R.id.stat_player_grade);
        TextView count = (TextView) view.findViewById(R.id.stat_games_count);
        TextView success = (TextView) view.findViewById(R.id.stat_success);

        Player p = mPlayers.get(position);

        name.setText(p.mName);
        grade.setText(String.valueOf(p.mGrade));

        if (p.statistics != null) {
            success.setText(String.valueOf(p.statistics.successRate));
            count.setText(String.valueOf(p.statistics.gamesCount));
        }

        view.setTag(p);

        return view;
    }
}