package org.pathfinderfr.app.util;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

public class RaceAlternateTraitFilter {

    private HashSet<Long> filterRace;

    public RaceAlternateTraitFilter(String preferences) {
        filterRace = new HashSet<>();

        Log.d(RaceAlternateTraitFilter.class.getSimpleName(), "Preferences: " + preferences );

        // load preferences
        if(preferences != null) {
            for(String r : Arrays.asList(preferences.split(","))) {
                try {
                    filterRace.add(Long.valueOf(r));
                } catch(NumberFormatException e) {}
            }
        }
    }

    public void clearFilters() {
        filterRace.clear();
    }

    public void addFilterRace(Long raceId) {
        filterRace.add(raceId);
    }

    public boolean hasAnyFilter() {
        return hasFilterRace();
    }

    /**
     * @param raceId race identifier
     * @return true if filter is enabled for given trait. false if disabled or "All" selected
     */
    public boolean isFilterRaceEnabled(Long raceId) {
        return filterRace.contains(raceId);
    }

    public boolean hasFilterRace() {
        return !filterRace.isEmpty();
    }

    public Long[] getFilterRace() {
        return filterRace.toArray(new Long[0]);
    }

    /**
     * @return filters as String (useful for import/export as preferences)
     */
    public String generatePreferences() {
        StringBuffer buf = new StringBuffer();
        if(!filterRace.isEmpty()) {
            for (Long cl : filterRace) {
                buf.append(cl).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
}
