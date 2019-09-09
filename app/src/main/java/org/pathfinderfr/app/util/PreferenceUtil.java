package org.pathfinderfr.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.database.entity.CharacterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PreferenceUtil {

    public static String[] getSources(Context ctx) {
        List<String> list = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        for(String source: ConfigurationUtil.getInstance().getAvailableSources()) {
            if(preferences.getBoolean("source_" + source, true)) {
                list.add(source.toUpperCase());
            }
        }
        return list.toArray(new String[0]);
    }

    public static boolean sourceIsActive(Context ctx, String source) {
        String[] activeSources = getSources(ctx);
        for(String src : activeSources) {
            if(src.equals(source)) {
                return true;
            }
        }
        return false;
    }

    public static synchronized String getApplicationUUID(Context ctx) {
        String uuid = PreferenceManager.getDefaultSharedPreferences(ctx).getString(MainActivity.KEY_APP_UUID, null);
        if(uuid == null) {
            uuid = UUID.randomUUID().toString();
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(MainActivity.KEY_APP_UUID, uuid).apply();
        }
        return uuid;
    }
}
