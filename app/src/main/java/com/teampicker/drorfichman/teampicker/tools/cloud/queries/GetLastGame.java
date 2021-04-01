package com.teampicker.drorfichman.teampicker.tools.cloud.queries;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.tools.cloud.FirebaseHelper;

public class GetLastGame {

    public interface Results {
        void queryResults(Game result);
    }

    public static void query(Context ctx, Results caller) {
        ValueEventListener query = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Game g = null;
                for (DataSnapshot snapshotNode : dataSnapshot.getChildren()) {
                    g = snapshotNode.getValue(Game.class);
                    Log.i("Games", "Game " + g.gameId + " = " + g.dateString);
                }

                caller.queryResults(g);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("pullGamesFromCloud", "onCancelled", databaseError.toException());
            }
        };
        FirebaseHelper.games().orderByKey().limitToLast(1).addListenerForSingleValueEvent(query);
    }
}
