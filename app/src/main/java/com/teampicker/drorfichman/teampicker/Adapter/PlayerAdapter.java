package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.View.MainActivity;

import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerAdapter extends ArrayAdapter<Player> {

    private final Context context;
    private final List<Player> mPlayers;

    public PlayerAdapter(Context ctx, List<Player> players) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_item, parent, false);
        TextView name = (TextView) view.findViewById(R.id.player_name);
        TextView grade = (TextView) view.findViewById(R.id.player_grade);
        final CheckBox vComing = (CheckBox) view.findViewById(R.id.player_coming);
        TextView recentPerformance = (TextView) view.findViewById(R.id.player_recent_performance);

        final Player player = mPlayers.get(position);
        name.setText(player.mName);
        grade.setText(String.valueOf(player.mGrade));
        vComing.setChecked(player.isComing);

        setPlayerRecentPerformance(recentPerformance, player);

        view.findViewById(R.id.player_gk).setVisibility(player.isGK ? View.VISIBLE : View.INVISIBLE);
        view.findViewById(R.id.player_d).setVisibility(player.isDefender ? View.VISIBLE : View.INVISIBLE);
        view.findViewById(R.id.player_pm).setVisibility(player.isPlaymaker ? View.VISIBLE : View.INVISIBLE);

        view.setTag(player);

        vComing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DB", "Player clicked checkbox");
                player.isComing = vComing.isChecked();
                DbHelper.updatePlayerComing(context, player.mName, vComing.isChecked());

                if (context instanceof MainActivity) {
                    ((MainActivity) context).setActivityTitle();
                }
            }
        });

        return view;
    }

    private void setPlayerRecentPerformance(TextView recentPerformance, Player player) {
        int performance = player.getSuccess();
        if (performance > 0) {
            recentPerformance.setText(String.valueOf("+" + performance));
            recentPerformance.setTextColor(Color.GREEN);
            recentPerformance.setVisibility(View.VISIBLE);
        } else if (performance < 0) {
            recentPerformance.setText(String.valueOf(performance));
            recentPerformance.setTextColor(Color.RED);
            recentPerformance.setVisibility(View.VISIBLE);
        } else {
            recentPerformance.setVisibility(View.INVISIBLE);
        }
    }
}