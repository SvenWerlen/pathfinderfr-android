package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import org.pathfinderfr.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatFactory extends DBEntityFactory {

    public static final String FACTORY_ID        = "FEATS";

    private static final String TABLENAME         = "feats";
    private static final String COLUMN_SUMMARY    = "summary";
    private static final String COLUMN_CATEGORY   = "category";
    private static final String COLUMN_CONDITIONS = "conditions";
    private static final String COLUMN_REQUIRES   = "requires";
    private static final String COLUMN_ADVANTAGE  = "advantage";
    private static final String COLUMN_SPECIAL    = "special";
    private static final String COLUMN_NORMAL     = "normal";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_SUMMARY      = "Résumé";
    private static final String YAML_DESC         = "Description"; // no description yet
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";
    private static final String YAML_CATEGORY     = "Catégorie";
    private static final String YAML_CONDITIONS   = "Conditions";
    private static final String YAML_CONDREFS     = "ConditionsRefs";
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
                        "%s integer version, " +
                        "%s text, %s text, %s text, %s text," +
                        "%s text, %s text, %s text, %s text, %s text, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID, COLUMN_VERSION,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_SUMMARY, COLUMN_CATEGORY, COLUMN_CONDITIONS, COLUMN_REQUIRES, COLUMN_ADVANTAGE, COLUMN_SPECIAL,  COLUMN_NORMAL);
        return query;
    }

    /**
     * @return SQL statement for upgrading DB from v18 to v19
     */
    public String getQueryUpgradeV19() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_SUMMARY);
    }

    /**
     * @return SQL statement for upgrading DB from v21 to v22
     */
    public String getQueryUpgradeV22() {
        return String.format("ALTER TABLE %s ADD COLUMN %s text;", getTableName(), COLUMN_REQUIRES);
    }

    /**
     * @return the query to fetch all entities (including fields required for filtering)
     */
    @Override
    public String getQueryFetchAll(String... sources) {
        String filters = "";
        if(sources != null && sources.length > 0) {
            String sourceList = StringUtil.listToString(sources, ',', '\'');
            filters = String.format("WHERE %s IN (%s)", COLUMN_SOURCE, sourceList);
        }
        return String.format("SELECT %s,%s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_CATEGORY, COLUMN_REQUIRES, getTableName(), filters, COLUMN_NAME);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Feat)) {
            return null;
        }
        Feat feat = (Feat) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassArchetypesFactory.COLUMN_VERSION, feat.getVersion());
        contentValues.put(FeatFactory.COLUMN_NAME, feat.getName());
        contentValues.put(FeatFactory.COLUMN_DESC, feat.getDescription());
        contentValues.put(FeatFactory.COLUMN_REFERENCE, feat.getReference());
        List<Long> ids = new ArrayList<>();
        for(Long id : feat.getRequires()) {
            ids.add(id);
        }
        contentValues.put(FeatFactory.COLUMN_REQUIRES, StringUtil.listToString(ids.toArray(new Long[0]),","));
        contentValues.put(FeatFactory.COLUMN_SOURCE, feat.getSource());
        contentValues.put(FeatFactory.COLUMN_CATEGORY, feat.getSummary());
        contentValues.put(FeatFactory.COLUMN_CATEGORY, feat.getCategory());
        contentValues.put(FeatFactory.COLUMN_CONDITIONS, feat.getConditions());
        contentValues.put(FeatFactory.COLUMN_ADVANTAGE, feat.getAdvantage());
        contentValues.put(FeatFactory.COLUMN_SPECIAL, feat.getSpecial());
        contentValues.put(FeatFactory.COLUMN_NORMAL, feat.getNormal());
        contentValues.put(FeatFactory.COLUMN_SUMMARY, feat.getSummary());
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Feat feat = new Feat();

        feat.setId(resource.getLong(resource.getColumnIndex(FeatFactory.COLUMN_ID)));
        feat.setVersion(resource.getInt(resource.getColumnIndex(FeatFactory.COLUMN_VERSION)));
        feat.setName(extractValue(resource,FeatFactory.COLUMN_NAME));
        feat.setDescription(extractValue(resource,FeatFactory.COLUMN_DESC));
        feat.setReference(extractValue(resource,FeatFactory.COLUMN_REFERENCE));
        feat.setSource(extractValue(resource,FeatFactory.COLUMN_SOURCE));
        feat.setSummary(extractValue(resource,FeatFactory.COLUMN_SUMMARY));
        feat.setCategory(extractValue(resource,FeatFactory.COLUMN_CATEGORY));
        feat.setConditions(extractValue(resource,FeatFactory.COLUMN_CONDITIONS));
        String requires = extractValue(resource,FeatFactory.COLUMN_REQUIRES);
        if(requires != null && requires.length() > 0) {
            String[] list = requires.split(",");
            for(String el : list) {
                try {
                    feat.getRequires().add(Long.valueOf(el));
                } catch (NumberFormatException e) {
                    Log.w(FeatFactory.class.getSimpleName(), "Invalid Feat ID " + el);
                }
            }
        }
        feat.setAdvantage(extractValue(resource,FeatFactory.COLUMN_ADVANTAGE));
        feat.setSpecial(extractValue(resource,FeatFactory.COLUMN_SPECIAL));
        feat.setNormal(extractValue(resource,FeatFactory.COLUMN_NORMAL));
        feat.setSummary(extractValue(resource,FeatFactory.COLUMN_SUMMARY));
        return feat;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Feat feat = new Feat();
        feat.setName((String)attributes.get((String)YAML_NAME));
        feat.setDescription((String)attributes.get(YAML_DESC));
        feat.setReference((String)attributes.get(YAML_REFERENCE));
        feat.setSource((String)attributes.get(YAML_SOURCE));
        feat.setSummary((String)attributes.get(YAML_SUMMARY));
        feat.setCategory((String)attributes.get(YAML_CATEGORY));
        feat.setConditions((String)attributes.get(YAML_CONDITIONS));
        Object refs = attributes.get(YAML_CONDREFS);
        if(refs instanceof List) {
            List<Object> list = (List<Object>)refs;
            for(Object ref : list) {
                if(ref instanceof String) {
                    feat.getRequiresRef().add((String)ref);
                }
            }
        }
        feat.setAdvantage((String)attributes.get(YAML_ADVANTAGE));
        feat.setSpecial((String)attributes.get(YAML_SPECIAL));
        feat.setNormal((String)attributes.get(YAML_NORMAL));
        feat.setSummary((String)attributes.get(YAML_SUMMARY));
        return feat.isValid() ? feat : null;
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
        buf.append(generateItemDetail(templateItem, YAML_SPECIAL, feat.getSpecial()));
        buf.append(generateItemDetail(templateItem, YAML_NORMAL, feat.getNormal()));
        return String.format(templateList,buf.toString());
    }
}
