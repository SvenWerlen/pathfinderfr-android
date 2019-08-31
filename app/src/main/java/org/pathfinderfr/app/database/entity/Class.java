package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.CharacterUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Class extends DBEntity {

    // class-specific
    private String alignment;
    private String hitDie;
    private Set<String> skills;
    private List<Level> levels;
    private String altName;

    public Class() {
        skills = new HashSet<>();
        levels = new ArrayList<>();
    }

    @Override
    public String getNameShort() {
        return getShortName(false);
    }

    public String getShortName(boolean useAlternateName) {
        String name = useAlternateName && altName != null ? this.altName : this.name;
        // @TODO replace ugly fix for Barbare vs Barde
        if("Barbare".equals(name)) {
            return "Brb";
        }
        // @TODO replace ugly fix for Prêtre vs Prêtre Combattant
        else if("Prêtre combattant".equals(name)) {
            return "Prc";
        }
        // @TODO replace ugly fix for Arcaniste vs Archer mage
        else if("Archer mage".equals(name)) {
            return "ArM";
        }
        // @TODO replace ugly fix for Chamane vs Champion occultiste
        else if("Champion occultiste".equals(name)) {
            return "Chp";
        }
        // @TODO replace ugly fix for Magicien vs Magus
        else if("Magus".equals(name)) {
            return "Mgs";
        }
        name = name.toLowerCase();
        if(name.length()>=3) {
            return name.substring(0, 1).toUpperCase() + name.substring(1, 3);
        } else {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public List<Level> getLevels() {
        return levels;
    }
    public void setAlignment(String alignment) { this.alignment = alignment; };
    public String getAlignment() { return this.alignment; };
    public void setHitDie(String hitDie) { this.hitDie = hitDie; };
    public String getHitDie() { return this.hitDie; };
    public String getAltName() { return altName; }
    public void setAltName(String altName) { this.altName = altName; }

    public Set<String> getSkills() {
        return skills;
    }
    public int getMaxLevel() {
        if(levels.size()>0) {
            return levels.get(levels.size() - 1).getLvl();
        }
        return 0;
    }

    public Level getLevel(int lvl) {
        lvl = Math.min(lvl, 20); // maximum 20
        for(Level l : levels) {
            if(l.getLvl() == lvl) {
                return l;
            }
        }
        return null;
    }

    @Override
    public DBEntityFactory getFactory() {
        return ClassFactory.getInstance();
    }

    /**
     * Sub-class for class' level (evolution)
     */
    public static class Level {
        private int id;
        private int[] bab; // base attack bonus
        private int fortitude;
        private int reflex;
        private int will;
        private int maxSpellLvl;
        public Level(int lvl, int[] bab, int fortitude, int reflex, int will, int maxSpellLvl) {
            this.id = lvl;
            this.bab = bab;
            this.fortitude = fortitude; this.reflex = reflex; this.will = will;
            this.maxSpellLvl = maxSpellLvl;
        }
        public int getId() { return id; }
        public int getLvl() { return id; }
        public void setId(int id) { this.id = id; }
        public void setLvl(int lvl) { this.id = lvl; }
        public int[] getBaseAttackBonus() { return bab; }
        public String getBaseAttackBonusAsString() { return CharacterUtil.getAttackBonusAsString(bab); }
        public void setBaseAttackBonus(int[] bab) { this.bab = bab; }
        public int getReflexBonus() { return reflex; }
        public void setReflexBonus(int bonus) { this.reflex = bonus; }
        public int getFortitudeBonus() { return fortitude; }
        public void setFortitudeBonus(int bonus) { this.fortitude = bonus; }
        public int getWillBonus() { return will; }
        public void setWillBonus(int bonus) { this.will = bonus; }
        public int getMaxSpellLvl() { return maxSpellLvl; }
        public void setMaxSpellLvl(int value) { this.maxSpellLvl = value; }
    }
}
