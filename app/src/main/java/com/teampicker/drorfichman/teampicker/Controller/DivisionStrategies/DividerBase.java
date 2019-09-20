package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Controller.OptionalDivision;
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

public abstract class DividerBase implements IDivider {

    abstract int optionsCount();

    @Override
    public void divide(Context ctx, @NonNull ArrayList<Player> comingPlayers,
                       @NonNull List<Player> players1,
                       @NonNull List<Player> players2) {

        OptionalDivision selected = divideRandomly(cloneList(comingPlayers));
        int selectedGrade = gradeOption(ctx, selected);

        for (int option = 0; option < optionsCount(); ++option) {
            OptionalDivision other = divideRandomly(cloneList(comingPlayers));
            int otherGrade = gradeOption(ctx, other);
            if (preferNewOption(selectedGrade, otherGrade)) {
                selected = other;
                selectedGrade = otherGrade;
            }
        }

        players1.addAll(selected.players1.players);
        players2.addAll(selected.players2.players);
    }

    abstract int gradeOption(Context ctx, OptionalDivision option);

    abstract boolean preferNewOption(int selected, int another);

    static OptionalDivision divideRandomly(ArrayList<Player> players) {
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

    public static ArrayList<Player> cloneList(List<Player> players) {
        ArrayList<Player> clone = new ArrayList<>(players.size());
        clone.addAll(players);
        return clone;
    }

    static int getRandom(Random r, int value) {
        return r.nextInt(value);
    }
}
