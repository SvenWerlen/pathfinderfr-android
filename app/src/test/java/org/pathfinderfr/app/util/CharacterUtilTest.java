package org.pathfinderfr.app.util;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class CharacterUtilTest {

    @Before
    public void init() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void getAbilityBonus() {
        assertEquals(-5, CharacterUtil.getAbilityBonus(-1));
        assertEquals(-5, CharacterUtil.getAbilityBonus(1));
        assertEquals(-4, CharacterUtil.getAbilityBonus(2));
        assertEquals(-4, CharacterUtil.getAbilityBonus(3));
        assertEquals(-3, CharacterUtil.getAbilityBonus(4));
        assertEquals(-3, CharacterUtil.getAbilityBonus(5));
        assertEquals(-2, CharacterUtil.getAbilityBonus(6));
        assertEquals(-2, CharacterUtil.getAbilityBonus(7));
        assertEquals(-1, CharacterUtil.getAbilityBonus(8));
        assertEquals(-1, CharacterUtil.getAbilityBonus(9));
        assertEquals(0, CharacterUtil.getAbilityBonus(10));
        assertEquals(0, CharacterUtil.getAbilityBonus(11));
        assertEquals(1, CharacterUtil.getAbilityBonus(12));
        assertEquals(1, CharacterUtil.getAbilityBonus(13));
        assertEquals(2, CharacterUtil.getAbilityBonus(14));
        assertEquals(2, CharacterUtil.getAbilityBonus(15));
        assertEquals(3, CharacterUtil.getAbilityBonus(16));
        assertEquals(3, CharacterUtil.getAbilityBonus(17));
        assertEquals(4, CharacterUtil.getAbilityBonus(18));
        assertEquals(4, CharacterUtil.getAbilityBonus(19));
    }
}

