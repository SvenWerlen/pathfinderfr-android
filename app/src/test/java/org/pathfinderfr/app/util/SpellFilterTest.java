package org.pathfinderfr.app.util;

import android.util.Log;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class SpellFilterTest {

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
        assertEquals(l2.size(),l1.size());
        for(int i=0; i<l1.size();i++) {
            assertEquals(l2.get(i),l1.get(i));
        }
    }

    @Test
    public void schoolsUniqAndSortAndAccent() {
        PowerMockito.mockStatic(Log.class);

        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1, "Illusion", null));
        spells.add(generateSpell(2, "Universel", null));
        spells.add(generateSpell(3, "Évocation", null));
        spells.add(generateSpell(4,  "Illusion", null));
        spells.add(generateSpell(5,  "Universel", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation","Illusion","Universel"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getSchools(),expected);

    }

    @Test
    public void schoolsCaseInsensitive() {
        PowerMockito.mockStatic(Log.class);

        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  "IllUSion", null));
        spells.add(generateSpell(2,  "Universel", null));
        spells.add(generateSpell(3,  "évocation", null));
        spells.add(generateSpell(4,  "IlluSion", null));
        spells.add(generateSpell(5,  "UniverSEL", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation","Illusion","Universel"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getSchools(),expected);
    }

    @Test
    public void schoolsStripCharacters() {
        PowerMockito.mockStatic(Log.class);

        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  "Illusion(guérison)", null));
        spells.add(generateSpell(2,  "Universel", null));
        spells.add(generateSpell(3,  "Évocation;", null));
        spells.add(generateSpell(4,  "Illusion ;", null));
        spells.add(generateSpell(5,  "Universel", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation","Illusion","Universel"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getSchools(),expected);

    }

    @Test
    public void schoolsErrorCases() {
        PowerMockito.mockStatic(Log.class);

        // invalid entries 2 and 5
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  "Illusion(guérison)", null));
        spells.add(generateSpell(2,  null, null));
        spells.add(generateSpell(3,  "Évocation;", null));
        spells.add(generateSpell(4,  "Illusion ;", null));
        spells.add(generateSpell(5,  "#34;?", null));
        List<String> expected = Arrays.asList(new String[]{"Évocation","Illusion"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getSchools(),expected);
    }


    @Test
    public void classesUniqAndSortAndAccent() {
        PowerMockito.mockStatic(Log.class);

        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  null, "chaman 1"));
        spells.add(generateSpell(2,  null, "alchimiste 1, chaman 2"));
        spells.add(generateSpell(3,  null, "sorcière 1"));
        spells.add(generateSpell(4,  null, "mag 9, sorcière 2"));
        spells.add(generateSpell(5,  null, "palad 1, chaman 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc","Cha","Mag","Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getClasses(),expected);
    }

    @Test
    public void classesCaseInsensitive() {
        PowerMockito.mockStatic(Log.class);

        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  null, "chAman 1"));
        spells.add(generateSpell(2,  null, "alchimiste 1, CHaman 2"));
        spells.add(generateSpell(3,  null, "sOrcière 1"));
        spells.add(generateSpell(4,  null, "mag 9, sorcière 2"));
        spells.add(generateSpell(5,  null, "PALad 1, chaman 2, pAladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc","Cha","Mag","Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getClasses(),expected);
    }


    @Test
    public void classesStripCharacters() {
        PowerMockito.mockStatic(Log.class);

        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  null, "chaman 1;"));
        spells.add(generateSpell(2,  null, "alchimiste 1, chaman 2"));
        spells.add(generateSpell(3,  null, "sorcière 1"));
        spells.add(generateSpell(4,  null, "mag 9(blast), sorcière 2"));
        spells.add(generateSpell(5,  null, "palad 1, cham;an 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc","Cha","Mag","Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getClasses(),expected);
    }


    @Test
    public void classesErrorCases() {
        PowerMockito.mockStatic(Log.class);

        // entries 2 and 4 are invalid
        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  null, "chaman 1"));
        spells.add(generateSpell(2,  null, null));
        spells.add(generateSpell(3,  null, "sorcière 1"));
        spells.add(generateSpell(4,  null, "&*# 9, sorcière 2"));
        spells.add(generateSpell(5,  null, "palad 1, chaman 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Cha","Pal","Sor"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getClasses(),expected);
    }


    @Test
    public void classesMultiple() {
        PowerMockito.mockStatic(Log.class);

        List<Spell> spells = new ArrayList<>();
        spells.add(generateSpell(1,  null, "chaman 1"));
        spells.add(generateSpell(2,  null, "alchimiste/élémentaliste 1, chaman 2"));
        spells.add(generateSpell(3,  null, "sorcière 1"));
        spells.add(generateSpell(4,  null, "mag 9, sorcière 2"));
        spells.add(generateSpell(5,  null, "palad 1, chaman 2, paladin 4"));
        List<String> expected = Arrays.asList(new String[]{"Alc","Cha", "Élé", "Mag","Pal", "Sor"});

        SpellFilter filter = new SpellFilter(spells,null);
        assertlistsMatch(filter.getClasses(),expected);
    }
}
