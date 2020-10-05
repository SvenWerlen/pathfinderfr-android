package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;

public class ArmorFactory extends DBEntityFactory {

    public static final String FACTORY_ID             = "ARMORS";

    private static final String TABLENAME             = "armors";
    private static final String COLUMN_COST           = "cost";
    private static final String COLUMN_BONUS          = "bonus";
    private static final String COLUMN_BONUS_DEX_MAX  = "bonusdexmax";
    private static final String COLUMN_MALUS          = "malus";
    private static final String COLUMN_CAST_FAIL      = "castfail";
    private static final String COLUMN_SPEED9         = "speed9";
    private static final String COLUMN_SPEED6         = "speed6";
    private static final String COLUMN_WEIGHT         = "weight";
    private static final String COLUMN_CATEGORY       = "category";

    private static final String YAML_NAME           = "Nom";
    private static final String YAML_DESC           = "Description";
    private static final String YAML_REFERENCE      = "Référence";
    private static final String YAML_SOURCE         = "Source";
    private static final String YAML_COST           = "Prix";
    private static final String YAML_BONUS          = "Bonus";
    private static final String YAML_BONUS_DEX_MAX  = "BonusDexMax";
    private static final String YAML_MALUS          = "Malus";
    private static final String YAML_CAST_FAIL      = "ÉchecProfane";
    private static final String YAML_SPEED9         = "Vit9m";
    private static final String YAML_SPEED6         = "Vit6m";
    private static final String YAML_WEIGHT         = "Poids";
    private static final String YAML_CATEGORY       = "Catégorie";


    private static ArmorFactory instance;

    private ArmorFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized ArmorFactory getInstance() {
        if (instance == null) {
            instance = new ArmorFactory();
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
                        "%s text, %s text, %s text, %s text," +   // name, desc, ref, source
                        "%s text," +                              // category
                        "%s text, %s text, %s text, %s text," +   // cost, bonus, bonus_dex, malus
                        "%s text, %s text, %s text, %s text" +    // cast, speed9, speed6, weight
                        ")",
                TABLENAME, COLUMN_ID, COLUMN_VERSION,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_CATEGORY,
                COLUMN_COST, COLUMN_BONUS, COLUMN_BONUS_DEX_MAX, COLUMN_MALUS,
                COLUMN_CAST_FAIL, COLUMN_SPEED9, COLUMN_SPEED6, COLUMN_WEIGHT);

        return query;
    }

