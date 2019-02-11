package org.pathfinderfr.app.util;

public class CharacterUtil {


    public static int getAbilityBonus(int abilityValue) {
        // make sure ability > 0
        abilityValue = Math.max(1, abilityValue);
        return (int)Math.floor((abilityValue - 10) / 2.0);
    }

    public static String getAttackBonusAsString(int[] bab) {
        if(bab == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        for(int val : bab) {
            buf.append('+').append(val).append('/');
        }
        if(bab.length>0) {
            buf.deleteCharAt(buf.length()-1);
        }
        return buf.toString();
    }
}
