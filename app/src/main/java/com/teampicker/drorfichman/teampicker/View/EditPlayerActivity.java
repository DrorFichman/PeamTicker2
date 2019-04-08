package com.teampicker.drorfichman.teampicker.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerDbHelper;
import com.teampicker.drorfichman.teampicker.R;

public class EditPlayerActivity extends AppCompatActivity {

    public static final String PLAYER = "player";

    private Player pPlayer;
    private EditText vGrade;
    private EditText vName;
    private TextView vResults;
    private CheckBox isGK;
    private CheckBox isDefender;
    private CheckBox isPlaymaker;

    @NonNull
    public static Intent getEditPlayerIntent(Context context, String playerName) {
        Intent intent = new Intent(context, EditPlayerActivity.class);
        intent.putExtra(EditPlayerActivity.PLAYER, playerName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_player);

        Intent intent = getIntent();
        if (intent.hasExtra(PLAYER)) {
            pPlayer = DbHelper.getPlayer(this, intent.getStringExtra(PLAYER));
        }

        vName = (EditText) findViewById(R.id.edit_player_name);
        vGrade = (EditText) findViewById(R.id.edit_player_grade);
        vResults = (TextView) findViewById(R.id.player_results);
        isGK = (CheckBox) findViewById(R.id.player_is_gk);
        isDefender = (CheckBox) findViewById(R.id.player_is_defender);
        isPlaymaker = (CheckBox) findViewById(R.id.player_is_playmaker);

        // TODO change to stars
        // TODO plus/minus ratio

        vName.setText(pPlayer.mName);
        vName.setEnabled(false);
        vGrade.setHint(String.valueOf(pPlayer.mGrade));

        vResults.setText(pPlayer.getResults());

        initPlayerAttributes();

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(1);

                String stringGrade = vGrade.getText().toString();
                if (!TextUtils.isEmpty(stringGrade)) {
                    Integer newGrade = Integer.valueOf(stringGrade);

                    if (newGrade > 99 || newGrade < 0) {
                        Toast.makeText(getApplicationContext(), "Score must be between 0-99", Toast.LENGTH_LONG).show();
                        return;
                    }

                    DbHelper.updatePlayer(getApplicationContext(), pPlayer.mName, newGrade);
                    Toast.makeText(getApplicationContext(), "Player updated", Toast.LENGTH_LONG).show();
                }

                finishNow(1);
            }
        });

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishNow(0);
            }
        });
    }

    private void initPlayerAttributes() {
        isGK.setChecked(pPlayer.isGK);
        isDefender.setChecked(pPlayer.isDefender);
        isPlaymaker.setChecked(pPlayer.isPlaymaker);

        isGK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerDbHelper.setIsGK(EditPlayerActivity.this, pPlayer.mName, isGK.isChecked());
                Toast.makeText(EditPlayerActivity.this, "Player's attribute saved", Toast.LENGTH_SHORT).show();
            }
        });

        isDefender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerDbHelper.setIsDefender(EditPlayerActivity.this, pPlayer.mName, isDefender.isChecked());
                Toast.makeText(EditPlayerActivity.this, "Player's attribute saved", Toast.LENGTH_SHORT).show();
            }
        });

        isPlaymaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerDbHelper.setIsPlaymaker(EditPlayerActivity.this, pPlayer.mName, isPlaymaker.isChecked());
                Toast.makeText(EditPlayerActivity.this, "Player's attribute saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        vGrade.setFocusableInTouchMode(true);
        vGrade.requestFocus();
    }

    private void finishNow(int result) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vGrade.getWindowToken(), 0);

        setResult(result);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
