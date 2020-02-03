package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.FeatFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatsUtil {

    private static final String SEP = "     ";

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


    private static void fillUnlockedFeatsRecursive(List<DBEntity> feats, Feat feat, int depth,  Map<Long, Feat> cFeats) {
        List<Feat> unlocked = FeatsUtil.getUnlockedFeats(feat);
        if(unlocked == null || unlocked.size() == 0) {
            return;
        }
        for(Feat u : unlocked) {
            if(cFeats.containsKey(u.getId())) {
                Feat f = cFeats.get(u.getId());
                f.setDepth(-depth);
                f.setName(StringUtil.generate(depth, SEP) + f.getName());
                feats.add(f);
                fillUnlockedFeatsRecursive(feats, f, depth+1, cFeats );
            }
            // check if all requirement are met
            // if ok, feat is added to the list
            else {
                boolean reqOK = true;
                for(Long id : u.getRequires()) {
                    if(!cFeats.containsKey(id)) {
                        reqOK = false;
                        break;
                    }
                }
                if(reqOK) {
                    u.setDepth(depth);
                    u.setName(StringUtil.generate(depth, SEP) + u.getName());
                    feats.add(u);
                }
            }
        }
    }

    public static List<DBEntity> getFeatsList(List<DBEntity> fullList, Character character) {
        List<DBEntity> feats = new ArrayList<>();
        Map<Long, Feat> added = new HashMap<>();
        // register all features possessed
        for(Feat f : character.getFeats()) {
            added.put(f.getId(), f);
        }
        // first: list of feats from character & unlocked feats (recursively)
        Feat sep = new Feat();
        if(character.getFeats().size() > 0) {
            sep.setName(ConfigurationUtil.getInstance().getProperties().getProperty("feat.unlocked"));
            feats.add(sep);
            for (Feat f : character.getFeats()) {
                if (f.getRequires().size() == 0) {
                    f.setDepth(-1);
                    feats.add(f);
                    fillUnlockedFeatsRecursive(feats, f, 1, added);
                }
            }
        }
        // update added features
        for(DBEntity e : feats) {
            added.put(e.getId(), (Feat)e);
        }
        // second: separator
        sep = new Feat();
        sep.setName(ConfigurationUtil.getInstance().getProperties().getProperty("feat.nodeps"));
        feats.add(sep);
        // third: others (with no dependency)
        for(DBEntity e : fullList) {
            Feat f = (Feat)e;
            if(!added.containsKey(f.getId()) && f.getRequires().size() == 0) {
                feats.add(f);
            }
        }
        return feats;
    }

}
