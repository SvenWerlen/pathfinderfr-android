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

        val = SpellUtil.cleanClasses("Alch 1,  Conj 2, Ens/Mag 3, Magus 4, Sor 5");
        assertEquals(6, val.size());
        assertEquals("Alc", val.get(0).first);
        assertEquals((Integer)1, val.get(0).second);
        assertEquals("Con", val.get(1).first);
        assertEquals((Integer)2, val.get(1).second);
        assertEquals("Ens", val.get(2).first);
        assertEquals((Integer)3, val.get(2).second);
        assertEquals("Mag", val.get(3).first);
        assertEquals((Integer)3, val.get(3).second);
        assertEquals("Mgs", val.get(4).first);
        assertEquals((Integer)4, val.get(4).second);
        assertEquals("Sor", val.get(5).first);
        assertEquals((Integer)5, val.get(5).second);
    }
}
