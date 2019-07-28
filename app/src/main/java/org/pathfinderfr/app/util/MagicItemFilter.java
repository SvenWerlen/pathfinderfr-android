package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Equipment;
import org.pathfinderfr.app.database.entity.MagicItem;
import org.pathfinderfr.app.database.entity.Weapon;

import java.util.Arrays;
import java.util.HashSet;

public class MagicItemFilter {

    private HashSet<Integer> filterCategory;

    public MagicItemFilter(String preferences) {
        filterCategory = new HashSet<>();

        Log.d(MagicItemFilter.class.getSimpleName(), "Preferences: " + preferences );

        // load preferences
        if(preferences != null && preferences.length() > 0) {
            String[] prefs = preferences.split(":", -1);
            for(String p : prefs) {
                try {
                    filterCategory.add(Integer.parseInt(p));
                } catch(NumberFormatException e) {}
            }
        }
    }

    public void clearFilters() {
        filterCategory.clear();
    }

    public void addFilterCategory(int category) {
        filterCategory.add(category);
    }

    public boolean hasAnyFilter() {
        return hasFilterCategory();
    }

    /**
     * @param category magicitem category
     * @return true if filter is enabled for given category. false if disabled or "All" selected
     */
    public boolean isFilterCategoryEnabled(int category) {
        return filterCategory.contains(category);
    }

    public boolean isFiltered(DBEntity entity) {
        if(entity instanceof MagicItem) {
            return !filterCategory.contains(((MagicItem)entity).getType());
        }
        return false;
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
            for (Integer cat : filterCategory) {
                buf.append(cat).append(':');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
}
