package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.entity.ClassFeature;

import java.util.Arrays;
import java.util.HashSet;

public class ClassFeatureFilter {

    public static final long FILTER_CLASS_SHOW_ALL = 0;
    public static final long FILTER_ARCH_BASE = 0;

    private long filterClass;
    private long filterArchetype;

    public ClassFeatureFilter(String preferences) {
        filterClass = FILTER_CLASS_SHOW_ALL;
        filterArchetype = FILTER_ARCH_BASE;

        Log.d(ClassFeatureFilter.class.getSimpleName(), "Preferences: " + preferences );

        // load preferences
        if(preferences != null) {
            String[] prefs = preferences.split(":", -1);
            if(prefs.length == 2) {
                try {
                    if(prefs[0].length() > 0) {
                        filterClass = Long.parseLong(prefs[0]);
                    }
                    if(prefs[1].length() > 0) {
                        filterArchetype = Long.parseLong(prefs[1]);
                    }
                } catch(NumberFormatException e) {}
            }
        }
    }

    public void clearFilters() {
        filterClass = FILTER_CLASS_SHOW_ALL;
        filterArchetype = FILTER_ARCH_BASE;
    }

    public long getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(long classId) {
        filterClass = classId;
    }

    public long getFilterArchetype() {
        return filterArchetype;
    }

    public void setFilterArchetype(long archId) {
        filterArchetype = archId;
    }

    public boolean hasAnyFilter() {
        return filterClass > 0 || filterArchetype > 0;
    }

    public boolean isFiltered(ClassFeature cf) {
        if(filterClass == FILTER_CLASS_SHOW_ALL) {
            return false;
        }
        // class doesn't match
        if(cf.getClass_().getId() != filterClass) {
            return true;
        }
        // archetype doesn't match
        if(filterArchetype == FILTER_ARCH_BASE) {
            return cf.getClassArchetype() != null;
        } else {
            return cf.getClassArchetype() == null || cf.getClassArchetype().getId() != filterArchetype;
        }
    }

                              /**
     * @return filters as String (useful for import/export as preferences)
     */
    public String generatePreferences() {
        StringBuffer buf = new StringBuffer();
        if(filterClass > 0) {
            buf.append(filterClass);
        }
        buf.append(':');
        if(filterClass > 0) {
            buf.append(filterArchetype);
        }
        return buf.toString();
    }
}
