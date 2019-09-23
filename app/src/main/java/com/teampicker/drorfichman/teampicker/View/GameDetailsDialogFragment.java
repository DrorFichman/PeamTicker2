package com.teampicker.drorfichman.teampicker.View;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapterGameHistory;
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

    private PlayerTeamAdapterGameHistory adapter1;
    private PlayerTeamAdapterGameHistory adapter2;
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
        team1List = view.findViewById(R.id.game_details_team1);
        team2List = view.findViewById(R.id.game_details_team2);

        AdapterView.OnItemLongClickListener onPlayerClick = (adapterView, view1, i, l) -> {
            Player player = (Player) adapterView.getItemAtPosition(i);
            checkPlayerChange(player);
            return false;
        };
        team1List.setOnItemLongClickListener(onPlayerClick);
        team2List.setOnItemLongClickListener(onPlayerClick);

        copyGame = view.findViewById(R.id.copy_game);
        copyGame.setOnClickListener(view12 -> {
            DbHelper.clearComingPlayers(getActivity());
            DbHelper.setPlayerComing(getActivity(), mTeam1);
            DbHelper.setPlayerComing(getActivity(), mTeam2);
            DbHelper.saveTeams(getActivity(), mTeam1, mTeam2);
            dismiss();
        });
        copyGame.setOnLongClickListener(operationExplanation);

        view.findViewById(R.id.game_details_ok).setOnClickListener(v -> dismiss());

        refreshTeams();

        return view;
    }

    View.OnLongClickListener operationExplanation = view -> {
        switch (view.getId()) {
            case R.id.copy_game:
                Toast.makeText(getActivity(), "Copy coming players and teams", Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
    };

    private void checkPlayerChange(final Player player) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle("Modify");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to modify this player attendance?")
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    movePlayer(player);
                    dialog.dismiss();
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

        adapter1 = new PlayerTeamAdapterGameHistory(getActivity(), mTeam1, missedPlayers);
        adapter2 = new PlayerTeamAdapterGameHistory(getActivity(), mTeam2, missedPlayers);

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
