package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.ConfigurationUtil;

import java.text.Collator;

public class ClassFeature extends DBEntity {

    // classfeature-specific
    private String conditions;
    private Class class_;
    private ClassArchetype archetype;
    private ClassFeature linkedTo;
    private String linkedName;
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
        int idx = name.indexOf("(");
        if( idx > 0) {
          name = name.substring(0, idx).trim();
        }
        // remove (category:)
        idx = name.indexOf(":");
        if( idx > 0) {
            name = name.substring(idx+1).trim();
        }
        return name;
    }

    @Override
    public String getNameLong() {
        String template = ConfigurationUtil.getInstance(null).getProperties().getProperty("template.classfeatures.name");
        if(getClass_() != null) {
            return String.format(template, getClass_().getNameShort(), getLevel(), getName());
        } else {
            return getName();
        }
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

    public String getLinkedName() { return linkedName; }
    public void setLinkedName(String linkedName) { this.linkedName = linkedName; }

    @Override
    public int compareTo(DBEntity o) {
        if(!(o instanceof ClassFeature)) {
            return super.compareTo(o);
        }
        ClassFeature cf = (ClassFeature)o;
        if(getName() == null || cf.getName() == null) {
            return 0;
        } else if(getLevel() == cf.getLevel()) {
            return super.compareTo(cf);
        } else {
            return Integer.compare(getLevel(), ((ClassFeature) o).getLevel());
        }
    }
}
