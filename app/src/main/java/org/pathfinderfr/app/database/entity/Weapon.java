package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Weapon extends DBEntity {

    // weapon-specific
    private String cost;
    private String damageSmall;
    private String damageMedium;
    private String critical;
    private String range;
    private String weight;
    private String type;
    private String special;

    @Override
    public boolean isValid() {
        return getName() != null && getName().length() > 0;
    }

    @Override
    public DBEntityFactory getFactory() {
        return WeaponFactory.getInstance();
    }

    @Override
    public String getNameLong() {
        if(getDamageMedium() != null) {
            return String.format("%s (%s, %s, %s)", getName(), getDamageMedium(), getCritical(), getType());
        } else {
            return getName();
        }
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getDamageSmall() {
        return damageSmall;
    }

    public void setDamageSmall(String damageSmall) {
        this.damageSmall = damageSmall;
    }

    public String getDamageMedium() {
        return damageMedium;
    }

    public void setDamageMedium(String damageMedium) {
        this.damageMedium = damageMedium;
    }

    public String getDamageForSize(int size) {
        if(size == Character.SIZE_MEDIUM) {
            return getDamageMedium();
        } else if(size == Character.SIZE_SMALL) {
            return getDamageSmall();
        } else {
            // not supported
            return "";
        }
    }

    public int getDamageBonus(int strBonus) {
        if("-".equals(getRangeInMeters())) {
            return strBonus;
        } else if (name.toLowerCase().contains("fronde")) {
            return strBonus;
        } else if (name.toLowerCase().contains("composite")) {
            return strBonus;
        } else if (name.toLowerCase().contains("arc") && strBonus < 0) {
            return strBonus;
        } else {
            return 0;
        }
    }

    public String getCritical() {
        return critical;
    }

    public void setCritical(String critical) {
        this.critical = critical;
    }

    public String getRange() {
        return range;
    }

    public boolean isRanged() {
        return range != null && range.indexOf(" (") > 0;
    }

    public String getRangeInMeters() {
        if(range != null && range.indexOf(" (") > 0) {
            return range.substring(0, range.indexOf(" ("));
        } else {
            return "-";
        }
    }

    public void setRange(String range) {
        this.range = range;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }
}
