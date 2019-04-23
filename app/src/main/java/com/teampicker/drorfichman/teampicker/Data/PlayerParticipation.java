package com.teampicker.drorfichman.teampicker.Data;

import com.teampicker.drorfichman.teampicker.Controller.StatisticsData;

import java.io.Serializable;

public class PlayerParticipation implements Serializable {

    public String mName;
    public String mParticipatedWith;

    public StatisticsData statisticsWith = new StatisticsData();
    public StatisticsData statisticsVs = new StatisticsData();
}
