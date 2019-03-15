package org.pathfinderfr.app.database.entity;

public class Ability extends DBEntity {

    // ability-specific
    private String conditions;
    private String class_;
    private boolean auto;
    private int level;


    @Override
    public DBEntityFactory getFactory() {
        return AbilityFactory.getInstance();
    }

    public boolean isAuto() { return auto; }
    public void setAuto(boolean auto) { this.auto = auto; }

    public String getConditions() {
        return conditions;
    }
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getClass_() { return class_; }
    public void setClass(String class_) { this.class_ = class_; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
