package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.Spell;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellFilter {

    private HashSet<String> filterSchool;
    private HashSet<Long> filterClass;
    private HashSet<Long> filterLevel;
    private int filterMaxLevel;

    public SpellFilter(String preferences) {
        filterSchool = new HashSet<>();
        filterClass = new HashSet<>();
        filterLevel = new HashSet<>();
        filterMaxLevel = 9;

        Log.d(SpellFilter.class.getSimpleName(), "Preferences: " + preferences );

        // load preferences
        if(preferences != null) {
            String[] prefs = preferences.split(":", -1);
            if(prefs.length == 3) {
                if(prefs[0].length()>0) {
                    filterSchool.addAll(Arrays.asList(prefs[0].split(",")));
                }
                if(prefs[1].length()>0) {
                    for(String cl : Arrays.asList(prefs[1].split(","))) {
                        try {
                            filterClass.add(Long.valueOf(cl));
                        } catch(NumberFormatException e) {}
                    }
                }
                if(prefs[2].length()>0) {
                    for(String lvl : Arrays.asList(prefs[2].split(","))) {
                        try {
                            filterLevel.add(Long.valueOf(lvl));
                        } catch(NumberFormatException e) {}
                    }
                }
            }
        }
    }

    public void clearFilters() {
       filterSchool.clear();
       filterClass.clear();
       filterLevel.clear();
       filterMaxLevel = 9;
    }

    public void setFilterMaxLevel(int filterMaxLevel) {
        this.filterMaxLevel = filterMaxLevel;
    }

    public int getFilterMaxLevel() {
        return filterMaxLevel;
    }

    public void addFilterSchool(String school) {
        filterSchool.add(school.toLowerCase());
    }

    public void addFilterClass(Long classId) {
        filterClass.add(classId);
    }

    public void addFilterLevel(Long level) {
        filterLevel.add(level);
    }

    public boolean hasAnyFilter() {
        return hasFilterSchool() || hasFilterClass() || hasFilterLevel();
    }

    /**
     * @param school spell school
     * @return true if filter is enabled for given school. false if disabled or "All" selected
     */
    public boolean isFilterSchoolEnabled(String school) {
        return school != null && filterSchool.contains(school.toLowerCase());
    }

    public boolean hasFilterSchool() {
        return !filterSchool.isEmpty();
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
     * @param level spell level
     * @return true if filter is enabled for given level. false if disabled or "All" selected
     */
    public boolean isFilterLevelEnabled(Long level) {
        return filterLevel.contains(level);
    }

    public boolean hasFilterLevel() {
        return !filterLevel.isEmpty();
    }

    public Long[] getFilterLevel() {
        return filterLevel.toArray(new Long[0]);
    }

    /**
     * @return filters as String (useful for import/export as preferences)
     */
    public String generatePreferences() {
        StringBuffer buf = new StringBuffer();
        if(!filterSchool.isEmpty()) {
            for (String s : filterSchool) {
                buf.append(s).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append(':');
        if(!filterClass.isEmpty()) {
            for (Long cl : filterClass) {
                buf.append(cl).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append(':');
        if(!filterLevel.isEmpty()) {
            for (Long lvl : filterLevel) {
                buf.append(lvl).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
}
