package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Map;

public abstract class DBEntityFactory {

    protected static final String COLUMN_ID = "id";
    protected static final String COLUMN_NAME = "name";
    protected static final String COLUMN_DESC = "description";
    protected static final String COLUMN_REFERENCE = "reference";

    /**
     * @return the factory identifier
     */
    public abstract String getFactoryId();

    /**
     * @return name of the table to be used with that entity
     */
    public abstract String getTableName();

    /**
     * @return SQL statement for creating the table
     */
    public abstract String getQueryCreateTable();

    /**
     * @return the query to fetch one entity (search by ID)
     */
    public String getQueryFetchById(long id) {
        return String.format("SELECT * FROM %s where %s=%d", getTableName(), COLUMN_ID, id);
    }

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAll() {
        return String.format("SELECT %s,%s FROM %s ORDER BY %s",
                COLUMN_ID, COLUMN_NAME, getTableName(), COLUMN_ID);
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
    public abstract DBEntity generateEntity(@NonNull final Map<String, String> attributes);

    /**
     * Generates the content (generally HTML) based on entity details
     * (for example, spell duration or spell range)
     *
     * @param templateList template for the list. Must contain %s (content).
     * @param templateItem template for one item. Must contain %s (attribute name), and %s (attribute value)
     * @return
     */
    public abstract String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem);
}
