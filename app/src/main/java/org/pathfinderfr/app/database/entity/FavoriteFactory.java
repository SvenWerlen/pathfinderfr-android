package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Map;

public class FavoriteFactory extends DBEntityFactory {

    public static final String FACTORY_ID         = "FAVORITES";

    private static final String TABLENAME         = "favorites";
    private static final String COLUMN_FACTORY_ID = "factoryid";
    private static final String COLUMN_ENTITY_ID  = "entityid";

    private static FavoriteFactory instance;

    private FavoriteFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized FavoriteFactory getInstance() {
        if (instance == null) {
            instance = new FavoriteFactory();
        }
        return instance;
    }

    @Override
    public String getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public String getTableName() {
        return TABLENAME;
    }

    @Override
    public String getQueryCreateTable() {
        String query = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "%s integer PRIMARY key, " +
                        "%s text, " +
                        "%s text, " +
                        "%s integer" +
                        ")",
                TABLENAME, COLUMN_ID, COLUMN_NAME,
                COLUMN_FACTORY_ID, COLUMN_ENTITY_ID);
        return query;
    }

    /**
     * Replaces default implementation in order to add column "factoryid" and "entityid" and change default ordering
     * No support for sources
     */
    @Override
    public String getQueryFetchAll(String... sources) {
        return String.format("SELECT %s,%s,%s,%s FROM %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_FACTORY_ID, COLUMN_ENTITY_ID, getTableName(),
                COLUMN_FACTORY_ID + "," + COLUMN_NAME);
    }

    public String getQueryFetchByIds(String factoryId, long id) {
        return String.format("SELECT * FROM %s where %s='%s' and %s=%d",
                getTableName(), COLUMN_FACTORY_ID, factoryId, COLUMN_ENTITY_ID, id);
    }

    public String getQueryDeleteByIds(String factoryId, long id) {
        return String.format("DELETE FROM %s where %s='%s' and %s=%d",
                getTableName(), COLUMN_FACTORY_ID, factoryId, COLUMN_ENTITY_ID, id);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if(entity.getId() <= 0) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteFactory.COLUMN_NAME, entity.getNameLong());
        contentValues.put(FavoriteFactory.COLUMN_FACTORY_ID, entity.getFactory().getFactoryId());
        contentValues.put(FavoriteFactory.COLUMN_ENTITY_ID, entity.getId());
        return contentValues;
    }

    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {

        String factoryId = resource.getString(resource.getColumnIndex(FavoriteFactory.COLUMN_FACTORY_ID));
        String name = resource.getString(resource.getColumnIndex(FavoriteFactory.COLUMN_NAME));
        //String classifier = ConfigurationUtil.getInstance().getProperties().getProperty("template.favorite." + factoryId.toLowerCase());

        DBEntity entity;
        if(SkillFactory.FACTORY_ID.equalsIgnoreCase(factoryId)) {
            entity = new Skill();
        } else if(FeatFactory.FACTORY_ID.equalsIgnoreCase(factoryId)) {
            entity = new Feat();
        } else if(ClassFeatureFactory.FACTORY_ID.equalsIgnoreCase(factoryId)) {
            entity = new ClassFeature();
        } else if(SpellFactory.FACTORY_ID.equalsIgnoreCase(factoryId)) {
            entity = new Spell();
        } else if(ConditionFactory.FACTORY_ID.equalsIgnoreCase(factoryId)) {
            entity = new Condition();
        } else if(WeaponFactory.FACTORY_ID.equalsIgnoreCase(factoryId)) {
            entity = new Weapon();
        } else {
            return null;
        }

        entity.setId(resource.getLong(resource.getColumnIndex(FavoriteFactory.COLUMN_ENTITY_ID)));
        entity.setName(name);
        return entity;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        throw new UnsupportedOperationException("This method (generateEntity) should never be used for favorites");
    }

    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        throw new UnsupportedOperationException("This method (generateDetails) should never be used for favorites");
    }
}
