package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Map;

public class SkillFactory extends DBEntityFactory {

    public static final String FACTORY_ID           = "SKILLS";

    private static final String TABLENAME           = "skills";
    private static final String COLUMN_ABILITY      = "ability";
    private static final String COLUMN_TRAINING     = "training";
    private static final String COLUMN_ARMORPENALTY = "armor";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_ABILITY      = "Caractéristique associée";
    private static final String YAML_TRAINING     = "Formation nécessaire";
    private static final String YAML_ARMORPENALTY = "Malus d’armure";


    private static SkillFactory instance;

    private SkillFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized SkillFactory getInstance() {
        if (instance == null) {
            instance = new SkillFactory();
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
                        "%s text, %s text, %s text," +
                        "%s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE,
                COLUMN_ABILITY, COLUMN_TRAINING, COLUMN_ARMORPENALTY);
        return query;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Skill)) {
            return null;
        }
        Skill skill = (Skill) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(SkillFactory.COLUMN_NAME, skill.getName());
        contentValues.put(SkillFactory.COLUMN_DESC, skill.getDescription());
        contentValues.put(SkillFactory.COLUMN_REFERENCE, skill.getReference());
        contentValues.put(SkillFactory.COLUMN_ABILITY, skill.getAbility());
        contentValues.put(SkillFactory.COLUMN_TRAINING, skill.getTraining());
        contentValues.put(SkillFactory.COLUMN_ARMORPENALTY, skill.getArmorpenalty());
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
        Skill skill = new Skill();

        skill.setId(resource.getLong(resource.getColumnIndex(SpellFactory.COLUMN_ID)));
        skill.setName(extractValue(resource,SkillFactory.COLUMN_NAME));
        skill.setDescription(extractValue(resource,SkillFactory.COLUMN_DESC));
        skill.setReference(extractValue(resource,SkillFactory.COLUMN_REFERENCE));
        skill.setAbility(extractValue(resource,SkillFactory.COLUMN_ABILITY));
        skill.setTraining(extractValue(resource,SkillFactory.COLUMN_TRAINING));
        skill.setArmorpenalty(extractValue(resource,SkillFactory.COLUMN_ARMORPENALTY));
        return skill;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, String> attributes) {
        Skill skill = new Skill();
        skill.setName(attributes.get(YAML_NAME));
        skill.setDescription(attributes.get(YAML_DESC));
        skill.setReference(attributes.get(YAML_REFERENCE));
        skill.setAbility(attributes.get(YAML_ABILITY));
        skill.setTraining(attributes.get(YAML_TRAINING));
        skill.setArmorpenalty(attributes.get(YAML_ARMORPENALTY));
        return skill.isValid() ? skill : null;
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
        if(!(entity instanceof Skill)) {
            return "";
        }
        Skill skill = (Skill)entity;
        StringBuffer buf = new StringBuffer();
        buf.append(generateItemDetail(templateItem, YAML_ABILITY, skill.getAbility()));
        buf.append(generateItemDetail(templateItem, YAML_TRAINING, skill.getTraining()));
        buf.append(generateItemDetail(templateItem, YAML_ARMORPENALTY, skill.getArmorpenalty()));
        return String.format(templateList,buf.toString());
    }
}
