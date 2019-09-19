package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import android.content.Context;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.CollaborationHelper;
import com.teampicker.drorfichman.teampicker.Controller.OptionalDivision;
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

public class DivideCollaboration implements IDivider {

    private static final int OPTIONS = 7;

    @Override
    public void divide(Context ctx, @NonNull ArrayList<Player> comingPlayers,
                       @NonNull List<Player> players1,
                       @NonNull List<Player> players2) {

        OptionalDivision selected = divideRandomly(cloneList(comingPlayers));
        int selectedValue = selected.winRateStdDiv(ctx);

        for (int option = 0; option < OPTIONS; ++option) {
            OptionalDivision another = divideRandomly(cloneList(comingPlayers));
            int otherValue = another.winRateStdDiv(ctx);
            if ((otherValue < selectedValue && otherValue > 0) || (selectedValue == -1)) {
                selected = another;
                selectedValue = otherValue;
            }
        }

        players1.addAll(selected.players1.players);
        players2.addAll(selected.players2.players);
    }

    public static ArrayList<Player> cloneList(List<Player> players) {
        ArrayList<Player> clone = new ArrayList<>(players.size());
        clone.addAll(players);
        return clone;
    }

    private static OptionalDivision divideRandomly(ArrayList<Player> players) {
        OptionalDivision option = new OptionalDivision();
        Random r = new Random();
        while (players.size() > 0) {
            int a = getRandom(r, players.size());
            if (option.players1.getCount() > option.players2.getCount()) // more players of this type
                option.players2.players.add(players.get(a));
            else
                option.players1.players.add(players.get(a));
            players.remove(a);
        }

        return option;
    }

    private static int getRandom(Random r, int value) {
        return r.nextInt(value);
    }
}
