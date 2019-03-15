package org.pathfinderfr.app.util;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

public class ClassFeatureFilter {

    private HashSet<Long> filterClass;
    private int filterMaxLevel;

    public ClassFeatureFilter(String preferences) {
        filterClass = new HashSet<>();
        filterMaxLevel = 20;

        Log.d(ClassFeatureFilter.class.getSimpleName(), "Preferences: " + preferences );

        // load preferences
        if(preferences != null) {
            String[] prefs = preferences.split(":", -1);
            if(prefs.length == 2) {
                if(prefs[0].length()>0) {
                    for(String cl : Arrays.asList(prefs[0].split(","))) {
                        try {
                            filterClass.add(Long.valueOf(cl));
                        } catch(NumberFormatException e) {}
                    }
                }
                if(prefs[1].length()>0) {
                    try {
                        filterMaxLevel = Integer.valueOf(prefs[1]);
                    } catch(NumberFormatException e) {}
                }
            }
        }
    }

    public void clearFilters() {
       filterClass.clear();
    }

    public void setFilterMaxLevel(Integer filterMaxLevel) {
        this.filterMaxLevel = filterMaxLevel;
    }

    public Integer getFilterMaxLevel() {
        return filterMaxLevel;
    }

    public void addFilterClass(Long classId) {
        filterClass.add(classId);
    }

    public boolean hasAnyFilter() {
        return hasFilterClass() || filterMaxLevel < 20;
    }

    /**
     * @param classId spell classId
     * @return true if filter is enabled for given class. false if disabled or "All" selected
     */
    public boolean isFilterClassEnabled(Long classId) {
        return filterClass.contains(classId);
    }

    public boolean hasFilterClass() {
        return !filterClass.isEmpty();
    }

    public Long[] getFilterClass() {
        return filterClass.toArray(new Long[0]);
    }

    /**
     * @return filters as String (useful for import/export as preferences)
     */
    public String generatePreferences() {
        StringBuffer buf = new StringBuffer();
        if(!filterClass.isEmpty()) {
            for (Long cl : filterClass) {
                buf.append(cl).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append(':');
        buf.append(filterMaxLevel);
        return buf.toString();
    }
}
