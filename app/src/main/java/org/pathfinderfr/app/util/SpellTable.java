package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.entity.Spell;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for building a table similar to
 * http://www.pathfinder-fr.org/Wiki/Pathfinder-RPG.Liste%20des%20sorts%20densorceleursmagiciens.ashx
 *
 * Levels [0..9]
 *   School
 *      Spell 1
 *      Spell 2
 *      ...
 */
public class SpellTable {
    private List<String> classNames;
    private Map<Integer, SpellLevel> levels;
    public SpellTable(List<String> classNames) {
        this.classNames = classNames;
        levels = new HashMap<>();
    }

    public List<SpellLevel> getLevels() {
        List<SpellLevel> list = new ArrayList<>(levels.values());
        Collections.sort(list);
        return list;
    }

    public void addSpell(Spell spell) {
        // check if spell exists for given classes
        boolean found = false;
        for(String cl : classNames) {
            if(spell.getLevel().indexOf(cl) >= 0) {
                found = true;
                break;
            }
        }
        if(!found) {
            return;
        }

        Pair<String, Integer> level = SpellUtil.getLevel(classNames,spell,false);
        // ignore if spell doesn't below to classes
        if(level != null) {
            if(levels.containsKey(level.second)) {
                levels.get(level.second).addSpell(spell);
            } else {
                SpellLevel lvl = new SpellLevel(level.second);
                lvl.addSpell(spell);
                levels.put(level.second, lvl);
            }
        }
        //else { Log.d(SpellTable.class.getSimpleName(), "Spell '" + spell.getName() + "' skipped: + " + spell.getLevel());}
    }

    public static class SpellLevel implements Comparable<SpellLevel>{
        private int level;
        private Map<String,SpellSchool> schools;
        public SpellLevel(int lvl) {
            level = lvl;
            schools = new HashMap<>();
        }

        public int getLevel() { return level; }
        public List<SpellSchool> getSchools() {
            List<SpellSchool> list = new ArrayList<>(schools.values());
            Collections.sort(list);
            return list;
        }

        public List<Spell> getSpells() {
            List<Spell> spells = new ArrayList<>();
            for(SpellSchool school: schools.values()) {
                spells.addAll(school.spells);
            }
            Collections.sort(spells);
            return spells;
        }

        public void addSpell(Spell spell) {
            String schoolName = spell.getSchool();
            // ignore if spell with invalid school (should never happen)
            if(schoolName != null) {
                if(schools.containsKey(schoolName)) {
                    schools.get(schoolName).addSpell(spell);
                } else {
                    SpellSchool school = new SpellSchool(schoolName);
                    school.addSpell(spell);
                    schools.put(schoolName, school);
                }
            } else {
                Log.w(SpellTable.class.getSimpleName(), "Spell '" + spell.getName() + "' skipped: + " + spell.getSchool());
            }
        }

        @Override
        public int compareTo(SpellLevel o) {
            return new Integer(level).compareTo(level);
        }
    }

    public static class SpellSchool implements Comparable<SpellSchool> {
        private String schoolName;
        private List<Spell> spells;
        public SpellSchool(String name) {
            schoolName = name;
            spells = new ArrayList<>();
        }

        public String getSchoolName() { return schoolName; }
        public List<Spell> getSpells() {
            Collections.sort(spells);
            return spells;
        }

        public void addSpell(Spell spell) {
            spells.add(spell);
        }

        @Override
        public int compareTo(SpellSchool o) {
            Collator collator = Collator.getInstance();
            return Collator.getInstance().compare(schoolName,o.getSchoolName());
        }
    }

}