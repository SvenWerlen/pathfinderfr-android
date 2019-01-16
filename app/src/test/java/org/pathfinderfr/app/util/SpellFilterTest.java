package org.pathfinderfr.app.util;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.pathfinderfr.app.database.entity.Spell;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class SpellFilterTest {

    @Before
    public void init() {
        PowerMockito.mockStatic(Log.class);
    }

    private static Spell generateSpell(long id, String school, String level) {
        Spell s = new Spell();
        s.setId(id);
        s.setName("Spell" + id);
        s.setSchool(school);
        s.setLevel(level);
        return s;
    }

    private static void assertlistsMatch(List<String> l1, List<String> l2) {
        assertNotNull(l1);
        assertNotNull(l2);
        assertEquals(l2.size(), l1.size());
        for (int i = 0; i < l1.size(); i++) {
            assertEquals(l2.get(i), l1.get(i));
        }
    }

    private static void assertSpellListsMatch(List<Spell> l1, List<Spell> l2) {
        assertNotNull(l1);
        assertNotNull(l2);
        assertEquals(l2.size(), l1.size());
        for (int i = 0; i < l1.size(); i++) {
            assertEquals(l2.get(i), l1.get(i));
        }
    }

    @Test
    public void schoolsUniqAndSortAndAccent() {
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, "Illusion", null));
        spells.add(generateSpell(2, "Universel", null));
        spells.add(generateSpell(3, "Évocation", null));
        spells.add(generateSpell(4, "Illusion", null));
        spells.add(generateSpell(5, "Universel", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation", "Illusion", "Universel"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getSchools(), expected);

    }

    @Test
    public void schoolsCaseInsensitive() {
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, "IllUSion", null));
        spells.add(generateSpell(2, "Universel", null));
        spells.add(generateSpell(3, "évocation", null));
        spells.add(generateSpell(4, "IlluSion", null));
        spells.add(generateSpell(5, "UniverSEL", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation", "Illusion", "Universel"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getSchools(), expected);
    }

    @Test
    public void schoolsStripCharacters() {
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, "Illusion(guérison)", null));
        spells.add(generateSpell(2, "Universel", null));
        spells.add(generateSpell(3, "Évocation;", null));
        spells.add(generateSpell(4, "Illusion ;", null));
        spells.add(generateSpell(5, "Universel", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation", "Illusion", "Universel"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getSchools(), expected);

    }

    @Test
    public void schoolsErrorCases() {
        // invalid entries 2 and 5
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, "Illusion(guérison)", null));
        spells.add(generateSpell(2, null, null));
        spells.add(generateSpell(3, "Évocation;", null));
        spells.add(generateSpell(4, "Illusion ;", null));
        spells.add(generateSpell(5, "#34;?", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation", "Illusion"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getSchools(), expected);
    }


    @Test
    public void classesUniqAndSortAndAccent() {
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, null, "chaman 1"));
        spells.add(generateSpell(2, null, "alchimiste 1, chaman 2"));
        spells.add(generateSpell(3, null, "sorcière 1"));
        spells.add(generateSpell(4, null, "mag 9, sorcière 2"));
        spells.add(generateSpell(5, null, "palad 1, chaman 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc", "Cha", "Mag", "Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getClasses(), expected);
    }

    @Test
    public void classesCaseInsensitive() {
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, null, "chAman 1"));
        spells.add(generateSpell(2, null, "alchimiste 1, CHaman 2"));
        spells.add(generateSpell(3, null, "sOrcière 1"));
        spells.add(generateSpell(4, null, "mag 9, sorcière 2"));
        spells.add(generateSpell(5, null, "PALad 1, chaman 2, pAladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc", "Cha", "Mag", "Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getClasses(), expected);
    }


    @Test
    public void classesStripCharacters() {
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, null, "chaman 1;"));
        spells.add(generateSpell(2, null, "alchimiste 1, chaman 2"));
        spells.add(generateSpell(3, null, "sorcière 1"));
        spells.add(generateSpell(4, null, "mag 9(blast), sorcière 2"));
        spells.add(generateSpell(5, null, "palad 1, cham;an 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc", "Cha", "Mag", "Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getClasses(), expected);
    }


    @Test
    public void classesErrorCases() {
        // entries 2 and 4 are invalid
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, null, "chaman 1"));
        spells.add(generateSpell(2, null, null));
        spells.add(generateSpell(3, null, "sorcière 1"));
        spells.add(generateSpell(4, null, "&*# 9, sorcière 2"));
        spells.add(generateSpell(5, null, "palad 1, chaman 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Cha", "Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getClasses(), expected);
    }


    @Test
    public void classesMultiple() {
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, null, "chaman 1"));
        spells.add(generateSpell(2, null, "alchimiste/élémentaliste 1, chaman 2"));
        spells.add(generateSpell(3, null, "sorcière 1"));
        spells.add(generateSpell(4, null, "mag 9, sorcière 2"));
        spells.add(generateSpell(5, null, "palad 1, chaman 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc", "Cha", "Élé", "Mag", "Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells, null);
        assertlistsMatch(filter.getClasses(), expected);
    }


    @Test
    public void filterManagement() {
        Spell spell1 = generateSpell(1, "Illusion", "chaman 1");
        Spell spell2 = generateSpell(2, "Illusion", "alchimiste/magicien 1, chaman 2");
        Spell spell3 = generateSpell(3, "Évocation", "sorcière 1");
        Spell spell4 = generateSpell(4, "Universel", "mag 9, sorcière 2");
        Spell spell5 = generateSpell(5, "Évocation", "palad 1, chaman 2, paladin 4");
        List<Spell> spells = Arrays.asList(new Spell[]{spell1, spell2, spell3, spell4, spell5});

        SpellFilter filter = new SpellFilter(spells, null);
        assertFalse(filter.hasFilterClass());
        assertFalse(filter.hasFilterLevel());
        assertFalse(filter.hasFilterSchool());
        assertFalse(filter.isFilterClassEnabled("Class"));
        assertFalse(filter.isFilterLevelEnabled("1"));
        assertFalse(filter.isFilterSchoolEnabled("School"));
        filter.addFilterClass("Class");
        assertTrue(filter.hasFilterClass());
        assertFalse(filter.hasFilterLevel());
        assertFalse(filter.hasFilterSchool());
        assertTrue(filter.isFilterClassEnabled("Class"));
        assertFalse(filter.isFilterLevelEnabled("1"));
        assertFalse(filter.isFilterSchoolEnabled("School"));
        filter.addFilterLevel("1");
        assertTrue(filter.hasFilterClass());
        assertTrue(filter.hasFilterLevel());
        assertFalse(filter.hasFilterSchool());
        assertTrue(filter.isFilterClassEnabled("Class"));
        assertTrue(filter.isFilterLevelEnabled("1"));
        assertFalse(filter.isFilterSchoolEnabled("School"));
        filter.addFilterSchool("School");
        assertTrue(filter.hasFilterClass());
        assertTrue(filter.hasFilterLevel());
        assertTrue(filter.hasFilterSchool());
        assertTrue(filter.isFilterClassEnabled("Class"));
        assertTrue(filter.isFilterLevelEnabled("1"));
        assertTrue(filter.isFilterSchoolEnabled("School"));

        filter.clearFilters();
        filter.addFilterClass("Class2");
        assertFalse(filter.isFilterClassEnabled("Class"));
        assertTrue(filter.isFilterClassEnabled("Class2"));

        filter.clearFilters();
        List<Spell> expected = Arrays.asList(new Spell[]{spell1, spell2, spell3, spell4, spell5});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.addFilterSchool("IllUSIOn");
        expected = Arrays.asList(new Spell[]{spell1, spell2});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.addFilterSchool("Universel");
        expected = Arrays.asList(new Spell[]{spell1, spell2, spell4});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.addFilterSchool("Unkown");
        expected = Arrays.asList(new Spell[]{spell1, spell2, spell4});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterClass("MAG");
        expected = Arrays.asList(new Spell[]{spell2, spell4});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.addFilterClass("chA");
        expected = Arrays.asList(new Spell[]{spell1, spell2, spell4, spell5});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterLevel("2");
        expected = Arrays.asList(new Spell[]{spell2, spell4, spell5});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterLevel("4");
        expected = Arrays.asList(new Spell[]{spell5});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterClass("cha");
        filter.addFilterLevel("4");
        expected = Arrays.asList(new Spell[]{});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterClass("pal");
        filter.addFilterLevel("4");
        expected = Arrays.asList(new Spell[]{spell5});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterClass("cha");
        filter.addFilterLevel("2");
        expected = Arrays.asList(new Spell[]{spell2, spell5});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterClass("mag");
        filter.addFilterLevel("1");
        expected = Arrays.asList(new Spell[]{spell2});
        assertSpellListsMatch(filter.getFilteredList(), expected);

        filter.clearFilters();
        filter.addFilterSchool("school");
        assertEquals(filter.generatePreferences(),"school::");
        SpellFilter filter2 = new SpellFilter(null, filter.generatePreferences());
        assertTrue(filter2.isFilterSchoolEnabled("school"));
        assertEquals(filter.generatePreferences(),filter2.generatePreferences());

        filter.addFilterSchool("other");
        filter2 = new SpellFilter(null, filter.generatePreferences());
        assertEquals(filter.generatePreferences(),filter2.generatePreferences());

        filter.clearFilters();
        filter.addFilterClass("mag");
        assertEquals(filter.generatePreferences(),":mag:");
        filter2 = new SpellFilter(null, filter.generatePreferences());
        assertTrue(filter2.isFilterClassEnabled("mag"));
        assertEquals(filter.generatePreferences(),filter2.generatePreferences());

        filter.addFilterClass("ens");
        filter2 = new SpellFilter(null, filter.generatePreferences());
        assertEquals(filter.generatePreferences(),filter2.generatePreferences());

        filter.clearFilters();
        filter.addFilterLevel("8");
        assertEquals(filter.generatePreferences(),"::8");
        filter2 = new SpellFilter(null, filter.generatePreferences());
        assertTrue(filter2.isFilterLevelEnabled("8"));
        assertEquals(filter.generatePreferences(),filter2.generatePreferences());

        filter.addFilterLevel("2");
        filter2 = new SpellFilter(null, filter.generatePreferences());
        assertEquals(filter.generatePreferences(),filter2.generatePreferences());

        filter.clearFilters();
        assertEquals(filter.generatePreferences(),"::");
        filter.addFilterSchool("school");
        assertEquals(filter.generatePreferences(),"school::");
        filter.addFilterClass("mag");
        assertEquals(filter.generatePreferences(),"school:mag:");
        filter.addFilterLevel("1");
        assertEquals(filter.generatePreferences(),"school:mag:1");
        filter.addFilterSchool("other");
        assertTrue(filter.isFilterSchoolEnabled("school"));
        assertTrue(filter.isFilterSchoolEnabled("other"));
        filter.addFilterClass("ens");
        assertTrue(filter.isFilterClassEnabled("mag"));
        assertTrue(filter.isFilterClassEnabled("ens"));
        filter.addFilterLevel("2");
        assertTrue(filter.isFilterLevelEnabled("1"));
        assertTrue(filter.isFilterLevelEnabled("2"));
    }

}
