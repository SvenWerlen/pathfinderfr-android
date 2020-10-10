package org.pathfinderfr.app.database.entity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class DBEntityFactory {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESC = "description";
    public static final String COLUMN_REFERENCE = "reference";
    public static final String COLUMN_SOURCE = "source";

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
     * @return Version 4 introduced a version for almost all tables
     */
    public String getQueryUpgradeV4Version() {
        return String.format("ALTER TABLE %s ADD COLUMN %s integer;", getTableName(), COLUMN_VERSION);
    }

    /**
     * @return the query to fetch one entity (search by ID)
     */
    @SuppressLint("DefaultLocale")
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
    public String getQueryFetchByName(String name, int version) {
        return String.format("SELECT * FROM %s where %s=\"%s\" and %s=%d ", getTableName(), COLUMN_NAME, name, COLUMN_VERSION, version);
    }

    /**
     * @return the query to fetch multiple entities (search by foreign key(s))
     */
    public String getQueryFetchByForeignKeys(long... id) {
        throw new IllegalStateException(String.format("Table %s doesn't support fetchByForeignKeys", getTableName()));
    }

    /**
     * @param version data version
     * @param sources enabled versions
     * @return SQL filters
     */
    protected String getFilters(Integer version, String... sources) {
        String filters;
        if(version == null || version <= 0) {
            filters = String.format(Locale.CANADA,"WHERE (%s IS NULL OR %s > 0)", COLUMN_VERSION, COLUMN_VERSION);
        } else {
            filters = String.format(Locale.CANADA, "WHERE %s=%d", COLUMN_VERSION, version);
        }
        if(sources != null && sources.length > 0) {
            String sourceList = StringUtil.listToString(sources, ',', '\'');
            filters += String.format(" AND %s IN (%s)", COLUMN_SOURCE, sourceList);
        }
        return filters;
    }

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAll(Integer version, String... sources) {
        return String.format("SELECT %s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, getTableName(),
                getFilters(version, sources),
                COLUMN_NAME);
    }

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAllWithAllFields(Integer version, String... sources) {
        String filters = String.format(Locale.CANADA,"WHERE %s=%d", COLUMN_VERSION, version);
        if(sources != null && sources.length > 0) {
            String sourceList = StringUtil.listToString(sources, ',', '\'');
            filters += String.format(" AND %s IN (%s)", COLUMN_SOURCE, sourceList);
        }
        return String.format("SELECT * FROM %s %s ORDER BY %s COLLATE UNICODE",
                getTableName(),
                filters, // order by
                COLUMN_NAME);
    }

    /**
     * Generates the content values required for inserting the entity into the database.
     *
     * @return ContentValues filled with values from entity
     */
    public abstract ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity);

    /**
     * Generates the content values required for inserting the entity into the database.
     * @param entity database entity
     * @param flags indicates which parts to update
     *
     * @return ContentValues filled with values from entity
     */
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity, @NonNull Set<Integer> flags) {
        // when flags are not implemented => generate all
        return generateContentValuesFromEntity(entity);
    }

    /**
     * Generates an entity based on given cursor
     *
     * @param resource cursor pointing to DB resource
     * @return a new instance of the entity filled according to data from DB
     */
    public abstract DBEntity generateEntity(@NonNull final Cursor resource);

    /**
     * Generates an entity based on given cursor
     * @param flags indicates which parts to load
     *
     * @param resource cursor pointing to DB resource
     * @return a new instance of the entity filled according to data from DB
     */
    public DBEntity generateEntity(@NonNull final Cursor resource, @NonNull Set<Integer> flags) {
        // when flags are not implemented => generate all
        return generateEntity(resource);
    }

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
     * Generates the HTML content to be rendered
     * Null by default. Must be implemented by each factory
     */
    public String generateHTMLContent(@NonNull DBEntity entity) { return null; }

    protected static String extractValue(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getString(resource.getColumnIndex(columnName));
        } else {
            return null;
        }
    }

    protected static int extractValueAsInt(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getInt(resource.getColumnIndex(columnName));
        } else {
            return -1;
        }
    }

    protected static long extractValueAsLong(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getLong(resource.getColumnIndex(columnName));
        } else {
            return -1;
        }
    }

    protected static boolean extractValueAsBoolean(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getInt(resource.getColumnIndex(columnName)) == 1;
        } else {
            return false;
        }
    }

    protected static String extractDescription(@NonNull Map<String, Object> attributes, String field, String fieldHTML) {
        String description = null;
        if(attributes.containsKey(fieldHTML)) {
            description = (String) attributes.get(fieldHTML);
        }
        else {
            description = (String)attributes.get(field);
            if(description != null) {
                description = description.replaceAll("\\n", "<br />");
            } else {
                description = "-";
            }
        }
        return description;
    }

    /**
     * Utility function that returns the template with regex replaced
     * @param template template (@see assets/templates.properties)
     * @param propKey name of the property
     * @param propValue value of the property
     * @return "" if value is null
     */
    protected static String generateItemDetail(String template, String propKey, String propValue) {
        if(!StringUtil.isEmpty(propValue)) {
            return String.format(template, propKey, propValue);
        } else {
            return "";
        }
    }

    /**
     * Utility function that returns the template with regex replaced
     * @param template template (@see assets/templates.properties)
     * @param propKey name of the property
     * @param propValue value of the property
     * @return "" if value is null
     */
    protected static String generateItemDetail(String template, String propKey, List<String> propValue) {
        if(propValue != null && propValue.size() > 0) {
            return String.format(template, propKey, StringUtil.listToString(propValue, ", "));
        } else {
            return "";
        }
    }

    /**
     * @param key key to identify the text in properties
     * @return translated text from properties
     */
    protected String getTranslatedText(String key) {
        return ConfigurationUtil.getInstance().getProperties().getProperty(key);
    }

    /**
     * Reset any existing cache or similar
     */
    public void cleanup() {}
}
