package com.teampicker.drorfichman.teampicker.View;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.R;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class NewPlayerActivity extends AppCompatActivity {

    private EditText vGrade;
    private EditText vName;
    private Button vBirth;
    private CheckBox isGK;
    private CheckBox isDefender;
    private CheckBox isPlaymaker;
    private CheckBox isUnbreakable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_player);

        vName = (EditText) findViewById(R.id.edit_player_name);
        vGrade = (EditText) findViewById(R.id.edit_player_grade);
        vBirth = (Button) findViewById(R.id.edit_player_birthday);
        isGK = (CheckBox) findViewById(R.id.player_is_gk);
        isDefender = (CheckBox) findViewById(R.id.player_is_defender);
        isPlaymaker = (CheckBox) findViewById(R.id.player_is_playmaker);
        isUnbreakable = (CheckBox) findViewById(R.id.player_is_unbreaking);

        findViewById(R.id.player_participation_btn).setVisibility(View.INVISIBLE);

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(1);

                String stringGrade = vGrade.getText().toString();
                if (TextUtils.isEmpty(stringGrade)) {
                    Toast.makeText(getApplicationContext(), "Fill player's grade", Toast.LENGTH_LONG).show();
                    return;
                }

                Integer newGrade = Integer.valueOf(stringGrade);

                if (newGrade > 99 || newGrade < 0) {
                    Toast.makeText(getApplicationContext(), "Grade must be between 0-99", Toast.LENGTH_LONG).show();
                    return;
                }

                String newName = vName.getText().toString().trim();
                if (TextUtils.isEmpty(newName)) {
                    Toast.makeText(getApplicationContext(), "Fill player's name", Toast.LENGTH_LONG).show();
                    return;
                }

                Player p = new Player(newName, newGrade);
                boolean isCreated = createNewPlayer(p);

                if (isCreated) {
                    setPlayerBirthday(p);
                    setAttributes(p);

                    Toast.makeText(getApplicationContext(), "Player added", Toast.LENGTH_LONG).show();

                    finishNow(1);
                } else {
                    Toast.makeText(getApplicationContext(), "Player name is already taken", Toast.LENGTH_LONG).show();
                }
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

    private void setAttributes(Player p) {
        p.isGK = isGK.isChecked();
        p.isDefender = isDefender.isChecked();
        p.isPlaymaker = isPlaymaker.isChecked();
        p.isUnbreakable = isUnbreakable.isChecked();
        DbHelper.updatePlayerAttributes(this, p);
    }

    private void setPlayerBirthday(Player p) {
        if (vBirth.getTag() != null) {
            Calendar date =  (Calendar) vBirth.getTag();
            Integer newYear = date.get(Calendar.YEAR);
            Integer newMonth = date.get(Calendar.MONTH);

            if (newYear < 1900 || newYear > Calendar.getInstance().get(Calendar.YEAR)) {
                Toast.makeText(getApplicationContext(), "Year must be between 1900-now", Toast.LENGTH_LONG).show();
                return;
            }

            DbHelper.updatePlayerBirth(getApplicationContext(), p.mName, newYear, newMonth);
        }
    }

    private boolean createNewPlayer(Player p) {
        return DbHelper.insertPlayer(NewPlayerActivity.this, p.mName, p.mGrade);
    }

    @Override
    public void onResume() {
        super.onResume();

        vName.setFocusableInTouchMode(true);
        vName.requestFocus();
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

    public void showDatePicker(View view) {
        DatePickerDialog d = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int i2) {
                month++;
                setBirthday(year, month);
            }
        }, 2005, 4, 25);
        d.show();
    }

    private void setBirthday(int year, int month) {
        vBirth.setText(month + "/" + year);
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        vBirth.setTag(date);
    }
}
