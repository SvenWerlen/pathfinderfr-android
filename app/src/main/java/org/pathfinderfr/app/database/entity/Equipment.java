package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.StringUtil;

public class Equipment extends DBEntity {

    // equipment-specific
    private String cost;
    private String weight;
    private String category;

    @Override
    public boolean isValid() {
        return getName() != null && getName().length() > 0;
    }

    @Override
    public DBEntityFactory getFactory() {
        return EquipmentFactory.getInstance();
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getWeight() {
        return weight;
    }

    public int getWeightInGrams() {
        return StringUtil.parseWeight(weight);
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
