package com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;

public class DivideByAge implements IDivider {

    @Override
    public void divide(Context ctx, @NonNull ArrayList<Player> comingPlayers,
                       @NonNull List<Player> players1,
                       @NonNull List<Player> players2, int divideAttemptsCount,
                       TeamDivision.onTaskInProgress update) {

        ArrayList<Player> players = DividerBase.cloneList(comingPlayers);

        Collections.sort(players, Comparator.comparingInt(Player::getAge));

        players1.addAll(players.subList(0, players.size() / 2));
        players2.addAll(players.subList(players.size() / 2, players.size()));
    }
}
