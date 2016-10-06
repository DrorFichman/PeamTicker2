package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerTeamAdapter extends ArrayAdapter<Player> {
    private final Context context;
    private final List<Player> mPlayers;

    public PlayerTeamAdapter(Context ctx, List<Player> players) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.player_item, parent, false);

        Player player = mPlayers.get(position);

        rowView.findViewById(R.id.player_coming).setVisibility(View.GONE);
        TextView name = (TextView) rowView.findViewById(R.id.player_name);
        name.setText(player.mName);

        ArrayList<ImageView> starView = new ArrayList();
        starView.add((ImageView) rowView.findViewById(R.id.res_1));
        starView.add((ImageView) rowView.findViewById(R.id.res_2));
        starView.add((ImageView) rowView.findViewById(R.id.res_3));
        starView.add((ImageView) rowView.findViewById(R.id.res_4));
        starView.add((ImageView) rowView.findViewById(R.id.res_5));

        for (ImageView im : starView) {
            im.setVisibility(View.INVISIBLE);
        }

        for (int r = 0; r < player.results.size() && r < starView.size(); ++r) {
            ResultEnum res = player.results.get(r);
            if (res == ResultEnum.Win) {
                starView.get(r).setImageResource(R.drawable.star_w_16);
            } else if (res == ResultEnum.Lose) {
                starView.get(r).setImageResource(R.drawable.star_l_16);
            } else if (res == ResultEnum.Tie) {
                starView.get(r).setImageResource(R.drawable.star_t_16);
            }
            starView.get(r).setVisibility(View.VISIBLE);
        }

        TextView grade = (TextView) rowView.findViewById(R.id.player_grade);

        if (player.isGradeDisplayed) {
            grade.setText(String.valueOf(player.mGrade));
            grade.setVisibility(View.VISIBLE);
        } else {
            grade.setVisibility(View.GONE);
        }

        return rowView;
    }
}