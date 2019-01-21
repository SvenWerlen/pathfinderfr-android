package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
                        "%s text, %s text," +
                        "%s integer, %s integer, %s integer, " +
                        "%s integer, %s integer, %s integer" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_RACE, COLUMN_CLASSES,
                COLUMN_ABILITY_STR, COLUMN_ABILITY_DEX, COLUMN_ABILITY_CON,
                COLUMN_ABILITY_INT, COLUMN_ABILITY_WIS, COLUMN_ABILITY_CHA);
        return query;
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Character)) {
            return null;
        }
        Character c = (Character) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CharacterFactory.COLUMN_NAME, c.getName());
        contentValues.put(CharacterFactory.COLUMN_DESC, c.getDescription());
        contentValues.put(CharacterFactory.COLUMN_REFERENCE, c.getReference()); // there is no reference
        contentValues.put(CharacterFactory.COLUMN_SOURCE, c.getSource());       // there is no source

        contentValues.put(CharacterFactory.COLUMN_ABILITY_STR, c.getStrength());
        contentValues.put(CharacterFactory.COLUMN_ABILITY_DEX, c.getDexterity());
        contentValues.put(CharacterFactory.COLUMN_ABILITY_CON, c.getConstitution());
        contentValues.put(CharacterFactory.COLUMN_ABILITY_INT, c.getIntelligence());
        contentValues.put(CharacterFactory.COLUMN_ABILITY_WIS, c.getWisdom());
        contentValues.put(CharacterFactory.COLUMN_ABILITY_CHA, c.getCharisma());

        // race is stored using format <raceId>:<raceName>
        // (class names must be kept for to be able to migrate data if IDs changes during import)
        if(c.getRace() != null) {
            String value = c.getRace().getId() + ":" + c.getRace().getName();
            contentValues.put(CharacterFactory.COLUMN_RACE, value);
            Log.d(CharacterFactory.class.getSimpleName(), "Race: " + value);
        }

        // classes are stored using format <class1Id>:<class1Name>:<class1Level>#<class2Id>:<class2Name>:<class2Level>
        // (class names must be kept for to be able to migrate data if IDs changes during import)
        if(c.getClassesCount() > 0) {
            StringBuffer value = new StringBuffer();
            for(int i=0; i<c.getClassesCount(); i++) {
                value.append(c.getClass(i).first.id).append(':');
                value.append(c.getClass(i).first.name).append(':');
                value.append(c.getClass(i).second);
                if(i+1 != c.getClassesCount()) {
                    value.append('#');
                }
            }
            Log.d(CharacterFactory.class.getSimpleName(), "Classes: " + value.toString());
            contentValues.put(CharacterFactory.COLUMN_CLASSES, value.toString());
        }

        return contentValues;
    }

    private static String extractValue(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getString(resource.getColumnIndex(columnName));
        } else {
            return null;
        }
    }

    private static int extractValueInt(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getInt(resource.getColumnIndex(columnName));
        } else {
            return -1;
        }
    }

    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Character c = new Character();

        c.setId(resource.getLong(resource.getColumnIndex(CharacterFactory.COLUMN_ID)));
        c.setName(extractValue(resource, CharacterFactory.COLUMN_NAME));
        c.setDescription(extractValue(resource, CharacterFactory.COLUMN_DESC));
        c.setReference(extractValue(resource, CharacterFactory.COLUMN_REFERENCE));
        c.setSource(extractValue(resource, CharacterFactory.COLUMN_SOURCE));

        c.setStrength(extractValueInt(resource, CharacterFactory.COLUMN_ABILITY_STR));
        c.setDexterity(extractValueInt(resource, CharacterFactory.COLUMN_ABILITY_DEX));
        c.setConstitution(extractValueInt(resource, CharacterFactory.COLUMN_ABILITY_CON));
        c.setIntelligence(extractValueInt(resource, CharacterFactory.COLUMN_ABILITY_INT));
        c.setWisdom(extractValueInt(resource, CharacterFactory.COLUMN_ABILITY_WIS));
        c.setCharisma(extractValueInt(resource, CharacterFactory.COLUMN_ABILITY_CHA));

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
                if(clDetails != null && clDetails.length == 3) {
                    try {
                        long classId = Long.parseLong(clDetails[0]);
                        String className = clDetails[1];
                        int level = Integer.parseInt(clDetails[2]);
                        Class clEntity = (Class) DBHelper.getInstance(null).fetchEntity(classId, ClassFactory.getInstance());
                        // class found
                        if(clEntity != null && clEntity.getName().equals(className)) {
                            c.addOrSetClass(clEntity, level);
                        }
                        // class not found => search by name
                        else {
                            Log.w(CharacterFactory.class.getSimpleName(), "Couldn't find class by id: " + cl);
                            clEntity = (Class) DBHelper.getInstance(null).fetchEntityByName(className, ClassFactory.getInstance());
                            if(clEntity != null) {
                                c.addOrSetClass(clEntity, level);
                            }
                        }
                    } catch(NumberFormatException nfe) {
                        Log.e(CharacterFactory.class.getSimpleName(), "Stored class '" + cl + "' is invalid (NFE)!");
                    }
                }
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

