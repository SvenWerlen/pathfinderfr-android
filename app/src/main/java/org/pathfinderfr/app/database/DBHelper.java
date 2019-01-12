package org.pathfinderfr.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.FavoriteFactory;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pathfinderfr-data.db";

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
        super(context, DATABASE_NAME, null, 1);
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
            // never drop favorites!
            if(!(f instanceof FavoriteFactory)) {
                db.execSQL(String.format("DROP TABLE IF EXISTS %s", f.getTableName()));
            }
            db.execSQL(f.getQueryCreateTable());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        for (DBEntityFactory f : EntityFactories.FACTORIES) {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", f.getTableName()));
        }
        onCreate(db);
        */
    }

    /**
     * Inserts the entity into the database
     *
     * @param entity entity instance
     * @return true if insertion succeeded
     */
    public boolean insertEntity(DBEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = entity.getFactory().generateContentValuesFromEntity(entity);
        long rowId = db.insert(entity.getFactory().getTableName(), null, contentValues);
        return rowId != -1;
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


    public List<DBEntity> getAllEntities(DBEntityFactory factory) {
        ArrayList<DBEntity> list = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery(factory.getQueryFetchAll(), null);
            System.out.println("Number of elements found in database: " + res.getCount());
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

}

