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
import android.os.AsyncTask;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages file file changes and creates word readable prefs instances
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("SetWorldReadable")
public final class WorldReadablePrefsManager {

    @SuppressLint("StaticFieldLeak")
    private static WorldReadablePrefsManager instance;

    private final Context context;
    private final List<FileObserverListener> fileObserverListeners = new ArrayList<>();

    private FileObserver fileObserver;

    private WorldReadablePrefsManager(Context context) {
        this.context = context;

        fixFolderPermissionsAsyncImpl();
        registerFileObserver();
    }

    /**
     * Initializes the world readable prefs manager
     */
    public static void init(@NonNull Context context) {
        if (instance == null) {
            instance = new WorldReadablePrefsManager(context.getApplicationContext());
        }
    }

    /**
     * Returns the default shared preferences
     */
    @NonNull
    public static SharedPreferences getDefaultPrefs() {
        if (instance == null) {
            throw new IllegalStateException("you have to call init first");
        }
        WorldReadablePrefs prefs = new WorldReadablePrefs(instance.context);
        instance.fileObserverListeners.add(prefs);
        return prefs;
    }

    /**
     * Returns a new shared preferences instance
     */
    @NonNull
    public static SharedPreferences getPrefs(@NonNull String name) {
        if (instance == null) {
            throw new IllegalStateException("you have to call init first");
        }
        WorldReadablePrefs prefs = new WorldReadablePrefs(instance.context, name);
        instance.fileObserverListeners.add(prefs);
        return prefs;
    }

    /**
     * This fixes the folder permissions of this app
     * It's recommend to call this in every activity on create
     */
    public static void fixFolderPermissionsAsync() {
        if (instance == null) {
            throw new IllegalStateException("you have to call init first");
        }

        instance.fixFolderPermissionsAsyncImpl();
    }

    private void fixFolderPermissionsAsyncImpl() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // main dir
                File pkgFolder = new File(context.getApplicationInfo().dataDir);
                if (pkgFolder.exists()) {
                    pkgFolder.setExecutable(true, false);
                    pkgFolder.setReadable(true, false);
                }
                // cache dir
                File cacheFolder = context.getCacheDir();
                if (cacheFolder.exists()) {
                    cacheFolder.setExecutable(true, false);
                    cacheFolder.setReadable(true, false);
                }
                // files dir
                File filesFolder = context.getFilesDir();
                if (filesFolder.exists()) {
                    filesFolder.setExecutable(true, false);
                    filesFolder.setReadable(true, false);
                    for (File f : filesFolder.listFiles()) {
                        f.setExecutable(true, false);
                        f.setReadable(true, false);
                    }
                }
                // shared prefs
                File sharedPrefsFolder = new File(context.getFilesDir().getAbsolutePath()
                        + "/../shared_prefs");
                if (sharedPrefsFolder.exists()) {
                    sharedPrefsFolder.setExecutable(true, false);
                    sharedPrefsFolder.setReadable(true, false);
                    for (File f : filesFolder.listFiles()) {
                        f.setExecutable(true, false);
                        f.setReadable(true, false);
                    }
                }
            }
        });
    }

    private void registerFileObserver() {
        File sharedPrefsFolder = new File(context.getFilesDir().getAbsolutePath()
                + "/../shared_prefs");
        fileObserver = new FileObserver(sharedPrefsFolder.getPath(),
                FileObserver.ATTRIB | FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, String path) {
                for (FileObserverListener l : fileObserverListeners) {
                    if ((event & FileObserver.ATTRIB) != 0) {
                        l.onFileAttributesChanged(path);
                    }
                }
            }
        };
        fileObserver.startWatching();
    }

    interface FileObserverListener {
        void onFileAttributesChanged(String path);
    }
}
