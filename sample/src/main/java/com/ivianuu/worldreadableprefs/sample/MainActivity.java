package com.ivianuu.worldreadableprefs.sample;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ivianuu.worldreadableprefs.WorldReadablePrefsManager;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // normally you would call this in your application class
        WorldReadablePrefsManager.init(this);
        WorldReadablePrefsManager.setDebug(true);
        WorldReadablePrefsManager.fixFolderPermissionsAsync();
        WorldReadablePrefsManager.getPrefs("my_prefs");

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Prefs()).commit();
        }
    }

    public static class Prefs extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName("my_prefs");
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
