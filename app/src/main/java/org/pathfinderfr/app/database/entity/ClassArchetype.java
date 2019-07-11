package org.pathfinderfr.app.database.entity;

public class ClassArchetype extends DBEntity {

    // archetypes-specific
    private Class class_;


    @Override
    public DBEntityFactory getFactory() {
        return ClassArchetypesFactory.getInstance();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && class_ != null;
    }

    public Class getClass_() { return class_; }
    public void setClass(Class class_) { this.class_ = class_; }
}
