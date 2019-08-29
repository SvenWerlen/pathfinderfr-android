package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.ConfigurationUtil;

public class ClassFeature extends DBEntity {

    // classfeature-specific
    private String conditions;
    private Class class_;
    private ClassArchetype archetype;
    private ClassFeature linkedTo;
    private boolean auto;
    private int level;


    @Override
    public DBEntityFactory getFactory() {
        return ClassFeatureFactory.getInstance();
    }

    @Override
    public String getNameShort() {
        String name = getName();
        // remove (infos)
        //int idx = name.indexOf("(");
        //if( idx > 0) {
        //  name = name.substring(0, idx).trim();
        //}
        // remove (category:)
        int idx = name.indexOf(":");
        if( idx > 0) {
            name = name.substring(idx+1).trim();
        }
        return name;
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

    public ClassArchetype getClassArchetype() { return archetype; }
    public void setClassArchetype(ClassArchetype archetype) { this.archetype = archetype; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public ClassFeature getLinkedTo() { return linkedTo; }
    public void setLinkedTo(ClassFeature linkedTo) { this.linkedTo = linkedTo; }
}
