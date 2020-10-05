package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;

public class WeaponFactory extends DBEntityFactory {

    public static final String FACTORY_ID             = "WEAPONS";

    private static final String TABLENAME             = "weapons";
    private static final String COLUMN_COST           = "cost";
    private static final String COLUMN_DAMAGES_SMALL  = "damagesS";
    private static final String COLUMN_DAMAGES_MEDIUM = "damagesM";
    private static final String COLUMN_CRITICAL       = "critical";
    private static final String COLUMN_RANGE          = "range";
    private static final String COLUMN_WEIGHT         = "weight";
    private static final String COLUMN_TYPE           = "type";
    private static final String COLUMN_SPECIAL        = "special";

    private static final String YAML_NAME           = "Nom";
    private static final String YAML_DESC           = "Description";
    private static final String YAML_REFERENCE      = "Référence";
    private static final String YAML_SOURCE         = "Source";
    private static final String YAML_COST           = "Prix";
    private static final String YAML_DAMAGES_SMALL  = "DégâtsP";
    private static final String YAML_DAMAGES_MEDIUM = "DégâtsM";
    private static final String YAML_CRITICAL       = "Critique";
    private static final String YAML_RANGE          = "Portée";
    private static final String YAML_WEIGHT         = "Poids";
    private static final String YAML_TYPE           = "Type";
    private static final String YAML_SPECIAL        = "Spécial";


    private static WeaponFactory instance;

    private WeaponFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized WeaponFactory getInstance() {
        if (instance == null) {
            instance = new WeaponFactory();
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
                        "%s integer version, " +
                        "%s text, %s text, %s text, %s text," +
                        "%s text, %s text, %s text, %s text," +
                        "%s text, %s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID, COLUMN_VERSION,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_COST, COLUMN_DAMAGES_SMALL, COLUMN_DAMAGES_MEDIUM, COLUMN_CRITICAL,
                COLUMN_RANGE, COLUMN_WEIGHT, COLUMN_TYPE, COLUMN_SPECIAL);

        return query;
    }

    /**
     * @return the query to fetch all entities (including fields required for display)
     */
    @Override
    public String getQueryFetchAll(Integer version, String... sources) {
        return String.format("SELECT %s,%s,%s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_DAMAGES_MEDIUM, COLUMN_CRITICAL, COLUMN_TYPE, getTableName(), getFilters(version, sources), COLUMN_NAME);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Weapon)) {
            return null;
        }
        Weapon weapon = (Weapon) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassArchetypesFactory.COLUMN_VERSION, weapon.getVersion());
        contentValues.put(WeaponFactory.COLUMN_NAME, weapon.getName());
        contentValues.put(WeaponFactory.COLUMN_DESC, weapon.getDescription());
        contentValues.put(WeaponFactory.COLUMN_REFERENCE, weapon.getReference());
        contentValues.put(WeaponFactory.COLUMN_SOURCE, weapon.getSource());
        contentValues.put(WeaponFactory.COLUMN_COST, weapon.getCost());
        contentValues.put(WeaponFactory.COLUMN_DAMAGES_SMALL, weapon.getDamageSmall());
        contentValues.put(WeaponFactory.COLUMN_DAMAGES_MEDIUM, weapon.getDamageMedium());
        contentValues.put(WeaponFactory.COLUMN_CRITICAL, weapon.getCritical());
        contentValues.put(WeaponFactory.COLUMN_RANGE, weapon.getRange());
        contentValues.put(WeaponFactory.COLUMN_WEIGHT, weapon.getWeight());
        contentValues.put(WeaponFactory.COLUMN_TYPE, weapon.getType());
        contentValues.put(WeaponFactory.COLUMN_SPECIAL, weapon.getSpecial());

        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Weapon weapon = new Weapon();

        weapon.setId(resource.getLong(resource.getColumnIndex(WeaponFactory.COLUMN_ID)));
        weapon.setVersion(extractValueAsInt(resource, WeaponFactory.COLUMN_VERSION));
        weapon.setName(extractValue(resource, WeaponFactory.COLUMN_NAME));
        weapon.setDescription(extractValue(resource, WeaponFactory.COLUMN_DESC));
        weapon.setReference(extractValue(resource, WeaponFactory.COLUMN_REFERENCE));
        weapon.setSource(extractValue(resource, WeaponFactory.COLUMN_SOURCE));
        weapon.setCost(extractValue(resource, WeaponFactory.COLUMN_COST));
        weapon.setDamageSmall(extractValue(resource, WeaponFactory.COLUMN_DAMAGES_SMALL));
        weapon.setDamageMedium(extractValue(resource, WeaponFactory.COLUMN_DAMAGES_MEDIUM));
        weapon.setCritical(extractValue(resource, WeaponFactory.COLUMN_CRITICAL));
        weapon.setRange(extractValue(resource, WeaponFactory.COLUMN_RANGE));
        weapon.setWeight(extractValue(resource, WeaponFactory.COLUMN_WEIGHT));
        weapon.setType(extractValue(resource, WeaponFactory.COLUMN_TYPE));
        weapon.setSpecial(extractValue(resource, WeaponFactory.COLUMN_SPECIAL));
        return weapon;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Weapon weapon = new Weapon();
        weapon.setName((String)attributes.get(YAML_NAME));
        weapon.setDescription((String)attributes.get(YAML_DESC));
        weapon.setReference((String)attributes.get(YAML_REFERENCE));
        weapon.setSource((String)attributes.get(YAML_SOURCE));
        weapon.setCost((String)attributes.get(YAML_COST));
        weapon.setDamageSmall((String)attributes.get(YAML_DAMAGES_SMALL));
        weapon.setDamageMedium((String)attributes.get(YAML_DAMAGES_MEDIUM));
        weapon.setCritical((String)attributes.get(YAML_CRITICAL));
        weapon.setRange((String)attributes.get(YAML_RANGE));
        weapon.setWeight((String)attributes.get(YAML_WEIGHT));
        weapon.setType((String)attributes.get(YAML_TYPE));
        weapon.setSpecial((String)attributes.get(YAML_SPECIAL));

        return weapon.isValid() ? weapon : null;
    }


    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Weapon)) {
            return "";
        }
        Weapon weapon = (Weapon)entity;
        StringBuffer buf = new StringBuffer();
        String source = weapon.getSource() == null ? null : getTranslatedText("source." + weapon.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        buf.append(generateItemDetail(templateItem, YAML_COST, weapon.getCost()));
        buf.append(generateItemDetail(templateItem, YAML_DAMAGES_SMALL, weapon.getDamageSmall()));
        buf.append(generateItemDetail(templateItem, YAML_DAMAGES_MEDIUM, weapon.getDamageMedium()));
        buf.append(generateItemDetail(templateItem, YAML_CRITICAL, weapon.getCritical()));
        buf.append(generateItemDetail(templateItem, YAML_RANGE, weapon.getRange()));
        buf.append(generateItemDetail(templateItem, YAML_WEIGHT, weapon.getWeight()));
        buf.append(generateItemDetail(templateItem, YAML_TYPE, weapon.getType()));
        buf.append(generateItemDetail(templateItem, YAML_SPECIAL, weapon.getSpecial()));
        return String.format(templateList,buf.toString());
    }
}
