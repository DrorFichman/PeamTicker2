package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.List;

/**
 * Created by drorfichman on 7/30/16.
 */
public class GameAdapter extends ArrayAdapter<Game> {

    private final Context context;
    private final List<Game> mGames;

    public GameAdapter(Context ctx, List<Game> games) {
        super(ctx, -1, games);
        context = ctx;
        mGames = games;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.game_item, parent, false);
        TextView dateView = (TextView) view.findViewById(R.id.game_date);
        TextView resultSet = (TextView) view.findViewById(R.id.game_result_set);
        ImageView res = (ImageView) view.findViewById(R.id.res_1);
        TextView playerGrade = (TextView) view.findViewById(R.id.game_player_grade);

        Game g = mGames.get(position);

        String date = g.date;
        int gameId = g.gameId;
        String team1 = String.valueOf(g.team1Score);
        String team2 = String.valueOf(g.team2Score);

        dateView.setText(date);

        String details = String.valueOf(team1 + " - " + team2);
        resultSet.setText(details);

        setPlayerResult(res, g);
        setPlayerGrade(playerGrade, g);


        view.setTag(R.id.game_id, gameId);
        view.setTag(R.id.game_details, details);

        return view;
    }

    private void setPlayerGrade(TextView playerGrade, Game g) {
        if (g.playerGrade > 0) {
            playerGrade.setVisibility(View.VISIBLE);
            playerGrade.setText(String.valueOf(g.playerGrade));
        } else {
            playerGrade.setVisibility(View.GONE);
        }
    }

    private void setPlayerResult(ImageView starView, Game g) {
        if (g.playerResult != null) {

            ResultEnum res = g.playerResult;
            if (res == ResultEnum.Win) {
                starView.setImageResource(R.drawable.circle_win);
            } else if (res == ResultEnum.Lose) {
                starView.setImageResource(R.drawable.circle_lose);
            } else if (res == ResultEnum.Tie) {
                starView.setImageResource(R.drawable.circle_draw);
            } else if (res == ResultEnum.Missed) {
                starView.setImageResource(R.drawable.circle_na);
            }
            starView.setVisibility(View.VISIBLE);

        } else {
            starView.setVisibility(View.GONE);
        }
    }
}