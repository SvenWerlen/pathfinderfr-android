package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Map;

public class FeatFactory extends DBEntityFactory {

    public static final String FACTORY_ID        = "FEATS";

    private static final String TABLENAME         = "feats";
    private static final String COLUMN_CATEGORY   = "category";
    private static final String COLUMN_CONDITIONS = "conditions";
    private static final String COLUMN_ADVANTAGE  = "advantage";
    private static final String COLUMN_SPECIAL    = "special";
    private static final String COLUMN_NORMAL     = "normal";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description"; // no description yet
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";
    private static final String YAML_CATEGORY     = "Catégorie";
    private static final String YAML_CONDITIONS   = "Conditions";
    private static final String YAML_ADVANTAGE    = "Avantage";
    private static final String YAML_SPECIAL      = "Spécial";
    private static final String YAML_NORMAL       = "Normal";


    private static FeatFactory instance;

    private FeatFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized FeatFactory getInstance() {
        if (instance == null) {
            instance = new FeatFactory();
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
                        "%s text, %s text, %s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_CATEGORY, COLUMN_CONDITIONS, COLUMN_ADVANTAGE, COLUMN_SPECIAL,  COLUMN_NORMAL);
        return query;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Feat)) {
            return null;
        }
        Feat feat = (Feat) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(FeatFactory.COLUMN_NAME, feat.getName());
        contentValues.put(FeatFactory.COLUMN_DESC, feat.getDescription());
        contentValues.put(FeatFactory.COLUMN_REFERENCE, feat.getReference());
        contentValues.put(FeatFactory.COLUMN_SOURCE, feat.getSource());
        contentValues.put(FeatFactory.COLUMN_CATEGORY, feat.getCategory());
        contentValues.put(FeatFactory.COLUMN_CONDITIONS, feat.getConditions());
        contentValues.put(FeatFactory.COLUMN_ADVANTAGE, feat.getAdvantage());
        contentValues.put(FeatFactory.COLUMN_SPECIAL, feat.getSpecial());
        contentValues.put(FeatFactory.COLUMN_NORMAL, feat.getNormal());
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
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Feat feat = new Feat();

        feat.setId(resource.getLong(resource.getColumnIndex(FeatFactory.COLUMN_ID)));
        feat.setName(extractValue(resource,FeatFactory.COLUMN_NAME));
        feat.setDescription(extractValue(resource,FeatFactory.COLUMN_DESC));
        feat.setReference(extractValue(resource,FeatFactory.COLUMN_REFERENCE));
        feat.setSource(extractValue(resource,FeatFactory.COLUMN_SOURCE));
        feat.setCategory(extractValue(resource,FeatFactory.COLUMN_CATEGORY));
        feat.setConditions(extractValue(resource,FeatFactory.COLUMN_CONDITIONS));
        feat.setAdvantage(extractValue(resource,FeatFactory.COLUMN_ADVANTAGE));
        feat.setSpecial(extractValue(resource,FeatFactory.COLUMN_SPECIAL));
        feat.setNormal(extractValue(resource,FeatFactory.COLUMN_NORMAL));
        return feat;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, String> attributes) {
        Feat feat = new Feat();
        feat.setName(attributes.get(YAML_NAME));
        feat.setDescription(attributes.get(YAML_DESC));
        feat.setReference(attributes.get(YAML_REFERENCE));
        feat.setSource(attributes.get(YAML_SOURCE));
        feat.setCategory(attributes.get(YAML_CATEGORY));
        feat.setConditions(attributes.get(YAML_CONDITIONS));
        feat.setAdvantage(attributes.get(YAML_ADVANTAGE));
        feat.setSpecial(attributes.get(YAML_SPECIAL));
        feat.setNormal(attributes.get(YAML_NORMAL));
        return feat.isValid() ? feat : null;
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
        if(!(entity instanceof Feat)) {
            return "";
        }
        Feat feat = (Feat)entity;
        StringBuffer buf = new StringBuffer();
        String source = feat.getSource() == null ? null : getTranslatedText("source." + feat.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        buf.append(generateItemDetail(templateItem, YAML_CATEGORY, feat.getCategory()));
        buf.append(generateItemDetail(templateItem, YAML_CONDITIONS, feat.getConditions()));
        buf.append(generateItemDetail(templateItem, YAML_ADVANTAGE, feat.getAdvantage()));
        buf.append(generateItemDetail(templateItem, YAML_SPECIAL, feat.getSpecial()));
        buf.append(generateItemDetail(templateItem, YAML_NORMAL, feat.getNormal()));
        return String.format(templateList,buf.toString());
    }
}
