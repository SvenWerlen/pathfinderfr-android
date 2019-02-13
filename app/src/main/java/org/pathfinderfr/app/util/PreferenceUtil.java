package org.pathfinderfr.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

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
}
