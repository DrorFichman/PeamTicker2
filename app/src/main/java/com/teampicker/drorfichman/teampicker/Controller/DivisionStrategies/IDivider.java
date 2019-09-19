package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public interface IDivider {

    void divide(@NonNull ArrayList<Player> comingPlayers,
                @NonNull List<Player> players1,
                @NonNull List<Player> players2);
}
