package com.teampicker.drorfichman.teampicker.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Game;
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;

public class FirebaseHelper {

    public interface DataCallback {
        void DataChanged();
    }

    private enum Node {
        players,
        games,
        playersGames
    }

    private static DatabaseReference games() {
        return getNode(Node.games);
    }

    private static DatabaseReference playersGames() {
        return getNode(Node.playersGames);
    }

    private static DatabaseReference players() {
        return getNode(Node.players);
    }

    private static DatabaseReference getNode(Node node) {
        if (AuthHelper.getUser() == null || AuthHelper.getUerUID().isEmpty()) {
            Log.e("AccountFB", "User is not connected" + AuthHelper.getUser());
            return null;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference(AuthHelper.getUerUID()).child(node.name());
    }

    public static String sanitizeKey(String key) {
        if (key == null) return null;
        return key.replaceAll("\\.", "");
    }

    public static void syncToCloud(Context ctx) {
        syncPlayersToCloud(ctx, () ->
                syncGamesToCloud(ctx, () ->
                        syncPlayersGamesToCloud(ctx, () ->
                                Toast.makeText(ctx, "Sync completed", Toast.LENGTH_LONG).show())));
    }


    public static void pullFromCloud(Context ctx, DataCallback handler) {
        pullPlayersFromCloud(ctx, () ->
                pullGamesFromCloud(ctx, () ->
                        pullPlayersGamesFromCloud(ctx, () -> {
                            Toast.makeText(ctx, "Pull completed", Toast.LENGTH_LONG).show();
                            handler.DataChanged();
                        })));
    }

    private static void pullPlayersFromCloud(Context ctx, DataCallback handler) {
        ValueEventListener playerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("pullPlayersFromCloud", "Delete local players DB");
                DbHelper.deletePlayersContents(ctx);

                for (DataSnapshot snapshotNode : dataSnapshot.getChildren()) {
                    Player p = snapshotNode.getValue(Player.class);
                    boolean created = DbHelper.insertPlayer(ctx, p);
                    if (!created) Log.e("pullPlayersFromCloud", "Failed to insert " + p.mName);
                }

                Log.i("pullPlayersFromCloud", "Local players DB updated from cloud");
                handler.DataChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("pullPlayersFromCloud", "onCancelled", databaseError.toException());
            }
        };

        players().addListenerForSingleValueEvent(playerListener);
    }

    private static void syncPlayersToCloud(Context ctx, DataCallback handler) {
        players().removeValue((error, ref) -> {
            Log.i("syncPlayersToCloud", "Deleted " + error);
            if (error == null) {
                ArrayList<Player> players = DbHelper.getPlayers(ctx);
                for (Player p : players) {
                    storePlayer(p);
                }
                Log.i("syncPlayersToCloud", "Sync completed");
                handler.DataChanged();
            } else {
                Log.i("syncPlayersToCloud", "Sync failed " + error);
                Toast.makeText(ctx, "Failed to sync players data " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void syncGamesToCloud(Context ctx, DataCallback handler) {
        games().removeValue((error, ref) -> {
            Log.i("syncGamesToCloud", "Deleted " + error);
            if (error == null) {
                ArrayList<Game> games = DbHelper.getGames(ctx);
                for (Game g : games) {
                    storeGame(g);
                }
                Log.i("syncGamesToCloud", "Sync completed");
                handler.DataChanged();
            } else {
                Log.i("syncGamesToCloud", "Sync failed " + error);
                Toast.makeText(ctx, "Failed to sync games data " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void pullGamesFromCloud(Context ctx, DataCallback handler) {
        ValueEventListener gamesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("pullGamesFromCloud", "Delete local players DB");
                DbHelper.deleteGamesContents(ctx);

                for (DataSnapshot snapshotNode : dataSnapshot.getChildren()) {
                    Game g = snapshotNode.getValue(Game.class);
                    DbHelper.insertGame(ctx, g);
                }

                Log.i("pullGamesFromCloud", "Local games DB updated from cloud");
                handler.DataChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("pullGamesFromCloud", "onCancelled", databaseError.toException());
            }
        };

        games().addListenerForSingleValueEvent(gamesListener);
    }

    private static void pullPlayersGamesFromCloud(Context ctx, DataCallback handler) {
        handler.DataChanged();
    }

    private static void syncPlayersGamesToCloud(Context ctx, DataCallback handler) {
        handler.DataChanged();
    }

    private static void storePlayer(Player p) {
        players().child(sanitizeKey(p.name())).setValue(p);
    }

    private static void storeGame(Game g) {
        games().child(String.valueOf(g.gameId)).setValue(g);
    }
    }
}

