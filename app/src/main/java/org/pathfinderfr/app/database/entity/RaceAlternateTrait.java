package org.pathfinderfr.app.database.entity;

import java.util.ArrayList;
import java.util.List;

public class RaceAlternateTrait extends DBEntity {

    // classfeature-specific
    private Race race;
    private List<String> replaces;
    private List<String> alters;


    @Override
    public DBEntityFactory getFactory() {
        return RaceAlternateTraitFactory.getInstance();
    }

    @Override
    public String getNameLong() {
        return race == null ? getName() : String.format("%s (%s)", getName(), getRace().getName());
    }

    @Override
    public boolean isValid() {
        int repNum = replaces == null ? 0 : replaces.size();
        int altNum = alters == null ? 0 : alters.size();
        return super.isValid() && race != null && (repNum + altNum > 0);
    }

    public Race getRace() { return race; }
    public void setRace(Race race) { this.race = race; }

    public List<String> getReplaces() {
        return replaces == null ? new ArrayList<String>() : new ArrayList<String>(replaces);
    }

    public synchronized void replaces(String traitName) {
        if(replaces == null) {
            replaces = new ArrayList<>();
        }
        replaces.add(traitName);
    }

    public List<String> getAlters() {
        return alters == null ? new ArrayList<String>() :  new ArrayList<String>(alters);
    }

    public synchronized void alters(String traitName) {
        if(alters == null) {
            alters = new ArrayList<>();
        }
        alters.add(traitName);
    }
}
