package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.FeatFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatsUtil {

    /**
     * This function generates a list of feats that are required for the specified feat
     * The list is ordered as a path to the specified feat
     */
    public static List<Feat> getRequiredFeatsPath(Feat feat) {
        DBHelper dbHelper = DBHelper.getInstance(null);
        if(dbHelper == null || feat == null || feat.getRequires().size() == 0) {
            return null;
        }
        // retrieve IDs
        long[] ids = new long[feat.getRequires().size()];
        for(int i = 0; i<feat.getRequires().size(); i++) {
            ids[i] = feat.getRequires().get(i);
        }
        // retrieve feats from IDs
        List<DBEntity> requires = dbHelper.fetchAllEntitiesById(ids, FeatFactory.getInstance());
        // sort feats from root
        List<Feat> result = new ArrayList<>();
        // list of all considered feats
        Set<Long> all = new HashSet<>();
        for(DBEntity e : requires) {
            all.add(e.getId());
        }
        // list of added feats
        Set<Long> added = new HashSet<>();
        while(true) {
            Feat f = getNextFeat(requires, all, added);
            if(f == null || result.size() > requires.size()) {
                break;
            }
            System.out.println("Found : " + f.getName());
            result.add(f);
            added.add(f.getId());
        }
        // list should contain the same number of element (only sorted)
        if(result.size() != requires.size()) {
            Log.w(FeatsUtil.class.getSimpleName(), String.format("Sorting didn't work as expected (%d != %d)", result.size(), requires.size()));
        }
        return result;
    }

    /**
     * Searches for the next feat with no dependency
     * @param source list of feats
     * @param treated list of feats (ids) already treated
     * @return
     */
    private static Feat getNextFeat(List<DBEntity> source, Set<Long> ids, Set<Long> treated) {
        for(DBEntity e : source) {
            // ignore if feat already treated
            if(treated.contains(e.getId())) {
                continue;
            }
            Feat f = (Feat)e;
            boolean depNotMeet = false;
            System.out.println("Considering : " + f.getName());
            for(long id : f.getRequires()) {
                if(!treated.contains(id) && ids.contains(id)) {
                    depNotMeet = true;
                    break;
                }
            }
            if(!depNotMeet) {
                return f;
            }
        }
        return null;
    }

    /**
     * This function generates a list of feats that are unlocked if the specified feat is added to the character
     */
    public static List<Feat> getUnlockedFeats(Feat feat) {
        DBHelper dbHelper = DBHelper.getInstance(null);
        if(dbHelper == null || feat == null) {
            return null;
        }
        // retrieve the list of feats depending on the specified feat
        List<DBEntity> list = dbHelper.getAllEntities(FeatFactory.getInstance());
        List<Feat> allResults = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for(DBEntity e : list) {
            Feat f = (Feat)e;
            if(f.getRequires().contains(feat.getId())) {
                allResults.add(f);
                ids.add(f.getId());
            }
        }
        // remove the feats that have other related dependencies
        List<Feat> results = new ArrayList<>();
        for(Feat f : allResults) {
            boolean noOtherDep = true;
            for(Long dep : f.getRequires()) {
                if(ids.contains(dep)) {
                    noOtherDep = false;
                    break;
                }
            }
            if(noOtherDep) {
                results.add(f);
            }
        }
        return results;
    }

}
