package org.pathfinderfr.app.util;

import org.junit.Test;
import org.pathfinderfr.app.util.StringUtil;

import static org.junit.Assert.assertEquals;

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

        source = new String[] {"a"};
        expected = "a";
        assertEquals(StringUtil.listToString(source,','), expected);

        source = new String[] {};
        expected = "";
        assertEquals(StringUtil.listToString(source,','), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "a|b|c|d";
        assertEquals(StringUtil.listToString(source,'|'), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "'a'|'b'|'c'|'d'";
        assertEquals(StringUtil.listToString(source,'|','\''), expected);

        source = source = null;
        expected = null;
        assertEquals(StringUtil.listToString(source,'|'), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "abcd";
        assertEquals(StringUtil.listToString(source,null), expected);

        source = source = new String[] {"a"};
        expected = "'a'";
        assertEquals(StringUtil.listToString(source,',','\''), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "'a''b''c''d'";
        assertEquals(StringUtil.listToString(source,null,'\''), expected);

        source = source = new String[] {"a","b","c","d"};
        expected = "_a_-_b_-_c_-_d_";
        assertEquals(StringUtil.listToString(source,'-','_'), expected);
    }
}
