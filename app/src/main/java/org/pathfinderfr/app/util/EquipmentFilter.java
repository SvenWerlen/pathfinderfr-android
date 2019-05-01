package org.pathfinderfr.app.util;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

public class EquipmentFilter {

    private HashSet<String> filterCategory;

    public EquipmentFilter(String preferences) {
        filterCategory = new HashSet<>();

        Log.d(EquipmentFilter.class.getSimpleName(), "Preferences: " + preferences );

        // load preferences
        if(preferences != null && preferences.length() > 0) {
            String[] prefs = preferences.split(":", -1);
            filterCategory.addAll(Arrays.asList(prefs));
        }
    }

    public void clearFilters() {
        filterCategory.clear();
    }

    public void addFilterCategory(String category) {
        filterCategory.add(category);
    }

    public boolean hasAnyFilter() {
        return hasFilterCategory();
    }

    /**
     * @param category equipment category
     * @return true if filter is enabled for given category. false if disabled or "All" selected
     */
    public boolean isFilterCategoryEnabled(String category) {
        return filterCategory.contains(category);
    }

    public boolean hasFilterCategory() {
        return !filterCategory.isEmpty();
    }

    public Long[] getFilterClass() {
        return filterCategory.toArray(new Long[0]);
    }

    /**
     * @return filters as String (useful for import/export as preferences)
     */
    public String generatePreferences() {
        StringBuffer buf = new StringBuffer();
        if(!filterCategory.isEmpty()) {
            for (String cat : filterCategory) {
                buf.append(cat).append(':');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
}
