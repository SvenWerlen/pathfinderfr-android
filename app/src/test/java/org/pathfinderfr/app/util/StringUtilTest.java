package org.pathfinderfr.app.util;

import org.junit.Test;
import org.pathfinderfr.app.util.StringUtil;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StringUtilTest {

    @Test
    public void extractWebSiteValid() {
        String source = "http://www.pathfinder-fr.org";
        String expected = "www.pathfinder-fr.org";
        assertEquals(StringUtil.extractWebSite(source), expected);

        source = "https://www.pathfinder-fr.org";
        expected = "www.pathfinder-fr.org";
        assertEquals(StringUtil.extractWebSite(source), expected);

        source = "http://www.pathfinder-fr.org/Wiki/Pathfinder-RPG.Abondance%20de%20munitions.ashx";
        expected = "www.pathfinder-fr.org";
        assertEquals(StringUtil.extractWebSite(source), expected);
    }

    @Test
    public void extractWebSiteInvalid() {
        String source = "www.pathfinder-fr.org";
        String expected = null;
        assertEquals(StringUtil.extractWebSite(source), expected);

        source = "www.pa\\thf/_%inder-fr.org";
        expected = "??";
        assertEquals(StringUtil.extractWebSite(source), expected);
    }

    @Test
    public void ListToString() {
        String[] source = new String[] {"a","b","c","d"};
        String expected = "a,b,c,d";
        assertEquals(StringUtil.listToString(source,','), expected);
        assertEquals(StringUtil.listToString(source,","), expected);

        source = new String[] {"a","b","c","d"};
        expected = "a, b, c, d";
        assertEquals(StringUtil.listToString(source,", "), expected);

        source = new String[] {"a"};
        expected = "a";
        assertEquals(StringUtil.listToString(source,','), expected);
        assertEquals(StringUtil.listToString(source,","), expected);

        source = new String[] {};
        expected = "";
        assertEquals(StringUtil.listToString(source,','), expected);
        assertEquals(StringUtil.listToString(source,","), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "a|b|c|d";
        assertEquals(StringUtil.listToString(source,'|'), expected);
        assertEquals(StringUtil.listToString(source,"|"), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "'a'|'b'|'c'|'d'";
        assertEquals(StringUtil.listToString(source,'|','\''), expected);
        assertEquals(StringUtil.listToString(source,"|",'\''), expected);

        source = source = null;
        expected = null;
        assertEquals(StringUtil.listToString(source,'|'), expected);
        assertEquals(StringUtil.listToString(source,"|"), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "abcd";
        assertEquals(StringUtil.listToString(source, (Character)null), expected);

        source = source = new String[] {"a"};
        expected = "'a'";
        assertEquals(StringUtil.listToString(source,',','\''), expected);
        assertEquals(StringUtil.listToString(source,",",'\''), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "'a''b''c''d'";
        assertEquals(StringUtil.listToString(source,(Character)null,'\''), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "_a_-_b_-_c_-_d_";
        assertEquals(StringUtil.listToString(source,'-','_'), expected);
        assertEquals(StringUtil.listToString(source,"-",'_'), expected);
    }

    private static void assertlistsMatch(int[] l1, int[] l2) {
        assertNotNull(l1);
        assertNotNull(l2);
        assertEquals(l2.length, l1.length);
        for (int i = 0; i < l1.length; i++) {
            assertEquals(l2[i], l1[i]);
        }
    }

    @Test
    public void integerHandling() {
        int[] source = new int[] {2, 1, -4, 3};
        String expected = "2:1:-4:3";
        assertEquals(expected, StringUtil.listToString(source,':'));

        String[] sourceList = new String[] {"2", "+1", "-4", "3"};
        int[] expectedList = new int[] {2, 1, -4, 3};
        assertlistsMatch(expectedList, StringUtil.stringListToIntList(sourceList));
    }

    @Test
    public void parseWeight() {
        assertEquals(20, StringUtil.parseWeight("20 g"));
        assertEquals(0, StringUtil.parseWeight("20,5 g"));
        assertEquals(20000, StringUtil.parseWeight("20 kg"));
        assertEquals(2500, StringUtil.parseWeight("2,5 kg"));
        assertEquals(0, StringUtil.parseWeight("20 gkg"));
        assertEquals(0, StringUtil.parseWeight("special"));
        assertEquals(0, StringUtil.parseWeight(null));
        assertEquals(0, StringUtil.parseWeight(""));
    }

    @Test
    public void string2Cost() {
        assertEquals(11000L, StringUtil.string2Cost("110 po") );
        assertEquals(1000000, StringUtil.string2Cost("10 000 po"));
        assertEquals(1100L, StringUtil.string2Cost("110 pa"));
        assertEquals(110L, StringUtil.string2Cost("110 pc"));
        assertEquals(0L, StringUtil.string2Cost("-"));
        assertEquals(1L, StringUtil.string2Cost("1 pc-20 po"));
        assertEquals(0L, StringUtil.string2Cost("+10 po"));
        assertEquals(0L, StringUtil.string2Cost("spécial"));
    }

    @Test
    public void cost2String() {
        assertEquals("5 pc", StringUtil.cost2String(5L));
        assertEquals("6 pa, 5 pc", StringUtil.cost2String(65L));
        assertEquals("6 pa", StringUtil.cost2String(60L));
        assertEquals("12 po, 6 pa, 5 pc", StringUtil.cost2String(1265L));
        assertEquals("12 po, 6 pa", StringUtil.cost2String(1260L));
        assertEquals("12 po", StringUtil.cost2String(1200L));
        assertEquals("12 000 po", StringUtil.cost2String(1200000L));
    }

    @Test
    public void extractCritical() {
        assertArrayEquals(new int[] {20, 2, 0}, StringUtil.extractCritical("×2"));
        assertArrayEquals(new int[] {20, 2, 3}, StringUtil.extractCritical("×2/×3"));
        assertArrayEquals(new int[] {18, 2, 0}, StringUtil.extractCritical("18-20/×2"));
        assertArrayEquals(new int[] {18, 2, 3}, StringUtil.extractCritical("18-20/×2/×3"));
    }
}
