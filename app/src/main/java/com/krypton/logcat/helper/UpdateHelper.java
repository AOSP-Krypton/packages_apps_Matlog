package com.krypton.logcat.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.krypton.logcat.util.Callback;
import com.krypton.logcat.util.Function;

import com.krypton.logcat.R;

/**
 * Helper for applying app-wide updates of persistent data.
 *
 * @author nlawson
 */
public class UpdateHelper {

    public static boolean areUpdatesNecessary(Context context) {
        for (Update update : Update.values()) {
            if (update.getIsNecessary().apply(context)) {
                return true;
            }
        }
        return false;
    }

    public static void runUpdatesIfNecessary(Context context) {
        for (Update update : Update.values()) {
            if (update.getIsNecessary().apply(context)) {
                update.getRunUpdate().onCallback(context);
            }
        }
    }

    private enum Update {

        // update to move saved logs from /sdcard/catlog_saved_logs to /sdcard/catlog/saved_logs
        Update2(new Function<Context, Boolean>() {

            @Override
            public Boolean apply(Context context) {
                return SaveLogHelper.checkIfSdCardExists() && SaveLogHelper.legacySavedLogsDirExists();
            }
        }, new Callback<Context>() {

            @Override
            public void onCallback(Context context) {
                if (SaveLogHelper.checkIfSdCardExists()) {
                    SaveLogHelper.moveLogsFromLegacyDirIfNecessary();
                }
            }
        }),

        // update to request superuser READ_LOGS permission on JellyBean
        Update3(new Function<Context, Boolean>() {

            @Override
            public Boolean apply(Context context) {

                boolean isJellyBean = VersionHelper.getVersionSdkIntCompat() >= VersionHelper.VERSION_JELLYBEAN;

                return isJellyBean && !PreferenceHelper.getJellybeanRootRan(context);
            }
        }, new Callback<Context>() {

            @Override
            public void onCallback(Context context) {
                SuperUserHelper.requestRoot(context);
            }
        }),;

        private Function<Context, Boolean> isNecessary;
        private Callback<Context> runUpdate;

        Update(Function<Context, Boolean> isNecessary, Callback<Context> runUpdate) {
            this.isNecessary = isNecessary;
            this.runUpdate = runUpdate;
        }

        public Function<Context, Boolean> getIsNecessary() {
            return isNecessary;
        }

        public Callback<Context> getRunUpdate() {
            return runUpdate;
        }
    }
}
