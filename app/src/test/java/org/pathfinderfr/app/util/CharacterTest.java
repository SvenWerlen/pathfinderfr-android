package org.pathfinderfr.app.util;

import org.junit.Test;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.Class;

import static org.junit.Assert.assertEquals;

public class CharacterTest {

    @Test
    public void getAbilityBonus() {
        Class cl1 = new Class();
        cl1.setId(1);
        cl1.setName("Class 1");
        cl1.getLevels().add(new Class.Level(1, new int[] { 1 }, 0,0,0,0));
        cl1.getLevels().add(new Class.Level(2, new int[] { 5 }, 0,0,0,0));
        cl1.getLevels().add(new Class.Level(3, new int[] { 6, 1 }, 0,0,0,0));
        cl1.getLevels().add(new Class.Level(4, new int[] { 11, 6, 1 }, 0,0,0,0));
        Class cl2 = new Class();
        cl2.setId(2);
        cl2.setName("Class 2");
        cl2.getLevels().add(new Class.Level(1, new int[] { 1 }, 0,0,0,0));
        cl2.getLevels().add(new Class.Level(2, new int[] { 5 }, 0,0,0,0));
        cl2.getLevels().add(new Class.Level(3, new int[] { 6, 1 }, 0,0,0,0));
        cl2.getLevels().add(new Class.Level(4, new int[] { 11, 6, 1 }, 0,0,0,0));

        Character c = new Character();
        c.setStrength(24);
        c.setDexterity(18);
        c.addOrSetClass(cl1, null, 1);
        assertEquals("+1", c.getBaseAttackBonusAsString());
        c.addOrSetClass(cl1, null, 3);
        assertEquals("+6/+1", c.getBaseAttackBonusAsString());
        c.addOrSetClass(cl1, null, 4);
        assertEquals("+11/+6/+1", c.getBaseAttackBonusAsString());
        c.addOrSetClass(cl1, null, 1);
        c.addOrSetClass(cl2, null, 1);
        assertEquals("+2", c.getBaseAttackBonusAsString());
        c.addOrSetClass(cl1, null, 2);
        c.addOrSetClass(cl2, null, 1);
        assertEquals("+6/+1", c.getBaseAttackBonusAsString());
        c.addOrSetClass(cl1, null, 3);
        c.addOrSetClass(cl2, null, 1);
        assertEquals("+7/+2", c.getBaseAttackBonusAsString());
        c.addOrSetClass(cl1, null, 4);
        c.addOrSetClass(cl2, null, 1);
        assertEquals("+12/+7/+2", c.getBaseAttackBonusAsString());
        assertEquals("+19/+14/+9", c.getAttackBonusMeleeAsString(0));
        c.setSizeType(Character.SIZE_SMALL);
        assertEquals("+20/+15/+10", c.getAttackBonusMeleeAsString(0));
        c.setSizeType(Character.SIZE_TINY);
        assertEquals("+21/+16/+11", c.getAttackBonusMeleeAsString(0));
        c.setSizeType(Character.SIZE_MEDIUM);
        c.addOrSetClass(cl1, null, 4);
        c.addOrSetClass(cl2, null, 4);
        assertEquals("+22/+17/+12/+7", c.getBaseAttackBonusAsString());
        assertEquals(22, c.getBaseAttackBonusBest());
        assertEquals("+29/+24/+19/+14", c.getAttackBonusMeleeAsString(0));
        assertEquals("+26/+21/+16/+11", c.getAttackBonusRangeAsString(0));

    }
}
