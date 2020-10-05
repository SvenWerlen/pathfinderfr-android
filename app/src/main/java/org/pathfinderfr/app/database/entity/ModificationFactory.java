package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import org.pathfinderfr.app.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ModificationFactory extends DBEntityFactory {

    public static final String FACTORY_ID             = "MODIFICATION";

    private static final String TABLENAME             = "modifs";
    private static final String COLUMN_CHARACTER_ID   = "characterid";
    private static final String COLUMN_ITEM_ID        = "itemid";
    private static final String COLUMN_BONUS          = "bonus";
    private static final String COLUMN_ICON           = "icon";
    private static final String COLUMN_ENABLED        = "enabled";

    public static final Integer FLAG_ALL      = 1;
    public static final Integer FLAG_ACTIVE   = 2;

    private static ModificationFactory instance;

    private ModificationFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized ModificationFactory getInstance() {
        if (instance == null) {
            instance = new ModificationFactory();
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
                        "%s integer, %s integer," +             // characterId, itemId
                        "%s text, %s text, " +                  // name, description
                        "%s text, %s text, %s integer " +       // bonus, icon, enabled
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_CHARACTER_ID, COLUMN_ITEM_ID,
                COLUMN_NAME, COLUMN_DESC,
                COLUMN_BONUS, COLUMN_ICON, COLUMN_ENABLED);

        return query;
    }

    public String getQueryCreateIndex() {
        return String.format( "CREATE INDEX IF NOT EXISTS ix_character ON %s (%s)", TABLENAME, COLUMN_CHARACTER_ID);
    }

    @Override
    public String getQueryFetchAll(Integer version, String... sources) {
        return super.getQueryFetchAllWithAllFields(version);
    }

    @Override
    public String getQueryFetchAllWithAllFields(Integer version, String... sources) {
        return super.getQueryFetchAllWithAllFields(version);
    }

    @Override
    public String getQueryFetchByForeignKeys(long... id) {
        if(id == null || id.length == 0) {
            return getQueryFetchAll(-1);
        }
        return String.format(Locale.CANADA, "SELECT * FROM %s WHERE %s=%d", TABLENAME, COLUMN_CHARACTER_ID, id[0]);
    }

    public static String getQueryDeleteModificationsForCharacter(long characterId) {
        return String.format(Locale.CANADA, "DELETE FROM %s WHERE %s=%d", TABLENAME, COLUMN_CHARACTER_ID, characterId);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        Set<Integer> flags = new HashSet<Integer>();
        flags.add(FLAG_ALL);
        return generateContentValuesFromEntity(entity, flags);
    }

    /**
     * Converts modifs/bonus to a string representation (for storage purposes)
     */
    private static String modifsToString(List<Pair<Integer, Integer>> modifs) {
        if(modifs.size() == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        for(Pair<Integer, Integer> bonus : modifs) {
            buf.append(bonus.first).append(':').append(bonus.second).append(",");
        }
        buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }

    private static List<Pair<Integer, Integer>> stringToModifs(String value) {
        List<Pair<Integer, Integer>> list = new ArrayList<>();
        String[] modifs = value.split(",");
        for(String modif: modifs) {
            String[] val = modif.split(":");
            if(val.length != 2) {
                Log.w(ModificationFactory.class.getSimpleName(),"Invalid modif value count! Skipping!");
                continue;
            }
            try {
                list.add(new Pair<>(Integer.parseInt(val[0]), Integer.parseInt(val[1])));
            } catch(NumberFormatException nfe) {
                Log.w(ModificationFactory.class.getSimpleName(),"Invalid modif value (NFE): " + nfe.getMessage());
            }
        }
        return list;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity, Set<Integer> flags) {
        if (!(entity instanceof Modification)) {
            return null;
        }
        Modification modif = (Modification) entity;
        ContentValues contentValues = new ContentValues();
        if(flags.contains(FLAG_ALL)) {
            contentValues.put(ModificationFactory.COLUMN_NAME, modif.getName());
            contentValues.put(ModificationFactory.COLUMN_DESC, modif.getDescription());
            contentValues.put(ModificationFactory.COLUMN_CHARACTER_ID, modif.getCharacterId());
            contentValues.put(ModificationFactory.COLUMN_ITEM_ID, modif.getItemId());
            contentValues.put(ModificationFactory.COLUMN_BONUS, modifsToString(modif.getModifs()));
            contentValues.put(ModificationFactory.COLUMN_ICON, modif.getIcon());

        }
        if(flags.contains(FLAG_ALL) || flags.contains(FLAG_ACTIVE)) {
            contentValues.put(ModificationFactory.COLUMN_ENABLED, modif.isEnabled() ? 1 : 0);
        }
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Modification modif = new Modification();

        modif.setId(resource.getLong(resource.getColumnIndex(ModificationFactory.COLUMN_ID)));
        modif.setCharacterId(resource.getLong(resource.getColumnIndex(ModificationFactory.COLUMN_CHARACTER_ID)));
        modif.setItemId(resource.getLong(resource.getColumnIndex(ModificationFactory.COLUMN_ITEM_ID)));
        modif.setName(extractValue(resource, ModificationFactory.COLUMN_NAME));
        modif.setDescription(extractValue(resource, ModificationFactory.COLUMN_DESC));
        modif.setModifs(stringToModifs(resource.getString(resource.getColumnIndex(ModificationFactory.COLUMN_BONUS))));
        modif.setIcon(extractValue(resource, ModificationFactory.COLUMN_ICON));
        modif.setEnabled(extractValueAsInt(resource, ModificationFactory.COLUMN_ENABLED) == 1);
        return modif;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        throw new UnsupportedOperationException();
    }
}
