package org.pathfinderfr.app.database.entity;

import java.util.ArrayList;
import java.util.List;

public class Feat extends DBEntity {

    // feat-specific
    private String summary;
    private String category;
    private String conditions;
    private List<Long> requires; // relations to other feats
    private String advantage;
    private String special;
    private String normal;

    // only used during YAML import
    private List<String> requiresRef;

    @Override
    public DBEntityFactory getFactory() {
        return FeatFactory.getInstance();
    }

    public Feat() {
        requiresRef = new ArrayList<>();
        requires = new ArrayList<>();
    }

    /**
     * Override default behaviour because feats can have no description
     */
    @Override
    public boolean isValid() {
        return getName() != null && getName().length() > 0;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCombat() {
        return category != null && category.toLowerCase().indexOf("combat") >= 0;
    }

    public String getConditions() {
        return conditions;
    }
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public List<String> getRequiresRef() {
        return requiresRef;
    }

    public void setRequiresRef(List<String> requiresRef) {
        this.requiresRef = requiresRef;
    }

    public List<Long> getRequires() {
        return requires;
    }

    public void setRequires(List<Long> requires) {
        this.requires = requires;
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

    @Override
    public String getDescription() {
        return getAdvantage();
    }
}
