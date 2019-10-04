package com.teampicker.drorfichman.teampicker.View;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.graphics.Color;
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
import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.ResultEnum;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Comparator;

import androidx.annotation.NonNull;

/**
 * Created by drorfichman on 10/11/16.
 */
public class GameDetailsDialogFragment extends DialogFragment {

    public static final String PARAM_GAMES_LIST = "games_list";
    public static final String PARAM_GAME_INDEX = "list_index";
    private static final String PARAM_GAME = "game";
    private static final String PARAM_SELECTED_PLAYER = "selected_player";

    private ArrayList<Player> mTeam1;
    private ArrayList<Player> mTeam2;

    private Game mCurrGame;
    private int mCurrGameIndex;
    private ArrayList<Game> mGames;
    private String mSelectedPlayer;

    private PlayerTeamAdapterGameHistory adapter1;
    private PlayerTeamAdapterGameHistory adapter2;

    private ListView team1List;
    private ListView team2List;
    private TextView score;
    private TextView date;

    static GameDetailsDialogFragment newInstance(ArrayList<Game> games, int gameIndexId, Game game, Player player) {

        GameDetailsDialogFragment f = new GameDetailsDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(PARAM_GAME, game);
        args.putInt(PARAM_GAME_INDEX, gameIndexId);
        args.putSerializable(PARAM_GAMES_LIST, games);
        args.putString(PARAM_SELECTED_PLAYER, player != null ? player.mName : null);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrGame = (Game) getArguments().getSerializable(PARAM_GAME);
        mGames = (ArrayList<Game>) getArguments().getSerializable(PARAM_GAMES_LIST);
        mCurrGameIndex = getArguments().getInt(PARAM_GAME_INDEX);
        mSelectedPlayer = getArguments().getString(PARAM_SELECTED_PLAYER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_game_details_dialog, container, false);

        team1List = view.findViewById(R.id.game_details_team1);
        team2List = view.findViewById(R.id.game_details_team2);

        score = view.findViewById(R.id.game_details_title);
        date = view.findViewById(R.id.game_details_date);

        AdapterView.OnItemLongClickListener onPlayerClick = (adapterView, view1, i, l) -> {
            Player player = (Player) adapterView.getItemAtPosition(i);
            checkPlayerChange(player);
            return false;
        };
        team1List.setOnItemLongClickListener(onPlayerClick);
        team2List.setOnItemLongClickListener(onPlayerClick);

        View copyGame = view.findViewById(R.id.copy_game);
        copyGame.setOnClickListener(view12 -> {
            DbHelper.clearComingPlayers(getActivity());
            DbHelper.setPlayerComing(getActivity(), mTeam1);
            DbHelper.setPlayerComing(getActivity(), mTeam2);
            DbHelper.saveTeams(getActivity(), mTeam1, mTeam2);
            Toast.makeText(getActivity(), R.string.copy_players_success, Toast.LENGTH_SHORT).show();
            dismiss();
        });
        copyGame.setOnLongClickListener(operationExplanation);

        view.findViewById(R.id.next_game).setOnClickListener(v -> {
            if (mCurrGameIndex > 0) {
                mCurrGameIndex--;
                mCurrGame = mGames.get(mCurrGameIndex);
                refreshTeams();
            }
        });

        view.findViewById(R.id.previous_game).setOnClickListener(v -> {
            if (mCurrGameIndex < mGames.size() - 1) {
                mCurrGameIndex++;
                mCurrGame = mGames.get(mCurrGameIndex);
                refreshTeams();
            }
        });

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

        DbHelper.modifyPlayerResult(getActivity(), mCurrGame.gameId, player.mName);

        refreshTeams();
    }

    private void refreshTeams() {
        score.setText(mCurrGame.getScore());
        score.setTextColor(mCurrGame.playerResult != null ? mCurrGame.playerResult.color : Color.BLACK);
        date.setText(mCurrGame.date);

        mTeam1 = DbHelper.getCurrTeam(getActivity(), mCurrGame.gameId, TeamEnum.Team1, 0);
        mTeam2 = DbHelper.getCurrTeam(getActivity(), mCurrGame.gameId, TeamEnum.Team2, 0);
        ArrayList missedPlayers = findMissedPlayers();

        mTeam1.sort(Comparator.comparing(Player::name));
        mTeam2.sort(Comparator.comparing(Player::name));

        adapter1 = new PlayerTeamAdapterGameHistory(getActivity(), mTeam1, missedPlayers, mSelectedPlayer);
        adapter2 = new PlayerTeamAdapterGameHistory(getActivity(), mTeam2, missedPlayers, mSelectedPlayer);

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
