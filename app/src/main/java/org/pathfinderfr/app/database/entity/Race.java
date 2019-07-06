package org.pathfinderfr.app.database.entity;

import java.util.ArrayList;
import java.util.List;

public class Race extends DBEntity {

    // race-specific
    private List<Trait> traits;

    public Race() {
        traits = new ArrayList<>();
    }

    public List<Trait> getTraits() {
        return traits;
    }

    // TODO: improve that method
    public Trait getAbilitiesTrait() {
        for(Trait t : traits) {
            if("Caractéristiques".equals(t.getName())) {
                return t;
            }
        }
        return null;
    }

    /**
     * Races have no description!
     */
    @Override
    public boolean isValid() {
        return getName() != null && getName().length() > 0;
    }

    @Override
    public DBEntityFactory getFactory() {
        return RaceFactory.getInstance();
    }

    /**
     * Sub-class for races' traits
     */
    public static class Trait {
        private String name;
        private String description;
        public Trait(String name, String desc) { this.name = name; this.description = desc; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

}
