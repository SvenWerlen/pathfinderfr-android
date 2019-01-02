package org.pathfinderfr.app.database.entity;

public class Spell extends DBEntity {

    private long id;
    private String name;
    private String description;

    // spell-specific
    private String school;
    private String level;
    private String castingTime;
    private String components;
    private String range;
    private String target;
    private String duration;
    private String savingThrow;
    private String spellResistance;


    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
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
