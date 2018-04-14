package com.teampicker.drorfichman.teampicker.Adapter;

import android.content.Context;
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

        final Player p = mPlayers.get(position);
        name.setText(p.mName);
        grade.setText(String.valueOf(p.mGrade));
        vComing.setChecked(p.isComing);

        view.findViewById(R.id.player_gk).setVisibility(p.isGK ? View.VISIBLE : View.INVISIBLE);
        view.findViewById(R.id.player_d).setVisibility(p.isDefender ? View.VISIBLE : View.INVISIBLE);
        view.findViewById(R.id.player_pm).setVisibility(p.isPlaymaker ? View.VISIBLE : View.INVISIBLE);

        view.setTag(p);

        vComing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DB", "Player clicked checkbox");
                p.isComing = vComing.isChecked();
                DbHelper.updatePlayerComing(context, p.mName, vComing.isChecked());

                if (context instanceof MainActivity) {
                    ((MainActivity) context).setActivityTitle();
                }
            }
        });

        return view;
    }
}