package com.teampicker.drorfichman.teampicker.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.Adapter.PlayerParticipationAdapter;
import com.teampicker.drorfichman.teampicker.Controller.Sort.Sorting;
import com.teampicker.drorfichman.teampicker.Controller.Sort.sortType;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;
import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.ScreenshotHelper;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlayerParticipationFragment extends Fragment implements Sorting.sortingCallbacks {

    private ArrayList<PlayerParticipation> players = new ArrayList<>();
    private PlayerParticipationAdapter playersAdapter;
    private Player pPlayer;
    private ArrayList<Player> blue;
    private ArrayList<Player> orange;

    private int games = 50;
    private Sorting sorting = new Sorting(this, sortType.gamesWith);

    private ListView playersList;
    private View titles;
    private TextView name;

    public PlayerParticipationFragment(String playerName,
                                       ArrayList<Player> blueTeam, ArrayList<Player> orangeTeam) {
        super(R.layout.layout_participation_fragment);
        pPlayer = DbHelper.getPlayer(getContext(), playerName);
        orange = orangeTeam;
        blue = blueTeam;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        playersList = root.findViewById(R.id.players_participation_list);
        playersList.setOnItemClickListener(onPlayerClick);

        titles = root.findViewById(R.id.titles);
        name = root.findViewById(R.id.player_name);

        setTeamIcon(root);

        sorting.setHeadlineSorting(root, getActivity(), R.id.player_name, null, sortType.name);
        sorting.setHeadlineSorting(root, getActivity(), R.id.part_games_count_with, "Games\nWith", sortType.gamesWith);
        sorting.setHeadlineSorting(root, getActivity(), R.id.part_wins_percentage_with, "Success\nWith", sortType.successWith);
        sorting.setHeadlineSorting(root, getActivity(), R.id.part_games_count_against, "Games\nVs", sortType.gamesVs);
        sorting.setHeadlineSorting(root, getActivity(), R.id.part_wins_percentage_against, "Success\nVs", sortType.successVs);

        refreshPlayers();

        setHasOptionsMenu(true);

        return root;
    }

    private void setTeamIcon(View root) {

        ImageView teamIcon = root.findViewById(R.id.team_icon);
        if (orange != null && orange.contains(pPlayer)) {
            teamIcon.setImageResource(R.drawable.circle_orange);
            teamIcon.setVisibility(View.VISIBLE);
        } else if (blue != null && blue.contains(pPlayer)) {
            teamIcon.setImageResource(R.drawable.circle_blue);
            teamIcon.setVisibility(View.VISIBLE);
        } else {
            teamIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.statisctics_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_statistics:
                final Runnable r = () -> ScreenshotHelper.takeListScreenshot(getActivity(),
                        playersList, titles, playersAdapter);
                new Handler().postDelayed(r, 200);
                break;
            case R.id.action_last_10_games:
                games = 10;
                refreshPlayers();
                break;
            case R.id.action_last_50_games:
                games = 50;
                refreshPlayers();
                break;
            case R.id.action_no_limit:
                games = -1;
                refreshPlayers();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private AdapterView.OnItemClickListener onPlayerClick = (parent, view, position, id) -> {
        String selected = ((PlayerParticipation) parent.getItemAtPosition(position)).mName;
        Intent gameActivityIntent = GamesActivity.getGameActivityIntent(getContext(), pPlayer.mName, selected);
        startActivity(gameActivityIntent);
    };

    private void refreshPlayers() {
        Context context = getContext(); if (context == null) return;

        HashMap<String, PlayerParticipation> result = DbHelper.getPlayersParticipationStatistics(context, games, pPlayer.mName);
        players.clear();
        players.addAll(result.values());

        Player player = DbHelper.getPlayer(context, pPlayer.mName, games);
        name.setText(getString(R.string.player_participation_statistics,
                        player.mName,
                        player.statistics.gamesCount,
                        player.statistics.getWinRate()));

        sorting.sort(players);

        playersAdapter = new PlayerParticipationAdapter(context, players, blue, orange);
        playersList.setAdapter(playersAdapter);
    }

    //region sort
    @Override
    public void refresh() {
        refreshPlayers();
    }
    //endregion
}
