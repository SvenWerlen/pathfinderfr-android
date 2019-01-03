package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;

public class SpellFactory extends DBEntityFactory {

    public static final String FACTORY_ID        = "SPELLS";

    private static final String TABLENAME         = "spells";
    private static final String COLUMN_SCHOOL     = "school";
    private static final String COLUMN_LEVEL      = "level";
    private static final String COLUMN_CASTING    = "castingtime";
    private static final String COLUMN_COMPONENTS = "components";
    private static final String COLUMN_RANGE      = "range";
    private static final String COLUMN_TARGET     = "target";
    private static final String COLUMN_DURATION   = "duration";
    private static final String COLUMN_SAVING     = "savingthrow";
    private static final String COLUMN_SPELL_RES  = "spellresistance";
    private static final String COLUMN_AREA       = "area";

    private static final String YAML_NAME       = "Nom";
    private static final String YAML_DESC       = "Description";
    private static final String YAML_REFERENCE  = "Référence";
    private static final String YAML_SCHOOL     = "École";
    private static final String YAML_LEVEL      = "Niveau";
    private static final String YAML_CASTING    = "Temps d’incantation";
    private static final String YAML_COMPONENTS = "Composantes";
    private static final String YAML_RANGE      = "Portée";
    private static final String YAML_TARGET     = "Cible";
    private static final String YAML_DURATION   = "Durée";
    private static final String YAML_SAVING     = "Jet de sauvegarde";
    private static final String YAML_SPELL_RES  = "Résistance à la magie";
    private static final String YAML_AREA       = "Zone";


    private static SpellFactory instance;

    private SpellFactory() {
    }

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
    public String getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public String getTableName() {
        return SpellFactory.TABLENAME;
    }




