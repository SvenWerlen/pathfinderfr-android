package org.pathfinderfr.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pathfinderfr-data.db";

    private List<DBEntityFactory> factories;

    public DBHelper(Context context) {
        super(context,DATABASE_NAME,null,1);

        factories = new ArrayList<>();
        factories.add(SpellFactory.getInstance());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(DBEntityFactory f : factories) {
            db.execSQL(f.getQueryCreateTable());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(DBEntityFactory f : factories) {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", f.getTableName()));
        }
        onCreate(db);
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


    public List<DBEntity> getAllEntities(DBEntityFactory factory) {
        ArrayList<DBEntity> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( factory.getQueryFetchAll(), null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            list.add(factory.generateEntity(res));
            res.moveToNext();
        }

        return list;
    }

}

