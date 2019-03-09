package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import org.pathfinderfr.app.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClassFactory extends DBEntityFactory {

    public static final String FACTORY_ID          = "CLASSES";

    private static final String TABLENAME          = "classes";
    private static final String COLUMN_ALIGNMENT   = "alignment";
    private static final String COLUMN_HITDIE      = "hitdie";
    private static final String COLUMN_CLASSSKILLS = "skills";
    private static final String COLUMN_LEVELS      = "levels";

    private static final String YAML_NAME          = "Nom";
    private static final String YAML_DESC          = "Description";
    private static final String YAML_REFERENCE     = "Référence";
    private static final String YAML_SOURCE        = "Source";
    private static final String YAML_ALIGNMENT     = "Alignement";
    private static final String YAML_HITDIE        = "DésDeVie";
    private static final String YAML_CLASSSKILLS   = "CompétencesDeClasse";
    private static final String YAML_CLASSSKILL    = "Compétence";
    private static final String YAML_LEVELS        = "Progression";
    private static final String YAML_LEVEL_LVL     = "Niveau";
    private static final String YAML_LEVEL_BAB     = "BBA";
    private static final String YAML_LEVEL_FORT    = "Vigueur";
    private static final String YAML_LEVEL_REFL    = "Réflexes";
    private static final String YAML_LEVEL_WILL    = "Volonté";
    private static final String YAML_LEVEL_SP_MAX  = "SortMax";

    private static ClassFactory instance;

    private ClassFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized ClassFactory getInstance() {
        if (instance == null) {
            instance = new ClassFactory();
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
                        "%s text, %s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_ALIGNMENT, COLUMN_HITDIE, COLUMN_CLASSSKILLS, COLUMN_LEVELS);
        return query;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Class)) {
            return null;
        }
        Class cl = (Class) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassFactory.COLUMN_NAME, cl.getName());
        contentValues.put(ClassFactory.COLUMN_DESC, cl.getDescription());
        contentValues.put(ClassFactory.COLUMN_REFERENCE, cl.getReference());
        contentValues.put(ClassFactory.COLUMN_SOURCE, cl.getSource());
        contentValues.put(ClassFactory.COLUMN_ALIGNMENT, cl.getAlignment());
        contentValues.put(ClassFactory.COLUMN_HITDIE, cl.getHitDie());
        contentValues.put(ClassFactory.COLUMN_CLASSSKILLS, StringUtil.listToString(cl.getSkills().toArray(new String[0]),':'));

        StringBuffer buf = new StringBuffer();
        for(Class.Level lvl : cl.getLevels()) {
            buf.append(lvl.getId()).append('|');
            buf.append(StringUtil.listToString(lvl.getBaseAttackBonus(), ':')).append('|');
            buf.append(lvl.getFortitudeBonus()).append('|');
            buf.append(lvl.getReflexBonus()).append('|');
            buf.append(lvl.getWillBonus()).append('|');
            buf.append(lvl.getMaxSpellLvl());
            buf.append('#');
        }
        if(buf.length() > 0) {
            buf.deleteCharAt(buf.length()-1);
        }
        contentValues.put(ClassFactory.COLUMN_LEVELS, buf.toString());
        return contentValues;
    }

    private static String extractValue(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getString(resource.getColumnIndex(columnName));
        } else {
            return null;
        }
    }

    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Class cl = new Class();

        cl.setId(resource.getLong(resource.getColumnIndex(ClassFactory.COLUMN_ID)));
        cl.setName(extractValue(resource, ClassFactory.COLUMN_NAME));
        cl.setDescription(extractValue(resource, ClassFactory.COLUMN_DESC));
        cl.setReference(extractValue(resource, ClassFactory.COLUMN_REFERENCE));
        cl.setSource(extractValue(resource, ClassFactory.COLUMN_SOURCE));
        cl.setAlignment(extractValue(resource, ClassFactory.COLUMN_ALIGNMENT));
        cl.setHitDie(extractValue(resource, ClassFactory.COLUMN_HITDIE));
        String skillsValue = extractValue(resource, ClassFactory.COLUMN_CLASSSKILLS);
        if(skillsValue != null && skillsValue.length() > 0) {
            cl.getSkills().addAll(Arrays.asList(skillsValue.split(":")));
        }

        String levelsValue = extractValue(resource, ClassFactory.COLUMN_LEVELS);
        if(levelsValue != null && levelsValue.length() > 0) {
            String[] levels = levelsValue.split("#");
            for(String level : levels) {
                String[] props = level.split("\\|");
                if(props.length < 5) {
                    continue;
                }
                try {
                    int lvl = Integer.parseInt(props[0]);
                    int fort = Integer.parseInt(props[2]);
                    int refl = Integer.parseInt(props[3]);
                    int will = Integer.parseInt(props[4]);
                    int[] bab = StringUtil.stringListToIntList(props[1].split(":"));
                    int maxSpellLvl = 9;
                    // max spell level has been introduced in 2.2.0. Might not be in database
                    if(props.length >= 6) {
                        maxSpellLvl = Integer.parseInt(props[5]);
                    }
                    cl.getLevels().add(new Class.Level(lvl, bab, fort, refl, will, maxSpellLvl));
                } catch(NumberFormatException e) {
                    Log.w(ClassFactory.class.getSimpleName(), "Couldn't parse some numbers: " +level, e);
                }
            }
        }
        return cl;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Class cl = new Class();
        cl.setName((String)attributes.get(YAML_NAME));
        cl.setDescription((String)attributes.get(YAML_DESC));
        cl.setReference((String)attributes.get(YAML_REFERENCE));
        cl.setSource((String)attributes.get(YAML_SOURCE));
        cl.setAlignment((String)attributes.get(YAML_ALIGNMENT));
        cl.setHitDie((String)attributes.get(YAML_HITDIE));
        Object skills = attributes.get(YAML_CLASSSKILLS);
        if(skills instanceof List) {
            List<Object> list = (List<Object>)skills;
            for(Object t : list) {
                Map<String, String> map = (Map<String, String>) t;
                cl.getSkills().add(map.get(YAML_CLASSSKILL));
            }
        }

        Object levels = attributes.get(YAML_LEVELS);
        if(levels instanceof List) {
            List<Object> list = (List<Object>)levels;
            for(Object t : list) {
                Map<String,String> map = (Map<String,String>)t;
                try {
                    int lvl = Integer.parseInt(map.get(YAML_LEVEL_LVL).replaceAll("\\+",""));
                    int fort = Integer.parseInt(map.get(YAML_LEVEL_FORT).replaceAll("\\+",""));
                    int refl = Integer.parseInt(map.get(YAML_LEVEL_REFL).replaceAll("\\+",""));
                    int will = Integer.parseInt(map.get(YAML_LEVEL_WILL).replaceAll("\\+",""));
                    int[] bab = StringUtil.stringListToIntList(map.get(YAML_LEVEL_BAB).replaceAll("\\+","").split("/"));
                    int maxSpellLvl = Integer.parseInt(map.get(YAML_LEVEL_SP_MAX));
                    cl.getLevels().add(new Class.Level(lvl, bab, fort, refl, will, maxSpellLvl));
                } catch(NumberFormatException e) {
                    Log.w(ClassFactory.class.getSimpleName(), "Couldn't parse some numbers: " + map.values(), e);
                }
            }
        }
        return cl.isValid() ? cl : null;
    }

    /**
     * Utility function that returns the template with regex replaced
     * @param template template (@see assets/templates.properties)
     * @param propKey name of the property
     * @param propValue value of the property
     * @return "" if value is null
     */
    private static String generateItemDetail(String template, String propKey, String propValue) {
        if(propValue != null) {
            return String.format(template, propKey, propValue);
        } else {
            return "";
        }
    }

    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Class)) {
            return "";
        }
        Class cl = (Class)entity;
        StringBuffer buf = new StringBuffer();
        String source = cl.getSource() == null ? null : getTranslatedText("source." + cl.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        /*
        for(Class.Level t : cl.getLevels()) {
            buf.append(generateItemDetail(templateItem, t.getName(), t.getDescription()));
        }*/
        return String.format(templateList,buf.toString());
    }
}

