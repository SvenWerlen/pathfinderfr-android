package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Map;

public class ConditionFactory extends DBEntityFactory {

    public static final String FACTORY_ID         = "CONDITIONS";

    private static final String TABLENAME         = "conditions";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";

    private static ConditionFactory instance;

    private ConditionFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized ConditionFactory getInstance() {
        if (instance == null) {
            instance = new ConditionFactory();
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
                        "%s text, %s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE);
        return query;
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Condition)) {
            return null;
        }
        Condition condition = (Condition) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConditionFactory.COLUMN_NAME, condition.getName());
        contentValues.put(ConditionFactory.COLUMN_DESC, condition.getDescription());
        contentValues.put(ConditionFactory.COLUMN_REFERENCE, condition.getReference());
        contentValues.put(ConditionFactory.COLUMN_SOURCE, condition.getSource());
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Condition condition = new Condition();

        condition.setId(resource.getLong(resource.getColumnIndex(ConditionFactory.COLUMN_ID)));
        condition.setName(extractValue(resource, ConditionFactory.COLUMN_NAME));
        condition.setDescription(extractValue(resource, ConditionFactory.COLUMN_DESC));
        condition.setReference(extractValue(resource, ConditionFactory.COLUMN_REFERENCE));
        condition.setSource(extractValue(resource, ConditionFactory.COLUMN_SOURCE));
        return condition;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Condition condition = new Condition();
        condition.setName((String)attributes.get(YAML_NAME));
        condition.setDescription((String)attributes.get(YAML_DESC));
        condition.setReference((String)attributes.get(YAML_REFERENCE));
        condition.setSource((String)attributes.get(YAML_SOURCE));
        return condition.isValid() ? condition : null;
    }


    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Condition)) {
            return "";
        }
        Condition condition = (Condition)entity;
        StringBuffer buf = new StringBuffer();
        String source = condition.getSource() == null ? null : getTranslatedText("source." + condition.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        return String.format(templateList,buf.toString());
    }
}
