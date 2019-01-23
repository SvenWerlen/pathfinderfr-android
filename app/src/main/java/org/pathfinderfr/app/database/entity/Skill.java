package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.database.entity.DBEntity;

public class Skill extends DBEntity {

    // skill-specific
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

    public String getAbilityId() {
        if(ability == null) {
            return null;
        } else if(ability.length() <= 3) {
            return ability.toUpperCase();
        } else {
            return ability.substring(0,3).toUpperCase();
        }
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
