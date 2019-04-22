package org.pathfinderfr.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.pathfinderfr.app.database.entity.ArmorFactory;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.ConditionFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellClassLevel;
import org.pathfinderfr.app.database.entity.SpellClassLevelFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.database.entity.VersionFactory;
import org.pathfinderfr.app.database.entity.WeaponFactory;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.SpellFilter;
import org.pathfinderfr.app.util.SpellUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pathfinderfr-data.db";
    public static final int DATABASE_VERSION = 12;

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
        // create version table
        db.execSQL(VersionFactory.getInstance().getQueryCreateTable());
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

    public void clear(DBEntityFactory factory) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", factory.getTableName()));
        db.execSQL(factory.getQueryCreateTable());
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
        // version 3 introduced new entities (races, classes and characters)
        if(oldVersion == 2) {
            db.execSQL(RaceFactory.getInstance().getQueryCreateTable());
            db.execSQL(ClassFactory.getInstance().getQueryCreateTable());
            db.execSQL(CharacterFactory.getInstance().getQueryCreateTable());
            oldVersion = 3;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 3");
        }
        // version 4 introduced new column on character for modifs
        if(oldVersion == 3) {
            db.execSQL(CharacterFactory.getInstance().getQueryUpgradeV4());
            oldVersion = 4;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 4");
        }
        // version 5 introduced new column on character for hitpoints
        if(oldVersion == 4) {
            db.execSQL(CharacterFactory.getInstance().getQueryUpgradeV5());
            oldVersion = 5;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 5");
        }
        // version 6 introduced new column on character for speed
        if(oldVersion == 5) {
            db.execSQL(CharacterFactory.getInstance().getQueryUpgradeV6());
            oldVersion = 6;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 6");
        }
        // version 7 introduced new table for spell filtering optimization
        if(oldVersion == 6) {
            db.execSQL(SpellClassLevelFactory.getInstance().getQueryCreateTable());
            db.execSQL(SpellClassLevelFactory.getInstance().getQueryCreateIndex());
            oldVersion = 7;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 7");
        }
        // version 8 introduced new entities (abilities)
        if(oldVersion == 7) {
            db.execSQL(ClassFeatureFactory.getInstance().getQueryCreateTable());
            oldVersion = 8;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 8");
        }
        // version 9 introduced new column on character for class features
        if(oldVersion == 8) {
            db.execSQL(CharacterFactory.getInstance().getQueryUpgradeV9());
            oldVersion = 9;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 9");
        }
        // version 10 introduced new table for versions
        if(oldVersion == 9) {
            db.execSQL(VersionFactory.getInstance().getQueryCreateTable());
            oldVersion = 10;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 10");
        }
        // version 11 introduced new tables for conditions, weapons and armors
        // fixes version table not created!
        if(oldVersion == 10) {
            db.execSQL(ConditionFactory.getInstance().getQueryCreateTable());
            db.execSQL(WeaponFactory.getInstance().getQueryCreateTable());
            db.execSQL(ArmorFactory.getInstance().getQueryCreateTable());
            db.execSQL(VersionFactory.getInstance().getQueryCreateTable());
            oldVersion = 11;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 11");
        }
        // version 12 introduced new column on character for inventory
        if(oldVersion == 11) {
            db.execSQL(CharacterFactory.getInstance().getQueryUpgradeV12());
            oldVersion = 12;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 12");
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
        long rowId = db.update(entity.getFactory().getTableName(), contentValues, DBEntityFactory.COLUMN_ID + "=" + entity.getId(), null);
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
     * Delete one element
     *
     * @param entity entity instance
     * @return true if deletion succeeded
     */
    public boolean deleteEntity(DBEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(entity instanceof Character) {
            String query = String.format("DELETE FROM %s WHERE id=%d", entity.getFactory().getTableName(), entity.getId());
            db.execSQL(query);
            return true;
        }
        // not supported for other tables
        return false;
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
     * @param ids IDs of the entities to be fetched
     * @param factory factory corresponding to the element (skill, feet, spell, etc.)
     * @return the entity as object (assuming that it will always be found)
     */
    public List<DBEntity> fetchAllEntitiesById(long[] ids, DBEntityFactory factory) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( factory.getQueryFetchAllById(ids), null );
        // not found?
        if(res.getCount()<1) {
            return null;
        }
        res.moveToFirst();
        List<DBEntity> list = new ArrayList<>();
        while (res.isAfterLast() == false) {
            list.add(factory.generateEntity(res));
            res.moveToNext();
        }
        return list;
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

        Log.i(DBHelper.class.getSimpleName(), String.format("getAllEntities with %d filters", (sources == null ? 0 : sources.length)));

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res;
            res = db.rawQuery(factory.getQueryFetchAll(sources), null);
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
            res = db.rawQuery(factory.getQueryFetchAllWithAllFields(sources), null);
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
            if(sources == null || sources.length == 0 || count == 0) {
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

    public void fillSpellClassLevel() {
        SQLiteDatabase db = this.getWritableDatabase();
        // clear table
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", SpellClassLevelFactory.TABLENAME));
        db.execSQL(SpellClassLevelFactory.getInstance().getQueryCreateTable());
        db.execSQL(SpellClassLevelFactory.getInstance().getQueryCreateIndex());

        List<DBEntity> spells = getAllEntities(SpellFactory.getInstance());
        List<DBEntity> classes = getAllEntities(ClassFactory.getInstance());
        // Map of <classShortName,classId>
        Map<String,Long> classMap = new HashMap<>();
        for(DBEntity cl : classes) {
            classMap.put(((Class)cl).getShortName(),cl.getId());
        }

        for(DBEntity spell : spells) {
            List<Pair<String,Integer>> classLevels = SpellUtil.cleanClasses(((Spell)spell).getLevel());
            for(Pair<String,Integer> clLvl: classLevels) {
                // find matching class
                if(classMap.containsKey(clLvl.first)) {
                    SpellClassLevel entity = new SpellClassLevel();
                    entity.setSpellId(spell.getId());
                    entity.setClassId(classMap.get(clLvl.first));
                    entity.setLevel(clLvl.second);
                    ContentValues contentValues = SpellClassLevelFactory.getInstance().generateContentValues(entity);
                    db.insert(SpellClassLevelFactory.TABLENAME, null, contentValues);

                    // TODO: ugly hack for Arc (same as Ens/Mag) and "PrêC" and "Ora" (same as Prê)
                    if("Prê".equals(clLvl.first) && classMap.containsKey("Prc")) {
                        entity.setClassId(classMap.get("Prc"));
                        contentValues = SpellClassLevelFactory.getInstance().generateContentValues(entity);
                        db.insert(SpellClassLevelFactory.TABLENAME, null, contentValues);
                    }
                    if("Prê".equals(clLvl.first) && classMap.containsKey("Ora")) {
                        entity.setClassId(classMap.get("Ora"));
                        contentValues = SpellClassLevelFactory.getInstance().generateContentValues(entity);
                        db.insert(SpellClassLevelFactory.TABLENAME, null, contentValues);
                    }
                    if("Ens".equals(clLvl.first) && classMap.containsKey("Arc")) {
                        entity.setClassId(classMap.get("Arc"));
                        contentValues = SpellClassLevelFactory.getInstance().generateContentValues(entity);
                        db.insert(SpellClassLevelFactory.TABLENAME, null, contentValues);
                    }
                }
                // something is wrong
                else {
                    Log.w(DBHelper.class.getSimpleName(), "Couldn't find class matching: " + clLvl.first);
                }
            }
        }
    }

    public Set<Long> getClassesWithSpells() {
        Set<Long> list = new HashSet<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery(SpellClassLevelFactory.getInstance().getQueryClassesWithSpells(), null);
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                list.add(res.getLong(0));
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        }

        return list;
    }

    public Set<String> getSpellSchools() {
        List<String> list = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery(SpellFactory.getInstance().getQuerySchools(), null);
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                list.add(res.getString(0));
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        }

        Collections.sort(list);
        Set<String> result = new HashSet<>();
        result.addAll(list);
        return result;
    }

    public List<Spell> getSpells(SpellFilter filter, String... sources) {
        ArrayList<Spell> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery(SpellFactory.getInstance().getQueryFetchAll(filter, sources), null);
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                Spell spell = (Spell) SpellFactory.getInstance().generateEntity(res);
                if(!filter.hasFilterSchool() || filter.isFilterSchoolEnabled(spell.getSchool())) {
                    list.add(spell);
                }
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        }
        return list;
    }

    /**
     * @return true if spell indexes have been generated
     */
    public boolean hasSpellIndexes() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = String.format("SELECT COUNT(*) as total FROM spellclasslevel");
            Cursor res = db.rawQuery(query, null);
            res.moveToFirst();
            return res.getLong(res.getColumnIndex("total")) > 0;
        } catch(SQLiteException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public Integer getVersion(String dataId) {
        Log.i(DBHelper.class.getSimpleName(), String.format("Retrieving version of data %s", dataId));
        VersionFactory factory = VersionFactory.getInstance();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res;
            res = db.rawQuery(factory.getQueryFetchVersion(dataId), null);
            // not found?
            if(res.getCount()<1) {
                return null;
            }
            res.moveToFirst();
            return res.getInt(res.getColumnIndex("version"));
        } catch(SQLiteException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public boolean updateVersion(String dataId, int version) {
        Log.i(DBHelper.class.getSimpleName(), String.format("Updating version of data %s", dataId));
        VersionFactory factory = VersionFactory.getInstance();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res;
            res = db.rawQuery(factory.getQueryFetchVersion(dataId), null);
            String query;
            // not found?
            if(res.getCount()<1) {
                query = factory.getQueryInsertVersion(dataId, version);
            } else {
                query = factory.getQueryUpdateVersion(dataId, version);
            }
            Log.i(DBHelper.class.getSimpleName(), String.format("Executing SQL query: %s", query));
            db.execSQL(query);
            return true;
        } catch(SQLiteException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}

