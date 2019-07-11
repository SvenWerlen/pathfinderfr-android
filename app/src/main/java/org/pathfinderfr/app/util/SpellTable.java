package org.pathfinderfr.app.util;

import android.util.Log;

import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.Spell;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private List<Triplet<Class,ClassArchetype,Integer>> classLevels;
    private Map<Integer, SpellLevel> levels;
    public SpellTable(List<Triplet<Class, ClassArchetype,Integer>> classLevels) {
        this.classLevels = classLevels;
        levels = new HashMap<>();
    }

    public List<SpellLevel> getLevels() {
        List<SpellLevel> list = new ArrayList<>(levels.values());
        Collections.sort(list);
        return list;
    }

    public void addSpell(Spell spell) {
        for(Pair<String,Integer> pair: SpellUtil.getLevel(classLevels, spell)) {
            // ignore if spell doesn't match max spell level of class
            if(levels.containsKey(pair.second)) {
                levels.get(pair.second).addSpell(spell, pair.first);
            } else {
                SpellLevel lvl = new SpellLevel(pair.second);
                lvl.addSpell(spell, pair.first);
                levels.put(pair.second, lvl);
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

        public List<SpellAndClass> getSpells() {
            List<SpellAndClass> spells = new ArrayList<>();
            for(SpellSchool school: schools.values()) {
                spells.addAll(school.spells.values());
            }
            Collections.sort(spells);
            return spells;
        }

        public void addSpell(Spell spell, String cl) {
            String schoolName = spell.getSchool();
            // ignore if spell with invalid school (should never happen)
            if(schoolName != null) {
                if(schools.containsKey(schoolName)) {
                    schools.get(schoolName).addSpell(spell, cl);
                } else {
                    SpellSchool school = new SpellSchool(schoolName);
                    school.addSpell(spell, cl);
                    schools.put(schoolName, school);
                }
            } else {
                Log.w(SpellTable.class.getSimpleName(), "Spell '" + spell.getName() + "' skipped: + " + spell.getSchool());
            }
        }

        @Override
        public int compareTo(SpellLevel o) {
            return new Integer(level).compareTo(o.getLevel());
        }
    }

    public static class SpellSchool implements Comparable<SpellSchool> {
        private String schoolName;
        private LinkedHashMap<Long, SpellAndClass> spells;
        public SpellSchool(String name) {
            schoolName = name;
            spells = new LinkedHashMap<>();
        }

        public String getSchoolName() { return schoolName; }
        public List<SpellAndClass> getSpells() {
            List<SpellAndClass> result = new ArrayList(spells.values());
            Collections.sort(result);
            return result;
        }

        public void addSpell(Spell spell, String cl) {
            if(spells.containsKey(spell.getId())) {
                spells.get(spell.getId()).addClass(cl);
            } else {
                SpellAndClass spellAndClass = new SpellAndClass(spell);
                spellAndClass.addClass(cl);
                spells.put(spell.getId(),spellAndClass);
            }
        }

        @Override
        public int compareTo(SpellSchool o) {
            Collator collator = Collator.getInstance();
            return Collator.getInstance().compare(schoolName,o.getSchoolName());
        }
    }

    public static class SpellAndClass implements Comparable<SpellAndClass> {
        Set<String> classes;
        Spell spell;
        public SpellAndClass(Spell sp) {
            classes = new HashSet<>();
            spell = sp;
        }
        public void addClass(String className) {
            classes.add(className);
        }
        public List<String> getClasses() {
            return new ArrayList<String>(classes);
        }
        public Spell getSpell() {
            return spell;
        }

        @Override
        public int compareTo(SpellAndClass sc2) {
            return this.spell.compareTo(sc2.spell);
        }
    }

}