package org.pathfinderfr.app.database.entity;

import android.util.Log;

import org.pathfinderfr.app.util.CharacterUtil;
import org.pathfinderfr.app.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Character extends DBEntity {

    public static final int ABILITY_STRENGH      = 0;
    public static final int ABILITY_DEXTERITY    = 1;
    public static final int ABILITY_CONSTITUTION = 2;
    public static final int ABILITY_INTELLIGENCE = 3;
    public static final int ABILITY_WISDOM       = 4;
    public static final int ABILITY_CHARISMA     = 5;

    public static final int SIZE_SMALL = -1;
    public static final int SIZE_MEDIUM = 0;


    // character-specific
    int[] abilities;
    Race race;
    List<Pair<Class,Integer>> classes;
    Map<Long,Integer> skills;

    public Character() {
        abilities = new int[] { 10, 10, 10, 10, 10, 10 };
        classes = new ArrayList<>();
        skills = new HashMap<>();
    }

    @Override
    public DBEntityFactory getFactory() {
        return CharacterFactory.getInstance();
    }

    public int getAbilityValue(int ability) {
        if(ability <0  && ability > abilities.length) {
            return 0;
        }
        return abilities[ability];
    }

    public void setAbilityValue(int ability, int value) {
        if(ability <0  && ability > abilities.length) {
            return;
        }
        abilities[ability] = value;
    }

    public int getAbilityModif(int ability) {
        if(ability <0  && ability > abilities.length) {
            return 0;
        }
        return CharacterUtil.getAbilityBonus(abilities[ability]);
    }

    public int getStrength() { return getAbilityValue(ABILITY_STRENGH); }
    public int getStrengthModif() { return getAbilityModif(ABILITY_STRENGH); }
    public void setStrength(int value) { setAbilityValue(ABILITY_STRENGH, value); }
    public int getDexterity() { return getAbilityValue(ABILITY_DEXTERITY); }
    public int getDexterityModif() { return getAbilityModif(ABILITY_DEXTERITY); }
    public void setDexterity(int value) { setAbilityValue(ABILITY_DEXTERITY, value); }
    public int getConstitution() { return getAbilityValue(ABILITY_CONSTITUTION); }
    public int getConstitutionModif() { return getAbilityModif(ABILITY_CONSTITUTION); }
    public void setConstitution(int value) { setAbilityValue(ABILITY_CONSTITUTION, value); }
    public int getIntelligence() { return getAbilityValue(ABILITY_INTELLIGENCE); }
    public int getIntelligenceModif() { return getAbilityModif(ABILITY_INTELLIGENCE); }
    public void setIntelligence(int value) { setAbilityValue(ABILITY_INTELLIGENCE, value); }
    public int getWisdom() { return getAbilityValue(ABILITY_WISDOM); }
    public int getWisdomModif() { return getAbilityModif(ABILITY_WISDOM); }
    public void setWisdom(int value) { setAbilityValue(ABILITY_WISDOM, value); }
    public int getCharisma() { return getAbilityValue(ABILITY_CHARISMA); }
    public int getCharismaModif() { return getAbilityModif(ABILITY_CHARISMA); }
    public void setCharisma(int value) { setAbilityValue(ABILITY_CHARISMA, value); }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public Pair<Class,Integer> getClass(int idx) {
        if(idx >= classes.size()) {
            return null;
        }
        return classes.get(idx);
    }

    public int getClassesCount() {
        return classes.size();
    }

    public void addOrSetClass(Class cl, int level) {
        // check that this class is not already in
        for(int i=0; i<classes.size(); i++) {
            Pair<Class,Integer> c = classes.get(i);
            if(c.first.getId() == cl.getId()) {
                classes.set(i, new Pair<Class, Integer>(c.first, level));
                Collections.sort(classes, new ClassComparator());
                return;
            }
        }
        classes.add(new Pair<Class, Integer>(cl,level));
        Collections.sort(classes, new ClassComparator());
    }

    public void removeClass(Class cl) {
        Pair<Class,Integer> found = null;
        for(Pair<Class,Integer> c : classes) {
            if(c.first.getId() == cl.getId()) {
                found = c;
                break;
            }
        }
        if(found != null) {
            classes.remove(found);
        }
    }

    /**
     * @param id id not to include
     * @return the list of class ids (except the provided one)
     */
    public long[] getOtherClassesIds(long id) {
        List<Long> list = new ArrayList<>();
        for(Pair<Class,Integer> c : classes) {
            if(c.first.getId() != id) {
                list.add(c.first.getId());
            }
        }
        long[] result = new long[list.size()];
        for(int i=0; i<list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * @param id id not to include
     * @return the total number of level (excluding the provided one)
     */
    public int getOtherClassesLevel(long id) {
        int total = 0;
        for(Pair<Class,Integer> c : classes) {
            if(c.first.getId() != id) {
                total+=c.second;
            }
        }
        return total;
    }

    /**
     * @return character's level
     */
    public int getLevel() {
        return getOtherClassesLevel(-1);
    }

    /**
     * Sort by level (higher first) then name
     */
    private class ClassComparator implements java.util.Comparator<Pair<Class, Integer>> {

        @Override
        public int compare(Pair<Class, Integer> p1, Pair<Class, Integer> p2) {
            if(p1 == null || p2 == null) {
                return 0;
            } else if(p1.second != p2.second) {
                return Long.compare(p2.second,p1.second);
            } else {
                return p1.first.getName().compareTo(p2.first.getName());
            }

        }
    }

    public int getRaceSize() {
        if(race == null) {
            return SIZE_MEDIUM;
        }
        for(Race.Trait t : race.getTraits()) {
            // TODO: improve size handling to be language independent
            if("Petite taille".equalsIgnoreCase(t.getName())) {
                return SIZE_SMALL;
            }
        }
        return SIZE_MEDIUM;
    }

    public int getInitiative() { return getDexterityModif(); }
    public int getArmorClass() {
        int sizeModif = getRaceSize() == SIZE_SMALL ? 1 : 0;
        return 10 + getDexterityModif() + sizeModif;
    }
    public int getMagicResistance() { return 0; }

    public int getSavingThrowsReflexesTotal() { return getDexterityModif() + getSavingThrowsReflexes(); }
    public int getSavingThrowsFortitudeTotal() { return getConstitutionModif() + getSavingThrowsFortitude(); }
    public int getSavingThrowsWillTotal() { return getWisdomModif() + getSavingThrowsWill(); }

    /**
     * @return saving throws based on attached classes (and levels)
     */
    public int getSavingThrowsReflexes() {
        if(classes == null || classes.size() == 0) {
            return 0;
        }
        int total = 0;
        for(Pair<Class, Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.second) {
                    total+=lvl.getReflexBonus();
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find saving throws for %s and level %d", cl.first.getName(), cl.second));
            }
        }
        return total;
    }

    /**
     * @return saving throws based on attached classes (and levels)
     */
    public int getSavingThrowsFortitude() {
        if(classes == null || classes.size() == 0) {
            return 0;
        }
        int total = 0;
        for(Pair<Class, Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.second) {
                    total+=lvl.getFortitudeBonus();
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find saving throws for %s and level %d", cl.first.getName(), cl.second));
            }
        }
        return total;
    }

    /**
     * @return saving throws based on attached classes (and levels)
     */
    public int getSavingThrowsWill() {
        if(classes == null || classes.size() == 0) {
            return 0;
        }
        int total = 0;
        for(Pair<Class, Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.second) {
                    total+=lvl.getWillBonus();
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find saving throws for %s and level %d", cl.first.getName(), cl.second));
            }
        }
        return total;
    }

    /**
     * @return base attack bonus (BAB) based on attached classes (and levels)
     */
    public int[] getBaseAttackBonus() {
        if(classes == null || classes.size() == 0) {
            return null;
        }
        List<Integer> bab = new ArrayList<>();
        for(Pair<Class, Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.second) {
                    int[] bonus = lvl.getBaseAttackBonus();
                    if(bonus != null) {
                        for(int i=0; i<bonus.length; i++) {
                            if(i>=bab.size()) {
                                bab.add(bonus[i]);
                            } else {
                                bab.set(i, bab.get(i) + bonus[i]);
                            }
                        }
                    }
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find base attack bonus (bab) for %s and level %d", cl.first.getName(), cl.second));
            }
        }
        // convert to int[]
        int[] result = new int[bab.size()];
        for(int i=0; i<bab.size(); i++) {
            result[i]=bab.get(i);
        }
        return result;
    }

    /**
     * @return base attack bonus (BAB) based on attached classes (and levels), as string
     */
    public String getBaseAttackBonusAsString() {
        int[] bab = getBaseAttackBonus();
        if(bab == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for(int val : bab) {
            buf.append('+').append(val).append('/');
        }
        if(bab.length>0) {
            buf.deleteCharAt(buf.length()-1);
        }
        return buf.toString();
    }

    /**
     * @return combat maneuver bonus (CMB) = BAB + STR + SIZE
     */
    public int getCombatManeuverBonus() {
        int[] bab = getBaseAttackBonus();
        int bonus = 0;
        if(bab != null && bab.length > 0) {
            bonus += bab[0];
        }
        int sizeModif = getRaceSize() == SIZE_SMALL ? -1 : 0;
        return bonus + getStrengthModif() + sizeModif;
    }

    /**
     * @return combat maneuver defense (CMD) = 10 + BAB + STR + DEX + SIZE
     */
    public int getCombatManeuverDefense() {
        int[] bab = getBaseAttackBonus();
        int bonus = 0;
        if(bab != null && bab.length > 0) {
            bonus += bab[0];
        }
        int sizeModif = getRaceSize() == SIZE_SMALL ? -1 : 0;
        return 10 + bonus + getStrengthModif() + getDexterityModif() + sizeModif;
    }

    /**
     * @param skillId skill identifier
     * @param rank ranks for that skill
     * @return true if rank was changed, false if nothing changed
     */
    public boolean setSkillRank(long skillId, int rank) {
        if(!skills.containsKey(skillId) && rank > 0) {
            skills.put(skillId, rank);
            return true;
        } else if(skills.containsKey(skillId) && skills.get(skillId) != rank) {
            skills.put(skillId, rank);
            return true;
        }
        return false;
    }

    public int getSkillRank(long skillId) {
        if(skills.containsKey(skillId)) {
            return skills.get(skillId);
        } else {
            return 0;
        }
    }

    public Set<Long> getSkills() {
        return skills.keySet();
    }
}
