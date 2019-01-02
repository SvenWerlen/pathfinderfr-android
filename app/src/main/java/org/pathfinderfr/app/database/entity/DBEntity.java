package org.pathfinderfr.app.database.entity;

import java.io.Serializable;

public abstract class DBEntity implements Serializable {

    public abstract DBEntityFactory getFactory();

    public abstract long getId();

    public abstract String getName();

    public abstract String getDescription();

    public boolean isValid() {
        return getName() != null && getName().length() > 0
                && getDescription() != null && getDescription().length() > 0;
    }

    @Override
    public String toString() {
        String id = getId() > 0 ? String.valueOf(getId()) : "-";
        String desc = getDescription();
        desc = desc.length() > 20 ? desc.substring(0, 20) + "..." : desc;
        return String.format("%s(%s, %s, %s)", this.getClass().getSimpleName(), id, getName(), desc);
    }
}
