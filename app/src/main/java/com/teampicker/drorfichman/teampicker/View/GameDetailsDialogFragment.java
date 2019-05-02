package com.teampicker.drorfichman.teampicker.View;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapter;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;

/**
 * Created by drorfichman on 10/11/16.
 */
public class GameDetailsDialogFragment extends DialogFragment {

    private static Context ctx;

    private ArrayList<Player> mTeam1;
    private ArrayList<Player> mTeam2;
    private String mDetails;
    private int mGameId;

    private static final String PARAM_TEAM1 = "team1";
    private static final String PARAM_TEAM2 = "team2";
    private static final String PARAM_DETAILS = "details";
    private static final String PARAM_GAME_ID = "game_id";

    private AdapterView.OnItemLongClickListener onPlayerClick;
    private PlayerTeamAdapter adapter1;
    private PlayerTeamAdapter adapter2;

    static GameDetailsDialogFragment newInstance(Context context,
                                                 ArrayList<Player> team1, ArrayList<Player> team2,
                                                 int gameId, String details) {

        GameDetailsDialogFragment f = new GameDetailsDialogFragment();
        ctx = context;

        Bundle args = new Bundle();
        args.putSerializable(PARAM_TEAM1, team1);
        args.putSerializable(PARAM_TEAM2, team2);
        args.putSerializable(PARAM_DETAILS, details);
        args.putInt(PARAM_GAME_ID, gameId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTeam1 = (ArrayList<Player>) getArguments().getSerializable(PARAM_TEAM1);
        mTeam2 = (ArrayList<Player>) getArguments().getSerializable(PARAM_TEAM2);
        mDetails = getArguments().getString(PARAM_DETAILS);
        mGameId = getArguments().getInt(PARAM_GAME_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_game_details_dialog, container, false);

        ((TextView) view.findViewById(R.id.game_details_title)).setText(mDetails);
        ListView team1List = (ListView) view.findViewById(R.id.game_details_team1);
        ListView team2List = (ListView) view.findViewById(R.id.game_details_team2);

        adapter1 = new PlayerTeamAdapter(getActivity(), mTeam1);
        team1List.setAdapter(adapter1);

        adapter2 = new PlayerTeamAdapter(getActivity(), mTeam2);
        team2List.setAdapter(adapter2);

        onPlayerClick = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Player player = (Player) adapterView.getItemAtPosition(i);
                checkPlayerChange(player);
                return false;
            }
        };
        team1List.setOnItemLongClickListener(onPlayerClick);
        team2List.setOnItemLongClickListener(onPlayerClick);

        view.findViewById(R.id.game_details_ok).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void checkPlayerChange(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);

        alertDialogBuilder.setTitle("Modify");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to move this player to the other team?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        movePlayer(player);
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void movePlayer(Player player) {

        DbHelper.modifyPlayerResult(ctx, mGameId, player.mName);

        mTeam1 = DbHelper.getCurrTeam(ctx, mGameId, TeamEnum.Team1, 0);
        mTeam2 = DbHelper.getCurrTeam(ctx, mGameId, TeamEnum.Team2, 0);

        adapter1.clear();
        adapter1.addAll(mTeam1);
        adapter2.clear();
        adapter2.addAll(mTeam2);
    }
}
