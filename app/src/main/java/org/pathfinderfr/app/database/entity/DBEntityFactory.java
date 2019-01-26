package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;

public abstract class DBEntityFactory {

    protected static final String COLUMN_ID = "id";
    protected static final String COLUMN_NAME = "name";
    protected static final String COLUMN_DESC = "description";
    protected static final String COLUMN_REFERENCE = "reference";
    protected static final String COLUMN_SOURCE = "source";

    /**
     * @return the factory identifier
     */
    public abstract String getFactoryId();

    /**
     * @return name of the table to be used with that entity
     */
    public abstract String getTableName();

    /**
     * @return name of the source column
     */
    public String getColumnSource() { return COLUMN_SOURCE; }

    /**
     * @return SQL statement for creating the table
     */
    public abstract String getQueryCreateTable();

    /**
     * @return SQL statement for upgrading DB from v1 to v2
     */
    public String getQueryUpgradeV2() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_SOURCE);
    }

    /**
     * @return the query to fetch one entity (search by ID)
     */
    public String getQueryFetchById(long id) {
        return String.format("SELECT * FROM %s where %s=%d", getTableName(), COLUMN_ID, id);
    }

    /**
     * @return the query to fetch multiple entities (search by ID)
     */
    public String getQueryFetchAllById(long[] ids) {
        String idList = StringUtil.listToString(ids,',');
        return String.format("SELECT * FROM %s where %s IN (%s)", getTableName(), COLUMN_ID, idList);
    }

    /**
     * @return the query to fetch one entity (search by name)
     */
    public String getQueryFetchByName(String name) {
        return String.format("SELECT * FROM %s where %s=\"%s\"", getTableName(), COLUMN_NAME, name);
    }

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAll() {
        return String.format("SELECT %s,%s FROM %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, getTableName(), COLUMN_NAME);
    }

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAll(String... sources) {
        if(sources == null || sources.length == 0) {
            return getQueryFetchAll();
        }
        String sourceList = StringUtil.listToString( sources,',','\'');
        return String.format("SELECT %s,%s FROM %s WHERE %s IN (%s) ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, getTableName(), COLUMN_SOURCE, sourceList, COLUMN_NAME);
    }

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAllWithAllFields() {
        return String.format("SELECT * FROM %s ORDER BY %s COLLATE UNICODE",
                getTableName(), COLUMN_NAME);
    }

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAllWithAllFields(String... sources) {
        if(sources == null || sources.length == 0) {
            return getQueryFetchAllWithAllFields();
        }
        String sourceList = StringUtil.listToString( sources,',','\'');
        return String.format("SELECT * FROM %s WHERE %s IN (%s) ORDER BY %s COLLATE UNICODE",
                getTableName(), COLUMN_SOURCE, sourceList, COLUMN_NAME);
    }

    /**
     * Generates the content values required for inserting the entity into the database.
     *
     * @return ContentValues filled with values from entity
     */
    public abstract ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity);

    /**
     * Generates an entity based on given cursor
     *
     * @param resource cursor pointing to DB resource
     * @return a new instance of the entity filled according to data from DB
     */
    public abstract DBEntity generateEntity(@NonNull final Cursor resource);

    /**
     * Generates an entity based on a map (build from YAML file)
     *
     * @param attributes map with all attributes for that entry/entity
     * @return a new instance of the entity filled according to attributes
     */
    public abstract DBEntity generateEntity(@NonNull final Map<String, Object> attributes);

    /**
     * Generates the content (generally HTML) based on entity details
     * (for example, spell duration or spell range)
     *
     * @param templateList template for the list. Must contain %s (content).
     * @param templateItem template for one item. Must contain %s (attribute name), and %s (attribute value)
     * @return
     */
    public abstract String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem);


    /**
     * @param key key to identify the text in properties
     * @return translated text from properties
     */
    protected String getTranslatedText(String key) {
        return ConfigurationUtil.getInstance().getProperties().getProperty(key);
    }
}
