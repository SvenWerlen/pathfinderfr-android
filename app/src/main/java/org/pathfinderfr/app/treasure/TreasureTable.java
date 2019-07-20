package org.pathfinderfr.app.treasure;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreasureTable {

    private String name;
    private List<TreasureRow> rows;

    public TreasureTable() {
        rows = new ArrayList<>();
    }

    public void addRow(String value) {
        TreasureRow row = new TreasureRow(value);
        if(row.isValid()) {
            rows.add(new TreasureRow(value));
        }
    }

    public List<TreasureRow> getRows() { return rows; }

    public int maxChoice(int type) {
        int maxVal = 0;
        // special case (equal chance for each entry)
        for(TreasureRow row : rows) {
            if(row.isChoice()) {
                return rows.size();
            }
        }

        // normal case
        for(TreasureRow row : rows) {
            Integer val = row.getDiceTo(type);
            if(val != null && val > maxVal) {
                maxVal = row.getDiceTo(type);
            }
        }
        return maxVal;
    }

    public String getChoice(int type, int dice) {
        // special case (equal chance for each entry)
        for(TreasureRow row : rows) {
            if(row.isChoice()) {
                return dice < rows.size() ? rows.get(dice).getResultName() : null;
            }
        }
        // normal case
        for(TreasureRow row : rows) {
            Integer val = row.getDiceTo(type);
            if(val != null && val >= dice) {
                return row.getResultName();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Treasure table\n==================\n");
        for(TreasureRow row : rows) {
            buf.append(String.format("%02d %02d %02d %s\n",
                    row.getWeakTo(), row.getIntermediateTo(), row.getPowerfulTo(),
                    row.getResultName()));
        }
        return buf.toString();
    }
}
