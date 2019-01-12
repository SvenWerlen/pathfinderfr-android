package org.pathfinderfr.app.database.entity;

import java.io.Serializable;

public abstract class DBEntity implements Serializable {

    public abstract DBEntityFactory getFactory();

    protected long id;
    protected String name;
    protected String description;
    protected String reference;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getNameLong() { return name; }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public boolean isValid() {
        return getName() != null && getName().length() > 0
                && getDescription() != null && getDescription().length() > 0;
    }

    @Override
    public String toString() {
        String id = getId() > 0 ? String.valueOf(getId()) : "-";
        String desc = getDescription() == null ? "" : getDescription();
        desc = desc.length() > 20 ? desc.substring(0, 20) + "..." : desc;
        return String.format("%s(%s, %s, %s)", this.getClass().getSimpleName(), id, getName(), desc);
    }
}
