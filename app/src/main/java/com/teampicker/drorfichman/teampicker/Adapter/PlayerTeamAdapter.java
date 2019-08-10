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
    private final List<Player> mColorPlayers;
    private final List<Player> mMarkedPlayers;

    boolean isAttributesVisible;
    boolean isGameHistoryVisible;
    boolean isGradeVisible;

    public PlayerTeamAdapter(Context ctx, List<Player> players,
                             List<Player> coloredPlayers, List<Player> markedPlayers,
                             boolean showInternalData) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
        mColorPlayers = coloredPlayers != null ? coloredPlayers : new ArrayList<Player>();
        mMarkedPlayers = markedPlayers != null ? markedPlayers : new ArrayList<Player>();
        isAttributesVisible = showInternalData;
        isGameHistoryVisible = showInternalData;
        isGradeVisible = showInternalData;
    }

    public PlayerTeamAdapter(Context ctx, List<Player> players) {
        super(ctx, -1, players);
        context = ctx;
        mPlayers = players;
        mColorPlayers = new ArrayList<>();
        mMarkedPlayers = new ArrayList<>();
        isAttributesVisible = false;
        isGameHistoryVisible = false;
        isGradeVisible = false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.player_team_item, parent, false);

        Player player = mPlayers.get(position);

        TextView name = (TextView) rowView.findViewById(R.id.player_team_name);
        name.setText(player.mName + (mMarkedPlayers.contains(player) ? " **" : ""));

        rowView.findViewById(R.id.player_gk).setVisibility(isAttributesVisible && player.isGK ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_d).setVisibility(isAttributesVisible && player.isDefender ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_pm).setVisibility(isAttributesVisible && player.isPlaymaker ? View.VISIBLE : View.GONE);
        rowView.findViewById(R.id.player_breaking).setVisibility(isAttributesVisible && player.isBreakable ? View.VISIBLE : View.GONE);

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

        TextView grade = (TextView) rowView.findViewById(R.id.player_team_grade);
        if (isGradeVisible) {
            grade.setText(String.valueOf(player.mGrade));
            grade.setVisibility(View.VISIBLE);
        } else {
            grade.setVisibility(View.GONE);
        }

        if (mColorPlayers.contains(player)) {
            rowView.setBackgroundColor(Color.CYAN);
        } else {
            rowView.setBackgroundColor(Color.TRANSPARENT);
        }

        return rowView;
    }
}