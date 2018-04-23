package com.teampicker.drorfichman.teampicker.View;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.PlayerDbHelper;
import com.teampicker.drorfichman.teampicker.R;

public class NewPlayerActivity extends AppCompatActivity {

    private EditText vGrade;
    private EditText vName;
    private CheckBox isGK;
    private CheckBox isDefender;
    private CheckBox isPlaymaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_player);

        vName = (EditText) findViewById(R.id.edit_player_name);
        vGrade = (EditText) findViewById(R.id.edit_player_grade);
        isGK = (CheckBox) findViewById(R.id.player_is_gk);
        isDefender = (CheckBox) findViewById(R.id.player_is_defender);
        isPlaymaker = (CheckBox) findViewById(R.id.player_is_playmaker);

        isGK.setVisibility(View.GONE);
        isDefender.setVisibility(View.GONE);
        isPlaymaker.setVisibility(View.GONE);

        // TODO change to stars
        // TODO plus/minus ratio

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

                boolean isCreated = createNewPlayer(newName, newGrade, isGK.isChecked(), isDefender.isChecked(), isPlaymaker.isChecked());
                if (isCreated) {
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

    private boolean createNewPlayer(String name, int grade, boolean isGK, boolean isDefender, boolean isPlaymaker) {
        boolean inserted = DbHelper.insertPlayer(NewPlayerActivity.this, name, grade);
        if (inserted) {
            PlayerDbHelper.setIsGK(NewPlayerActivity.this, name, isGK);
            PlayerDbHelper.setIsPlaymaker(NewPlayerActivity.this, name, isPlaymaker);
            PlayerDbHelper.setIsDefender(NewPlayerActivity.this, name, isDefender);
        }
        return inserted;
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
}
