package org.pathfinderfr.app.database.entity;

public class Feat extends DBEntity {

    // feat-specific
    private String category;
    private String conditions;
    private String advantage;
    private String special;
    private String normal;

    @Override
    public DBEntityFactory getFactory() {
        return FeatFactory.getInstance();
    }

    /**
     * Override default behaviour because feats can have no description
     */
    @Override
    public boolean isValid() {
        return getName() != null && getName().length() > 0;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getAdvantage() {
        return advantage;
    }

    public void setAdvantage(String advantage) {
        this.advantage = advantage;
    }

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }
}
