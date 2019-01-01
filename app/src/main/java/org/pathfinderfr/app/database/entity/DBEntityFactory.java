package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

public abstract class DBEntityFactory {

    /**
     * @return name of the table to be used with that entity
     */
    public abstract String getTableName();

    /**
     * @return SQL statement for creating the table
     */
    public abstract String getQueryCreateTable();

    /**
     * @return the query to fetch all entities
     */
    public String getQueryFetchAll() {
        return "select * from " + getTableName();
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
}
