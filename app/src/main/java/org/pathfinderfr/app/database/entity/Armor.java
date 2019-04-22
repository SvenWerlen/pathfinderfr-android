package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Armor extends DBEntity {

    // weapon-specific
    private String cost;
    private String bonus;
    private String bonusDexMax;
    private String malus;
    private String castFail;
    private String speed9;
    private String speed6;
    private String weight;


    @Override
    public boolean isValid() {
        return getName() != null && getName().length() > 0;
    }

    @Override
    public DBEntityFactory getFactory() {
        return ArmorFactory.getInstance();
    }

    @Override
    public String getNameLong() {
        if(getBonus() != null) {
            return String.format("%s (%s, %s, %s)", getName(), getBonus(), getMalus(), getCastFail());
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

    public String getBonus() {
        return bonus;
    }

    public void setBonus(String bonus) {
        this.bonus = bonus;
    }

    public String getBonusDexMax() {
        return bonusDexMax;
    }

    public void setBonusDexMax(String bonusDexMax) {
        this.bonusDexMax = bonusDexMax;
    }

    public String getMalus() {
        return malus;
    }

    public void setMalus(String malus) {
        this.malus = malus;
    }

    public String getCastFail() {
        return castFail;
    }

    public void setCastFail(String castFail) {
        this.castFail = castFail;
    }

    public String getSpeed9() {
        return speed9;
    }

    public void setSpeed9(String speed9) {
        this.speed9 = speed9;
    }

    public String getSpeed6() {
        return speed6;
    }

    public void setSpeed6(String speed6) {
        this.speed6 = speed6;
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
}
