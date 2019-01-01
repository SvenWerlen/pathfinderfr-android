package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public abstract class DBEntity implements Cloneable {

    public abstract DBEntityFactory getFactory();

    public abstract int getId();
    public abstract String getName();
    public abstract String getDescription();
}
