package org.pathfinderfr.app.database.entity;

import java.util.ArrayList;
import java.util.List;

public class Class extends DBEntity {

    // class-specific
    private String alignment;
    private String hitDie;
    private List<String> skills;
    private List<Level> levels;

    public Class() {
        skills = new ArrayList<>();
        levels = new ArrayList<>();
    }

    public List<Level> getLevels() {
        return levels;
    }
    public void setAlignment(String alignment) { this.alignment = alignment; };
    public String getAlignment() { return this.alignment; };
    public void setHitDie(String hitDie) { this.hitDie = hitDie; };
    public String getHitDie() { return this.hitDie; };
    public List<String> getSkills() {
        return skills;
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
        public Level(int lvl, int[] bab, int fortitude, int reflex, int will) {
            this.id = lvl;
            this.bab = bab;
            this.fortitude = fortitude; this.reflex = reflex; this.will = will;
        }
        public int getId() { return id; }
        public int getLvl() { return id; }
        public void setId(int id) { this.id = id; }
        public void setLvl(int lvl) { this.id = lvl; }
        public int[] getBaseAttackBonus() { return bab; }
        public void setBaseAttackBonus(int[] bab) { this.bab = bab; }
        public int getReflexBonus() { return reflex; }
        public void setReflexBonus(int bonus) { this.reflex = bonus; }
        public int getFortitudeBonus() { return fortitude; }
        public void setFortitudeBonus(int bonus) { this.fortitude = bonus; }
        public int getWillBonus() { return will; }
        public void setWillBonus(int bonus) { this.will = bonus; }
    }
}
