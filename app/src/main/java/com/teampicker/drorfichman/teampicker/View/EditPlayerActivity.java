package com.teampicker.drorfichman.teampicker.View;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EditPlayerActivity extends AppCompatActivity {

    public static final String PLAYER = "player";

    private Player pPlayer;
    private EditText vName;
    private EditText vGrade;
    private Button vBirth;
    private TextView vResults;
    private TextView vResultsSummary;
    private CheckBox isGK;
    private CheckBox isDefender;
    private CheckBox isPlaymaker;
    private CheckBox isUnbreakable;

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

        vBirth = (Button) findViewById(R.id.edit_player_birthday);

        vResults = (TextView) findViewById(R.id.player_results);
        vResultsSummary = (TextView) findViewById(R.id.player_results_summary);
        isGK = (CheckBox) findViewById(R.id.player_is_gk);
        isDefender = (CheckBox) findViewById(R.id.player_is_defender);
        isPlaymaker = (CheckBox) findViewById(R.id.player_is_playmaker);
        isUnbreakable = (CheckBox) findViewById(R.id.player_is_unbreaking);

        // TODO change to stars
        // TODO plus/minus ratio

        vName.setText(pPlayer.mName);
        vGrade.setHint(String.valueOf(pPlayer.mGrade));

        if (pPlayer.mBirthYear > 0) setBirthday(pPlayer.mBirthYear, pPlayer.mBirthMonth);

        vResults.setText(pPlayer.getResults());
        vResults.setOnClickListener(resultsClicked);

        if (pPlayer != null && pPlayer.statistics != null) {
            vResultsSummary.setText(
                    getString(R.string.player_stat,
                            String.valueOf(pPlayer.statistics.gamesCount),
                            String.valueOf(pPlayer.statistics.wins)));
            vResultsSummary.setOnClickListener(resultsClicked);
        }

        initPlayerAttributes();

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(1);
                boolean playerUpdated = false;

                String newGradeNumber = vGrade.getText().toString();
                String newName = vName.getText().toString();

                if (!TextUtils.isEmpty(newGradeNumber)) { // update grade
                    Integer newGradeString = Integer.valueOf(newGradeNumber);

                    if (newGradeString > 99 || newGradeString < 0) {
                        Toast.makeText(getApplicationContext(), "Score must be between 0-99", Toast.LENGTH_LONG).show();
                        return;
                    }
                    DbHelper.updatePlayerGrade(getApplicationContext(), pPlayer.mName, newGradeString);
                    playerUpdated = true;
                }

                if (!TextUtils.isEmpty(newName) && !pPlayer.mName.equals(newName)) { // update name
                    boolean updated = DbHelper.updatePlayerName(getApplicationContext(), pPlayer, newName);
                    if (updated) {
                        playerUpdated = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "Player name is already taken", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (vBirth.getTag() != null) { // update birth
                    String date = (String) vBirth.getTag();
                    Integer newYear = Integer.valueOf(date.split("/")[1]);
                    Integer newMonth = Integer.valueOf(date.split("/")[0]);

                    if (newYear < 1900 || newYear > Calendar.getInstance().get(Calendar.YEAR)) {
                        Toast.makeText(getApplicationContext(), "Year must be between 1900-now", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Log.i("AGE", "Year " + newYear + " month " + newMonth);
                    DbHelper.updatePlayerBirth(getApplicationContext(), pPlayer.mName, newYear, newMonth);
                    playerUpdated = true;
                }

                // update attributes
                setAttributes(pPlayer);

                if (playerUpdated)
                    Toast.makeText(getApplicationContext(), "Player updated", Toast.LENGTH_LONG).show();

                finishNow(1);
            }
        });

        findViewById(R.id.player_participation_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();

                Intent intent = PlayerParticipationActivity.getPlayerParticipationActivity(EditPlayerActivity.this, pPlayer.mName);
                startActivity(intent);
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

    private View.OnClickListener resultsClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideKeyboard();
            startActivity(GamesActivity.getGameActivityIntent(EditPlayerActivity.this, pPlayer.mName));
        }
    };

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vGrade.getWindowToken(), 0);
    }

    private void initPlayerAttributes() {
        isGK.setChecked(pPlayer.isGK);
        isDefender.setChecked(pPlayer.isDefender);
        isPlaymaker.setChecked(pPlayer.isPlaymaker);
        isUnbreakable.setChecked(pPlayer.isUnbreakable);
    }

    private void setAttributes(Player p) {
        p.isGK = isGK.isChecked();
        p.isDefender = isDefender.isChecked();
        p.isPlaymaker = isPlaymaker.isChecked();
        p.isUnbreakable = isUnbreakable.isChecked();
        DbHelper.updatePlayerAttributes(this, p);
    }

    @Override
    public void onResume() {
        super.onResume();

        vGrade.setFocusableInTouchMode(true);
        vGrade.requestFocus();
    }

    private void finishNow(int result) {
        hideKeyboard();

        setResult(result);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void showDatePicker(View view) {
        int year = pPlayer.mBirthYear > 0 ? pPlayer.mBirthYear : 2000;
        int month = pPlayer.mBirthMonth > 0 ? pPlayer.mBirthMonth - 1 : 0;
        DatePickerDialog d = new DatePickerDialog(this, 0, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int i2) {
                month++; // starts at 0...
                Log.i("AGE", "set year " + year + " month " + month);
                setBirthday(year, month);
            }
        }, year , month, 1);
        d.show();
    }

    private void setBirthday(int year, int month) {
        vBirth.setText(month + "/" + year);
        vBirth.setTag(month + "/" + year);
    }
}
