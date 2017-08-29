/*
 * Copyright 2017 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.worldreadableprefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Wraps shared preferences and adjusts r/w permissions on changes
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("SetWorldReadable")
final class WorldReadablePrefs implements SharedPreferences,
        WorldReadablePrefsManager.FileObserverListener {

    private static final String TAG = WorldReadablePrefs.class.getSimpleName();

    private final String prefsName;
    private final Context context;
    private final SharedPreferences prefs;
    private EditorWrapper editorWrapper;
    private boolean selfAttrChange;

    WorldReadablePrefs(@NonNull Context context) {
        this.context = context;
        this.prefsName = context.getPackageName() + "_preferences";
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

        maybePreCreateFile();

        fixPermissions(true);
    }

    WorldReadablePrefs(@NonNull Context context, @NonNull String prefsName) {
        this.context = context;
        this.prefsName = prefsName;
        this.prefs = context.getSharedPreferences(prefsName, 0);

        maybePreCreateFile();

        fixPermissions(true);
    }

    @Override
    public boolean contains(String key) {
        return prefs.contains(key);
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public EditorWrapper edit() {
        if (editorWrapper == null) {
            editorWrapper = new EditorWrapper(prefs.edit());
        }
        return editorWrapper;
    }

    @Override
    public Map<String, ?> getAll() {
        return prefs.getAll();
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    @Override
    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return prefs.getStringSet(key, defValues);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private void maybePreCreateFile() {
        try {
            File sharedPrefsFolder = new File(context.getFilesDir().getAbsolutePath()
                    + "/../shared_prefs");
            if (!sharedPrefsFolder.exists()) {
                sharedPrefsFolder.mkdir();
                sharedPrefsFolder.setExecutable(true, false);
                sharedPrefsFolder.setReadable(true, false);
            }
            File f = new File(sharedPrefsFolder.getAbsolutePath() + "/" + prefsName + ".xml");
            if (!f.exists()) {
                f.createNewFile();
                f.setReadable(true, false);
            }
        } catch (Exception e) {
            // catch
        }
    }

    private void fixPermissions(boolean force) {
        if (WorldReadablePrefsManager.DEBUG) {
            Log.d(TAG, "fixing permissions");
        }
        File sharedPrefsFolder = new File(context.getFilesDir().getAbsolutePath()
                + "/../shared_prefs");
        if (sharedPrefsFolder.exists()) {
            sharedPrefsFolder.setExecutable(true, false);
            sharedPrefsFolder.setReadable(true, false);
            File f = new File(sharedPrefsFolder.getAbsolutePath() + "/" + prefsName + ".xml");
            if (f.exists()) {
                selfAttrChange = !force;
                f.setReadable(true, false);
            }
        }
    }

    private void fixPermissions() {
        fixPermissions(false);
    }

    @Override
    public void onFileAttributesChanged(String path) {
        if (path != null && path.endsWith(prefsName + ".xml")) {
            if (selfAttrChange) {
                selfAttrChange = false;
                return;
            }
            fixPermissions();
        }
    }

    private final class EditorWrapper implements SharedPreferences.Editor {

        private final SharedPreferences.Editor editor;

        private EditorWrapper(SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        @Override
        public EditorWrapper putString(String key,
                                       String value) {
            editor.putString(key, value);
            return this;
        }

        @Override
        public EditorWrapper putStringSet(String key,
                                          Set<String> values) {
            editor.putStringSet(key, values);
            return this;
        }

        @Override
        public EditorWrapper putInt(String key,
                                    int value) {
            editor.putInt(key, value);
            return this;
        }

        @Override
        public EditorWrapper putLong(String key,
                                     long value) {
            editor.putLong(key, value);
            return this;
        }

        @Override
        public EditorWrapper putFloat(String key,
                                      float value) {
            editor.putFloat(key, value);
            return this;
        }

        @Override
        public EditorWrapper putBoolean(String key,
                                        boolean value) {
            editor.putBoolean(key, value);
            return this;
        }

        @Override
        public EditorWrapper remove(String key) {
            editor.remove(key);
            return this;
        }

        @Override
        public EditorWrapper clear() {
            editor.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return editor.commit();
        }

        @Override
        public void apply() {
            throw new UnsupportedOperationException(
                    "apply() not supported. Use commit() instead.");
        }
    }
}