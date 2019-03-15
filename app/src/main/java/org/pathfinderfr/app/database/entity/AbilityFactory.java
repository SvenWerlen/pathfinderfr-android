package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;

public class AbilityFactory extends DBEntityFactory {

    public static final String FACTORY_ID        = "ABILITIES";

    private static final String TABLENAME         = "abilities";
    private static final String COLUMN_CLASS      = "class";
    private static final String COLUMN_CONDITIONS = "conditions";
    private static final String COLUMN_AUTOMATIC  = "auto";
    private static final String COLUMN_LEVEL      = "level";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";
    private static final String YAML_CLASS        = "Classe";
    private static final String YAML_CONDITIONS   = "Conditions";
    private static final String YAML_AUTO         = "Auto";
    private static final String YAML_LEVEL        = "Niveau";

    private static AbilityFactory instance;

    private AbilityFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized AbilityFactory getInstance() {
        if (instance == null) {
            instance = new AbilityFactory();
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
                        "%s text, %s text, %s integer, %s integer" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_CLASS, COLUMN_CONDITIONS, COLUMN_LEVEL, COLUMN_AUTOMATIC);
        return query;
    }

    /**
     * @return the query to fetch all entities (including fields required for filtering)
     */
    @Override
    public String getQueryFetchAll(String... sources) {
        String filters = "";
        if(sources != null && sources.length > 0) {
            String sourceList = StringUtil.listToString(sources, ',', '\'');
            filters = String.format("WHERE %s IN (%s)", COLUMN_SOURCE, sourceList);
        }
        return String.format("SELECT %s,%s,%s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_CLASS, COLUMN_LEVEL, COLUMN_AUTOMATIC, getTableName(), filters, COLUMN_NAME);
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Ability)) {
            return null;
        }
        Ability ability = (Ability) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(AbilityFactory.COLUMN_NAME, ability.getName());
        contentValues.put(AbilityFactory.COLUMN_DESC, ability.getDescription());
        contentValues.put(AbilityFactory.COLUMN_REFERENCE, ability.getReference());
        contentValues.put(AbilityFactory.COLUMN_SOURCE, ability.getSource());
        contentValues.put(AbilityFactory.COLUMN_CLASS, ability.getClass_());
        contentValues.put(AbilityFactory.COLUMN_CONDITIONS, ability.getConditions());
        contentValues.put(AbilityFactory.COLUMN_LEVEL, ability.getLevel());
        contentValues.put(AbilityFactory.COLUMN_AUTOMATIC, ability.isAuto() ? 1 : 0);
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Ability ability = new Ability();

        ability.setId(resource.getLong(resource.getColumnIndex(AbilityFactory.COLUMN_ID)));
        ability.setName(extractValue(resource, AbilityFactory.COLUMN_NAME));
        ability.setDescription(extractValue(resource, AbilityFactory.COLUMN_DESC));
        ability.setReference(extractValue(resource, AbilityFactory.COLUMN_REFERENCE));
        ability.setSource(extractValue(resource, AbilityFactory.COLUMN_SOURCE));
        ability.setClass(extractValue(resource, AbilityFactory.COLUMN_CLASS));
        ability.setConditions(extractValue(resource, AbilityFactory.COLUMN_CONDITIONS));
        ability.setLevel(extractValueAsInt(resource, AbilityFactory.COLUMN_LEVEL));
        ability.setAuto(extractValueAsBoolean(resource, AbilityFactory.COLUMN_AUTOMATIC));
        return ability;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Ability ability = new Ability();
        ability.setName((String)attributes.get((String)YAML_NAME));
        ability.setDescription((String)attributes.get(YAML_DESC));
        ability.setReference((String)attributes.get(YAML_REFERENCE));
        ability.setSource((String)attributes.get(YAML_SOURCE));
        ability.setClass((String)attributes.get(YAML_CLASS));
        ability.setConditions((String)attributes.get(YAML_CONDITIONS));
        ability.setLevel(Integer.parseInt((String)attributes.get(YAML_LEVEL)));
        ability.setAuto("True".equals((String)attributes.get(YAML_AUTO)));
        return ability.isValid() ? ability : null;
    }



    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Ability)) {
            return "";
        }
        Ability ability = (Ability)entity;
        StringBuffer buf = new StringBuffer();
        String source = ability.getSource() == null ? null : getTranslatedText("source." + ability.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_CLASS, ability.getClass_()));
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        buf.append(generateItemDetail(templateItem, YAML_CONDITIONS, ability.getConditions()));
        buf.append(generateItemDetail(templateItem, YAML_LEVEL, String.valueOf(ability.getLevel())));
        return String.format(templateList,buf.toString());
    }
}
