package org.pathfinderfr.app.util;

public class CharacterUtil {


    public static int getAbilityBonus(int abilityValue) {
        // make sure ability > 0
        abilityValue = Math.max(1, abilityValue);
        return (int)Math.floor((abilityValue - 10) / 2.0);
    }
}
