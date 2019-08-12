package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.entity.Trait;

import java.util.Arrays;
import java.util.HashSet;

public class TraitFilter {

    public static final long FILTER_RACE_HIDE_ALL = -1;
    public static final long FILTER_RACE_SHOW_ALL = 0;
    public static final String FILTER_TYPE_HIDE_ALL = "hideall";
    //public static final String FILTER_TYPE_SHOW_ALL = null;

    private Long filterRace;
    private String filterType;

    public TraitFilter(String preferences) {
        Log.d(TraitFilter.class.getSimpleName(), "Preferences: " + preferences );

        filterRace = FILTER_RACE_SHOW_ALL;
        filterType = null;

        // load preferences
        if(preferences != null) {
            String[] prefs = preferences.split("#");

            if(prefs.length > 0) {
                try {
                    filterRace = Long.parseLong(prefs[0]);
                } catch(NumberFormatException nfe) {
                    Log.w(TraitFilter.class.getSimpleName(), "Invalid raceId " + prefs[0]);
                }
            }
            if(prefs.length > 1) {
                filterType = prefs[1].length() > 0 ? prefs[1] : null;
            }
        }
    }

    public void clearFilters() {
        filterRace = FILTER_RACE_SHOW_ALL;
        filterType = null;
    }

    public boolean isTraitVisible(Trait trait) {
        if(trait.isAltRacialTrait()) {
            return filterRace == FILTER_RACE_SHOW_ALL || (trait.getRace() != null && trait.getRace().getId() == filterRace);
        } else {
            return filterType == null || (trait.getName().contains(filterType.toLowerCase()));
        }
    }

    public void setRace(Long raceId) {
        filterRace = raceId == null ? -1L : raceId;
    }

    public Long getRace() { return filterRace; }

    public boolean hasAnyFilter() {
        return filterRace != FILTER_RACE_SHOW_ALL || filterType != null;
    }

    public void setType(String type) {
        filterType = type;
    }

    public String getType() { return filterType; }

    /**
     * @return filters as String (useful for import/export as preferences)
     */
    public String generatePreferences() {
        StringBuffer buf = new StringBuffer();
        buf.append(filterRace);
        buf.append("#");
        buf.append(filterType != null ? filterType : "");
        return buf.toString();
    }
}
