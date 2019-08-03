package org.pathfinderfr.app.database.entity;

import android.util.Log;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.CharacterUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.Triplet;

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

    public static final int SIZE_FINE        = 1;
    public static final int SIZE_DIMINUTIVE  = 2;
    public static final int SIZE_TINY        = 3;
    public static final int SIZE_SMALL       = 4;
    public static final int SIZE_MEDIUM      = 5;
    public static final int SIZE_LARGE_TALL  = 6;
    public static final int SIZE_LARGE_LONG  = 7;
    public static final int SIZE_HUGE_TALL   = 8;
    public static final int SIZE_HUGE_LONG   = 9;
    public static final int SIZE_GARG_TALL   = 10;
    public static final int SIZE_GARG_LONG   = 11;
    public static final int SIZE_COLO_TALL   = 12;
    public static final int SIZE_COLO_LONG   = 13;

    public static final int ALIGN_LG = 1;
    public static final int ALIGN_NG = 2;
    public static final int ALIGN_CG = 3;
    public static final int ALIGN_LN = 4;
    public static final int ALIGN_N = 5;
    public static final int ALIGN_CN = 6;
    public static final int ALIGN_LE = 7;
    public static final int ALIGN_NE = 8;
    public static final int ALIGN_CE = 9;

    public static final int SEX_M = 1;
    public static final int SEX_F = 2;

    public static final int SPEED_MANEUV_CLUMBSY = 1;
    public static final int SPEED_MANEUV_POOR    = 2;
    public static final int SPEED_MANEUV_AVERAGE = 3;
    public static final int SPEED_MANEUV_GOOD    = 4;
    public static final int SPEED_MANEUV_PERFECT = 5;

    public static final int MODIF_ABILITY_ALL = 1;
    public static final int MODIF_ABILITY_STR = 2;
    public static final int MODIF_ABILITY_DEX = 3;
    public static final int MODIF_ABILITY_CON = 4;
    public static final int MODIF_ABILITY_INT = 5;
    public static final int MODIF_ABILITY_WIS = 6;
    public static final int MODIF_ABILITY_CHA = 7;

    public static final int MODIF_SAVES_ALL     = 11;
    public static final int MODIF_SAVES_REF     = 12;
    public static final int MODIF_SAVES_FOR     = 13;
    public static final int MODIF_SAVES_WIL     = 14;
    public static final int MODIF_SAVES_MAG_ALL = 15;
    public static final int MODIF_SAVES_MAG_REF = 16;
    public static final int MODIF_SAVES_MAG_FOR = 17;
    public static final int MODIF_SAVES_MAG_WIL = 18;


    public static final int MODIF_COMBAT_INI = 21;
    public static final int MODIF_COMBAT_AC = 22;
    public static final int MODIF_COMBAT_MAG = 23;
    public static final int MODIF_COMBAT_HP = 24; // not supported
    public static final int MODIF_COMBAT_SPEED = 25;

    public static final int MODIF_COMBAT_AC_ARMOR = 26;
    public static final int MODIF_COMBAT_AC_SHIELD = 27;
    public static final int MODIF_COMBAT_AC_NATURAL = 28;
    public static final int MODIF_COMBAT_AC_PARADE = 29;

    public static final int MODIF_COMBAT_ATT_MELEE = 31;
    public static final int MODIF_COMBAT_ATT_RANGED = 32;
    public static final int MODIF_COMBAT_CMB = 33;
    public static final int MODIF_COMBAT_CMD = 34;

    public static final int MODIF_SKILL = 200;

    // character-specific
    int[] abilities;
    Race race;
    List<Triplet<Class,ClassArchetype,Integer>> classes;
    Map<Long,Integer> skills;
    List<Feat> feats;
    List<ClassFeature> features;
    List<RaceAlternateTrait> traits;
    List<CharacterModif> modifs;
    List<InventoryItem> invItems;
    int hitpoints;
    int speed;

    // infos additionnelles
    int alignment;
    String player;
    String divinity;
    String origin;
    int sizeType;
    int sex;
    int age;
    int height;
    int weight;
    String hair;
    String eyes;
    int speedDig;
    int speedWithArmor;
    int speedFly;
    int speedFlyManeuv;
    String languages;

    public Character() {
        abilities = new int[] { 10, 10, 10, 10, 10, 10 };
        classes = new ArrayList<>();
        skills = new HashMap<>();
        feats = new ArrayList<>();
        features = new ArrayList<>();
        traits = new ArrayList<>();
        modifs = new ArrayList<>();
        invItems = new ArrayList<>();
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
        public void update(CharacterModif modif) {
            source = modif.source;
            modifs = modif.modifs;
            icon = modif.icon;
        }
    }

    // Helper to keep inventory
    public static class InventoryItem implements Comparable<InventoryItem> {
        private String name;
        private int weight;
        private long objectId; // reference to original object
        public InventoryItem(String name, int weight, long objectId) {
            this.name = name;
            this.weight = weight;
            this.objectId = objectId;
        }
        public InventoryItem(InventoryItem copy) {
            this.name = copy.name;
            this.weight = copy.weight;
            this.objectId = copy.objectId;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
        public long getObjectId() { return this.objectId; }
        public boolean isValid() { return name != null && name.length() >= 3 && weight >= 0; }

        @Override
        public int compareTo(InventoryItem item) {
            return Collator.getInstance().compare(getName(),item.getName());
        }
    }

    @Override
    public String getNameLong() {
        String name = getName() == null ? "?" : getName();
        String race = getRace() == null ? "?" : getRace().getName();
        StringBuffer classes = new StringBuffer();
        for(int i = 0; i < getClassesCount(); i++) {
            Triplet<Class,ClassArchetype,Integer> cl = getClass(i);
            classes.append(", ").append(cl.first.getShortName()).append(" ").append(cl.third);
        }
        return name + " (" + race + classes.toString() + ")";
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

    public int getAlignment() { return alignment; }
    public void setAlignment(int alignment) { this.alignment = alignment; }
    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }
    public String getDivinity() { return divinity; }
    public void setDivinity(String divinity) { this.divinity = divinity; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public int getSizeType() { return sizeType; }
    public void setSizeType(int sizeType) { this.sizeType = sizeType; }
    public int getSex() { return sex; }
    public void setSex(int sex) { this.sex = sex; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public String getHair() { return hair; }
    public void setHair(String hair) { this.hair = hair; }
    public String getEyes() { return eyes; }
    public void setEyes(String eyes) { this.eyes = eyes; }
    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    /**
     * @param abilityId ability identifier
     * @return ability modifier
     */
    private int getAbilityModif(String abilityId) {
        if(abilityId == null) {
            return 0;
        }
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
        int bonus = getAdditionalBonus(MODIF_COMBAT_SPEED);
        return speed + bonus;
    }

    public int getBaseSpeed() {
        return speed;
    }

    public int getBaseSpeedWithArmor() {
        return speedWithArmor;
    }

    public int getBaseSpeedDig() {
        return speedDig;
    }

    public int getBaseSpeedFly() {
        return speedFly;
    }

    public int getBaseSpeedManeuverability() {
        return speedFlyManeuv;
    }

    public int getSpeedWithArmor() {
        int bonus = getAdditionalBonus(MODIF_COMBAT_SPEED);
        return speedWithArmor + bonus;
    }

    public float getSpeedSwimming() {
        return getSpeed() / 4f;
    }

    public float getSpeedClimbing() {
        return getSpeed() / 4f;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setSpeedWithArmor(int speed) {
        this.speedWithArmor = speed;
    }

    public void setSpeedDig(int speed) {
        this.speedDig = speed;
    }

    public void setSpeedFly(int speed) {
        this.speedFly = speed;
    }

    public void setSpeedManeuverability(int speed) {
        this.speedFlyManeuv = speed;
    }

    public Race getRace() {
        return race;
    }

    public String getRaceName() {
        return race == null ? "-" : race.getName();
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public Triplet<Class,ClassArchetype,Integer> getClass(int idx) {
        if(idx >= classes.size()) {
            return null;
        }
        return classes.get(idx);
    }

    public String getClassNames() {
        if(classes.size() == 0) {
            return "-";
        }
        StringBuffer buf = new StringBuffer();
        for(Triplet<Class,ClassArchetype,Integer> cl : classes) {
            buf.append(cl.first.getName()).append(' ').append(cl.third).append('/');
        }
        buf.deleteCharAt(buf.length()-1);
        return buf.toString();
    }

    public int getClassesCount() {
        return classes.size();
    }

    public void addOrSetClass(Class cl, ClassArchetype arch, int level) {
        // check that this class is not already in
        for(int i=0; i<classes.size(); i++) {
            Triplet<Class,ClassArchetype,Integer> c = classes.get(i);
            if(c.first.getId() == cl.getId()) {
                classes.set(i, new Triplet<Class,ClassArchetype,Integer>(c.first, arch, level));
                Collections.sort(classes, new ClassComparator());
                return;
            }
        }
        classes.add(new Triplet<Class,ClassArchetype,Integer>(cl, arch, level));
        Collections.sort(classes, new ClassComparator());
    }

    public void removeClass(Class cl) {
        Triplet<Class,ClassArchetype,Integer> found = null;
        for(Triplet<Class,ClassArchetype,Integer> c : classes) {
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
        for(Triplet<Class,ClassArchetype,Integer> c : classes) {
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
        for(Triplet<Class,ClassArchetype,Integer> c : classes) {
            if(c.first.getId() != id) {
                total+=c.third;
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
    private class ClassComparator implements java.util.Comparator<Triplet<Class,ClassArchetype,Integer>> {

        @Override
        public int compare(Triplet<Class,ClassArchetype,Integer> p1, Triplet<Class,ClassArchetype,Integer> p2) {
            if(p1 == null || p2 == null) {
                return 0;
            } else if(p1.third != p2.third) {
                return Long.compare(p2.third,p1.third);
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

    private static int getSizeModifier(int size) {
        switch(size) {
            case SIZE_FINE: return 8;
            case SIZE_DIMINUTIVE: return 4;
            case SIZE_TINY: return 2;
            case SIZE_SMALL: return 1;
            case SIZE_LARGE_TALL: return -1;
            case SIZE_LARGE_LONG: return -1;
            case SIZE_HUGE_TALL: return -2;
            case SIZE_HUGE_LONG: return -2;
            case SIZE_GARG_TALL: return -4;
            case SIZE_GARG_LONG: return -4;
            case SIZE_COLO_TALL: return -8;
            case SIZE_COLO_LONG: return -8;
            default: return 0;
        }
    }

    public int getInitiative() { return getDexterityModif() + getAdditionalBonus(MODIF_COMBAT_INI); }
    public int getArmorClass() {
        int bonus_armor = getAdditionalBonus(MODIF_COMBAT_AC_ARMOR);
        int bonus_shield = getAdditionalBonus(MODIF_COMBAT_AC_SHIELD);
        int bonus_size = getSizeModifierArmorClass();
        int bonus_natural = getAdditionalBonus(MODIF_COMBAT_AC_NATURAL);
        int bonus_parade = getAdditionalBonus(MODIF_COMBAT_AC_PARADE);
        int bonus_other = getAdditionalBonus(MODIF_COMBAT_AC);
        return 10 + bonus_armor + bonus_shield + getDexterityModif() + bonus_size + bonus_natural + bonus_parade + bonus_other;
    }

    public int getArmorClassContact() {
        int bonus_size = getSizeModifierArmorClass();
        int bonus_parade = getAdditionalBonus(MODIF_COMBAT_AC_PARADE);
        int bonus_other = getAdditionalBonus(MODIF_COMBAT_AC);
        return 10 + getDexterityModif() + bonus_size + bonus_parade + bonus_other;
    }

    public int getArmorClassFlatFooted() {
        int bonus_dex = getDexterityModif();
        int bonus_armor = getAdditionalBonus(MODIF_COMBAT_AC_ARMOR);
        int bonus_shield = getAdditionalBonus(MODIF_COMBAT_AC_SHIELD);
        int bonus_size = getSizeModifierArmorClass();
        int bonus_natural = getAdditionalBonus(MODIF_COMBAT_AC_NATURAL);
        int bonus_other = getAdditionalBonus(MODIF_COMBAT_AC);
        return 10 + bonus_armor + bonus_shield + ( bonus_dex < 0 ? bonus_dex : 0 ) + bonus_size + bonus_natural + bonus_other;
    }

    public int getSizeModifierArmorClass() {
        return getSizeModifier(getSizeType());
    }

    public int getSizeModifierManeuver() {
        return -getSizeModifier(getSizeType());
    }

    public String getArmorClassDetails() {
        StringBuffer buf = new StringBuffer();
        int bonus_armor = getAdditionalBonus(MODIF_COMBAT_AC_ARMOR);
        int bonus_shield = getAdditionalBonus(MODIF_COMBAT_AC_SHIELD);
        int bonus_dex = getDexterityModif();
        int bonus_size = getSizeModifierArmorClass();
        int bonus_natural = getAdditionalBonus(MODIF_COMBAT_AC_NATURAL);
        int bonus_parade = getAdditionalBonus(MODIF_COMBAT_AC_PARADE);
        int bonus_other = getAdditionalBonus(MODIF_COMBAT_AC);
        if(bonus_armor != 0) {
            buf.append(String.format("armure %+d, ", bonus_armor));
        }
        if(bonus_shield != 0) {
            buf.append(String.format("bouclier %+d, ", bonus_shield));
        }
        if(bonus_dex != 0) {
            buf.append(String.format("Dex %+d, ", bonus_dex));
        }
        if(bonus_size != 0) {
            buf.append(String.format("taille %+d, ", bonus_size));
        }
        if(bonus_natural != 0) {
            buf.append(String.format("naturelle %+d, ", bonus_natural));
        }
        if(bonus_parade != 0) {
            buf.append(String.format("parade %+d, ", bonus_parade));
        }
        if(bonus_other != 0) {
            buf.append(String.format("divers %+d, ", bonus_other));
        }
        if(buf.length()>0) {
            buf.delete(buf.length()-2,buf.length());
        }
        return buf.toString();
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
        for(Triplet<Class,ClassArchetype,Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.third) {
                    total+=lvl.getReflexBonus();
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find saving throws for %s and level %d", cl.first.getName(), cl.third));
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
        for(Triplet<Class,ClassArchetype,Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.third) {
                    total+=lvl.getFortitudeBonus();
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find saving throws for %s and level %d", cl.first.getName(), cl.third));
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
        for(Triplet<Class,ClassArchetype,Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.third) {
                    total+=lvl.getWillBonus();
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find saving throws for %s and level %d", cl.first.getName(), cl.third));
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
        bonus += getAdditionalBonus(MODIF_SAVES_MAG_ALL);
        bonus += getAdditionalBonus(bonusId);
        bonus += getAdditionalBonus(bonusId+4); // magic bonus
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
        for(Triplet<Class,ClassArchetype,Integer> cl : classes) {
            // find matching level (should be in order but ...)
            boolean found = false;
            for(Class.Level lvl: cl.first.getLevels()) {
                if(lvl.getLvl() == cl.third) {
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
                Log.w(Character.class.getSimpleName(), String.format("Couldn't find base attack bonus (bab) for %s and level %d", cl.first.getName(), cl.third));
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
    public int getBaseAttackBonusBest() {
        int[] bab = getBaseAttackBonus();
        return bab != null && bab.length > 0 ? bab[0] : 0;
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
        bonus += getBaseAttackBonusBest();
        int sizeModif = getSizeModifier(getSizeType());
        int addBonus = getAdditionalBonus(MODIF_COMBAT_CMB);
        return bonus + getStrengthModif() - sizeModif + addBonus;
    }

    /**
     * @return combat maneuver defense (CMD) = 10 + BAB + STR + DEX + SIZE
     */
    public int getCombatManeuverDefense() {
        int[] bab = getBaseAttackBonus();
        int bonus = 0;
        bonus += getBaseAttackBonusBest();
        int sizeModif = getSizeModifier(getSizeType());
        int addBonus = getAdditionalBonus(MODIF_COMBAT_CMD);
        return 10 + bonus + getStrengthModif() + getDexterityModif() - sizeModif + addBonus;
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

    public String getSkillsAsString() {
        if(skills.size() == 0) {
            return "-";
        }
        List<DBEntity> skills = DBHelper.getInstance(null).getAllEntitiesWithAllFields(SkillFactory.getInstance());
        StringBuffer buf = new StringBuffer();
        for(DBEntity s : skills) {
            int rank = getSkillRank(s.getId());
            if(rank > 0) {
                buf.append(String.format("%s %+d", s.getName(), getSkillTotalBonus((Skill)s)));
                buf.append(", ");
            }
        }
        if(buf.length()>0) {
            buf.delete(buf.length() - 2, buf.length());
        }
        return buf.toString();
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
        int classSkill = (rank > 0 && isClassSkill(skill.getName())) ? 3 : 0;
        int bonus = getAdditionalBonus(MODIF_SKILL + (int)skill.getId());
        return rank + abilityMod + classSkill + bonus;
    }

    public int getSkillModBonus(Skill skill) {
        if(skill == null) {
            return 0;
        }
        int rank = getSkillRank(skill.getId());
        int classSkill = (rank > 0 && isClassSkill(skill.getName())) ? 3 : 0;
        int bonus = getAdditionalBonus(MODIF_SKILL + (int)skill.getId());
        return classSkill + bonus;
    }

    public boolean isClassSkill(String skillName) {
        for(Triplet<Class,ClassArchetype,Integer> cl : classes) {
            if(cl.first.getSkills().contains(skillName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the list of traits (as a copy)
     */
    public List<RaceAlternateTrait> getAlternateTraits() {
        RaceAlternateTrait[] itemArray = new RaceAlternateTrait[traits.size()];
        itemArray = traits.toArray(itemArray);
        List<RaceAlternateTrait> traits = Arrays.asList(itemArray);
        final Collator collator = Collator.getInstance();
        Collections.sort(traits, new Comparator<RaceAlternateTrait>() {
            @Override
            public int compare(RaceAlternateTrait t1, RaceAlternateTrait t2) {
                return collator.compare(t1.getName(), t1.getName());
            }
        });
        return traits;
    }

    public String getAlternateTraitsAsString() {
        List<RaceAlternateTrait> traits = getAlternateTraits();
        if(traits.size() == 0) {
            return "-";
        }
        StringBuffer buf = new StringBuffer();
        for(RaceAlternateTrait f : traits) {
            buf.append(f.getName()).append(", ");
        }
        buf.delete(buf.length()-2, buf.length());
        return buf.toString();
    }

    public boolean hasAlternateTrait(RaceAlternateTrait trait) {
        for(RaceAlternateTrait t : traits) {
            if(t.getId() == trait.getId()) {
                return true;
            }
        }
        return false;
    }

    public void addAlternateTrait(RaceAlternateTrait trait) {
        traits.add(trait);
    }

    public boolean removeAlternateTrait(RaceAlternateTrait trait) {
        RaceAlternateTrait found = null;
        for(RaceAlternateTrait t : traits) {
            if(t.getId() == trait.getId()) {
                found = t;
                break;
            }
        }
        if(found != null) {
            traits.remove(found);
            return true;
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

    public String getFeatsAsString() {
        List<Feat> feats = getFeats();
        if(feats.size() == 0) {
            return "-";
        }
        StringBuffer buf = new StringBuffer();
        for(Feat f : feats) {
            buf.append(f.getName()).append(", ");
        }
        buf.delete(buf.length()-2, buf.length());
        return buf.toString();
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
     * @return the list of abilities (as a copy)
     */
    public List<ClassFeature> getClassFeatures() {
        ClassFeature[] itemArray = new ClassFeature[features.size()];
        itemArray = features.toArray(itemArray);
        List<ClassFeature> features = Arrays.asList(itemArray);
        final Collator collator = Collator.getInstance();
        Collections.sort(features, new Comparator<ClassFeature>() {
            @Override
            public int compare(ClassFeature a1, ClassFeature a2) {
                if(a1.getLevel() != a2.getLevel()) {
                    return Integer.compare(a1.getLevel(),a2.getLevel());
                } else if(a1.getClass_() != a2.getClass_()) {
                    return collator.compare(a1.getClass_().getShortName(), a2.getClass_().getShortName());
                }
                else {
                    return collator.compare(a1.getName(), a2.getName());
                }
            }
        });
        return features;
    }

    public boolean hasClassFeature(ClassFeature feature) {
        for(ClassFeature a : features) {
            if(a.getId() == feature.getId()) {
                return true;
            }
        }
        return false;
    }

    public void addClassFeature(ClassFeature feature) {
        features.add(feature);
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }

    public boolean removeClassFeature(ClassFeature feature) {
        ClassFeature found = null;
        for(ClassFeature a : features) {
            if(a.getId() == feature.getId()) {
                found = a;
                break;
            }
        }
        if(found != null) {
            features.remove(found);
            return true;
        }
        return false;
    }

    /**
     * @param feature class feature
     * @return false if feature doesn't match class or level
     */
    public boolean isValidClassFeature(ClassFeature feature) {
        for(Triplet<Class,ClassArchetype,Integer> cl: classes) {
            if(feature.getClass_().getId() == cl.first.getId() && feature.getLevel() <= cl.third) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param trait alternate trait
     * @return false if trait doesn't match race
     */
    public boolean isValidRacialTrait(RaceAlternateTrait trait) {
        if(race == null || trait == null) {
            return false;
        }
        return trait.getRace().getId() == getRace().getId();
    }

    /**
     * @param trait alternate trait
     * @return false if trait replaces/alters a trait that was already replaced/modified
     */
    public boolean isDuplicatedRacialTrait(RaceAlternateTrait trait) {
        if(race == null || trait == null) {
            return false;
        }
        for(RaceAlternateTrait t : getAlternateTraits()) {
            if(t.getId() == trait.getId()) {
                continue;
            }
            // check same replaces
            for(String repA : t.getReplaces()) {
                for(String repB : trait.getReplaces()) {
                    if(repA.equals(repB)) {
                        return true;
                    }
                }
            }
            // check same alters
            for(String repA : t.getAlters()) {
                for(String repB : trait.getAlters()) {
                    if(repA.equals(repB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if provided trait name is replaced
     * @param name trait name
     * @return alternate trait if replaced, null otherwise
     */
    public RaceAlternateTrait traitIsReplaced(String name) {
        for(RaceAlternateTrait t : getAlternateTraits()) {
            if(getRace() != null && getRace().getId() != t.getRace().getId()) {
                continue;
            }
            for(String rep : t.getReplaces()) {
                if(rep.equals(name)) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Checks if provided trait name is altered
     * @param name trait name
     * @return alternate trait if altered, null otherwise
     */
    public RaceAlternateTrait traitIsAltered(String name) {
        for(RaceAlternateTrait t : getAlternateTraits()) {
            if(getRace() != null && getRace().getId() != t.getRace().getId()) {
                continue;
            }
            for(String rep : t.getAlters()) {
                if(rep.equals(name)) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * @return the list of modifs
     */
    public List<CharacterModif> getModifs() {
        return modifs;
    }

    public String getModifsAsString() {
        StringBuffer buf = new StringBuffer();
        if(modifs.size() == 0) {
            return "-";
        }
        for(CharacterModif el : modifs) {
            if(el.isEnabled()) {
                buf.append(el.getSource()).append(", ");
            }
        }
        buf.delete(buf.length() - 2, buf.length());
        return buf.toString();
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

    public void addInventoryItem(InventoryItem item) {
        invItems.add(item);
        Collections.sort(invItems);
    }

    public void deleteInventoryItem(InventoryItem item) {
        invItems.remove(item);
    }

    public void deleteInventoryItem(int idx) {
        if(idx < 0 || idx >= invItems.size()) {
            return;
        }
        invItems.remove(idx);
    }

    public void modifyInventoryItem(int idx, InventoryItem item) {
        if(idx < 0 || idx >= invItems.size() || !item.isValid()) {
            return;
        }
        InventoryItem selItem = invItems.get(idx);
        selItem.setName(item.getName());
        selItem.setWeight(item.getWeight());
        Collections.sort(invItems);
    }

    /**
     * @return the list of inventory items (as a copy)
     */
    public List<InventoryItem> getInventoryItems() {
        List<InventoryItem> result = new ArrayList<>();
        for(InventoryItem el : invItems) {
            result.add(new InventoryItem(el));
        }
        return result;
    }

    public String getInventoryAsString() {
        if(invItems.size() == 0) {
            return "-";
        }
        StringBuffer buf = new StringBuffer();
        for(InventoryItem el : invItems) {
            buf.append(el.getName()).append(", ");
        }
        buf.delete(buf.length()-2, buf.length());
        return buf.toString();
    }

    public int getInventoryTotalWeight() {
        int weight = 0;
        for(InventoryItem el : invItems) {
            weight += el.getWeight();
        }
        return weight;
    }
}
