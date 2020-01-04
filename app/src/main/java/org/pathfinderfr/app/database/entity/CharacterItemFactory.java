package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import org.pathfinderfr.app.character.SheetMainFragment;
import org.pathfinderfr.app.database.DBHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CharacterItemFactory extends DBEntityFactory {

    public static final String FACTORY_ID = "CHARACITEMS";

    private static final String TABLENAME           = "characitems";
    private static final String COLUMN_CHARACTER_ID = "characterid";
    private static final String COLUMN_ORDER        = "corder";
    private static final String COLUMN_WEIGHT       = "weight";
    private static final String COLUMN_PRICE        = "price";
    private static final String COLUMN_ITEMREF      = "itemref";
    private static final String COLUMN_AMMO         = "ammo";
    private static final String COLUMN_CATEGORY     = "category";
    private static final String COLUMN_LOCATION     = "location";
    private static final String COLUMN_EQUIPED      = "equiped";

    public static final Integer FLAG_ALL      = 1;
    public static final Integer FLAG_ORDER    = 2;
    public static final Integer FLAG_EQUIPED  = 3;


    private static CharacterItemFactory instance;

    private CharacterItemFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized CharacterItemFactory getInstance() {
        if (instance == null) {
            instance = new CharacterItemFactory();
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
        String query = String.format( "CREATE TABLE IF NOT EXISTS %s (" +
                        "%s integer PRIMARY key, " +
                        "%s integer, %s integer," +                     // characterid, order
                        "%s text, %s text," +                           // name, desc
                        "%s integer, %s integer, %s integer," +         // weight, price, itemref
                        "%s text, %s integer, %s integer, %s integer" + // ammo, category, location, equiped
                        ")",
                TABLENAME, COLUMN_ID, COLUMN_CHARACTER_ID, COLUMN_ORDER,
                COLUMN_NAME, COLUMN_DESC,
                COLUMN_WEIGHT, COLUMN_PRICE, COLUMN_ITEMREF,
                COLUMN_AMMO, COLUMN_CATEGORY, COLUMN_LOCATION, COLUMN_EQUIPED);

        return query;
    }

    public String getQueryCreateIndex() {
        return String.format( "CREATE INDEX IF NOT EXISTS ix_character ON %s (%s)", TABLENAME, COLUMN_CHARACTER_ID);
    }

    @Override
    public String getQueryFetchAll(String... sources) {
        return super.getQueryFetchAllWithAllFields();
    }

    @Override
    public String getQueryFetchAllWithAllFields(String... sources) {
        return super.getQueryFetchAllWithAllFields();
    }

    @Override
    public String getQueryFetchByForeignKeys(long... id) {
        if(id == null || id.length == 0) {
            return getQueryFetchAll();
        }
        return String.format(Locale.CANADA, "SELECT * FROM %s WHERE %s=%d", TABLENAME, COLUMN_CHARACTER_ID, id[0]);
    }

    public static String getQueryDeleteItemsForCharacter(long characterId) {
        return String.format(Locale.CANADA, "DELETE FROM %s WHERE %s=%d", TABLENAME, COLUMN_CHARACTER_ID, characterId);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        Set<Integer> flags = new HashSet<Integer>();
        flags.add(FLAG_ALL);
        return generateContentValuesFromEntity(entity, flags);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity, Set<Integer> flags) {
        if (!(entity instanceof CharacterItem)) {
            return null;
        }
        CharacterItem item = (CharacterItem) entity;
        ContentValues contentValues = new ContentValues();
        if(flags.contains(FLAG_ALL)) {
            contentValues.put(CharacterItemFactory.COLUMN_NAME, item.getName());
            contentValues.put(CharacterItemFactory.COLUMN_DESC, item.getDescription());
            contentValues.put(CharacterItemFactory.COLUMN_CHARACTER_ID, item.getCharacterId());
            contentValues.put(CharacterItemFactory.COLUMN_WEIGHT, item.getWeight());
            contentValues.put(CharacterItemFactory.COLUMN_PRICE, item.getPrice());
            contentValues.put(CharacterItemFactory.COLUMN_ITEMREF, item.getItemRef());
            contentValues.put(CharacterItemFactory.COLUMN_AMMO, item.getAmmo());
            contentValues.put(CharacterItemFactory.COLUMN_CATEGORY, item.getCategory());
            contentValues.put(CharacterItemFactory.COLUMN_LOCATION, item.getLocation());
        }
        if(flags.contains(FLAG_ALL) || flags.contains(FLAG_ORDER)) {
            contentValues.put(CharacterItemFactory.COLUMN_ORDER, item.getOrder());
        }
        if(flags.contains(FLAG_ALL) || flags.contains(FLAG_EQUIPED)) {
            contentValues.put(CharacterItemFactory.COLUMN_EQUIPED, item.isEquiped() ? 1 : 0);
        }
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        CharacterItem item = new CharacterItem();

        item.setId(resource.getLong(resource.getColumnIndex(CharacterItemFactory.COLUMN_ID)));
        item.setCharacterId(resource.getLong(resource.getColumnIndex(CharacterItemFactory.COLUMN_CHARACTER_ID)));
        item.setOrder(resource.getInt(resource.getColumnIndex(CharacterItemFactory.COLUMN_ORDER)));
        item.setName(extractValue(resource, CharacterItemFactory.COLUMN_NAME));
        item.setDescription(extractValue(resource, CharacterItemFactory.COLUMN_DESC));
        item.setWeight(extractValueAsInt(resource, CharacterItemFactory.COLUMN_WEIGHT));
        item.setPrice(extractValueAsLong(resource, CharacterItemFactory.COLUMN_PRICE));
        item.setItemRef(extractValueAsLong(resource, CharacterItemFactory.COLUMN_ITEMREF));
        item.setAmmo(extractValue(resource, CharacterItemFactory.COLUMN_AMMO));
        item.setCategory(extractValueAsInt(resource, CharacterItemFactory.COLUMN_CATEGORY));
        item.setLocation(extractValueAsInt(resource, CharacterItemFactory.COLUMN_LOCATION));
        item.setEquiped(extractValueAsInt(resource, CharacterItemFactory.COLUMN_EQUIPED) == 1);
        return item;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        throw new UnsupportedOperationException();
    }

    public static void moveItem(DBHelper helper, List<CharacterItem> list, long itemIdToMove, long itemIdBefore) {
        Log.i(SheetMainFragment.class.getSimpleName(), "Item " + itemIdToMove + " moved before " + itemIdBefore);
        List<CharacterItem> items = list;
        // find item to move
        CharacterItem toMove = null;
        for(CharacterItem i : items) {
            if(i.getId() == itemIdToMove) {
                toMove = i;
                break;
            }
        }
        // not found??
        if(toMove == null) { return; }
        // build new list (ordered)
        int index = 0;
        for(CharacterItem i : items) {
            if(i.equals(toMove)) { continue; }
            if(i.getId() == itemIdBefore) {
                toMove.setOrder(index);
                index++;
            }
            i.setOrder(index);
            index++;
        }
        // at the end? (itemIdBefore = -1)
        if(itemIdBefore < 0) {
            toMove.setOrder(index);
        }
        // update all
        for(CharacterItem i : items) {
            helper.updateEntity(i, new HashSet<Integer>(Arrays.asList(CharacterItemFactory.FLAG_ORDER)));
        }
    }
}
