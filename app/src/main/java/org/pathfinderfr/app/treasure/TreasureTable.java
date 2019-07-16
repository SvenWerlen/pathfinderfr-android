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
