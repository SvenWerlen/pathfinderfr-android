package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;
import java.util.Properties;

public class SkillFactory extends DBEntityFactory {

    public static final String FACTORY_ID           = "SKILLS";

    private static final String TABLENAME           = "skills";
    private static final String COLUMN_ABILITY      = "ability";
    private static final String COLUMN_TRAINING     = "training";
    private static final String COLUMN_ARMORPENALTY = "armor";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_DESC_HTML    = "DescriptionHTML";
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
                        "%s integer version, " +
                        "%s text, %s text, %s text," +
                        "%s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID, COLUMN_VERSION,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE,
                COLUMN_ABILITY, COLUMN_TRAINING, COLUMN_ARMORPENALTY);
        return query;
    }

    @Override
    public String getQueryFetchAll(Integer version, String... sources) {
        // Skills have no sources
        return super.getQueryFetchAll(version);
    }

    @Override
    public String getQueryFetchAllWithAllFields(Integer version, String... sources) {
        // Skills have no sources
        return super.getQueryFetchAllWithAllFields(version);
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Skill)) {
            return null;
        }
        Skill skill = (Skill) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassArchetypesFactory.COLUMN_VERSION, skill.getVersion());
        contentValues.put(SkillFactory.COLUMN_NAME, skill.getName());
        contentValues.put(SkillFactory.COLUMN_DESC, skill.getDescription());
        contentValues.put(SkillFactory.COLUMN_REFERENCE, skill.getReference());
        contentValues.put(SkillFactory.COLUMN_ABILITY, skill.getAbility());
        contentValues.put(SkillFactory.COLUMN_TRAINING, skill.getTraining());
        contentValues.put(SkillFactory.COLUMN_ARMORPENALTY, skill.getArmorpenalty());
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Skill skill = new Skill();

        skill.setId(resource.getLong(resource.getColumnIndex(SkillFactory.COLUMN_ID)));
        skill.setVersion(extractValueAsInt(resource, SkillFactory.COLUMN_VERSION));
        skill.setName(extractValue(resource,SkillFactory.COLUMN_NAME));
        skill.setDescription(extractValue(resource,SkillFactory.COLUMN_DESC));
        skill.setReference(extractValue(resource,SkillFactory.COLUMN_REFERENCE));
        skill.setAbility(extractValue(resource,SkillFactory.COLUMN_ABILITY));
        skill.setTraining(extractValue(resource,SkillFactory.COLUMN_TRAINING));
        skill.setArmorpenalty(extractValue(resource,SkillFactory.COLUMN_ARMORPENALTY));
        return skill;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Skill skill = new Skill();
        skill.setName((String)attributes.get(YAML_NAME));
        skill.setDescription(extractDescription(attributes, YAML_DESC, YAML_DESC_HTML));
        skill.setReference((String)attributes.get(YAML_REFERENCE));
        skill.setAbility((String)attributes.get(YAML_ABILITY));
        skill.setTraining((String)attributes.get(YAML_TRAINING));
        skill.setArmorpenalty((String)attributes.get(YAML_ARMORPENALTY));
        return skill.isValid() ? skill : null;
    }

    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        return "";
    }

    @Override
    public String generateHTMLContent(@NonNull DBEntity entity) {
        if(!(entity instanceof Skill)) {
            return "";
        }
        Skill skill = (Skill)entity;

        Properties cfg =  ConfigurationUtil.getInstance(null).getProperties();
        String templateItem = cfg.getProperty("template.item.prop");

        StringBuilder buf = new StringBuilder();
        buf.append("<ul class=\"props\">");
        buf.append(generateItemDetail(templateItem, YAML_ABILITY, skill.getAbility()));
        buf.append(generateItemDetail(templateItem, YAML_TRAINING, skill.getTraining()));
        buf.append(generateItemDetail(templateItem, YAML_ARMORPENALTY, skill.getArmorpenalty()));
        buf.append("</ul>");
        buf.append(StringUtil.cleanText(skill.getDescription()));

        return buf.toString();
    }
}
