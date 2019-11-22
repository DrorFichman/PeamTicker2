package com.teampicker.drorfichman.teampicker.View;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.teampicker.drorfichman.teampicker.R;
import com.teampicker.drorfichman.teampicker.tools.SettingsHelper;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setAttemptsPreference();
    }

    private void setAttemptsPreference() {
        EditTextPreference attemptsPref = findPreference("divide_attempts");
        attemptsPref.setOnBindEditTextListener(editText ->
        {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setSingleLine();
        });
    }
}
