package org.pathfinderfr.app.database.entity;

public class SpellClassLevel {

    private long id;
    private long spellId;
    private long classId;
    private int level;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSpellId() {
        return spellId;
    }

    public void setSpellId(long spellId) {
        this.spellId = spellId;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
