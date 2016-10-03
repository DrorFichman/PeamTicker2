package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerContract;
import com.teampicker.drorfichman.teampicker.R;

/**
 * Created by drorfichman on 7/30/16.
 */
public class PlayerAdapter extends CursorAdapter {

    public PlayerAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.player_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.player_name);
        TextView grade = (TextView) view.findViewById(R.id.player_grade);
        final CheckBox vComing = (CheckBox) view.findViewById(R.id.player_coming);
        final String pName = cursor.getString(cursor.getColumnIndexOrThrow(PlayerContract.PlayerEntry.NAME));
        int pGrade = cursor.getInt(cursor.getColumnIndexOrThrow(PlayerContract.PlayerEntry.GRADE));
        boolean pComing = cursor.getInt(cursor.getColumnIndexOrThrow(PlayerContract.PlayerEntry.IS_COMING)) == 1;
        Player p = new Player(pName, pGrade);
        name.setText(pName);
        grade.setText(String.valueOf(pGrade));
        vComing.setChecked(pComing);

        view.setTag(p);

        vComing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DB", "Player clicked checkbox");
                DbHelper.updatePlayerComing(context, pName, vComing.isChecked());
            }
        });
    }
}