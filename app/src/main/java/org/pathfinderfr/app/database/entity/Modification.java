package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.Pair;

import java.util.List;

public class Modification extends DBEntity {

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
    public static final int MODIF_COMBAT_MAG_LVL = 123;

    public static final int MODIF_COMBAT_AC_ARMOR = 26;
    public static final int MODIF_COMBAT_AC_SHIELD = 27;
    public static final int MODIF_COMBAT_AC_NATURAL = 28;
    public static final int MODIF_COMBAT_AC_PARADE = 29;

    public static final int MODIF_COMBAT_ATT_MELEE = 31;
    public static final int MODIF_COMBAT_ATT_RANGED = 32;
    public static final int MODIF_COMBAT_CMB = 33;
    public static final int MODIF_COMBAT_CMD = 34;
    public static final int MODIF_COMBAT_DAM_MELEE = 35;
    public static final int MODIF_COMBAT_DAM_RANGED = 36;
    public static final int MODIF_COMBAT_CRIT_RANGE = 37;
    public static final int MODIF_COMBAT_CRIT_MULT = 38;

    public static final int MODIF_SKILL_ALL = 41;
    public static final int MODIF_SKILL_FOR = 42;
    public static final int MODIF_SKILL_DEX = 43;
    public static final int MODIF_SKILL_CON = 44; // doesn't exist
    public static final int MODIF_SKILL_INT = 45;
    public static final int MODIF_SKILL_WIS = 46;
    public static final int MODIF_SKILL_CHA = 47;

    public static final int MODIF_SKILL = 200;

    private long characterId;
    private long itemId;
    private List<Pair<Integer,Integer>> modifs;
    private String icon;
    private boolean enabled;

    public Modification() {}

    public Modification(String name, List<Pair<Integer,Integer>> modifs, String icon) {
        this(name, modifs, icon, false);
    }

    public Modification(String name, List<Pair<Integer,Integer>> modifs, String icon, boolean enabled) {
        this.name = name;
        this.modifs = modifs;
        this.icon = icon;
        this.enabled = enabled;
    }

    public Modification(Modification clone) {
        this.name = clone.name;
        this.description = clone.description;
        this.characterId = clone.characterId;
        this.itemId = clone.itemId;
        this.modifs = clone.modifs;
        this.icon = clone.icon;
        this.enabled = clone.enabled;
    }

    @Override
    public void setReference(String reference) {
        throw new IllegalStateException("Modification items have no reference!");
    }

    @Override
    public void setSource(String source) {
        throw new IllegalStateException("Modification items have no reference!");
    }

    public long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getModifCount() { return modifs == null ? 0 : modifs.size(); }

    public Pair<Integer, Integer> getModif(int idx) {
        if(idx >= 0 && idx < modifs.size()) {
            return modifs.get(idx);
        }
        return null;
    }

    public List<Pair<Integer, Integer>> getModifs() {
        return modifs;
    }

    public void setModifs(List<Pair<Integer, Integer>> modifs) {
        this.modifs = modifs;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isValid() {
        return name != null && name.length() > 0
                && modifs != null && modifs.size() > 0
                && icon != null && icon.length() > 0;
    }

    @Override
    public DBEntityFactory getFactory() {
        return ModificationFactory.getInstance();
    }

    public static int modificationForAbility(String abilityId) {
        if(abilityId == null) {
            return 0;
        }
        // TODO: make it language-independant
        switch(abilityId) {
            case "FOR": return MODIF_SKILL_FOR;
            case "DEX": return MODIF_SKILL_DEX;
            case "CON": return MODIF_SKILL_CON;
            case "INT": return MODIF_SKILL_INT;
            case "SAG": return MODIF_SKILL_WIS;
            case "CHA": return MODIF_SKILL_CHA;
            default: return 0;
        }
    }

}
