package org.pathfinderfr.app.treasure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreasureRow {

    public static final int TYPE_WEAK = 0;
    public static final int TYPE_INTERMEDIATE = 1;
    public static final int TYPE_POWERFUL = 2;

    private boolean choice;
    private Integer weakTo, intermediateTo, powerfulTo;
    private String resultName;

    public boolean isChoice() { return choice; }

    public Integer getWeakTo() {
        return weakTo;
    }

    public Integer getIntermediateTo() {
        return intermediateTo;
    }

    public Integer getPowerfulTo() {
        return powerfulTo;
    }

    public Integer getDiceTo(int type) {
        if(type == TYPE_INTERMEDIATE) {
            return getIntermediateTo();
        } else if(type == TYPE_POWERFUL) {
            return getPowerfulTo();
        }
        return getWeakTo();
    }

    public boolean isValid() {
        return choice || !(weakTo == null && intermediateTo == null && powerfulTo == null);
    }

    public String getResultName() {
        return resultName;
    }

    private TreasureRow() {}

    public static TreasureRow newTreasureRow(String name) {
        TreasureRow row = new TreasureRow();
        row.resultName = name;
        row.choice = true;
        return row;
    }

    public TreasureRow(String value) {
        String[] val = value == null ? null : value.split("\t");
        if(val == null || val.length < 2 || val.length > 4) {
            throw new IllegalArgumentException("Invalid treasure row: " + value);
        }
        try {
            Pattern p1 = Pattern.compile("(\\d+)-(\\d+)");   // the pattern to search for
            Pattern p2 = Pattern.compile("(\\d+)");   // the pattern to search for
            resultName = val[val.length-1];
            if(!val[val.length-2].equals("-")) {
                // if we find a match, get the group
                Matcher m1 = p1.matcher(val[val.length-2]);
                Matcher m2 = p2.matcher(val[val.length-2]);
                if (m1.find()) {
                    powerfulTo = Integer.parseInt(m1.group(2));
                } else if(m2.find()) {
                    powerfulTo = Integer.parseInt(m2.group(1));
                }
            }
            if(val.length > 2 && !val[val.length-3].equals("-")) {
                // if we find a match, get the group
                Matcher m1 = p1.matcher(val[val.length-3]);
                Matcher m2 = p2.matcher(val[val.length-3]);
                if (m1.find()) {
                    intermediateTo = Integer.parseInt(m1.group(2));
                } else if(m2.find()) {
                    intermediateTo = Integer.parseInt(m2.group(1));
                }
            }
            if(val.length > 3 && !val[val.length-4].equals("-")) {
                // if we find a match, get the group
                Matcher m1 = p1.matcher(val[val.length-4]);
                Matcher m2 = p2.matcher(val[val.length-4]);
                if (m1.find()) {
                    weakTo = Integer.parseInt(m1.group(2));
                } else if(m2.find()) {
                    weakTo = Integer.parseInt(m2.group(1));
                }
            }

        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Invalid treasure row: " + value, e);
        }
    }
}