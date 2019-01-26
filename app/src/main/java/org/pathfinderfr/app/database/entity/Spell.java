package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.text.Collator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Spell extends DBEntity implements Comparable<Spell> {

    public static final String TEMPLATE_SPELL_LIST = "template.spell.list";

    // spell-specific
    private String school;
    private String level;
    private String castingTime;
    private String components;
    private String range;
    private String target;
    private String duration;
    private String savingThrow;
    private String spellResistance;
    private String area;

    @Override
    public DBEntityFactory getFactory() {
        return SpellFactory.getInstance();
    }

    @Override
    public String getNameLong() {

        // extract levels
        if(level != null && level.length() > 0) {
            Set<Integer> levels = new TreeSet();
            for(int idx = 0; idx < level.length(); idx++) {
                if(java.lang.Character.isDigit(level.charAt(idx))) {
                    levels.add(Integer.valueOf(level.substring(idx,idx+1)));
                }
            }
            StringBuffer levelDetail = new StringBuffer();
            for(int l : levels) {
                levelDetail.append(l).append('/');
            }
            if(levelDetail.length() > 1) {
                levelDetail.deleteCharAt(levelDetail.length()-1);
            }

            String detailTemplate = ConfigurationUtil.getInstance(null).getProperties().getProperty(TEMPLATE_SPELL_LIST);
            return String.format(detailTemplate, name, levelDetail);
        }
        return name;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCastingTime() {
        return castingTime;
    }

    public void setCastingTime(String castingTime) {
        this.castingTime = castingTime;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSavingThrow() {
        return savingThrow;
    }

    public void setSavingThrow(String savingThrow) {
        this.savingThrow = savingThrow;
    }

    public String getSpellResistance() {
        return spellResistance;
    }

    public void setSpellResistance(String spellResistance) {
        this.spellResistance = spellResistance;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public int compareTo(Spell o) {
        return Collator.getInstance().compare(getName(),o.getName());
    }
}
