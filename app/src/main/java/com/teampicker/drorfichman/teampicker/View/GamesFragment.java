package com.teampicker.drorfichman.teampicker.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Adapter.GameAdapter;
import com.teampicker.drorfichman.teampicker.Adapter.PlayerTeamAdapterGameHistory;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.TeamEnum;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.DialogHelper;

import java.util.ArrayList;
import java.util.Comparator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class GamesFragment extends Fragment {

    private String mPlayerName;
    private String mPlayerCollaborator;

    private ListView gamesList;
    private GameAdapter gamesAdapter;

    private int mCurrGameId;
    private ArrayList<Player> mTeam1;
    private ArrayList<Player> mTeam2;

    private View gameDetails;
    private ListView team1List;
    private ListView team2List;

    public GamesFragment(String playerName, String collaborator) {
        super(R.layout.layout_games_activity_fragment);

        mPlayerName = playerName;
        mPlayerCollaborator = collaborator;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        gamesList = root.findViewById(R.id.games_list);
        gameDetails = root.findViewById(R.id.game_details_layout);

        team1List = root.findViewById(R.id.game_details_team1);
        team2List = root.findViewById(R.id.game_details_team2);

        team1List.setOnItemLongClickListener(onPlayerClick);
        team2List.setOnItemLongClickListener(onPlayerClick);

        gamesList.setOnItemClickListener((adapterView, view, position, l) -> {
            Game game = (Game) view.getTag(R.id.game);
            onGameClick(game);
        });

        gamesList.setOnItemLongClickListener((adapterView, view, position, l) -> {
            onGameLongClick(((Game) view.getTag(R.id.game)));
            return true;
        });

        refreshGames();

        return root;
    }

    private void refreshTeams() {
        Context activity = getContext(); if (activity == null) return;

        mTeam1 = DbHelper.getCurrTeam(activity, mCurrGameId, TeamEnum.Team1, 0);
        mTeam2 = DbHelper.getCurrTeam(activity, mCurrGameId, TeamEnum.Team2, 0);

        mTeam1.sort(Comparator.comparing(Player::name));
        mTeam2.sort(Comparator.comparing(Player::name));

        team1List.setAdapter(new PlayerTeamAdapterGameHistory(activity, mTeam1, mPlayerName, mPlayerCollaborator));
        team2List.setAdapter(new PlayerTeamAdapterGameHistory(activity, mTeam2, mPlayerName, mPlayerCollaborator));
    }

    private void refreshGames() {
        Context activity = getContext(); if (activity == null) return;

        ArrayList<Game> games;
        if (mPlayerName != null && mPlayerCollaborator != null) { // games in which both played
            games = DbHelper.getGames(activity, mPlayerName, mPlayerCollaborator);
        } else if (mPlayerName != null) { // games in which selected player played
            games = DbHelper.getGames(activity, mPlayerName);
        } else { // all games
            games = DbHelper.getGames(activity);
        }

        // Attach cursor adapter to the ListView
        gamesAdapter = new GameAdapter(activity, games, mCurrGameId);
        gamesList.setAdapter(gamesAdapter);
    }

    private void refreshSelectedGame() {
        gamesAdapter.setSelectedGameId(mCurrGameId);
        gamesAdapter.notifyDataSetChanged();
    }

    //region game click
    private void onGameClick(Game game) {
        if (mCurrGameId == game.gameId) {
            mCurrGameId = -1;
            gameDetails.setVisibility(View.GONE);
        } else {
            mCurrGameId = game.gameId;
            gameDetails.setVisibility(View.VISIBLE);
        }
        refreshSelectedGame();
        refreshTeams();
    }
    //endregion

    //region game long clicked
    private void onGameLongClick(Game game) {
        if (mCurrGameId > 0 && mCurrGameId == game.gameId) { // selected game - copy
            checkCopyGame();
        } else { // non-selected game - delete
            checkGameDeletion(game);
        }
    }

    private void checkCopyGame() {

        DialogHelper.showApprovalDialog(getContext(), getString(R.string.copy),
                "Copy coming players and teams?",
                ((dialog, which) -> copyGamePlayers()));
    }

    private void copyGamePlayers() {
        FragmentActivity activity = getActivity(); if (activity == null) return;
        DbHelper.clearComingPlayers(activity);
        DbHelper.setPlayerComing(activity, mTeam1);
        DbHelper.setPlayerComing(activity, mTeam2);
        DbHelper.saveTeams(activity, mTeam1, mTeam2);
        Toast.makeText(activity, R.string.copy_players_success, Toast.LENGTH_SHORT).show();
    }

    private void checkGameDeletion(final Game game) {
        Context activity = getContext(); if (activity == null) return;

        DialogHelper.showApprovalDialog(activity,
                getString(R.string.delete), "Do you want to remove (" + game.getDate(activity) + ")?",
                ((dialog, which) -> {
                    DbHelper.deleteGame(activity, game.gameId);
                    refreshGames();
                })
        );
    }
    //endregion

    //region player click
    private AdapterView.OnItemLongClickListener onPlayerClick = (parent, view, position, id) -> {
        Player player = (Player) parent.getItemAtPosition(position);
        checkPlayerChange(player);
        return false;
    };

    private void checkPlayerChange(final Player player) {

        DialogHelper.showApprovalDialog(getContext(),
                "Modify", "Do you want to modify this player attendance?",
                ((dialog, which) -> movePlayer(player))
        );
    }

    private void movePlayer(Player player) {
        Context context = getContext(); if (context == null) return;

        DbHelper.modifyPlayerResult(context, mCurrGameId, player.mName);
        refreshTeams();
    }
    //endregion
}
