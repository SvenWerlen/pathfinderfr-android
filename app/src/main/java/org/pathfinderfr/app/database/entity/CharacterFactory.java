package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharacterFactory extends DBEntityFactory {

    public static final String FACTORY_ID          = "CHARACTERS";

    private static final String TABLENAME          = "characters";
    private static final String COLUMN_RACE        = "race";
    private static final String COLUMN_CLASSES     = "classes";
    private static final String COLUMN_ABILITY_STR = "ab_str";
    private static final String COLUMN_ABILITY_DEX = "ab_dex";
    private static final String COLUMN_ABILITY_CON = "ab_con";
    private static final String COLUMN_ABILITY_INT = "ab_int";
    private static final String COLUMN_ABILITY_WIS = "ab_wis";
    private static final String COLUMN_ABILITY_CHA = "ab_cha";
    private static final String COLUMN_SKILLS      = "skills";
    private static final String COLUMN_FEATS       = "feats";
    private static final String COLUMN_CLFEATURES  = "clfeatures";
    private static final String COLUMN_ALTTRAITS   = "alttraits";
    private static final String COLUMN_MODIFS      = "modifs";
    private static final String COLUMN_HITPOINTS   = "hitpoints";
    private static final String COLUMN_HPTEMP      = "hitpointstemp";
    private static final String COLUMN_SPEED       = "speed";
    private static final String COLUMN_SPEED_ARMOR = "speedarmor";
    private static final String COLUMN_SPEED_DIG   = "speeddig";
    private static final String COLUMN_SPEED_FLY   = "speedfly";
    private static final String COLUMN_SPEED_FLYM  = "speedflym";
    private static final String COLUMN_INVENTORY   = "inventory";
    private static final String COLUMN_PLAYER      = "player";
    private static final String COLUMN_ALIGNMENT   = "align";
    private static final String COLUMN_DIVINITY    = "divinity";
    private static final String COLUMN_ORIGIN      = "origin";
    private static final String COLUMN_SIZETYPE    = "sizetype";
    private static final String COLUMN_SEX         = "sex";
    private static final String COLUMN_AGE         = "age";
    private static final String COLUMN_HEIGHT      = "height";
    private static final String COLUMN_WEIGHT      = "weight";
    private static final String COLUMN_HAIR        = "hair";
    private static final String COLUMN_EYES        = "eyes";
    private static final String COLUMN_LANG        = "languages";
    private static final String COLUMN_CP          = "cp";  // money (cupper)
    private static final String COLUMN_SP          = "sp";  // money (silver)
    private static final String COLUMN_GP          = "gp";  // money (gold)
    private static final String COLUMN_PP          = "pp";  // money (platin)
    private static final String COLUMN_XP          = "xp";  // experience
    private static final String COLUMN_SPELLS      = "spells";

    public static final Integer FLAG_ALL = 1;
    public static final Integer FLAG_SKILLS = 2;
    public static final Integer FLAG_SPELLS = 3;


    private static CharacterFactory instance;

    private CharacterFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized CharacterFactory getInstance() {
        if (instance == null) {
            instance = new CharacterFactory();
        }
        return instance;
    }

    @Override
    public String getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public String getTableName() {
        return TABLENAME;
    }

    @Override
    public String getQueryCreateTable() {
        String query = String.format( "CREATE TABLE IF NOT EXISTS %s (" +
                        "%s integer PRIMARY key, " +
                        "%s text, %s text, %s text, %s text," +
                        "%s text, %s integer, %s text, %s text," +                      // player, alignment, divinity, origin
                        "%s integer, %s integer, %s integer, %s integer, %s integer," + // sizeType, sex, age, height, weight
                        "%s text, %s text, " +                                          // hair, eyes
                        "%s text, %s text, " +                                          // race, classes
                        "%s integer, %s integer, %s integer, " +                        // str, dex, con
                        "%s integer, %s integer, %s integer," +                         // int, wis, cha
                        "%s text, %s text, %s text, %s text, %s text, %s text," +       // skills, feats, features, traits, modifs, inventory
                        "%s integer, %s integer, " +                                    // hitpoints, hitpointstemps
                        "%s integer, %s integer, %s integer, %s integer, %s integer," + // speed (reg, armor, dig, fly, maneuver)
                        "%s integer, %s integer, %s integer, %s integer," +             // money (cp, sp, gp, pp)
                        "%s text, %s integer, %s text" +                                // languages, xp, spells
                        ")",
                TABLENAME,
                COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_PLAYER, COLUMN_ALIGNMENT, COLUMN_DIVINITY, COLUMN_ORIGIN,
                COLUMN_SIZETYPE, COLUMN_SEX, COLUMN_AGE, COLUMN_HEIGHT, COLUMN_WEIGHT,
                COLUMN_HAIR, COLUMN_EYES,
                COLUMN_RACE, COLUMN_CLASSES,
                COLUMN_ABILITY_STR, COLUMN_ABILITY_DEX, COLUMN_ABILITY_CON,
                COLUMN_ABILITY_INT, COLUMN_ABILITY_WIS, COLUMN_ABILITY_CHA,
                COLUMN_SKILLS, COLUMN_FEATS, COLUMN_CLFEATURES, COLUMN_ALTTRAITS, COLUMN_MODIFS, COLUMN_INVENTORY,
                COLUMN_HITPOINTS, COLUMN_HPTEMP,
                COLUMN_SPEED, COLUMN_SPEED_ARMOR, COLUMN_SPEED_DIG, COLUMN_SPEED_FLY, COLUMN_SPEED_FLYM,
                COLUMN_CP, COLUMN_SP, COLUMN_GP, COLUMN_PP,
                COLUMN_LANG, COLUMN_XP, COLUMN_SPELLS);
        return query;
    }

    /**
     * @return the query to fetch all entities (including fields required for nameLong display)
     */
    @Override
    public String getQueryFetchAll(String... sources) {
        return String.format("SELECT %s,%s,%s,%s FROM %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_RACE, COLUMN_CLASSES, getTableName(), COLUMN_NAME);
    }

    /**
     * @return SQL statement for upgrading DB from v3 to v4
     */
    public String getQueryUpgradeV4() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_MODIFS);
    }

    /**
     * @return SQL statement for upgrading DB from v4 to v5
     */
    public String getQueryUpgradeV5() {
        return String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_HITPOINTS);
    }

    /**
     * @return SQL statement for upgrading DB from v5 to v6
     */
    public String getQueryUpgradeV6() {
        return String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SPEED);
    }

    /**
     * @return SQL statement for upgrading DB from v5 to v6
     */
    public String getQueryUpgradeV9() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_CLFEATURES);
    }

    /**
     * @return SQL statement for upgrading DB from v11 to v12
     */
    public String getQueryUpgradeV12() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_INVENTORY);
    }

    /**
     * @return SQL statement for upgrading DB from v14 to v15
     */
    public String getQueryUpgradeV15() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_ALTTRAITS);
    }

    /**
     * @return SQL statement for upgrading DB from v17 to v18
     */
    public List<String> getQueriesUpgradeV18() {
        List<String> changes = new ArrayList<>();
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_HPTEMP));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_PLAYER));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_ALIGNMENT));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_DIVINITY));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_ORIGIN));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SIZETYPE));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SEX));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_AGE));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_HEIGHT));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_WEIGHT));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_HAIR));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_EYES));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SPEED_ARMOR));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SPEED_DIG));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SPEED_FLY));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SPEED_FLYM));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_LANG));
        return changes;
    }

    /**
     * @return SQL statement for upgrading DB from v18 to v19
     */
    public List<String> getQueriesUpgradeV19() {
        List<String> changes = new ArrayList<>();
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_CP));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_SP));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_GP));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_PP));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_XP));
        changes.add(String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_SPELLS));
        return changes;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        Set<Integer> flags = new HashSet<Integer>();
        flags.add(FLAG_ALL);
        return generateContentValuesFromEntity(entity, flags);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity, Set<Integer> flags) {

        if (!(entity instanceof Character)) {
            return null;
        }
        Character c = (Character) entity;
        ContentValues contentValues = new ContentValues();

        if(flags.contains(FLAG_ALL)) {
            contentValues.put(CharacterFactory.COLUMN_NAME, c.getName());
            contentValues.put(CharacterFactory.COLUMN_DESC, c.getDescription());
            contentValues.put(CharacterFactory.COLUMN_REFERENCE, c.getReference()); // there is no reference
            contentValues.put(CharacterFactory.COLUMN_SOURCE, c.getSource());       // there is no source

            contentValues.put(CharacterFactory.COLUMN_ABILITY_STR, c.getAbilityValue(Character.ABILITY_STRENGH, false));
            contentValues.put(CharacterFactory.COLUMN_ABILITY_DEX, c.getAbilityValue(Character.ABILITY_DEXTERITY, false));
            contentValues.put(CharacterFactory.COLUMN_ABILITY_CON, c.getAbilityValue(Character.ABILITY_CONSTITUTION, false));
            contentValues.put(CharacterFactory.COLUMN_ABILITY_INT, c.getAbilityValue(Character.ABILITY_INTELLIGENCE, false));
            contentValues.put(CharacterFactory.COLUMN_ABILITY_WIS, c.getAbilityValue(Character.ABILITY_WISDOM, false));
            contentValues.put(CharacterFactory.COLUMN_ABILITY_CHA, c.getAbilityValue(Character.ABILITY_CHARISMA, false));

            contentValues.put(CharacterFactory.COLUMN_HITPOINTS, c.getHitpoints());
            contentValues.put(CharacterFactory.COLUMN_HPTEMP, c.getHitpointsTemp());
            contentValues.put(CharacterFactory.COLUMN_SPEED, c.getSpeed());

            // race is stored using format <raceId>:<raceName>
            // (class names must be kept for to be able to migrate data if IDs changes during import)
            if (c.getRace() != null) {
                String value = c.getRace().getId() + ":" + c.getRace().getName();
                contentValues.put(CharacterFactory.COLUMN_RACE, value);
                Log.d(CharacterFactory.class.getSimpleName(), "Race: " + value);
            }

            // classes are stored using format <class1Id>:<class1Name>:<class1Level>#<class2Id>:<class2Name>:<class2Level>
            // (class names must be kept for to be able to migrate data if IDs changes during import)
            if (c.getClassesCount() > 0) {
                StringBuffer value = new StringBuffer();
                for (int i = 0; i < c.getClassesCount(); i++) {
                    Triplet<Class, ClassArchetype, Integer> cl = c.getClass(i);
                    value.append(cl.first.id).append(':');
                    value.append(cl.first.name).append(':');
                    value.append(cl.third).append(':');
                    if (cl.second != null) {
                        value.append(c.getClass(i).second.id);
                    }
                    if (i + 1 != c.getClassesCount()) {
                        value.append('#');
                    }
                }
                Log.d(CharacterFactory.class.getSimpleName(), "Classes: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_CLASSES, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_CLASSES, "");
            }
        }

        if(flags.contains(FLAG_ALL) || flags.contains(FLAG_SKILLS)) {
            // skills are stored using format <skill1Id>:<ranks>:<isclassskill>#<skill2Id>:<ranks>:<isclassskill>#...
            // (assuming that skill ids won't change during data import)
            if (c.getSkills().size() > 0) {
                StringBuffer value = new StringBuffer();
                for (Long skillId : c.getSkills()) {
                    Skill skill = (Skill)DBHelper.getInstance(null).fetchEntity(skillId, SkillFactory.getInstance());
                    if(skill != null) {
                        value.append(skillId).append(':').append(c.getSkillRank(skillId));
                        if (!c.isClassSkillByDefault(skill) && c.isClassSkill(skill)) {
                            value.append(':').append(Boolean.TRUE);
                        }
                        value.append('#');
                    }
                }
                value.deleteCharAt(value.length() - 1);
                Log.d(CharacterFactory.class.getSimpleName(), "Skills: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_SKILLS, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_SKILLS, "");
            }
        }

        if(flags.contains(FLAG_ALL)) {
            // feats are stored using format <feat1Id>#<feat2Id>...
            // (assuming that feat ids won't change during data import)
            if (c.getFeats().size() > 0) {
                StringBuffer value = new StringBuffer();
                for (Feat feat : c.getFeats()) {
                    value.append(feat.getId()).append('#');
                }
                value.deleteCharAt(value.length() - 1);
                Log.d(CharacterFactory.class.getSimpleName(), "Feats: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_FEATS, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_FEATS, "");
            }

            // class features are stored using format <feat1Id>#<feat2Id>...
            // (assuming that class features ids won't change during data import)
            if (c.getClassFeatures().size() > 0) {
                StringBuffer value = new StringBuffer();
                for (ClassFeature feat : c.getClassFeatures()) {
                    value.append(feat.getId()).append('#');
                }
                if (value.length() > 0) {
                    value.deleteCharAt(value.length() - 1);
                }
                Log.d(CharacterFactory.class.getSimpleName(), "Class features: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_CLFEATURES, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_CLFEATURES, "");
            }

            // race alternate traits are stored using format <trait1Id>#<trait2Id>...
            // (assuming that traits ids won't change during data import)
            if (c.getTraits().size() > 0) {
                StringBuffer value = new StringBuffer();
                for (Trait trait : c.getTraits()) {
                    value.append(trait.getId()).append('#');
                }
                if (value.length() > 0) {
                    value.deleteCharAt(value.length() - 1);
                }
                Log.d(CharacterFactory.class.getSimpleName(), "Race alt. traits: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_ALTTRAITS, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_ALTTRAITS, "");
            }

            // modifs are stored using format  <modif1Source>:<modif1Bonuses>:<modif1Icon>:<modif1LinkTo>#<modif2Source>:<modif2Bonuses>:<modif2Icon>:<modif2LinkTo>
            // where modif1Bonuses are stored using format <bonus1Id>|<bonus1Value,<bonus2Id>|<bonus2Value>
            // (assuming that modif ids won't change during data import)
            if (c.getModifsCount() > 0) {
                StringBuffer value = new StringBuffer();
                for (Character.CharacterModif modif : c.getModifs()) {
                    value.append(modif.getSource()).append(':');
                    for (int i = 0; i < modif.getModifCount(); i++) {
                        value.append(modif.getModif(i).first).append('|');
                        value.append(modif.getModif(i).second).append(',');
                    }
                    value.deleteCharAt(value.length() - 1).append(':');
                    value.append(modif.getIcon()).append(':');
                    value.append(modif.getLinkToWeapon());
                    value.append('#');
                }
                value.deleteCharAt(value.length() - 1);
                Log.d(CharacterFactory.class.getSimpleName(), "Modifs: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_MODIFS, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_MODIFS, "");
            }

            // inventory are stored using format  <inventory1>|<inventory1-weight>#<inventory2>|<inventory2-weight>#...
            List<Character.InventoryItem> inventory = c.getInventoryItems();
            if (inventory.size() > 0) {
                StringBuffer value = new StringBuffer();
                for (Character.InventoryItem item : inventory) {
                    value.append(item.getName()).append('|');
                    value.append(item.getWeight()).append('|');
                    value.append(item.getObjectId()).append('|');
                    value.append(item.getInfos() != null ? item.getInfos() : "").append('|');
                    value.append(item.getPrice());
                    value.append('#');
                }
                value.deleteCharAt(value.length() - 1);
                Log.d(CharacterFactory.class.getSimpleName(), "Inventory: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_INVENTORY, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_INVENTORY, "");
            }

            // new 16 fields for PDF
            contentValues.put(CharacterFactory.COLUMN_SPEED_ARMOR, c.getSpeedWithArmor());
            contentValues.put(CharacterFactory.COLUMN_SPEED_DIG, c.getBaseSpeedDig());
            contentValues.put(CharacterFactory.COLUMN_SPEED_FLY, c.getBaseSpeedFly());
            contentValues.put(CharacterFactory.COLUMN_SPEED_FLYM, c.getBaseSpeedManeuverability());
            contentValues.put(CharacterFactory.COLUMN_PLAYER, c.getPlayer());
            contentValues.put(CharacterFactory.COLUMN_ALIGNMENT, c.getAlignment());
            contentValues.put(CharacterFactory.COLUMN_DIVINITY, c.getDivinity());
            contentValues.put(CharacterFactory.COLUMN_ORIGIN, c.getOrigin());
            contentValues.put(CharacterFactory.COLUMN_SIZETYPE, c.getSizeType());
            contentValues.put(CharacterFactory.COLUMN_SEX, c.getSex());
            contentValues.put(CharacterFactory.COLUMN_AGE, c.getAge());
            contentValues.put(CharacterFactory.COLUMN_HEIGHT, c.getHeight());
            contentValues.put(CharacterFactory.COLUMN_WEIGHT, c.getWeight());
            contentValues.put(CharacterFactory.COLUMN_HAIR, c.getHair());
            contentValues.put(CharacterFactory.COLUMN_EYES, c.getEyes());
            contentValues.put(CharacterFactory.COLUMN_LANG, c.getLanguages());
            // new fields for PDF
            contentValues.put(CharacterFactory.COLUMN_CP, c.getMoneyCP());
            contentValues.put(CharacterFactory.COLUMN_SP, c.getMoneySP());
            contentValues.put(CharacterFactory.COLUMN_GP, c.getMoneyGP());
            contentValues.put(CharacterFactory.COLUMN_PP, c.getMoneyPP());
            contentValues.put(CharacterFactory.COLUMN_XP, c.getExperience());
        }

        if(flags.contains(FLAG_ALL) || flags.contains(FLAG_SPELLS)) {
            // spells are stored using format <spell1Id>#<spell2Id>...
            // (assuming that spell ids won't change during data import)
            if (c.getSpells().size() > 0) {
                StringBuffer value = new StringBuffer();
                for (Spell spell : c.getSpells()) {
                    value.append(spell.getId()).append('#');
                }
                value.deleteCharAt(value.length() - 1);
                Log.d(CharacterFactory.class.getSimpleName(), "Spells: " + value.toString());
                contentValues.put(CharacterFactory.COLUMN_SPELLS, value.toString());
            } else {
                contentValues.put(CharacterFactory.COLUMN_SPELLS, "");
            }
        }

        return contentValues;
    }

    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Character c = new Character();

        c.setId(resource.getLong(resource.getColumnIndex(CharacterFactory.COLUMN_ID)));
        c.setName(extractValue(resource, CharacterFactory.COLUMN_NAME));
        c.setDescription(extractValue(resource, CharacterFactory.COLUMN_DESC));
        c.setReference(extractValue(resource, CharacterFactory.COLUMN_REFERENCE));
        c.setSource(extractValue(resource, CharacterFactory.COLUMN_SOURCE));

        c.setStrength(extractValueAsInt(resource, CharacterFactory.COLUMN_ABILITY_STR));
        c.setDexterity(extractValueAsInt(resource, CharacterFactory.COLUMN_ABILITY_DEX));
        c.setConstitution(extractValueAsInt(resource, CharacterFactory.COLUMN_ABILITY_CON));
        c.setIntelligence(extractValueAsInt(resource, CharacterFactory.COLUMN_ABILITY_INT));
        c.setWisdom(extractValueAsInt(resource, CharacterFactory.COLUMN_ABILITY_WIS));
        c.setCharisma(extractValueAsInt(resource, CharacterFactory.COLUMN_ABILITY_CHA));

        c.setHitpoints(extractValueAsInt(resource, CharacterFactory.COLUMN_HITPOINTS));
        c.setHitpointsTemp(extractValueAsInt(resource, CharacterFactory.COLUMN_HPTEMP));
        c.setSpeed(extractValueAsInt(resource, CharacterFactory.COLUMN_SPEED));

        // fill race
        String raceValue = extractValue(resource, CharacterFactory.COLUMN_RACE);
        if(raceValue != null && raceValue.length() > 0) {
            String[] race = raceValue.split(":");
            if(race.length == 2) {
                try {
                    long raceId = Long.valueOf(race[0]);
                    String raceName = race[1];
                    Race raceEntity = (Race) DBHelper.getInstance(null).fetchEntity(raceId, RaceFactory.getInstance());
                    // race found
                    if(raceEntity != null && raceEntity.getName().equals(raceName)) {
                        c.setRace(raceEntity);
                    }
                    // race not found => search by name
                    else {
                        Log.w(CharacterFactory.class.getSimpleName(), "Couldn't find race by id: " + race);
                        raceEntity = (Race) DBHelper.getInstance(null).fetchEntityByName(raceName, RaceFactory.getInstance());
                        if(raceEntity != null) {
                            c.setRace(raceEntity);
                        }
                    }

                } catch(NumberFormatException nfe) {
                    Log.e(CharacterFactory.class.getSimpleName(), "Stored raceId '" + race[0] + "' is invalid!");
                }
            }
        }

        // fill classes
        String classesValue = extractValue(resource, CharacterFactory.COLUMN_CLASSES);
        Log.d(CharacterFactory.class.getSimpleName(), "Classes found: " + classesValue);
        if(classesValue != null && classesValue.length() > 0) {
            String[] classes = classesValue.split("#");
            for(String cl : classes) {
                String[] clDetails = cl.split(":");
                if(clDetails != null && clDetails.length >= 3) {
                    try {
                        long classId = Long.parseLong(clDetails[0]);
                        String className = clDetails[1];
                        int level = Integer.parseInt(clDetails[2]);
                        long archetypeId = 0;
                        String archetypeName = null;
                        // archetype was added later
                        if(clDetails.length==4) {
                            archetypeId = Long.parseLong(clDetails[3]);
                        }

                        Class clEntity = (Class) DBHelper.getInstance(null).fetchEntity(classId, ClassFactory.getInstance());
                        ClassArchetype archEntity = archetypeId == 0 ? null : (ClassArchetype) DBHelper.getInstance(null).fetchEntity(archetypeId, ClassArchetypesFactory.getInstance());

                        // class not found => search by name
                        if(clEntity == null) {
                            Log.w(CharacterFactory.class.getSimpleName(), "Couldn't find class by id: " + cl);
                            clEntity = (Class) DBHelper.getInstance(null).fetchEntityByName(className, ClassFactory.getInstance());
                        }

                        if(clEntity != null) {
                            c.addOrSetClass(clEntity, archEntity, level);
                        }

                    } catch(NumberFormatException nfe) {
                        Log.e(CharacterFactory.class.getSimpleName(), "Stored class '" + cl + "' is invalid (NFE)!");
                    }
                }
            }
        }

        // fill skills
        String skillsValue = extractValue(resource, CharacterFactory.COLUMN_SKILLS);
        Log.d(CharacterFactory.class.getSimpleName(), "Skills found: " + skillsValue);
        if(skillsValue != null && skillsValue.length() > 0) {
            String[] skills = skillsValue.split("#");
            for(String skill : skills) {
                String[] skillDetails = skill.split(":");
                if (skillDetails.length >= 2) {
                    try {
                        long skillId = Long.parseLong(skillDetails[0]);
                        int ranks = Integer.parseInt(skillDetails[1]);
                        c.setSkillRank(skillId, ranks);
                        if(skillDetails.length >= 3) {
                            DBEntity entity = DBHelper.getInstance(null).fetchEntity(skillId, SkillFactory.getInstance());
                            if(entity != null) {
                                c.setClassSkill((Skill)entity, Boolean.valueOf(skillDetails[2]));
                            }
                        }
                    } catch (NumberFormatException nfe) {
                        Log.e(CharacterFactory.class.getSimpleName(), "Stored class '" + skill + "' is invalid (NFE)!");
                    }
                }
            }
        }

        // fill feats
        String featsValue = extractValue(resource, CharacterFactory.COLUMN_FEATS);
        Log.d(CharacterFactory.class.getSimpleName(), "Feats found: " + featsValue);
        if(featsValue != null && featsValue.length() > 0) {
            String[] feats = featsValue.split("#");
            long[] featIds = new long[feats.length];
            try {
                for(int i = 0; i < feats.length; i++) {
                    featIds[i] = Long.parseLong(feats[i]);
                }
                // retrieve all feats from DB
                List<DBEntity> list = DBHelper.getInstance(null).fetchAllEntitiesById(featIds, FeatFactory.getInstance());
                for(DBEntity e : list) {
                    c.addFeat((Feat)e);
                }
            } catch (NumberFormatException nfe) {
                Log.e(CharacterFactory.class.getSimpleName(), "Stored feat '" + featsValue + "' is invalid (NFE)!");
            }
        }

        // fill class features
        String featuresValue = extractValue(resource, CharacterFactory.COLUMN_CLFEATURES);
        Log.d(CharacterFactory.class.getSimpleName(), "Class features found: " + featuresValue);
        if(featuresValue != null && featuresValue.length() > 0) {
            String[] feats = featuresValue.split("#");
            long[] featIds = new long[feats.length];
            try {
                for(int i = 0; i < feats.length; i++) {
                    featIds[i] = Long.parseLong(feats[i]);
                }
                // retrieve all class features from DB
                List<DBEntity> list = DBHelper.getInstance(null).fetchAllEntitiesById(featIds, ClassFeatureFactory.getInstance());
                for(DBEntity e : list) {
                    c.addClassFeature((ClassFeature) e);
                }
            } catch (NumberFormatException nfe) {
                Log.e(CharacterFactory.class.getSimpleName(), "Stored class feature '" + featsValue + "' is invalid (NFE)!");
            }
        }

        // fill race alternate traits
        String traitsValue = extractValue(resource, CharacterFactory.COLUMN_ALTTRAITS);
        Log.d(CharacterFactory.class.getSimpleName(), "Race alt. traits found: " + traitsValue);
        if(traitsValue != null && traitsValue.length() > 0) {
            String[] traits = traitsValue.split("#");
            long[] traitIds = new long[traits.length];
            try {
                for(int i = 0; i < traits.length; i++) {
                    traitIds[i] = Long.parseLong(traits[i]);
                }
                // retrieve all traits from DB
                List<DBEntity> list = DBHelper.getInstance(null).fetchAllEntitiesById(traitIds, TraitFactory.getInstance());
                for(DBEntity e : list) {
                    c.addTrait((Trait)e);
                }
            } catch (NumberFormatException nfe) {
                Log.e(CharacterFactory.class.getSimpleName(), "Stored trait '" + traitsValue + "' is invalid (NFE)!");
            }
        }

        // inventory items are stored using format  using format  <inventory1>|<inventory1-weight>|<inventory1-objectId>|<inventory1-infos>#<inventory2>|<inventory2-weight>...
        String inventoryValue = extractValue(resource, CharacterFactory.COLUMN_INVENTORY);
        Log.d(CharacterFactory.class.getSimpleName(), "Inventory found: " + inventoryValue);
        if(inventoryValue != null && inventoryValue.length() > 0) {
            for(String item : inventoryValue.split("#")) {
                String[] itemElements = item.split("\\|");
                if(itemElements != null && itemElements.length >= 2) {
                    String name = itemElements[0];
                    int weight = 0;
                    try {
                        weight = Integer.parseInt(itemElements[1]);
                    } catch (NumberFormatException nfe) {
                        Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory weight '" + itemElements[1] + "' is invalid (NFE)!");
                    }
                    // item reference was introduced later
                    long objId = 0;
                    if(itemElements.length >= 3) {
                        try {
                            objId = Long.parseLong(itemElements[2]);
                        } catch (NumberFormatException nfe) {
                            Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory référence '" + itemElements[2] + "' is invalid (NFE)!");
                        }
                    }
                    // item additional info (ex: ammo)
                    String infos = null;
                    if(itemElements.length >= 4) {
                        infos = itemElements[3];
                    }
                    // item cost was introduced later
                    int price = 0;
                    if(itemElements.length >= 5) {
                        try {
                            price = Integer.parseInt(itemElements[4]);
                        } catch (NumberFormatException nfe) {
                            Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory price '" + itemElements[4] + "' is invalid (NFE)!");
                        }
                    }

                    Character.InventoryItem toAdd = new Character.InventoryItem(name, weight, price, objId, infos);
                    if (toAdd.isValid()) {
                        c.addInventoryItem(toAdd);
                    }
                }
            }
        }

        // modifs are stored using format  <modif1Source>:<modif1Bonuses>:<modif1Icon>:<modif1Linkto>#<modif2Source>:<modif2Bonuses>:<modif2Icon>:<modif1Linkto>
        // where modif1Bonuses are stored using format <bonus1Id>|<bonus1Value,<bonus2Id>|<bonus2Value>
        // (assuming that modif ids won't change during data import)
        // fill modifs
        String modifsValue = extractValue(resource, CharacterFactory.COLUMN_MODIFS);
        Log.d(CharacterFactory.class.getSimpleName(), "Modifs found: " + modifsValue);
        if(modifsValue != null && modifsValue.length() > 0) {
            for(String modif : modifsValue.split("#")) {
                String[] modElements = modif.split(":");
                if(modElements != null && modElements.length >= 3) {
                    String source = modElements[0];
                    String icon = modElements[2];
                    int linkToWeapon = modElements.length >= 4 ? Integer.parseInt(modElements[3]) : 0;
                    List<Pair<Integer, Integer>> bonuses = new ArrayList<>();
                    for (String bonusVal : modElements[1].split(",")) {
                        String[] bonusElements = bonusVal.split("\\|");
                        if (bonusElements != null && bonusElements.length == 2) {
                            try {
                                Integer bonusIdx = Integer.parseInt(bonusElements[0]);
                                Integer bonusValue = Integer.parseInt(bonusElements[1]);
                                bonuses.add(new Pair<Integer, Integer>(bonusIdx, bonusValue));
                            } catch (NumberFormatException nfe) {
                                Log.e(CharacterFactory.class.getSimpleName(), "Stored modif '" + bonusVal + "' is invalid (NFE)!");
                            }
                        }
                    }
                    Character.CharacterModif toAdd = new Character.CharacterModif(source, bonuses, icon, linkToWeapon);
                    if (toAdd.isValid()) {
                        c.addModif(toAdd);
                    }
                }
            }
        }

        // new 16 fields for PDF
        c.setSpeedWithArmor(extractValueAsInt(resource, CharacterFactory.COLUMN_SPEED_ARMOR));
        c.setSpeedDig(extractValueAsInt(resource, CharacterFactory.COLUMN_SPEED_DIG));
        c.setSpeedFly(extractValueAsInt(resource, CharacterFactory.COLUMN_SPEED_FLY));
        c.setSpeedManeuverability(extractValueAsInt(resource, CharacterFactory.COLUMN_SPEED_FLYM));
        c.setPlayer(extractValue(resource, CharacterFactory.COLUMN_PLAYER));
        c.setAlignment(extractValueAsInt(resource, CharacterFactory.COLUMN_ALIGNMENT));
        c.setDivinity(extractValue(resource, CharacterFactory.COLUMN_DIVINITY));
        c.setOrigin(extractValue(resource, CharacterFactory.COLUMN_ORIGIN));
        c.setSizeType(extractValueAsInt(resource, CharacterFactory.COLUMN_SIZETYPE));
        c.setSex(extractValueAsInt(resource, CharacterFactory.COLUMN_SEX));
        c.setAge(extractValueAsInt(resource, CharacterFactory.COLUMN_AGE));
        c.setHeight(extractValueAsInt(resource, CharacterFactory.COLUMN_HEIGHT));
        c.setWeight(extractValueAsInt(resource, CharacterFactory.COLUMN_WEIGHT));
        c.setHair(extractValue(resource, CharacterFactory.COLUMN_HAIR));
        c.setEyes(extractValue(resource, CharacterFactory.COLUMN_EYES));
        c.setLanguages(extractValue(resource, CharacterFactory.COLUMN_LANG));
        // new fields for PDF
        c.setMoneyCP(extractValueAsInt(resource, CharacterFactory.COLUMN_CP));
        c.setMoneySP(extractValueAsInt(resource, CharacterFactory.COLUMN_SP));
        c.setMoneyGP(extractValueAsInt(resource, CharacterFactory.COLUMN_GP));
        c.setMoneyPP(extractValueAsInt(resource, CharacterFactory.COLUMN_PP));
        c.setExperience(extractValueAsInt(resource, CharacterFactory.COLUMN_XP));

        // fill spells
        String spellsValue = extractValue(resource, CharacterFactory.COLUMN_SPELLS);
        Log.d(CharacterFactory.class.getSimpleName(), "Spells found: " + spellsValue);
        if(spellsValue != null && spellsValue.length() > 0) {
            String[] spells = spellsValue.split("#");
            long[] spellIds = new long[spells.length];
            try {
                for(int i = 0; i < spells.length; i++) {
                    spellIds[i] = Long.parseLong(spells[i]);
                }
                // retrieve all feats from DB
                List<DBEntity> list = DBHelper.getInstance(null).fetchAllEntitiesById(spellIds, SpellFactory.getInstance());
                for(DBEntity e : list) {
                    c.addSpell((Spell)e);
                }
            } catch (NumberFormatException nfe) {
                Log.e(CharacterFactory.class.getSimpleName(), "Stored spell '" + spellsValue + "' is invalid (NFE)!");
            }
        }

        return c;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        throw new UnsupportedOperationException("This class doesn't implement that method!");
    }

    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        throw new UnsupportedOperationException("This class doesn't implement that method!");
    }
}


