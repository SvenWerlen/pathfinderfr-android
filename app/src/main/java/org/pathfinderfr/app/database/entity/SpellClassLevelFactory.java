package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import java.util.Locale;

public class SpellClassLevelFactory {

    public static final String TABLENAME         = "spellclasslevel";
    public static final String COLUMN_ID         = "id";
    public static final String COLUMN_SPELLID    = "spellid";
    public static final String COLUMN_CLASSID    = "classid";
    public static final String COLUMN_LEVEL      = "level";

    private static SpellClassLevelFactory instance;

    private SpellClassLevelFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized SpellClassLevelFactory getInstance() {
        if (instance == null) {
            instance = new SpellClassLevelFactory();
        }
        return instance;
    }

    public String getQueryCreateTable() {
        return String.format( "CREATE TABLE IF NOT EXISTS %s (" +
                        "%s integer PRIMARY key, " +
                        "%s integer, %s integer, %s integer" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_SPELLID, COLUMN_CLASSID, COLUMN_LEVEL);
    }

    public String getQueryCreateIndex() {
        return String.format( "CREATE INDEX ix_class ON %s (%s)", TABLENAME, COLUMN_CLASSID);
    }

    public String getQuerySpells(Class cl, int maxLevl) {
        return String.format(Locale.CANADA, "SELECT * FROM %s WHERE %s=%d and %s<=%d",
                TABLENAME, COLUMN_CLASSID, cl.getId(), COLUMN_LEVEL, maxLevl);
    }

    public String getQueryClassesWithSpells() {
        return String.format("SELECT DISTINCT %s FROM %s",
                COLUMN_CLASSID, TABLENAME);
    }

    public ContentValues generateContentValues(@NonNull SpellClassLevel entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SpellClassLevelFactory.COLUMN_SPELLID, entity.getSpellId());
        contentValues.put(SpellClassLevelFactory.COLUMN_CLASSID, entity.getClassId());
        contentValues.put(SpellClassLevelFactory.COLUMN_LEVEL, entity.getLevel());
        return contentValues;
    }

    private static String extractValue(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getString(resource.getColumnIndex(columnName));
        } else {
            return null;
        }
    }

    public SpellClassLevel generateEntity(@NonNull final Cursor resource) {
        SpellClassLevel entity = new SpellClassLevel();

        entity.setId(resource.getLong(resource.getColumnIndex(SpellClassLevelFactory.COLUMN_ID)));
        entity.setSpellId(resource.getLong(resource.getColumnIndex(COLUMN_SPELLID)));
        entity.setClassId(resource.getLong(resource.getColumnIndex(COLUMN_CLASSID)));
        entity.setLevel(resource.getInt(resource.getColumnIndex(COLUMN_LEVEL)));
        return entity;
    }
}
