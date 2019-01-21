package org.pathfinderfr.app.util;

import org.junit.Test;
import org.pathfinderfr.app.util.StringUtil;

import java.util.List;

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
}
