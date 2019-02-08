package org.pathfinderfr.app.database.entity;

import android.util.Log;

import org.pathfinderfr.app.util.CharacterUtil;
import org.pathfinderfr.app.util.Pair;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    public static final int MODIF_ABILITY_ALL = 1;
    public static final int MODIF_ABILITY_STR = 2;
    public static final int MODIF_ABILITY_DEX = 3;
    public static final int MODIF_ABILITY_CON = 4;
    public static final int MODIF_ABILITY_INT = 5;
    public static final int MODIF_ABILITY_WIS = 6;
    public static final int MODIF_ABILITY_CHA = 7;

    public static final int MODIF_SAVES_ALL = 11;
    public static final int MODIF_SAVES_REF = 12;
    public static final int MODIF_SAVES_FOR = 13;
    public static final int MODIF_SAVES_WIL = 14;

    public static final int MODIF_COMBAT_INI = 21;
    public static final int MODIF_COMBAT_AC = 22;
    public static final int MODIF_COMBAT_MAG = 23;
    public static final int MODIF_COMBAT_HP = 24;
    public static final int MODIF_COMBAT_SPEED = 25;

    public static final int MODIF_COMBAT_ATT_MELEE = 31;
    public static final int MODIF_COMBAT_ATT_RANGED = 32;
    public static final int MODIF_COMBAT_CMB = 33;
    public static final int MODIF_COMBAT_CMD = 34;

    public static final int MODIF_SKILL = 200;

    // character-specific
    int[] abilities;
    Race race;
    List<Pair<Class,Integer>> classes;
    Map<Long,Integer> skills;
    List<Feat> feats;
    List<CharacterModif> modifs;
    int hitpoints;
    int speed;

    public Character() {
        abilities = new int[] { 10, 10, 10, 10, 10, 10 };
        classes = new ArrayList<>();
        skills = new HashMap<>();
        feats = new ArrayList<>();
        modifs = new ArrayList<>();
    }

    // Helper to keep modifs
    public static class CharacterModif {
        private String source;
        private List<Pair<Integer,Integer>> modifs;
        private String icon;
        private boolean enabled;
        public CharacterModif(String source, List<Pair<Integer,Integer>> modifs, String icon) {
            this(source, modifs, icon, false);
        }
        public CharacterModif(String source, List<Pair<Integer,Integer>> modifs, String icon, boolean enabled) {
            this.source = source;
            this.modifs = modifs;
            this.icon = icon;
            this.enabled = enabled;
        }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public int getModifCount() { return modifs == null ? 0 : modifs.size(); }
        public void setModifs(List<Pair<Integer,Integer>> modifs) { this.modifs = modifs; }
        public Pair<Integer,Integer> getModif(int index) { return index < 0 || index >= modifs.size() ? null : modifs.get(index); }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled;}
        public boolean isValid() { return source != null && source.length() > 0 && modifs != null && modifs.size() > 0 && icon != null && icon.length() > 0; }
    }

    @Override
    public DBEntityFactory getFactory() {
        return CharacterFactory.getInstance();
    }

    public int getAbilityValue(int ability, boolean withModif) {
        if(ability <0  || ability >= abilities.length) {
            return 0;
        }
        if(!withModif) {
            return abilities[ability];
        }
        // check if modif is applied
        int bonus = 0;
        bonus += getAdditionalBonus(MODIF_ABILITY_ALL);
        bonus += getAdditionalBonus(ability+2); // MODIF_ABILITY = ABILITY_ID + 2 (see above)
        return abilities[ability] + bonus;
    }

    public int getAbilityValue(int ability) {
        return getAbilityValue(ability, true);
    }

    public void setAbilityValue(int ability, int value) {
        if(ability <0  || ability >= abilities.length) {
            return;
        }
        abilities[ability] = value;
    }

    public int getAbilityModif(int ability) {
        return CharacterUtil.getAbilityBonus(getAbilityValue(ability));
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

    /**
     * @param abilityId ability identifier
     * @return ability modifier
     */
    private int getAbilityModif(String abilityId) {
        // TODO: make it language-independant
        switch(abilityId) {
            case "FOR": return getStrengthModif();
            case "DEX": return getDexterityModif();
            case "CON": return getConstitutionModif();
            case "INT": return getIntelligenceModif();
            case "SAG": return getWisdomModif();
            case "CHA": return getCharismaModif();
            default: return 0;
        }
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

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

    public int getInitiative() { return getDexterityModif() + getAdditionalBonus(MODIF_COMBAT_INI); }
    public int getArmorClass() {
        int sizeModif = getRaceSize() == SIZE_SMALL ? 1 : 0;
        int bonus = getAdditionalBonus(MODIF_COMBAT_AC);
        return 10 + getDexterityModif() + sizeModif + bonus;
    }
    public int getMagicResistance() {
        int bonus = getAdditionalBonus(MODIF_COMBAT_MAG);
        return bonus;
    }

    public int getSavingThrowsReflexesTotal() { return getDexterityModif() + getSavingThrowsReflexes() + getSavingThrowsBonus(MODIF_SAVES_REF); }
    public int getSavingThrowsFortitudeTotal() { return getConstitutionModif() + getSavingThrowsFortitude() + getSavingThrowsBonus(MODIF_SAVES_FOR); }
    public int getSavingThrowsWillTotal() { return getWisdomModif() + getSavingThrowsWill() + getSavingThrowsBonus(MODIF_SAVES_WIL); }

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
     * @param bonusId bonusId for corresponding SavingThrows
     * @return bonus to be applied on savingthrows
     */
    public int getSavingThrowsBonus(int bonusId) {
        // check if modif is applied
        int bonus = 0;
        bonus += getAdditionalBonus(MODIF_SAVES_ALL);
        bonus += getAdditionalBonus(bonusId);
        return bonus;
    }

    /**
     * @param bonusId bonusId for corresponding SavingThrows
     * @return bonus to be applied
     */
    public int getAdditionalBonus(int bonusId) {
        // check if modif is applied
        int bonus = 0;
        List<CharacterModif> modifs = getModifsForId(bonusId);
        // getModifsForId always returns 1 modification (matching the one being searched)
        for(CharacterModif mod : modifs) {
            if(mod.isEnabled()) {
                bonus += mod.getModif(0).second;
            }
        }
        return bonus;
    }

    /**
     * @param addBonus additional bonus
     * @return attack bonus (BAB) based on attached classes (and levels)
     */
    public int[] getAttackBonus(int addBonus) {
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
                                bab.add(bonus[i] + addBonus);
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
     * @return base attack bonus (BAB) based on attached classes (and levels)
     */
    public int[] getBaseAttackBonus() {
        return getAttackBonus(0);
    }

    /**
     * @return base attack bonus (BAB) based on attached classes (and levels), as string
     */
    public String getBaseAttackBonusAsString() {
        return CharacterUtil.getAttackBonusAsString(getBaseAttackBonus());
    }

    /**
     * @return attack bonus (melee), as string
     */
    public String getAttackBonusMeleeAsString() {
        int addBonus = getAdditionalBonus(MODIF_COMBAT_ATT_MELEE);
        return CharacterUtil.getAttackBonusAsString(getAttackBonus(addBonus + getStrengthModif()));
    }

    /**
     * @return attack bonus (range), as string
     */
    public String getAttackBonusRangeAsString() {
        int addBonus = getAdditionalBonus(MODIF_COMBAT_ATT_RANGED);
        return CharacterUtil.getAttackBonusAsString(getAttackBonus(addBonus + getDexterityModif()));
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
        int addBonus = getAdditionalBonus(MODIF_COMBAT_CMB);
        return bonus + getStrengthModif() + sizeModif + addBonus;
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
        int addBonus = getAdditionalBonus(MODIF_COMBAT_CMD);
        return 10 + bonus + getStrengthModif() + getDexterityModif() + sizeModif + addBonus;
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
        } else if(skills.containsKey(skillId) && rank == 0) {
            skills.remove(skillId);
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

    public int getSkillAbilityMod(Skill skill) {
        if(skill == null) {
            return 0;
        }
        return getAbilityModif(skill.getAbilityId());
    }

    public int getSkillTotalBonus(Skill skill) {
        if(skill == null) {
            return 0;
        }
        int rank = getSkillRank(skill.getId());
        int abilityMod = getSkillAbilityMod(skill);
        int classSkill = isClassSkill(skill.getName()) ? 3 : 0;
        int bonus = getAdditionalBonus(MODIF_SKILL + (int)skill.getId());
        return rank + abilityMod + classSkill + bonus;
    }

    public boolean isClassSkill(String skillName) {
        for(Pair<Class, Integer> cl : classes) {
            if(cl.first.getSkills().contains(skillName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the list of feats (as a copy)
     */
    public List<Feat> getFeats() {
        Feat[] itemArray = new Feat[feats.size()];
        itemArray = feats.toArray(itemArray);
        List<Feat> feats = Arrays.asList(itemArray);
        final Collator collator = Collator.getInstance();
        Collections.sort(feats, new Comparator<Feat>() {
            @Override
            public int compare(Feat f1, Feat f2) {
                return collator.compare(f1.getName(), f2.getName());
            }
        });
        return feats;
    }

    public boolean hasFeat(Feat feat) {
        for(Feat f : feats) {
            if(f.getId() == feat.getId()) {
                return true;
            }
        }
        return false;
    }

    public void addFeat(Feat feat) {
        feats.add(feat);
    }

    public boolean removeFeat(Feat feat) {
        Feat found = null;
        for(Feat f : feats) {
            if(f.getId() == feat.getId()) {
                found = f;
                break;
            }
        }
        if(found != null) {
            feats.remove(found);
            return true;
        }
        return false;
    }

    /**
     * @return the list of modifs (as a copy)
     */
    public List<CharacterModif> getModifs() {
        return modifs;
    }

    public List<CharacterModif> getModifsForId(Integer id) {
        List<CharacterModif> result = new ArrayList<>();
        for(CharacterModif el : modifs) {
            for(Pair<Integer,Integer> m: el.modifs) {
                if(m.first.longValue() == id.longValue()) {
                    result.add(new CharacterModif(el.source, Arrays.asList(m), el.getIcon(), el.isEnabled()));
                    break;
                }
            }
        }
        return result;
    }

    public int getModifsCount() {
        return modifs.size();
    }

    public void addModif(CharacterModif modif) {
        modifs.add(modif);
    }

    public void deleteModif(CharacterModif modif) {
        modifs.remove(modif);
    }

    public CharacterModif getModif(int idx) {
        if(idx < 0 || idx >= modifs.size()) {
            return null;
        }
        return modifs.get(idx);
    }
}
