package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerContract;
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

        Game g = mGames.get(position);

        String date = g.date;
        int gameId = g.gameId;
        String team1 = String.valueOf(g.team1Score);
        String team2 = String.valueOf(g.team2Score);

        dateView.setText(date);

        String details = String.valueOf(team1 + " - " + team2);
        resultSet.setText(details);

        view.setTag(R.id.game_id, gameId);
        view.setTag(R.id.game_details, details);

        return view;
    }
}