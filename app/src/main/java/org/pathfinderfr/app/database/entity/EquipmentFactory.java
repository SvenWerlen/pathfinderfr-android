package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;

public class EquipmentFactory extends DBEntityFactory {

    public static final String FACTORY_ID           = "EQUIPMENT";

    private static final String TABLENAME           = "equipment";
    private static final String COLUMN_COST         = "cost";
    private static final String COLUMN_WEIGHT       = "weight";
    private static final String COLUMN_CATEGORY     = "category";

    private static final String YAML_NAME           = "Nom";
    private static final String YAML_DESC           = "Description";
    private static final String YAML_REFERENCE      = "Référence";
    private static final String YAML_SOURCE         = "Source";
    private static final String YAML_COST           = "Prix";
    private static final String YAML_WEIGHT         = "Poids";
    private static final String YAML_CATEGORY       = "Catégorie";


    private static EquipmentFactory instance;

    private EquipmentFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized EquipmentFactory getInstance() {
        if (instance == null) {
            instance = new EquipmentFactory();
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
                        "%s text, %s text, %s text, %s text," +
                        "%s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_COST, COLUMN_WEIGHT, COLUMN_CATEGORY);

        return query;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Equipment)) {
            return null;
        }
        Equipment equipment = (Equipment) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(EquipmentFactory.COLUMN_NAME, equipment.getName());
        contentValues.put(EquipmentFactory.COLUMN_DESC, equipment.getDescription());
        contentValues.put(EquipmentFactory.COLUMN_REFERENCE, equipment.getReference());
        contentValues.put(EquipmentFactory.COLUMN_SOURCE, equipment.getSource());
        contentValues.put(EquipmentFactory.COLUMN_COST, equipment.getCost());
        contentValues.put(EquipmentFactory.COLUMN_WEIGHT, equipment.getWeight());
        contentValues.put(EquipmentFactory.COLUMN_CATEGORY, equipment.getCategory());

        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Equipment equipment = new Equipment();

        equipment.setId(resource.getLong(resource.getColumnIndex(EquipmentFactory.COLUMN_ID)));
        equipment.setName(extractValue(resource, EquipmentFactory.COLUMN_NAME));
        equipment.setDescription(extractValue(resource, EquipmentFactory.COLUMN_DESC));
        equipment.setReference(extractValue(resource, EquipmentFactory.COLUMN_REFERENCE));
        equipment.setSource(extractValue(resource, EquipmentFactory.COLUMN_SOURCE));
        equipment.setCost(extractValue(resource, EquipmentFactory.COLUMN_COST));
        equipment.setWeight(extractValue(resource, EquipmentFactory.COLUMN_WEIGHT));
        equipment.setCategory(extractValue(resource, EquipmentFactory.COLUMN_CATEGORY));
        return equipment;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Equipment equipment = new Equipment();
        equipment.setName((String)attributes.get(YAML_NAME));
        equipment.setDescription((String)attributes.get(YAML_DESC));
        equipment.setReference((String)attributes.get(YAML_REFERENCE));
        equipment.setSource((String)attributes.get(YAML_SOURCE));
        equipment.setCost((String)attributes.get(YAML_COST));
        equipment.setWeight((String)attributes.get(YAML_WEIGHT));
        equipment.setCategory((String)attributes.get(YAML_CATEGORY));

        return equipment.isValid() ? equipment : null;
    }


    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Equipment)) {
            return "";
        }
        Equipment equipment = (Equipment)entity;
        StringBuffer buf = new StringBuffer();
        String source = equipment.getSource() == null ? null : getTranslatedText("source." + equipment.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        buf.append(generateItemDetail(templateItem, YAML_COST, equipment.getCost()));
        buf.append(generateItemDetail(templateItem, YAML_WEIGHT, equipment.getWeight()));
        buf.append(generateItemDetail(templateItem, YAML_CATEGORY, equipment.getCategory()));
        return String.format(templateList,buf.toString());
    }
}
