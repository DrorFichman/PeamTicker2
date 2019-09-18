package com.teampicker.drorfichman.teampicker.Data;

import com.teampicker.drorfichman.teampicker.Controller.Sorting;
import com.teampicker.drorfichman.teampicker.Controller.StatisticsData;

import java.io.Serializable;

public class PlayerParticipation implements Serializable, Sorting.Sortable {

    public String mName;
    public String mParticipatedWith;

    public StatisticsData statisticsWith = new StatisticsData();
    public StatisticsData statisticsVs = new StatisticsData();

    //region Sortable
    @Override
    public String name() {
        return mName;
    }

    @Override
    public int gamesWithCount() {
        return statisticsWith.gamesCount;
    }

    @Override
    public int successWith() {
        return statisticsWith.successRate;
    }

    @Override
    public int winRateWith() {
        return statisticsWith.getWinRate();
    }

    @Override
    public int gamesVsCount() {
        return statisticsVs.gamesCount;
    }

    @Override
    public int successVs() {
        return statisticsVs.successRate;
    }

    @Override
    public int winRateVs() {
        return statisticsVs.getWinRate();
    }

    @Override
    public int grade() {
        return 0;
    }

    @Override
    public int suggestedGrade() {
        return 0;
    }

    @Override
    public int age() {
        return 0;
    }

    @Override
    public boolean attributes() {
        return false;
    }

    @Override
    public boolean coming() {
        return false;
    }

    @Override
    public int winRate() {
        return 0; // TODO?
    }

    @Override
    public int games() {
        return 0; // TODO?
    }

    @Override
    public int success() {
        return 0; // TODO?
    }
    //endregion
}
