package org.pathfinderfr.app.util;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.Race;
import org.powermock.api.mockito.PowerMockito;

import static org.junit.Assert.assertEquals;

public class CharacterFactoryTest {

    @Before
    public void init() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void getAbilityBonus() {
        Character c = new Character();
        c.setName("Dorg");
        c.setRace(new Race());
        c.getRace().setId(4);
        c.getRace().setName("Gnome");
        Class cl1 = new Class();
        cl1.setId(1);
        cl1.setName("Barbare");
        c.addOrSetClass(cl1, 2);
        Class cl2 = new Class();
        cl2.setId(3);
        cl2.setName("Druide");
        c.addOrSetClass(cl2, 4);
        c.setStrength(11);
        c.setDexterity(12);
        c.setConstitution(13);
        c.setIntelligence(14);
        c.setWisdom(15);
        c.setCharisma(16);
        c.setSkillRank(1, 4);
        c.setSkillRank(3, 3);
        c.setSkillRank(2, 2);
        System.out.println(CharacterFactory.exportCharacterAsYML(c));
    }
}
