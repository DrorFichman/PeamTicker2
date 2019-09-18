package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;

public class DivideByAge implements IDivider {

    @Override
    public void divide(@NonNull ArrayList<Player> comingPlayers,
                       @NonNull List<Player> players1,
                       @NonNull List<Player> players2) {

        Collections.sort(comingPlayers, Comparator.comparingInt(Player::getAge));

        players1.addAll(comingPlayers.subList(0, comingPlayers.size() / 2));
        players2.addAll(comingPlayers.subList(comingPlayers.size() / 2, comingPlayers.size()));
    }
}
