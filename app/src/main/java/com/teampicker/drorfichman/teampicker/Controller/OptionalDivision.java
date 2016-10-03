package com.teampicker.drorfichman.teampicker.Controller;

/**
 * Created by drorfichman on 9/17/16.
 */
public class OptionalDivision {
    public TeamData players1 = new TeamData();
    public TeamData players2 = new TeamData();

    public int score() {
        return Math.abs(players1.getSum() - players2.getSum());
    }
}
