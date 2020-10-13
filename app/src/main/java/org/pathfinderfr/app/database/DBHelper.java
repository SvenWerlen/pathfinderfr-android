package org.pathfinderfr.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import org.pathfinderfr.app.LoadDataActivity;
import org.pathfinderfr.app.database.entity.ArmorFactory;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.CharacterItemFactory;
import org.pathfinderfr.app.database.entity.ClassArchetypesFactory;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.ConditionFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.EquipmentFactory;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.MagicItemFactory;
import org.pathfinderfr.app.database.entity.Modification;
import org.pathfinderfr.app.database.entity.ModificationFactory;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.TraitFactory;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellClassLevel;
import org.pathfinderfr.app.database.entity.SpellClassLevelFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.database.entity.VersionFactory;
import org.pathfinderfr.app.database.entity.WeaponFactory;
import org.pathfinderfr.app.util.SpellFilter;
import org.pathfinderfr.app.util.SpellUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pathfinderfr-data.db";
    public static final int DATABASE_VERSION = 24;

    private static DBHelper instance;
    private static Map<String, Integer> versions;

    public static synchronized DBHelper getInstance(Context context) {
        if(instance == null) {
            if(context == null) {
                throw new IllegalArgumentException("Cannot create new DBHelper instance without context!");
            }
            instance = new DBHelper(context);
//            SQLiteDatabase db = instance.getWritableDatabase();
//            db.execSQL("ALTER TABLE characters ADD COLUMN hitpointstemp integer;");
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(DBEntityFactory fact : EntityFactories.FACTORIES) {
            executeNoFail(db, fact.getQueryCreateTable());
        }
        executeNoFail(db, SpellClassLevelFactory.getInstance().getQueryCreateTable());
        executeNoFail(db, SpellClassLevelFactory.getInstance().getQueryCreateIndex());
        executeNoFail(db, ModificationFactory.getInstance().getQueryCreateIndex());
        executeNoFail(db, CharacterItemFactory.getInstance().getQueryCreateIndex());
        executeNoFail(db, VersionFactory.getInstance().getQueryCreateTable());
    }

    public void clear(DBEntityFactory factory) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", factory.getTableName()));
        db.execSQL(factory.getQueryCreateTable());
        factory.cleanup();
    }

    public void clearDataWithVersion(DBEntityFactory factory, int version) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format(Locale.CANADA, "UPDATE %s SET version = -version WHERE version = %d", factory.getTableName(), version));
        factory.cleanup();
    }

    // DEBUGING ONLY
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // version 2 introduced a new column "source" in some tables (feats and spells)
        // version 2 also merged columns "target" and "area" but data will be kept until next reload
        if(oldVersion == 1) {
            executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV2());
            executeNoFail(db, SpellFactory.getInstance().getQueryUpgradeV2());
            oldVersion = 2;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 2");
        }
        // version 3 introduced new entities (races, classes and characters)
        if(oldVersion == 2) {
            executeNoFail(db, RaceFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, ClassFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, CharacterFactory.getInstance().getQueryCreateTable());
            oldVersion = 3;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 3");
        }
        // version 4 introduced new column on character for modifs
        if(oldVersion == 3) {
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV4());
            oldVersion = 4;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 4");
        }
        // version 5 introduced new column on character for hitpoints
        if(oldVersion == 4) {
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV5());
            oldVersion = 5;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 5");
        }
        // version 6 introduced new column on character for speed
        if(oldVersion == 5) {
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV6());
            oldVersion = 6;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 6");
        }
        // version 7 introduced new table for spell filtering optimization
        if(oldVersion == 6) {
            executeNoFail(db, SpellClassLevelFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, SpellClassLevelFactory.getInstance().getQueryCreateIndex());
            oldVersion = 7;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 7");
        }
        // version 8 introduced new entities (abilities)
        if(oldVersion == 7) {
            executeNoFail(db, ClassFeatureFactory.getInstance().getQueryCreateTable());
            oldVersion = 8;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 8");
        }
        // version 9 introduced new column on character for class features
        if(oldVersion == 8) {
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV9());
            oldVersion = 9;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 9");
        }
        // version 10 introduced new table for versions
        if(oldVersion == 9) {
            executeNoFail(db, VersionFactory.getInstance().getQueryCreateTable());
            oldVersion = 10;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 10");
        }
        // version 11 introduced new tables for conditions, weapons and armors
        // fixes version table not created!
        if(oldVersion == 10) {
            executeNoFail(db, ConditionFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, WeaponFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, ArmorFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, VersionFactory.getInstance().getQueryCreateTable());
            oldVersion = 11;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 11");
        }
        // version 12 introduced new column on character for inventory
        if(oldVersion == 11) {
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV12());
            oldVersion = 12;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 12");
        }
        // version 13 introduced new table for equipment
        if(oldVersion == 12) {
            executeNoFail(db, EquipmentFactory.getInstance().getQueryCreateTable());
            oldVersion = 13;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 13");
        }
        // version 14 introduced new table for race alternate traits
        if(oldVersion == 13) {
            executeNoFail(db, TraitFactory.getInstance().getQueryCreateTable());
            oldVersion = 14;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 14");
        }
        // version 15 introduced new column on character for alternate traits
        if(oldVersion == 14) {
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV15());
            oldVersion = 15;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 15");
        }
        // version 16 introduced new table for archetypes and new column on classfeatures for archetypes
        if(oldVersion == 15) {
            executeNoFail(db, ClassArchetypesFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, ClassFeatureFactory.getInstance().getQueryUpgradeV16());
            oldVersion = 16;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 16");
        }
        // version 17 introduced new table for magic item
        if(oldVersion == 16) {
            executeNoFail(db, MagicItemFactory.getInstance().getQueryCreateTable());
            oldVersion = 17;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 17");
        }
        // version 18 introduced 16 new columns on character (for PDF)
        if(oldVersion == 17) {
            List<String> queries = CharacterFactory.getInstance().getQueriesUpgradeV18();
            for(String q : queries) {
                executeNoFail(db, q);
            }
            oldVersion = 18;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 18");
        }
        // version 19 introduced 6 new columns on character, 1 new column on feat, 1 new column on armor (for PDF)
        if(oldVersion == 18) {
            executeNoFail(db, ArmorFactory.getInstance().getQueryUpgradeV19());
            executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV19());
            List<String> queries = CharacterFactory.getInstance().getQueriesUpgradeV19();
            for(String q : queries) {
                executeNoFail(db, q);
            }
            oldVersion = 19;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 19");
        }
        // version 20 introduced 6 new columns on character, 1 new column on feat, 1 new column on armor (for PDF)
        if(oldVersion == 19) {
            executeNoFail(db, ClassFactory.getInstance().getQueryUpgradeV20());
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV20());
            oldVersion = 20;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 20");
        }
        // version 21 introduced UUID for characters (for firebase messaging)
        if(oldVersion == 20) {
            executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV21());
            oldVersion = 21;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 21");
        }
        // version 22 introduced "requires" for feats
        if(oldVersion == 21) {
            executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV22());
            oldVersion = 22;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 22");
        }
        // version 23 introduced character items and modifs as separate database table
        if(oldVersion == 22) {
            executeNoFail(db,"DROP TABLE IF EXISTS characitems");
            executeNoFail(db,"DROP TABLE IF EXISTS modifs");
            executeNoFail(db, CharacterItemFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, CharacterItemFactory.getInstance().getQueryCreateIndex());
            executeNoFail(db, ModificationFactory.getInstance().getQueryCreateTable());
            executeNoFail(db, ModificationFactory.getInstance().getQueryCreateIndex());
            MigrationHelper.migrateCharacterItems(db);
            MigrationHelper.migrateModifs(db);
            // clean: pref_characterModifStates
            oldVersion = 23;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 23");
        }
        /**
         * MAJOR VERSION (4.x)
         */
        // version 24 introduced version for each catalog table
        if(oldVersion == 23) {
            executeNoFail(db, ArmorFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, ClassArchetypesFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, ClassFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, ClassFeatureFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, ConditionFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, EquipmentFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, MagicItemFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, RaceFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, SkillFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, SpellFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, TraitFactory.getInstance().getQueryUpgradeV4Version());
            executeNoFail(db, WeaponFactory.getInstance().getQueryUpgradeV4Version());
            oldVersion = 24;
            Log.i(DBHelper.class.getSimpleName(), "Database properly migrated to version 24 (v4)");
        }
    }

    /**
     * Execute create tables & upgrades in case something went wrong in previous versions
     */
    public void checkDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        // create table (if not exists)
        onCreate(db);

        // upgrade tables (if required)
        executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV2());
        executeNoFail(db, SpellFactory.getInstance().getQueryUpgradeV2());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV4());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV5());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV6());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV9());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV12());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV15());
        executeNoFail(db, ClassFeatureFactory.getInstance().getQueryUpgradeV16());
        for(String q : CharacterFactory.getInstance().getQueriesUpgradeV18()) {
            executeNoFail(db, q);
        }
        executeNoFail(db, ArmorFactory.getInstance().getQueryUpgradeV19());
        executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV19());
        for(String q : CharacterFactory.getInstance().getQueriesUpgradeV19()) {
            executeNoFail(db, q);
        }
        executeNoFail(db, ClassFactory.getInstance().getQueryUpgradeV20());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV20());
        executeNoFail(db, CharacterFactory.getInstance().getQueryUpgradeV21());
        executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV22());
        // version 4+
        executeNoFail(db, ArmorFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, ClassArchetypesFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, ClassFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, ClassFeatureFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, ConditionFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, EquipmentFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, FeatFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, MagicItemFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, RaceFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, SkillFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, SpellFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, TraitFactory.getInstance().getQueryUpgradeV4Version());
        executeNoFail(db, WeaponFactory.getInstance().getQueryUpgradeV4Version());
    }

    /**
     * Executes a sql query without throwing exceptions
     * @param sql sql query
     */
    public void executeNoFail(SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.d(DBHelper.class.getSimpleName(), "NoFail: " + e.getMessage());
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
     * Updates the entity (partially) into the database
     *
     * @param entity entity instance
     * @param flags data to be updated
     * @return true if update succeeded
     */
    public boolean updateEntity(DBEntity entity, Set<Integer> flags) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = entity.getFactory().generateContentValuesFromEntity(entity, flags);
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
        if(entity instanceof Character || entity instanceof CharacterItem || entity instanceof Modification) {
            db.execSQL(String.format("DELETE FROM %s WHERE id=%d", entity.getFactory().getTableName(), entity.getId()));
            if( entity instanceof Character ) {
                clearItemsAndModifsForCharacter(entity.getId());
            }
            return true;
        }
        // not supported for other tables
        return false;
    }

    public void clearItemsAndModifsForCharacter(long characterId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(CharacterItemFactory.getQueryDeleteItemsForCharacter(characterId));
        db.execSQL(ModificationFactory.getQueryDeleteModificationsForCharacter(characterId));
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
        boolean isFavorite = res.getCount() > 0;
        res.close();
        return isFavorite;
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
            res.close();
            return null;
        }
        res.moveToFirst();
        DBEntity entity = factory.generateEntity(res);
        res.close();
        return entity;
    }

    public String[] fetchCharacterUUIDs() {
        CharacterFactory factory = CharacterFactory.getInstance();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( factory.getQueryFetchAllUUIDs(), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return null;
        }
        res.moveToFirst();
        List<String> list = new ArrayList<>();
        while (!res.isAfterLast()) {
            String uuid = res.getString(res.getColumnIndex(CharacterFactory.COLUMN_UUID));
            if(uuid != null && uuid.length() > 0) {
                list.add(uuid);
            }
            res.moveToNext();
        }
        res.close();
        return list.toArray(new String[0]);
    }

    /**
     * @param uuid UUID of the entity to be fetched
     * @return the unique identifier of the character
     */
    public long fetchCharacterIdByUUID(String uuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( CharacterFactory.getInstance().getQueryFetchIdByUUID(uuid), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return -1;
        }
        res.moveToFirst();
        long id = res.getLong(res.getColumnIndex(CharacterFactory.COLUMN_ID));
        res.close();
        return id;
    }

    /**
     * @param id unique ID of the entity to be fetched
     * @param factory factory corresponding to the element (skill, feet, spell, etc.)
     * @param flags data to be fetched
     * @return the entity as object (assuming that it will always be found)
     */
    public DBEntity fetchEntity(long id, DBEntityFactory factory, Set<Integer> flags) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( factory.getQueryFetchById(id), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return null;
        }
        res.moveToFirst();
        DBEntity entity = factory.generateEntity(res, flags);
        res.close();
        return entity;
    }

    /**
     * @param item to be converted into weapon, armor, etc.
     * @return the entity as object (assuming that it will always be found)
     */
    public DBEntity fetchObjectEntity(CharacterItem item) {
        if(item.isNotLinked()) {
            return null;
        } else if(item.isWeapon()) {
            return fetchEntity(item.getOriginalItemRef(), WeaponFactory.getInstance());
        } else if(item.isArmor()) {
            return fetchEntity(item.getOriginalItemRef(), ArmorFactory.getInstance());
        } else if(item.isEquipment()) {
            return fetchEntity(item.getOriginalItemRef(), EquipmentFactory.getInstance());
        } else if(item.isMagicItem()) {
            return fetchEntity(item.getOriginalItemRef(), MagicItemFactory.getInstance());
        }
        return null;
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
            res.close();
            return new ArrayList<>();
        }
        res.moveToFirst();
        List<DBEntity> list = new ArrayList<>();
        while (!res.isAfterLast()) {
            DBEntity entity = factory.generateEntity(res);
            if(entity != null && !entity.isDeleted()) {
                list.add(entity);
            }
            res.moveToNext();
        }
        res.close();
        return list;
    }

    /**
     * @param name entity name to be searched
     * @param factory factory corresponding to the element (skill, feet, spell, etc.)
     * @return the entity as object (or null if not found)
     */
    public DBEntity fetchEntityByName(String name, DBEntityFactory factory) {
        SQLiteDatabase db = this.getReadableDatabase();
        Integer version = getVersions().get(factory.getFactoryId().toLowerCase());
        Cursor res =  db.rawQuery( factory.getQueryFetchByName(name, version == null ? -1 : version), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return null;
        }
        res.moveToFirst();
        DBEntity entity = factory.generateEntity(res);
        res.close();
        return entity;
    }

    /**
     * @param name entity name to be searched
     * @param factory factory corresponding to the element (skill, feet, spell, etc.)
     * @return the entity as object (or null if not found)
     */
    public List<DBEntity> fetchAllEntitiesByName(String name, DBEntityFactory factory) {
        SQLiteDatabase db = this.getReadableDatabase();
        Integer version = getVersion(factory.getFactoryId().toLowerCase());
        Cursor res =  db.rawQuery( factory.getQueryFetchByName(name, version), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return null;
        }
        res.moveToFirst();
        List<DBEntity> list = new ArrayList<>();
        while (!res.isAfterLast()) {
            DBEntity entity = factory.generateEntity(res);
            if(entity != null && !entity.isDeleted()) {
                list.add(entity);
            }
            res.moveToNext();
        }
        res.close();
        return list;
    }

    /**
     * @param ids foreign ids to search for
     * @param factory factory corresponding to the element (skill, feet, spell, etc.)
     * @return the entity as object (or null if not found)
     */
    public List<DBEntity> fetchAllEntitiesByForeignIds(long[] ids, DBEntityFactory factory) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( factory.getQueryFetchByForeignKeys(ids), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return new ArrayList<>();
        }
        res.moveToFirst();
        List<DBEntity> list = new ArrayList<>();
        while (!res.isAfterLast()) {
            DBEntity entity = factory.generateEntity(res);
            if(entity != null && !entity.isDeleted()) {
                list.add(entity);
            }
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public List<DBEntity> getAllEntities(DBEntityFactory factory, String... sources) {
        return getAllEntities(factory, null, sources);
    }

    public List<DBEntity> getAllEntities(DBEntityFactory factory, Integer dataVersion, String... sources) {
        ArrayList<DBEntity> list = new ArrayList<>();

        Log.i(DBHelper.class.getSimpleName(), String.format("getAllEntities with %d filters", (sources == null ? 0 : sources.length)));

        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Integer version = dataVersion == null ? getVersion(factory.getFactoryId().toLowerCase()) : dataVersion;
            res = db.rawQuery(factory.getQueryFetchAll(version, sources), null);
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (!res.isAfterLast()) {
                DBEntity entity = factory.generateEntity(res);
                if(entity != null && !entity.isDeleted()) {
                    list.add(entity);
                }
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        } finally {
            if(res != null) {
                res.close();
            }
        }
        return list;
    }

    public List<DBEntity> getAllEntitiesWithAllFields(DBEntityFactory factory, String... sources) {
        ArrayList<DBEntity> list = new ArrayList<>();

        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Integer version = getVersion(factory.getFactoryId().toLowerCase());
            res = db.rawQuery(factory.getQueryFetchAllWithAllFields(version, sources), null);
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                DBEntity entity = factory.generateEntity(res);
                if(entity != null && !entity.isDeleted()) {
                    list.add(entity);
                }
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        } finally {
            if(res != null) {
                res.close();
            }
        }

        return list;
    }


    public long getCountEntities(DBEntityFactory factory) {
        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = String.format("SELECT COUNT(*) as total FROM %s", factory.getTableName());
            res = db.rawQuery(query, null);
            res.moveToFirst();
            return res.getLong(res.getColumnIndex("total"));
        } catch(SQLiteException exception) {
            exception.printStackTrace();
            return 0;
        } finally {
            if(res != null) {
                res.close();
            }
        }
    }

    public long getCountEntities(DBEntityFactory factory, String... sources) {
        Cursor res = null;
        try {
            // check that column "source" exist for that table
            SQLiteDatabase db = this.getReadableDatabase();
            int count = 0;

            // Works with old SQLite versions
            res = db.rawQuery(String.format("PRAGMA table_info(%s);",
                    factory.getTableName()),null);

            res.moveToFirst();
            while (!res.isAfterLast()) {
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
        } finally {
            if(res != null) {
                res.close();
            }
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
            classMap.put(((Class)cl).getNameShort(),cl.getId());
        }

        for(DBEntity spell : spells) {
            List<org.pathfinderfr.app.util.Pair<String,Integer>> classLevels = SpellUtil.cleanClasses(((Spell)spell).getLevel());
            for(org.pathfinderfr.app.util.Pair<String,Integer> clLvl: classLevels) {
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

        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            res = db.rawQuery(SpellClassLevelFactory.getInstance().getQueryClassesWithSpells(), null);
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (!res.isAfterLast()) {
                list.add(res.getLong(0));
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        } finally {
            if(res != null) {
                res.close();
            }
        }

        return list;
    }

    public Set<String> getSpellSchools() {
        List<String> list = new ArrayList<>();

        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            res = db.rawQuery(SpellFactory.getInstance().getQuerySchools(), null);
            Log.i(DBHelper.class.getSimpleName(),"Number of elements found in database: " + res.getCount());
            res.moveToFirst();
            while (!res.isAfterLast()) {
                String school = res.getString(0);
                if(school != null && school.length() > 0) {
                    list.add(school);
                }
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        } finally {
            if(res != null) {
                res.close();
            }
        }

        Collections.sort(list);
        Set<String> result = new HashSet<>();
        result.addAll(list);
        return result;
    }

    public List<Spell> getSpells(SpellFilter filter, String... sources) {
        ArrayList<Spell> list = new ArrayList<>();
        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            res = db.rawQuery(SpellFactory.getInstance().getQueryFetchAll(filter, sources), null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                Spell spell = (Spell) SpellFactory.getInstance().generateEntity(res);
                if(!filter.hasFilterSchool() || filter.isFilterSchoolEnabled(spell.getSchool())) {
                    list.add(spell);
                }
                res.moveToNext();
            }
        } catch(SQLiteException exception) {
            exception.printStackTrace();
        } finally {
            if(res != null) {
                res.close();
            }
        }
        return list;
    }

    /**
     * @return true if spell indexes have been generated
     */
    public boolean hasSpellIndexes() {
        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = String.format("SELECT COUNT(*) as total FROM spellclasslevel");
            res = db.rawQuery(query, null);
            res.moveToFirst();
            return res.getLong(res.getColumnIndex("total")) > 0;
        } catch(SQLiteException exception) {
            exception.printStackTrace();
            return false;
        } finally {
            if(res != null) {
                res.close();
            }
        }
    }

    public Integer getVersion(String dataId) {
        Map<String, Integer> versions = getVersions();
        return versions.containsKey(dataId) ? versions.get(dataId) : Integer.valueOf(-1);
    }

    public synchronized Map<String, Integer> getVersions() {
        return getVersions(false);
    }

    public synchronized Map<String, Integer> getVersions(boolean force) {
        // retrieve versions (in cache)
        if(DBHelper.versions == null || !force) {
            DBHelper.versions = new HashMap<>();
            Log.i(DBHelper.class.getSimpleName(), "Retrieving versions");
            VersionFactory factory = VersionFactory.getInstance();

            Cursor res = null;
            try {
                SQLiteDatabase db = this.getReadableDatabase();
                res = db.rawQuery(factory.getQueryFetchAllVersion(), null);
                // not found?
                if (res.getCount() < 1) {
                    return DBHelper.versions;
                }
                res.moveToFirst();
                while (!res.isAfterLast()) {
                    String id = res.getString(res.getColumnIndex("id"));
                    Integer version = res.getInt(res.getColumnIndex("version"));
                    DBHelper.versions.put(id, version);
                    res.moveToNext();
                }
                res.close();
            } catch (SQLiteException exception) {
                exception.printStackTrace();
                return DBHelper.versions;
            } finally {
                if (res != null) {
                    res.close();
                }
            }
        }
        return DBHelper.versions;
    }

    public boolean updateVersion(String dataId, int version) {
        Log.i(DBHelper.class.getSimpleName(), String.format("Updating version of data %s", dataId));
        VersionFactory factory = VersionFactory.getInstance();

        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
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
        } finally {
            if(res != null) {
                res.close();
            }
        }
    }

    /**
     * Migrates all characters (based on latest dataset)
     * Returns the list of characters with unmatched and old data
     */
    public Map<String, Integer> migrateCharacters() {
        Map<String, Integer> unmatched = new HashMap<>();
        Map<String,List<Long>> dontRemove = new HashMap<>();
        List<DBEntity> list = this.getAllEntitiesWithAllFields(CharacterFactory.getInstance());
        for(DBEntity c : list) {
            List<DBEntity> notFound = MigrationHelper.migrate((Character)c, false);
            if(notFound.size() > 0) {
                unmatched.put(c.getName(), notFound.size());
                for(DBEntity e : notFound) {
                    String factoryId = e.getFactory().getFactoryId();
                    long id = e.getId();
                    // Get referenced item from CharacterItem
                    if(e instanceof CharacterItem) {
                        DBEntity realObj = fetchObjectEntity((CharacterItem)e);
                        factoryId = realObj.getFactory().getFactoryId();
                        id = realObj.getId();
                    }
                    if(!dontRemove.containsKey(factoryId)) {
                        dontRemove.put(factoryId, new ArrayList<Long>());
                    }
                    if(!dontRemove.get(factoryId).contains(id)) {
                        dontRemove.get(factoryId).add(id); // add ID to exclude
                    }
                }
            }
        }
        // delete all old entries except those that have not been yet migrated
        SQLiteDatabase db = this.getWritableDatabase();
        for(Pair<String, DBEntityFactory> c : LoadDataActivity.getCatalog()) {
            Integer version = getVersions().get(c.second.getFactoryId().toLowerCase());
            if(version != null) {
                // retrieve all IDs to exclude
                List<Long> excludeIds = dontRemove.containsKey(c.second.getFactoryId()) ? dontRemove.get(c.second.getFactoryId()) : new ArrayList<Long>();
                // build delete query
                String query = c.second.getQueryDeleteAllBut(version, excludeIds.toArray(new Long[0]));
                // delete!
                Log.i(DBHelper.class.getSimpleName(), "Delete all entries: " + query);
                db.execSQL(query);
            }
        }
        return unmatched;
    }
}

