package com.teampicker.drorfichman.teampicker.Data;

import java.util.Date;

public class BuilderPlayerCollaborationStatistics {
    GamesPlayersCache cache;
    int games = -1;
    Date upTo;

    public BuilderPlayerCollaborationStatistics setGames(int n) {
        games = n;
        return this;
    }
    public BuilderPlayerCollaborationStatistics setUpToDate(Date d) {
        upTo = d;
        return this;
    }
    public BuilderPlayerCollaborationStatistics setCached() {
        cache = new GamesPlayersCache();
        return this;
    }
}