    /**
     * @return SQL statement for upgrading DB from v18 to v19
     */
    public String getQueryUpgradeV19() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_CATEGORY);
    }

    /**
     * @return the query to fetch all entities (including fields required for display)
     */
    @Override
    public String getQueryFetchAll(String... sources) {
        String filters = "";
        if(sources != null && sources.length > 0) {
            String sourceList = StringUtil.listToString(sources, ',', '\'');
            filters = String.format("WHERE %s IN (%s)", COLUMN_SOURCE, sourceList);
        }
        return String.format("SELECT %s,%s,%s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_BONUS, COLUMN_MALUS, COLUMN_CAST_FAIL, getTableName(), filters, COLUMN_NAME);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Armor)) {
            return null;
        }
        Armor armor = (Armor) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ArmorFactory.COLUMN_VERSION, armor.getVersion());
        contentValues.put(ArmorFactory.COLUMN_NAME, armor.getName());
        contentValues.put(ArmorFactory.COLUMN_DESC, armor.getDescription());
        contentValues.put(ArmorFactory.COLUMN_REFERENCE, armor.getReference());
        contentValues.put(ArmorFactory.COLUMN_SOURCE, armor.getSource());
        contentValues.put(ArmorFactory.COLUMN_COST, armor.getCost());
        contentValues.put(ArmorFactory.COLUMN_BONUS, armor.getBonus());
        contentValues.put(ArmorFactory.COLUMN_BONUS_DEX_MAX, armor.getBonusDexMax());
        contentValues.put(ArmorFactory.COLUMN_MALUS, armor.getMalus());
        contentValues.put(ArmorFactory.COLUMN_CAST_FAIL, armor.getCastFail());
        contentValues.put(ArmorFactory.COLUMN_SPEED9, armor.getSpeed9());
        contentValues.put(ArmorFactory.COLUMN_SPEED6, armor.getSpeed6());
        contentValues.put(ArmorFactory.COLUMN_WEIGHT, armor.getWeight());
        contentValues.put(ArmorFactory.COLUMN_CATEGORY, armor.getCategory());

        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Armor armor = new Armor();

        armor.setId(resource.getLong(resource.getColumnIndex(ArmorFactory.COLUMN_ID)));
        armor.setVersion(resource.getInt(resource.getColumnIndex(ArmorFactory.COLUMN_VERSION)));
        armor.setName(extractValue(resource, ArmorFactory.COLUMN_NAME));
        armor.setDescription(extractValue(resource, ArmorFactory.COLUMN_DESC));
        armor.setReference(extractValue(resource, ArmorFactory.COLUMN_REFERENCE));
        armor.setSource(extractValue(resource, ArmorFactory.COLUMN_SOURCE));
        armor.setCost(extractValue(resource, ArmorFactory.COLUMN_COST));
        armor.setBonus(extractValue(resource, ArmorFactory.COLUMN_BONUS));
        armor.setBonusDexMax(extractValue(resource, ArmorFactory.COLUMN_BONUS_DEX_MAX));
        armor.setMalus(extractValue(resource, ArmorFactory.COLUMN_MALUS));
        armor.setCastFail(extractValue(resource, ArmorFactory.COLUMN_CAST_FAIL));
        armor.setSpeed9(extractValue(resource, ArmorFactory.COLUMN_SPEED9));
        armor.setSpeed6(extractValue(resource, ArmorFactory.COLUMN_SPEED6));
        armor.setWeight(extractValue(resource, ArmorFactory.COLUMN_WEIGHT));
        armor.setCategory(extractValue(resource, ArmorFactory.COLUMN_CATEGORY));
        return armor;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Armor armor = new Armor();
        armor.setName((String)attributes.get(YAML_NAME));
        armor.setDescription((String)attributes.get(YAML_DESC));
        armor.setReference((String)attributes.get(YAML_REFERENCE));
        armor.setSource((String)attributes.get(YAML_SOURCE));
        armor.setCost((String)attributes.get(YAML_COST));
        armor.setBonus((String)attributes.get(YAML_BONUS));
        armor.setBonusDexMax((String)attributes.get(YAML_BONUS_DEX_MAX));
        armor.setMalus((String)attributes.get(YAML_MALUS));
        armor.setCastFail((String)attributes.get(YAML_CAST_FAIL));
        armor.setSpeed9((String)attributes.get(YAML_SPEED9));
        armor.setSpeed6((String)attributes.get(YAML_SPEED6));
        armor.setWeight((String)attributes.get(YAML_WEIGHT));
        armor.setCategory((String)attributes.get(YAML_CATEGORY));

        return armor.isValid() ? armor : null;
    }


    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Armor)) {
            return "";
        }
        Armor armor = (Armor)entity;
        StringBuffer buf = new StringBuffer();
        String source = armor.getSource() == null ? null : getTranslatedText("source." + armor.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        buf.append(generateItemDetail(templateItem, YAML_CATEGORY, armor.getCategory()));
        buf.append(generateItemDetail(templateItem, YAML_COST, armor.getCost()));
        buf.append(generateItemDetail(templateItem, YAML_BONUS, armor.getBonus()));
        buf.append(generateItemDetail(templateItem, YAML_BONUS_DEX_MAX, armor.getBonusDexMax()));
        buf.append(generateItemDetail(templateItem, YAML_MALUS, armor.getMalus()));
        buf.append(generateItemDetail(templateItem, YAML_CAST_FAIL, armor.getCastFail()));
        buf.append(generateItemDetail(templateItem, YAML_SPEED9, armor.getSpeed9()));
        buf.append(generateItemDetail(templateItem, YAML_SPEED6, armor.getSpeed6()));
        buf.append(generateItemDetail(templateItem, YAML_WEIGHT, armor.getWeight()));
        return String.format(templateList,buf.toString());
    }
}
