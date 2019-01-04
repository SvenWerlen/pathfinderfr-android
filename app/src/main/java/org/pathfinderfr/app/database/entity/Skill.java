package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.database.entity.DBEntity;

public class Skill extends DBEntity {

    // feat-specific
    private String ability;
    private String training;
    private String armorpenalty;

    @Override
    public DBEntityFactory getFactory() {
        return SkillFactory.getInstance();
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public String getTraining() {
        return training;
    }

    public void setTraining(String training) {
        this.training = training;
    }

    public String getArmorpenalty() {
        return armorpenalty;
    }

    public void setArmorpenalty(String armorpenalty) {
        this.armorpenalty = armorpenalty;
    }
}
