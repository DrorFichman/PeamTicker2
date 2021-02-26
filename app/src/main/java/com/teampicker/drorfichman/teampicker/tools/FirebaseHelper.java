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
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;

public class FirebaseHelper {

    public interface DataCallback {
        void localDataChanged();
    }

    private enum Node {
        players,
        games,
    }

    private static DatabaseReference games() {
        return getNode(Node.games);
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

    public static void sync(Context ctx) {
        syncPlayers(ctx);
    }

    public static void pull(Context ctx, DataCallback handler) {
        pullPlayers(ctx, handler);
    }

    private static void storePlayer(Player p) {
        players().child(sanitizeKey(p.name())).setValue(p);
    }

    private static void pullPlayers(Context ctx, DataCallback handler) {
        ValueEventListener playerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("pullPlayers", "Delete local players DB");
                DbHelper.deletePlayersContents(ctx);

                for (DataSnapshot snapshotNode : dataSnapshot.getChildren()) {
                    Player p = snapshotNode.getValue(Player.class);
                    boolean created = DbHelper.insertPlayer(ctx, p);
                    if (!created) Log.e("pullPlayers", "Failed to insert " + p.mName);
                }

                Log.i("pullPlayers", "Local players DB updated from cloud");
                Toast.makeText(ctx, "Sync completed", Toast.LENGTH_LONG).show();
                handler.localDataChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("pull", "pullPlayers:onCancelled", databaseError.toException());
            }
        };

        players().addListenerForSingleValueEvent(playerListener);
    }

    private static void syncPlayers(Context ctx) {
        players().removeValue((error, ref) -> {
            Log.i("syncPlayers", "Deleted " + error);
            if (error == null) {
                ArrayList<Player> players = DbHelper.getPlayers(ctx);
                for (Player p : players) {
                    storePlayer(p);
                }
                Log.i("syncPlayers", "Sync completed");
                Toast.makeText(ctx, "Sync completed", Toast.LENGTH_LONG).show();
            } else {
                Log.i("syncPlayers", "Sync failed " + error);
                Toast.makeText(ctx, "Failed to sync data " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void syncGames(Context ctx) {
        // TODO same for games history
    }

    private static void pullGames(Context ctx, DataCallback handler) {
        // TODO same for games history
    }
}
