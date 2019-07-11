package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.ConfigurationUtil;

public class ClassFeature extends DBEntity {

    // classfeature-specific
    private String conditions;
    private Class class_;
    private ClassArchetype archetype;
    private boolean auto;
    private int level;


    @Override
    public DBEntityFactory getFactory() {
        return ClassFeatureFactory.getInstance();
    }

    @Override
    public String getNameLong() {
        if(archetype != null) {
            try {
                String template = ConfigurationUtil.getInstance().getProperties().getProperty("template.classfeatures.namelong");
                return String.format(template, getName(), archetype.getName());
            } catch(Exception e) {e.printStackTrace();}
        }
        return getName();
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
}
