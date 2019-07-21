package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Equipment;
import org.pathfinderfr.app.database.entity.Weapon;

import java.util.Arrays;
import java.util.HashSet;

public class EquipmentFilter {

    private HashSet<String> filterCategory;
    private final String armorId, weaponId;

    public EquipmentFilter(String preferences, String armorId, String weaponId) {
        this.armorId = armorId;
        this.weaponId = weaponId;

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

    public boolean isFiltered(DBEntity entity) {
        if(entity instanceof Equipment) {
            return !filterCategory.contains(((Equipment)entity).getCategory());
        } else if(entity instanceof Armor) {
            return !filterCategory.contains(armorId);
        } else if(entity instanceof Weapon) {
            return !filterCategory.contains(weaponId);
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
            for (String cat : filterCategory) {
                buf.append(cat).append(':');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
}
