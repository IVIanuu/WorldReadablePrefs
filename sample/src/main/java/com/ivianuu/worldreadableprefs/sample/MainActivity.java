package com.ivianuu.worldreadableprefs.sample;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        WorldReadablePrefsManager.fixFolderPermissionsAsync();

        // create prefs
        final SharedPreferences prefs = WorldReadablePrefsManager.getPrefs("my_prefs");

        // use it like normal with one exception
        // instead of apply you have to call commit!

        CheckBox testCheckbox = findViewById(R.id.test_checkbox);
        testCheckbox.setChecked(prefs.getBoolean("my_bool", false));
        testCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean("my_bool", b).commit();
            }
        });
    }
}
