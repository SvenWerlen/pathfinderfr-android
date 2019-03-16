package org.pathfinderfr.app.database.entity;

public class ClassFeature extends DBEntity {

    // classfeature-specific
    private String conditions;
    private Class class_;
    private boolean auto;
    private int level;


    @Override
    public DBEntityFactory getFactory() {
        return ClassFeatureFactory.getInstance();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && class_ != null;
    }

    public boolean isAuto() { return auto; }
    public void setAuto(boolean auto) { this.auto = auto; }

    public String getConditions() {
        return conditions;
    }
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Class getClass_() { return class_; }
    public void setClass(Class class_) { this.class_ = class_; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
