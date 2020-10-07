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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassArchetype ca = (ClassArchetype) o;
        return class_ != null ? class_.getName().equals(ca.class_.getName()) : ca.class_ == null;
    }
}
