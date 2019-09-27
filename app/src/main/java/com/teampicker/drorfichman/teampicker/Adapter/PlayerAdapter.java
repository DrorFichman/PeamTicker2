package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerAdapter extends ArrayAdapter<Player> {

    public interface onPlayerComingChange {
        void handle();
    }

    private final Context context;
    private final List<Player> mPlayers;
    private onPlayerComingChange handler;

    public PlayerAdapter(Context ctx, List<Player> players, onPlayerComingChange caller) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
        handler = caller;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_item, parent, false);
        TextView name = view.findViewById(R.id.player_name);
        TextView grade = view.findViewById(R.id.player_grade);
        final CheckBox vComing = view.findViewById(R.id.player_coming);
        TextView recentPerformance = view.findViewById(R.id.player_recent_performance);

        final Player player = mPlayers.get(position);
        name.setText(player.mName);
        grade.setText(String.valueOf(player.mGrade));
        vComing.setChecked(player.isComing);

        setAge(view, player);

        setPlayerRecentPerformance(recentPerformance, player);

        setAttributes(player, view.findViewById(R.id.player_attributes));

        view.setTag(player);

        vComing.setOnClickListener(view1 -> {
            player.isComing = vComing.isChecked();
            DbHelper.updatePlayerComing(context, player.mName, vComing.isChecked());

            if (handler != null) {
                handler.handle();
            }
        });

        return view;
    }

    private void setAttributes(Player player, TextView attributes) {
        attributes.setVisibility(player.hasAttributes() ? View.VISIBLE : View.INVISIBLE);
        attributes.setText(player.getAttributes());
    }

    private void setAge(View view, Player player) {
        int age = player.getAge();
        TextView ageView = view.findViewById(R.id.player_age);
        if (age > 0) {
            ageView.setText(String.valueOf(age));
            ageView.setVisibility(View.VISIBLE);
        } else {
            ageView.setVisibility(View.INVISIBLE);
        }
    }

    private void setPlayerRecentPerformance(TextView recentPerformance, Player player) {
        int suggestedGrade = player.getSuggestedGrade();

        if (suggestedGrade > player.mGrade) {
            recentPerformance.setText(String.valueOf(suggestedGrade));
            recentPerformance.setTextColor(Color.GREEN);
            recentPerformance.setVisibility(View.VISIBLE);
        } else if (suggestedGrade < player.mGrade) {
            recentPerformance.setText(String.valueOf(suggestedGrade));
            recentPerformance.setTextColor(Color.RED);
            recentPerformance.setVisibility(View.VISIBLE);
        } else {
            recentPerformance.setVisibility(View.INVISIBLE);
        }
    }
}