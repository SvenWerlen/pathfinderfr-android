package org.pathfinderfr.app.util;

import org.junit.Test;
import org.pathfinderfr.app.database.entity.Spell;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpellUtilTest {

    @Test
    public void test() {
        List<Pair<String, Integer>> val = SpellUtil.cleanClasses("Inq 1, Pal 1, Prê 1");
        assertEquals(3, val.size());
        assertEquals("Inq", val.get(0).first);
        assertEquals((Integer)1, val.get(0).second);
        assertEquals("Pal", val.get(1).first);
        assertEquals((Integer)1, val.get(1).second);
        assertEquals("Prê", val.get(2).first);
        assertEquals((Integer)1, val.get(2).second);
    }
}
