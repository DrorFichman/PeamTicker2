package com.teampicker.drorfichman.teampicker.View;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapter;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Created by drorfichman on 10/11/16.
 */
public class GameDetailsDialogFragment extends DialogFragment {

    private ArrayList<Player> mTeam1;
    private ArrayList<Player> mTeam2;
    private String mDetails;
    private int mGameId;

    private static final String PARAM_DETAILS = "details";
    private static final String PARAM_GAME_ID = "game_id";

    private AdapterView.OnItemLongClickListener onPlayerClick;
    private PlayerTeamAdapter adapter1;
    private PlayerTeamAdapter adapter2;
    private ListView team1List;
    private ListView team2List;
    private View copyGame;

    static GameDetailsDialogFragment newInstance(int gameId, String details) {

        GameDetailsDialogFragment f = new GameDetailsDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(PARAM_DETAILS, details);
        args.putInt(PARAM_GAME_ID, gameId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetails = getArguments().getString(PARAM_DETAILS);
        mGameId = getArguments().getInt(PARAM_GAME_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_game_details_dialog, container, false);

        ((TextView) view.findViewById(R.id.game_details_title)).setText(mDetails);
        team1List = (ListView) view.findViewById(R.id.game_details_team1);
        team2List = (ListView) view.findViewById(R.id.game_details_team2);

        refreshTeams();

        team1List.setAdapter(adapter1);
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

        copyGame = view.findViewById(R.id.copy_game);
        copyGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DbHelper.clearComingPlayers(getActivity());
                DbHelper.setPlayerComing(getActivity(), mTeam1);
                DbHelper.setPlayerComing(getActivity(), mTeam2);
                DbHelper.saveTeams(getActivity(), mTeam1, mTeam2);
                dismiss();
            }
        });
        copyGame.setOnLongClickListener(longClick);

        view.findViewById(R.id.game_details_ok).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    View.OnLongClickListener longClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.copy_game:
                    Toast.makeText(getActivity(), "Copy coming players and teams", Toast.LENGTH_LONG).show();
                    return true;
            }
            return false;
        }
    };

    private void checkPlayerChange(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle("Modify");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to modify this player attendance?")
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

        DbHelper.modifyPlayerResult(getActivity(), mGameId, player.mName);

        refreshTeams();
    }

    private void refreshTeams() {

        mTeam1 = DbHelper.getCurrTeam(getActivity(), mGameId, TeamEnum.Team1, 0);
        mTeam2 = DbHelper.getCurrTeam(getActivity(), mGameId, TeamEnum.Team2, 0);

        ArrayList missedPlayers = findMissedPlayers();

        adapter1 = new PlayerTeamAdapter(getActivity(), mTeam1, new ArrayList<Player>(), missedPlayers, null, null, false);
        adapter2 = new PlayerTeamAdapter(getActivity(), mTeam2, new ArrayList<Player>(), missedPlayers, null, null, false);

        team1List.setAdapter(adapter1);
        team2List.setAdapter(adapter2);
    }

    @NonNull
    private ArrayList findMissedPlayers() {
        ArrayList missedPlayers = new ArrayList();
        for (Player p : mTeam1) {
            if (ResultEnum.Missed.getValue() == p.gameResult) {
                missedPlayers.add(p);
            }
        }
        for (Player p : mTeam2) {
            if (ResultEnum.Missed.getValue() == p.gameResult) {
                missedPlayers.add(p);
            }
        }
        return missedPlayers;
    }
}
