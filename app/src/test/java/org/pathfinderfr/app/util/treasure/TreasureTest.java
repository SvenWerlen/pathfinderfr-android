package org.pathfinderfr.app.util.treasure;

import org.junit.Test;
import org.pathfinderfr.app.treasure.TreasureTable;

public class TreasureTest {

    @Test
    public void test() {
        String text = "01–04\t01–10\t01–10\tArmures et boucliers";
        TreasureTable table = new TreasureTable();
        table.addRow(text);
        System.out.println(table);
        text = "100\t01–10\t01–10\tArmures et boucliers";
        table = new TreasureTable();
        table.addRow(text);
        System.out.println(table);
        text = "100\t33\t01–10\tArmures et boucliers";
        table = new TreasureTable();
        table.addRow(text);
        System.out.println(table);
        text = "01-05\t-\t-\tParchemin 0 (NLS 1)";
        table = new TreasureTable();
        table.addRow(text);
        System.out.println(table);

    }
}
