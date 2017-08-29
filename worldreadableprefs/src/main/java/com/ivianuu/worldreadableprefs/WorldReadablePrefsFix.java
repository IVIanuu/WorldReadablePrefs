package com.ivianuu.worldreadableprefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.FileObserver;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * World readable prefs fix
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class WorldReadablePrefsFix {

    private final Context context;
    private final List<String> prefsToFix;

    private FileObserver fileObserver;
    private boolean selfChange;

    private WorldReadablePrefsFix(Builder builder) {
        this.context = builder.context;
        this.prefsToFix = builder.prefsToFix;

        maybeCreatePrefFiles();
    }

    /**
     * Starts fixing the permissions on changes
     */
    public void start() {
        if (fileObserver != null) {
            // already started
            return;
        }

        fixPermissions();

        File sharedPrefsFolder = new File(context.getFilesDir().getAbsolutePath()
                + "/../shared_prefs");
        fileObserver = new FileObserver(sharedPrefsFolder.getPath(),
                FileObserver.ATTRIB | FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, String path) {
                if (selfChange) {
                    // block self change
                    return;
                }
                if (isValid(path)) {
                    fixPermissions();
                }
            }
        };

        fileObserver.startWatching();
    }

    /**
     * Stops fixing the permissions on changes
     */
    public void stop() {
        if (fileObserver == null) {
            // already stopped
            return;
        }

        fileObserver.stopWatching();
        fileObserver = null;
    }

    private boolean isValid(String name) {
        return name != null && prefsToFix.contains(name.replace(".xml", ""));
    }

    private void fixPermissions() {
        selfChange = true;
        AsyncTask.execute(new Runnable() {
            @SuppressLint("SetWorldReadable")
            @Override
            public void run() {
                // main dir
                File pkgFolder = new File(context.getApplicationInfo().dataDir);
                if (pkgFolder.exists()) {
                    pkgFolder.setExecutable(true, false);
                    pkgFolder.setReadable(true, false);
                }
                // shared prefs
                File sharedPrefsFolder = new File(context.getFilesDir().getAbsolutePath()
                        + "/../shared_prefs");
                if (sharedPrefsFolder.exists()) {
                    sharedPrefsFolder.setExecutable(true, false);
                    sharedPrefsFolder.setReadable(true, false);
                    // shared pref childs
                    for (File f : sharedPrefsFolder.listFiles()) {
                        if (isValid(f.getPath().replace(sharedPrefsFolder.getPath() + "/", ""))) {
                            f.setExecutable(true, false);
                            f.setReadable(true, false);
                        }
                    }
                }

                selfChange = false;
            }
        });
    }

    private void maybeCreatePrefFiles() {
        AsyncTask.execute(new Runnable() {
            @SuppressLint("SetWorldReadable")
            @Override
            public void run() {
                for (String name : prefsToFix) {
                    try {
                        File sharedPrefsFolder = new File(context.getFilesDir().getAbsolutePath()
                                + "/../shared_prefs");
                        if (!sharedPrefsFolder.exists()) {
                            sharedPrefsFolder.mkdir();
                            sharedPrefsFolder.setExecutable(true, false);
                            sharedPrefsFolder.setReadable(true, false);
                        }
                        File f = new File(sharedPrefsFolder.getAbsolutePath() + "/" + name + ".xml");
                        if (!f.exists()) {
                            f.createNewFile();
                            f.setReadable(true, false);
                        }
                    } catch (Exception e) {
                        // catch
                    }
                }
            }
        });
    }

    /**
     * Returns a new builder
     */
    @NonNull
    public static Builder builder(@NonNull Context context) {
        return new Builder(context.getApplicationContext());
    }

    public static class Builder {

        private final Context context;
        private final List<String> prefsToFix = new ArrayList<>();

        private Builder(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        public Builder fixDefault() {
            return fix(context.getPackageName() + "_preferences");
        }

        /**
         * Adds the name to list of files to fix
         */
        @NonNull
        public Builder fix(@NonNull String name) {
            prefsToFix.add(name);
            return this;
        }

        /**
         * Builds the fix
         */
        @NonNull
        public WorldReadablePrefsFix build() {
            return new WorldReadablePrefsFix(this);
        }

        /**
         * Builds and starts the fix
         */
        @NonNull
        public WorldReadablePrefsFix start() {
            WorldReadablePrefsFix fix = build();
            fix.start();
            return fix;
        }
    }
}
