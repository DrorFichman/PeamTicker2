package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class DivideSimple implements IDivider {

    @Override
    public void divide(@NonNull ArrayList<Player> comingPlayers,
                       @NonNull List<Player> players1,
                       @NonNull List<Player> players2) {

        for (Player currPlayer : comingPlayers) {
            if (players1.size() == players2.size()) {
                players1.add(currPlayer);
            } else {
                players2.add(currPlayer);
            }
        }
    }
}