    @Override
    public String getQueryCreateTable() {
        String query = String.format( "CREATE TABLE %s (" +
                        "%s integer PRIMARY key, " +
                        "%s text, %s text, %s text," +
                        "%s text, %s text, %s text, %s text, %s text," +
                        "%s text, %s text, %s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE,
                COLUMN_SCHOOL, COLUMN_LEVEL, COLUMN_CASTING,  COLUMN_COMPONENTS, COLUMN_RANGE,
                COLUMN_TARGET, COLUMN_DURATION, COLUMN_SAVING, COLUMN_SPELL_RES, COLUMN_AREA);
        return query;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Spell)) {
            return null;
        }
        Spell spell = (Spell) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(SpellFactory.COLUMN_NAME, spell.getName());
        contentValues.put(SpellFactory.COLUMN_DESC, spell.getDescription());
        contentValues.put(SpellFactory.COLUMN_REFERENCE, spell.getReference());
        contentValues.put(SpellFactory.COLUMN_SCHOOL, spell.getSchool());
        contentValues.put(SpellFactory.COLUMN_LEVEL, spell.getLevel());
        contentValues.put(SpellFactory.COLUMN_CASTING, spell.getCastingTime());
        contentValues.put(SpellFactory.COLUMN_COMPONENTS, spell.getComponents());
        contentValues.put(SpellFactory.COLUMN_RANGE, spell.getRange());
        contentValues.put(SpellFactory.COLUMN_TARGET, spell.getTarget());
        contentValues.put(SpellFactory.COLUMN_DURATION, spell.getDuration());
        contentValues.put(SpellFactory.COLUMN_SAVING, spell.getSavingThrow());
        contentValues.put(SpellFactory.COLUMN_SPELL_RES, spell.getSpellResistance());
        contentValues.put(SpellFactory.COLUMN_AREA, spell.getArea());
        return contentValues;
    }

    private static String extractValue(@NonNull final Cursor resource, String columnName) {
        if(resource.getColumnIndex(columnName)>=0) {
            return resource.getString(resource.getColumnIndex(columnName));
        } else {
            return null;
        }
    }

    @Override
    public DBEntity generateEntity(@NonNull final Cursor resource) {
        Spell spell = new Spell();
        
        spell.setId(resource.getLong(resource.getColumnIndex(SpellFactory.COLUMN_ID)));
        spell.setName(extractValue(resource,SpellFactory.COLUMN_NAME));
        spell.setDescription(extractValue(resource,SpellFactory.COLUMN_DESC));
        spell.setReference(extractValue(resource,SpellFactory.COLUMN_REFERENCE));
        spell.setSchool(extractValue(resource,SpellFactory.COLUMN_SCHOOL));
        spell.setLevel(extractValue(resource,SpellFactory.COLUMN_LEVEL));
        spell.setCastingTime(extractValue(resource,SpellFactory.COLUMN_CASTING));
        spell.setComponents(extractValue(resource,SpellFactory.COLUMN_COMPONENTS));
        spell.setRange(extractValue(resource,SpellFactory.COLUMN_RANGE));
        spell.setTarget(extractValue(resource,SpellFactory.COLUMN_TARGET));
        spell.setDuration(extractValue(resource,SpellFactory.COLUMN_DURATION));
        spell.setSavingThrow(extractValue(resource,SpellFactory.COLUMN_SAVING));
        spell.setSpellResistance(extractValue(resource,SpellFactory.COLUMN_SPELL_RES));
        spell.setArea(extractValue(resource,SpellFactory.COLUMN_AREA));
        return spell;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, String> attributes) {
        Spell spell = new Spell();
        spell.setName(attributes.get(YAML_NAME));
        spell.setDescription(attributes.get(YAML_DESC));
        spell.setSchool(attributes.get(YAML_SCHOOL));
        spell.setReference(attributes.get(YAML_REFERENCE));
        spell.setLevel(attributes.get(YAML_LEVEL));
        spell.setCastingTime(attributes.get(YAML_CASTING));
        spell.setComponents(attributes.get(YAML_COMPONENTS));
        spell.setRange(attributes.get(YAML_RANGE));
        spell.setTarget(attributes.get(YAML_TARGET));
        spell.setDuration(attributes.get(YAML_DURATION));
        spell.setSavingThrow(attributes.get(YAML_SAVING));
        spell.setSpellResistance(attributes.get(YAML_SPELL_RES));
        spell.setArea(attributes.get(YAML_AREA));
        return spell.isValid() ? spell : null;
    }

    /**
     * Utility function that returns the template with regex replaced
     * @param template template (@see assets/templates.properties)
     * @param propKey name of the property
     * @param propValue value of the property
     * @return "" if value is null
     */
    private static String generateItemDetail(String template, String propKey, String propValue) {
        if(propValue != null) {
            return String.format(template, propKey, propValue);
        } else {
            return "";
        }
    }

    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Spell)) {
            return "";
        }
        Spell spell = (Spell)entity;
        StringBuffer buf = new StringBuffer();
        buf.append(generateItemDetail(templateItem, YAML_SCHOOL, spell.getSchool()));
        buf.append(generateItemDetail(templateItem, YAML_LEVEL, spell.getLevel()));
        buf.append(generateItemDetail(templateItem, YAML_CASTING, spell.getCastingTime()));
        buf.append(generateItemDetail(templateItem, YAML_COMPONENTS, spell.getComponents()));
        buf.append(generateItemDetail(templateItem, YAML_RANGE, spell.getRange()));
        buf.append(generateItemDetail(templateItem, YAML_TARGET, spell.getTarget()));
        buf.append(generateItemDetail(templateItem, YAML_DURATION, spell.getDuration()));
        buf.append(generateItemDetail(templateItem, YAML_SAVING, spell.getSavingThrow()));
        buf.append(generateItemDetail(templateItem, YAML_SPELL_RES, spell.getSpellResistance()));
        buf.append(generateItemDetail(templateItem, YAML_AREA, spell.getArea()));
        return String.format(templateList,buf.toString());
    }

    @Override
    public String getQueryFetchById(long id) {
        return String.format("SELECT * FROM %s where %s=%d", getTableName(), COLUMN_ID, id);
    }

}
