package com.teampicker.drorfichman.teampicker.Controller;

import com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies.DivideByAge;
import com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies.DivideByGrade;
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by drorfichman on 9/16/16.
 */
public class TeamDivision {

    public static void dividePlayers(@NonNull List<Player> comingPlayers,
                                     @NonNull List<Player> resultPlayers1,
                                     @NonNull List<Player> resultPlayers2) {

        resultPlayers1.clear();
        resultPlayers2.clear();

        ArrayList<Player> players = cloneList(comingPlayers);
        Collections.sort(players);

        new DivideByAge().divide(players, resultPlayers1, resultPlayers2);
    }

    public static ArrayList<Player> cloneList(List<Player> players) {
        ArrayList<Player> clone = new ArrayList<>(players.size());
        clone.addAll(players);
        return clone;
    }
}
