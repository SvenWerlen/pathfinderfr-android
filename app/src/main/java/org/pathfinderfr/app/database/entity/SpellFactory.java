package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

public class SpellFactory extends DBEntityFactory {

    private static final String TABLENAME   = "spells";
    private static final String COLUMN_ID   = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESC = "description";

    private static final String YAML_NAME   = "Nom";
    private static final String YAML_DESC   = "Description";


    private static SpellFactory instance;
    private SpellFactory() {}

    /**
     * @return then unique instance of that factory
     */
    public static synchronized SpellFactory getInstance() {
        if (instance == null) {
            instance = new SpellFactory();
        }
        return instance;
    }

    @Override
    public String getTableName() {
        return SpellFactory.TABLENAME;
    }

    @Override
    public String getQueryCreateTable() {
        return String.format("CREATE TABLE %s (%s integer PRIMARY key, %s text, %s text)",
                TABLENAME, COLUMN_ID, COLUMN_NAME, COLUMN_DESC);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if(!(entity instanceof Spell)) {
            return null;
        }
        Spell spell = (Spell)entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(SpellFactory.COLUMN_NAME, spell.getName());
        contentValues.put(SpellFactory.COLUMN_DESC, spell.getDescription());
        return contentValues;
    }

    @Override
    public DBEntity generateEntity(@NonNull final Cursor resource) {
        Spell spell = new Spell();
        spell.setId(resource.getLong(resource.getColumnIndex(SpellFactory.COLUMN_ID)));
        spell.setName(resource.getString(resource.getColumnIndex(SpellFactory.COLUMN_NAME)));
        spell.setDescription(resource.getString(resource.getColumnIndex(SpellFactory.COLUMN_DESC)));
        return spell;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, String> attributes) {
        Spell spell = new Spell();
        spell.setName(attributes.get(YAML_NAME));
        spell.setDescription(attributes.get(YAML_DESC));
        return spell.isValid() ? spell : null;
    }

    @Override
    public String getQueryFetchById(long id) {
        return String.format("SELECT * FROM %s where %s=%d", getTableName(), COLUMN_ID, id);
    }

}
