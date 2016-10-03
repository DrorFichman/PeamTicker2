package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.PlayerContract;
import com.teampicker.drorfichman.teampicker.R;

/**
 * Created by drorfichman on 7/30/16.
 */
public class GameAdapter extends CursorAdapter {

    public GameAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.game_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView date = (TextView) view.findViewById(R.id.game_date);
        TextView resultSet = (TextView) view.findViewById(R.id.game_result_set);

        String mDate = cursor.getString(cursor.getColumnIndexOrThrow(PlayerContract.GameEntry.DATE));
        String team1 = cursor.getString(cursor.getColumnIndexOrThrow(PlayerContract.GameEntry.TEAM1_SCORE));
        String team2 = cursor.getString(cursor.getColumnIndexOrThrow(PlayerContract.GameEntry.TEAM2_SCORE));

        date.setText(mDate);
        resultSet.setText(String.valueOf(team1 + " - " + team2));

        view.setTag(mDate);
    }
}