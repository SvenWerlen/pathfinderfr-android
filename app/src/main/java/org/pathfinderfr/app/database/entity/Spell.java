package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;

public class Spell extends DBEntity {

    private int id;
    private String name;
    private String description;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public DBEntityFactory getFactory() {
        return SpellFactory.getInstance();
    }
}
