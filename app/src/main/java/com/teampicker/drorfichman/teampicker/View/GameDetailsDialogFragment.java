package com.teampicker.drorfichman.teampicker.View;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapter;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;

/**
 * Created by drorfichman on 10/11/16.
 */
public class GameDetailsDialogFragment extends DialogFragment {

    private ArrayList<Player> mTeam1;
    private ArrayList<Player> mTeam2;
    private String mDetails;
    private String mGameId;

    private static final String PARAM_TEAM1 = "team1";
    private static final String PARAM_TEAM2 = "team2";
    private static final String PARAM_DETAILS = "details";
    private static final String PARAM_GAME_ID = "game_id";

    static GameDetailsDialogFragment newInstance(ArrayList<Player> team1, ArrayList<Player> team2,
                                                 String gameId, String details) {

        GameDetailsDialogFragment f = new GameDetailsDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(PARAM_TEAM1, team1);
        args.putSerializable(PARAM_TEAM2, team2);
        args.putSerializable(PARAM_DETAILS, details);
        args.putSerializable(PARAM_GAME_ID, gameId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTeam1 = (ArrayList<Player>) getArguments().getSerializable(PARAM_TEAM1);
        mTeam2 = (ArrayList<Player>) getArguments().getSerializable(PARAM_TEAM2);
        mDetails = getArguments().getString(PARAM_DETAILS);
        mGameId = getArguments().getString(PARAM_GAME_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_game_details_dialog, container, false);

        ((TextView) view.findViewById(R.id.game_details_title)).setText(mDetails);
        ListView team1List = (ListView) view.findViewById(R.id.game_details_team1);
        ListView team2List = (ListView) view.findViewById(R.id.game_details_team2);

        PlayerTeamAdapter p1 = new PlayerTeamAdapter(getActivity(), mTeam1);
        team1List.setAdapter(p1);

        PlayerTeamAdapter p2 = new PlayerTeamAdapter(getActivity(), mTeam2);
        team2List.setAdapter(p2);

        view.findViewById(R.id.game_details_ok).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }
}
