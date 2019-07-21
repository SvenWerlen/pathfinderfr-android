package org.pathfinderfr.app.treasure;

import android.content.Context;
import android.util.Log;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.SpellUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreasureUtil {

    private static final String PROPS_NAME = "magicitems.properties";

    public static final int TABLE_SOURCE_MJ            = 1;
    public static final int TABLE_SOURCE_MJRA          = 2;

    public static final String TABLE_MAIN              = "root";
    private static final String TABLE_ARMOR_CHOICE      = "Armures et boucliers";
    private static final String TABLE_WEAPON_CHOICE     = "Armes";
    private static final String TABLE_POTION_CHOICE     = "Potions et huiles";
    private static final String TABLE_RING_CHOICE       = "Anneaux";
    private static final String TABLE_PARCH_CHOICE      = "Parchemins";
    private static final String TABLE_WAND_CHOICE       = "Baguettes";
    private static final String TABLE_STAFF_CHOICE      = "Bâtons";
    private static final String TABLE_OBJECT_CHOICE     = "Objets merveilleux";

    private static final String TABLE_ARMOR_MAIN        = "armor";
    private static final String TABLE_ARMOR_PROPS       = "armor.props";
    private static final String TABLE_ARMOR_PROPS_MJRA  = "armor.props.mjra";
    private static final String TABLE_SHIELD_PROPS      = "shield.props";
    private static final String TABLE_ARMOR_SPEC        = "armor.specific";
    private static final String TABLE_ARMOR_SPEC_MJRA   = "armor.specific.mjra";
    private static final String TABLE_SHIELD_SPEC       = "shield.specific";
    private static final String TABLE_SHIELD_SPEC_MJRA  = "shield.specific.mjra";

    private static final String TABLE_ARMOR_KEY_ARMOR   = "Armure spécifique";
    private static final String TABLE_ARMOR_KEY_SHIELD  = "Bouclier spécifique";
    private static final String TABLE_ARMOR_KEY_PROP    = "Propriété spéciale";
    private static final String TABLE_ARMOR_KEY_2PROPS  = "Relancez deux fois le dé";

    private static final String TABLE_WEAPON_MAIN       = "weapon";
    private static final String TABLE_WEAPON_PROPS      = "weapon.props";
    private static final String TABLE_WEAPON_PROPS_MJRA = "weapon.props.mjra";
    private static final String TABLE_RANGED_PROPS      = "ranged.props";
    private static final String TABLE_RANGED_PROPS_MJRA = "ranged.props.mjra";
    private static final String TABLE_WEAPON_SPEC       = "weapon.specific";
    private static final String TABLE_WEAPON_SPEC_MJRA  = "weapon.specific.mjra";

    private static final String TABLE_WEAPON_KEY_WEAPON = "Arme spécifique";
    private static final String TABLE_WEAPON_KEY_PROP   = "Propriété spéciale";
    private static final String TABLE_WEAPON_KEY_2PROPS = "Relancez deux fois le dé";

    private static final String TABLE_POTION_MAIN       = "potion";
    private static final String TABLE_PARCH_MAIN        = "parch";
    private static final String TABLE_WAND_MAIN         = "wand";

    private static final String TABLE_STAFF_MAIN        = "staff";
    private static final String TABLE_STAFF_MAIN_MJRA   = "staff.mjra";

    private static final String TABLE_SPELL_MAIN        = "spell#";

    private static final String TABLE_RING_MAIN         = "ring";
    private static final String TABLE_RING_MAIN_MJRA    = "ring.mjra";

    private static final String TABLE_OBJECT_WEAK       = "object.weak";
    private static final String TABLE_OBJECT_WEAK_MJRA  = "object.weak.mjra";
    private static final String TABLE_OBJECT_INTERM     = "object.interm";
    private static final String TABLE_OBJECT_INTERM_MJRA= "object.interm.mjra";
    private static final String TABLE_OBJECT_POWER      = "object.power";
    private static final String TABLE_OBJECT_POWER_MJRA = "object.power.mjra";


    private static TreasureUtil instance;
    private Properties properties;

    public static synchronized TreasureUtil getInstance(Context context) {
        if(instance == null) {
            instance = new TreasureUtil(context);
        }
        return instance;
    }

    public TreasureUtil(Context context) {
        properties = new Properties();
        try {
            if(context != null) {
                properties.load(context.getAssets().open(PROPS_NAME));
            }
        } catch (IOException e) {
            Log.e(ConfigurationUtil.class.getSimpleName(),
                    String.format("Properties %s couldn't be found!", PROPS_NAME), e);
        }
    }

    /**
     * Reads magicitems.properties (see file) and fills Treasure Table
     *
     * @param tableId identifier of the table
     * @return TreasureTable object
     */
    public TreasureTable generateTable(String tableId) {
        if(tableId == null) {
            tableId = TABLE_MAIN;
        }
        // special case for spells
        if(tableId.startsWith(TreasureUtil.TABLE_SPELL_MAIN)) {
            int lvl = 0;
            try {
                lvl = Integer.parseInt(tableId.substring(TreasureUtil.TABLE_SPELL_MAIN.length()));
                Log.i(TreasureFragment.class.getSimpleName(), String.format("Spells of level %d", lvl));
            } catch(NumberFormatException e) {}
            List<String> spells = TreasureUtil.getSpellChoices(DBHelper.getInstance(null), lvl);
            TreasureTable table = new TreasureTable();
            for(String el : spells) {
                table.getRows().add(TreasureRow.newTreasureRow(el));
            }
            return table;
        }

        int idx = 1;
        TreasureTable table = new TreasureTable();
        while(properties.containsKey("table." + tableId + "." + idx)) {
            table.addRow(properties.get("table." + tableId + "." + idx).toString());
            idx++;
        }
        return table;
    }


    private static boolean entryExists(List<Pair<String,String>> history, String choice) {
        for(Pair<String, String> entry : history) {
            if (entry.second.equals(choice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of choices made in specific table
     */
    private static int countTableEntries(List<Pair<String,String>> history, String tablename) {
        int count = 0;
        for(Pair<String, String> entry : history) {
            if (entry.first.equals(tablename)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns true if give choice was made
     */
    public static String nextTable(int curType, int curSource, List<Pair<String,String>> history, String curTable, String choice) {
        if(history == null) {
            return null;
        }

        // make sure that choice doesn't already exist
        if(entryExists(history, choice)) {
            throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.choice"));
        }

        switch (curTable) {
            case TreasureUtil.TABLE_MAIN:
                if(TABLE_ARMOR_CHOICE.equals(choice)) {
                    return TreasureUtil.TABLE_ARMOR_MAIN;
                } else if(TABLE_WEAPON_CHOICE.equals(choice)) {
                    return TreasureUtil.TABLE_WEAPON_MAIN;
                } else if(TABLE_POTION_CHOICE.equals(choice)) {
                    return TreasureUtil.TABLE_POTION_MAIN;
                } else if(TABLE_RING_CHOICE.equals(choice)) {
                    return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_RING_MAIN : TreasureUtil.TABLE_RING_MAIN_MJRA;
                } else if(TABLE_PARCH_CHOICE.equals(choice)) {
                    return TreasureUtil.TABLE_PARCH_MAIN;
                } else if(TABLE_WAND_CHOICE.equals(choice)) {
                    return TreasureUtil.TABLE_WAND_MAIN;
                } else if(TABLE_STAFF_CHOICE.equals(choice)) {
                    return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_STAFF_MAIN : TreasureUtil.TABLE_STAFF_MAIN_MJRA;
                } else if(TABLE_OBJECT_CHOICE.equals(choice)) {
                    if(curType == TreasureRow.TYPE_INTERMEDIATE) {
                        return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_OBJECT_INTERM : TreasureUtil.TABLE_OBJECT_INTERM_MJRA;
                    } else if(curType == TreasureRow.TYPE_POWERFUL) {
                        return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_OBJECT_POWER : TreasureUtil.TABLE_OBJECT_POWER_MJRA;
                    } else {
                        return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_OBJECT_WEAK : TreasureUtil.TABLE_OBJECT_WEAK_MJRA;
                    }
                } else {
                    return null;
                }

            case TreasureUtil.TABLE_ARMOR_MAIN:
                if(TABLE_ARMOR_KEY_ARMOR.equals(choice)) {
                    // weak is not available for MJRA
                    return curSource == TreasureUtil.TABLE_SOURCE_MJ || curType == TreasureRow.TYPE_WEAK ? TreasureUtil.TABLE_ARMOR_SPEC : TreasureUtil.TABLE_ARMOR_SPEC_MJRA;
                } else if(TABLE_ARMOR_KEY_SHIELD.equals(choice)) {
                    // powerful required for MJRA
                    return curSource == TreasureUtil.TABLE_SOURCE_MJ || curType != TreasureRow.TYPE_POWERFUL ? TreasureUtil.TABLE_SHIELD_SPEC : TreasureUtil.TABLE_SHIELD_SPEC_MJRA;
                } else if(TABLE_ARMOR_KEY_PROP.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.special"));
                    }
                    return TreasureUtil.TABLE_ARMOR_MAIN; // rejouer le dé
                } else {
                    // check if history has special property
                    if(entryExists(history, TABLE_ARMOR_KEY_PROP)) {
                        // armor or shield
                        if(choice.toLowerCase().indexOf("armure") >= 0) {
                            return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_ARMOR_PROPS : TreasureUtil.TABLE_ARMOR_PROPS_MJRA;
                        } else {
                            return TreasureUtil.TABLE_SHIELD_PROPS;
                        }
                    }
                    return null;
                }

            case TreasureUtil.TABLE_ARMOR_SPEC:
            case TreasureUtil.TABLE_ARMOR_SPEC_MJRA:
            case TreasureUtil.TABLE_SHIELD_SPEC:
            case TreasureUtil.TABLE_SHIELD_SPEC_MJRA:
                if(entryExists(history, TABLE_ARMOR_KEY_PROP)) {
                    // armor or shield
                    if(entryExists(history, TABLE_ARMOR_SPEC) || entryExists(history, TABLE_ARMOR_SPEC_MJRA) ) {
                        return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_ARMOR_PROPS : TreasureUtil.TABLE_ARMOR_PROPS_MJRA;
                    } else {
                        return TreasureUtil.TABLE_SHIELD_PROPS;
                    }
                }
                break;

            case TreasureUtil.TABLE_ARMOR_PROPS:
                if(TABLE_ARMOR_KEY_2PROPS.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_ARMOR_PROPS;
                } else if(entryExists(history, TABLE_ARMOR_KEY_2PROPS)) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_ARMOR_PROPS) >= 2 ? null : TreasureUtil.TABLE_ARMOR_PROPS;
                }
                break;

            case TreasureUtil.TABLE_ARMOR_PROPS_MJRA:
                if(TABLE_ARMOR_KEY_2PROPS.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_ARMOR_PROPS_MJRA;
                } else if(entryExists(history, TABLE_ARMOR_KEY_2PROPS)) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_ARMOR_PROPS_MJRA) >= 2 ? null : TreasureUtil.TABLE_ARMOR_PROPS_MJRA;
                }
                break;

            case TreasureUtil.TABLE_SHIELD_PROPS:
                if(TABLE_ARMOR_KEY_2PROPS.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_SHIELD_PROPS;
                } else if(entryExists(history, TABLE_ARMOR_KEY_2PROPS)) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_SHIELD_PROPS) >= 2 ? null : TreasureUtil.TABLE_SHIELD_PROPS;
                }
                break;

            case TreasureUtil.TABLE_WEAPON_MAIN:
                if(TABLE_WEAPON_KEY_WEAPON.equals(choice)) {
                    return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_WEAPON_SPEC : TreasureUtil.TABLE_WEAPON_SPEC_MJRA;
                } else if(TABLE_WEAPON_KEY_PROP.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.special"));
                    }
                    return TreasureUtil.TABLE_WEAPON_MAIN; // rejouer le dé
                } else {
                    // check if history has special property
                    if(entryExists(history, TABLE_WEAPON_KEY_PROP)) {
                        // no easy way to find if weapon is ranged or not => random
                        if((new Random()).nextInt(2) % 2 == 0) {
                            return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_WEAPON_PROPS : TreasureUtil.TABLE_WEAPON_PROPS_MJRA;
                        } else {
                            return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_RANGED_PROPS : TreasureUtil.TABLE_RANGED_PROPS_MJRA;
                        }
                    }
                    return null;
                }

            case TreasureUtil.TABLE_WEAPON_SPEC:
            case TreasureUtil.TABLE_WEAPON_SPEC_MJRA:
                if(entryExists(history, TABLE_WEAPON_KEY_PROP)) {
                    // no easy way to find if weapon is ranged or not => random
                    if((new Random()).nextInt(2) % 2 == 0) {
                        return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_WEAPON_PROPS : TreasureUtil.TABLE_WEAPON_PROPS_MJRA;
                    } else {
                        return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_RANGED_PROPS : TreasureUtil.TABLE_RANGED_PROPS_MJRA;
                    }
                }
                break;

            case TreasureUtil.TABLE_WEAPON_PROPS:
                if(TABLE_WEAPON_KEY_2PROPS.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_WEAPON_PROPS;
                } else if(entryExists(history, TABLE_WEAPON_KEY_2PROPS)) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_WEAPON_PROPS) >= 2 ? null : TreasureUtil.TABLE_WEAPON_PROPS;
                }
                break;

            case TreasureUtil.TABLE_WEAPON_PROPS_MJRA:
                if(TABLE_WEAPON_KEY_2PROPS.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_WEAPON_PROPS_MJRA;
                } else if(entryExists(history, TABLE_WEAPON_KEY_2PROPS)) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_WEAPON_PROPS_MJRA) >= 2 ? null : TreasureUtil.TABLE_WEAPON_PROPS_MJRA;
                }
                break;

            case TreasureUtil.TABLE_RANGED_PROPS:
                if(TABLE_WEAPON_KEY_2PROPS.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_RANGED_PROPS;
                } else if(entryExists(history, TABLE_WEAPON_KEY_2PROPS)) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_RANGED_PROPS) >= 2 ? null : TreasureUtil.TABLE_RANGED_PROPS;
                }
                break;

            case TreasureUtil.TABLE_RANGED_PROPS_MJRA:
                if(TABLE_WEAPON_KEY_2PROPS.equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_RANGED_PROPS_MJRA;
                } else if(entryExists(history, TABLE_WEAPON_KEY_2PROPS)) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_RANGED_PROPS_MJRA) >= 2 ? null : TreasureUtil.TABLE_RANGED_PROPS_MJRA;
                }
                break;

            case TreasureUtil.TABLE_POTION_MAIN:
                Pattern pattern1 = Pattern.compile("Potion (\\d)");
                Matcher matcher1 = pattern1.matcher(choice);
                if (matcher1.find()) {
                    return TABLE_SPELL_MAIN + matcher1.group(1);
                }
                break;

            case TreasureUtil.TABLE_PARCH_MAIN:
                Pattern pattern2 = Pattern.compile("Parchemin (\\d)");
                Matcher matcher2 = pattern2.matcher(choice);
                if (matcher2.find()) {
                    return TABLE_SPELL_MAIN + matcher2.group(1);
                }
                break;

            case TreasureUtil.TABLE_WAND_MAIN:
                Pattern pattern3 = Pattern.compile("Baguette (\\d)");
                Matcher matcher3 = pattern3.matcher(choice);
                if (matcher3.find()) {
                    return TABLE_SPELL_MAIN + matcher3.group(1);
                }
                break;

            default:
                return null;
        }

        return null;
    }

    /**
     * Returns a list of spells matching given level
     */
    public static List<String> getSpellChoices(DBHelper helper, int level) {
        List<String> results = new ArrayList<>();
        List<DBEntity> spells = DBHelper.getInstance(null).getAllEntities(SpellFactory.getInstance());
        for(DBEntity e : spells) {
            List<Pair<String, Integer>> lvls = SpellUtil.cleanClasses(((Spell)e).getLevel());
            for(Pair<String,Integer> lvl: lvls) {
                if(lvl.second == level) {
                    results.add(e.getName());
                    break;
                }
            }
        }
        Collections.sort(results);
        return results;
    }


    public static boolean resultsIsSpell(List<Pair<String,String>> history) {
        for(Pair<String, String> h : history) {
            if(h.first.startsWith(TABLE_SPELL_MAIN)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getResults(List<Pair<String,String>> history) {
        List<String> results = new ArrayList<>();
        if(history.size() < 2) {
            return null;
        }
        final Set<String> IGNORE = new HashSet<>(Arrays.asList(
                TABLE_ARMOR_KEY_ARMOR, TABLE_ARMOR_KEY_SHIELD, TABLE_ARMOR_KEY_PROP, TABLE_ARMOR_KEY_2PROPS,
                TABLE_WEAPON_KEY_WEAPON, TABLE_WEAPON_KEY_PROP, TABLE_ARMOR_KEY_2PROPS
                ));

        for(int i=1; i<history.size(); i++) {
            if(!IGNORE.contains(history.get(i).second)) {
                results.add(history.get(i).second);
            }
        }
        return results;
    }

}
