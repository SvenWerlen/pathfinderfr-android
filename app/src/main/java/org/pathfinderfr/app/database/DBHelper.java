package org.pathfinderfr.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pathfinderfr-data.db";
    public static final int DATABASE_VERSION = 2;

    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context) {
        if(instance == null) {
            if(context == null) {
                throw new IllegalArgumentException("Cannot create new DBHelper instance without context!");
            }
            instance = new DBHelper(context);
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (DBEntityFactory f : EntityFactories.FACTORIES) {
            db.execSQL(f.getQueryCreateTable());
        }
    }

    public void clear() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (DBEntityFactory f : EntityFactories.FACTORIES) {
            // never drop favorites && characters!
            if(!(f instanceof FavoriteFactory) && !(f instanceof CharacterFactory)) {
                db.execSQL(String.format("DROP TABLE IF EXISTS %s", f.getTableName()));
            }
            db.execSQL(f.getQueryCreateTable());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // version 2 introduced a new column "source" in some tables (feats and spells)
        // version 2 also merged columns "target" and "area" but data will be kept until next reload
        if(oldVersion == 1) {
            db.execSQL(FeatFactory.getInstance().getQueryUpgradeV2());
            db.execSQL(SpellFactory.getInstance().getQueryUpgradeV2());
            oldVersion = 2;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 2");
        }
    }

    /**
     * Inserts the entity into the database
     *
     * @param entity entity instance
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertEntity(DBEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = entity.getFactory().generateContentValuesFromEntity(entity);
        return db.insert(entity.getFactory().getTableName(), null, contentValues);
    }


    /**
     * Updates the entity into the database
     *
     * @param entity entity instance
     * @return true if update succeeded
     */
    public boolean updateEntity(DBEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = entity.getFactory().generateContentValuesFromEntity(entity);
        long rowId = db.update(entity.getFactory().getTableName(), contentValues, null, null);
        return rowId > 0;
    }

    /**
     * Inserts the favorite into the database
     *
     * @param entity entity instance
     * @return true if insertion succeeded
     */
    public boolean insertFavorite(DBEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        DBEntityFactory factory = FavoriteFactory.getInstance();
        ContentValues contentValues = factory.generateContentValuesFromEntity(entity);
        long rowId = db.insert(factory.getTableName(), null, contentValues);
        return rowId != -1;
    }

    /**
     * Delete the favorite(s) from the database
     *
     * @param entity entity instance
     * @return true if insertion succeeded
     */
    public boolean deleteFavorite(DBEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = FavoriteFactory.getInstance().getQueryDeleteByIds(entity.getFactory().getFactoryId(), entity.getId());
        db.execSQL(query);
        return true;
    }

    /**
     * @param factoryId the type of element (skill, feat, spell, etc.)
     * @param entityId unique ID of the entity
     * @return true if the element has been selected as favorite, false otherwise
     */
    public boolean isFavorite(String factoryId, long entityId) {
        if(FavoriteFactory.FACTORY_ID.equalsIgnoreCase(factoryId)) {
            return true;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        String query = FavoriteFactory.getInstance().getQueryFetchByIds(factoryId, entityId);
        Cursor res = db.rawQuery(query, null);
        return res.getCount() > 0;
    }

    /**
     * @param id unique ID of the entity to be fetched
     * @param factory factory corresponding to the element (skill, feet, spell, etc.)
     * @return the entity as object (assuming that it will always be found)
     */
    public DBEntity fetchEntity(long id, DBEntityFactory factory) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( factory.getQueryFetchById(id), null );
        // not found?
        if(res.getCount()<1) {
            return null;
        }
        res.moveToFirst();
        return factory.generateEntity(res);
    }

    /**
     * @param name entity name to be searched
     * @param factory factory corresponding to the element (skill, feet, spell, etc.)
     * @return the entity as object (or null if not found)
     */
    public DBEntity fetchEntityByName(String name, DBEntityFactory factory) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( factory.getQueryFetchByName(name), null );
        // not found?
        if(res.getCount()<1) {
            return null;
        }
        res.moveToFirst();
        return factory.generateEntity(res);
    }


    public List<DBEntity> getAllEntities(DBEntityFactory factory, String... sources) {
        ArrayList<DBEntity> list = new ArrayList<>();

        Log.i(DBHelper.class.getSimpleName(), String.format("getAllEntities with %d filters",sources.length));

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res;
            if(sources.length > 0) {
                res = db.rawQuery(factory.getQueryFetchAll(sources), null);
            } else {
                res = db.rawQuery(factory.getQueryFetchAll(), null);
            }
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                list.add(factory.generateEntity(res));
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        }

        return list;
    }

    public List<DBEntity> getAllEntitiesWithAllFields(DBEntityFactory factory, String... sources) {
        ArrayList<DBEntity> list = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res;
            if(sources.length > 0) {
                res = db.rawQuery(factory.getQueryFetchAllWithAllFields(sources), null);
            } else {
                res = db.rawQuery(factory.getQueryFetchAllWithAllFields(), null);
            }
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                list.add(factory.generateEntity(res));
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        }

        return list;
    }


    public long getCountEntities(DBEntityFactory factory) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = String.format("SELECT COUNT(*) as total FROM %s", factory.getTableName());
            Cursor res = db.rawQuery(query, null);
            res.moveToFirst();
            return res.getLong(res.getColumnIndex("total"));
        } catch(SQLiteException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public long getCountEntities(DBEntityFactory factory, String... sources) {
        try {
            // check that column "source" exist for that table
            SQLiteDatabase db = this.getReadableDatabase();
            int count = 0;

            // Works with old SQLite versions
            Cursor res = db.rawQuery(String.format("PRAGMA table_info(%s);",
                    factory.getTableName()),null);

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                if(res.getColumnIndex("name")>=0 && factory.getColumnSource().equalsIgnoreCase(res.getString(res.getColumnIndex("name")))) {
                    count++;
                    break;
                }
                res.moveToNext();
            }

            /**
             * PRAGMA feature was added in SQLite version 3.16.0 (2017-01-02).
             *
            Cursor res = db.rawQuery(String.format("SELECT COUNT(*) AS total FROM pragma_table_info('%s') WHERE name='%s'",
                    factory.getTableName(),factory.getColumnSource()),null);
            res.moveToFirst();
            int count = res.getInt(res.getColumnIndex("total"));
             **/
            if(sources.length == 0 || count == 0) {
                if(count == 0) {
                    Log.i(DBHelper.class.getSimpleName(), String.format("No column %s found in table %s",
                            factory.getColumnSource(), factory.getTableName()));
                }
                return getCountEntities(factory);
            }

            String query = String.format("SELECT COUNT(*) as total FROM %s WHERE %s IN (%s)",
                    factory.getTableName(), factory.getColumnSource(), StringUtil.listToString(sources,',', '\''));

            res = db.rawQuery(query, null);
            res.moveToFirst();
            return res.getLong(res.getColumnIndex("total"));
        } catch(SQLiteException exception) {
            exception.printStackTrace();
            return 0;
        }
    }


}

