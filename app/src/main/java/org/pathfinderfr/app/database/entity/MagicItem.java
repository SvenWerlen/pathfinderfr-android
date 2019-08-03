package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.StringUtil;

public class MagicItem extends DBEntity {

    public static int TYPE_ARMOR_SHIELD = 1;
    public static int TYPE_WEAPON       = 2;
    public static int TYPE_POTION       = 3;
    public static int TYPE_RING         = 4;
    public static int TYPE_SCEPTER      = 5;
    public static int TYPE_PARCHMENT    = 6;
    public static int TYPE_STAFF        = 7;
    public static int TYPE_WAND         = 8;
    public static int TYPE_OBJECT       = 9;

    // weapon-specific
    private int type;
    private String cost;
    private String location;
    private String weight;
    private String aura;
    private int spellCasterLevel; // NLS
    private String manufCost;
    private String manufReq;

    @Override
    public boolean isValid() {
        return getName() != null && getName().length() > 0;
    }

    @Override
    public DBEntityFactory getFactory() {
        return MagicItemFactory.getInstance();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getAura() {
        return aura;
    }

    public void setAura(String aura) {
        this.aura = aura;
    }

    public int getSpellCasterLevel() {
        return spellCasterLevel;
    }

    public void setSpellCasterLevel(int spellCasterLevel) {
        this.spellCasterLevel = spellCasterLevel;
    }

    public String getManufacturingCost() {
        return manufCost;
    }

    public void setManufacturingCost(String manufCost) {
        this.manufCost = manufCost;
    }

    public String getManufacturingReq() {
        return manufReq;
    }

    public void setManufacturingReq(String manufReq) {
        this.manufReq = manufReq;
    }
}
