package org.pathfinderfr.app.util;

import org.junit.Test;
import org.pathfinderfr.app.database.entity.Spell;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpellTableTest {

    @Test
    public void test() {
        List<String> names = Arrays.asList("Ensorceleur", "Magicien");
        SpellTable table = new SpellTable(names);

        Spell spell = new Spell();
        spell.setName("Sort 1");
        spell.setLevel("Bar 3");
        spell.setSchool("Something");
        table.addSpell(spell);
        assertEquals(0, table.getLevels().size());

        spell = new Spell();
        spell.setName("Sort 2");
        spell.setLevel("Ens 3");
        spell.setSchool("Abjuration");
        table.addSpell(spell);
        assertEquals(1, table.getLevels().size());
        assertEquals(3, table.getLevels().get(0).getLevel());
        assertEquals("Abjuration", table.getLevels().get(0).getSchools().get(0).getSchoolName());
        assertEquals(1, table.getLevels().get(0).getSchools().get(0).getSpells().size());
        assertEquals("Sort 2", table.getLevels().get(0).getSchools().get(0).getSpells().get(0).getName());

        spell = new Spell();
        spell.setName("Sort 3");
        spell.setLevel("Bar/Ens 2");
        spell.setSchool("Abjuration (123);");
        table.addSpell(spell);
        assertEquals(2, table.getLevels().size());
        assertEquals(2, table.getLevels().get(0).getLevel());
        assertEquals(3, table.getLevels().get(1).getLevel());
        assertEquals(1, table.getLevels().get(0).getSchools().size());
        assertEquals("Abjuration", table.getLevels().get(0).getSchools().get(0).getSchoolName());

        spell = new Spell();
        spell.setName("Sort 4");
        spell.setLevel("Drui 4, Bar/Ens 2");
        spell.setSchool("Abjuration;");
        table.addSpell(spell);
        assertEquals(2, table.getLevels().size());
        assertEquals(2, table.getLevels().get(0).getLevel());
        assertEquals(3, table.getLevels().get(1).getLevel());
        assertEquals(1, table.getLevels().get(0).getSchools().size());
        assertEquals("Abjuration", table.getLevels().get(0).getSchools().get(0).getSchoolName());
        assertEquals(2, table.getLevels().get(0).getSchools().get(0).getSpells().size());
        assertEquals("Sort 3", table.getLevels().get(0).getSchools().get(0).getSpells().get(0).getName());
        assertEquals("Sort 4", table.getLevels().get(0).getSchools().get(0).getSpells().get(1).getName());
    }
}
