package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassFeatureFactory extends DBEntityFactory {

    public static final String FACTORY_ID        = "CLASSFEATURES";

    private static final String TABLENAME         = "classfeatures";
    private static final String COLUMN_CLASS      = "class";
    private static final String COLUMN_CONDITIONS = "conditions";
    private static final String COLUMN_AUTOMATIC  = "auto";
    private static final String COLUMN_LEVEL      = "level";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";
    private static final String YAML_CLASS        = "Classe";
    private static final String YAML_CONDITIONS   = "Conditions";
    private static final String YAML_AUTO         = "Auto";
    private static final String YAML_LEVEL        = "Niveau";

    private static ClassFeatureFactory instance;

    private Map<Long, Class> classesById;
    private Map<String, Class> classesByName;

    private ClassFeatureFactory() {
        classesById = new HashMap<>();
        classesByName = new HashMap<>();
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized ClassFeatureFactory getInstance() {
        if (instance == null) {
            instance = new ClassFeatureFactory();
        }
        return instance;
    }

    private synchronized Class getClass(String name) {
        if(classesByName.size() == 0) {
            classesById.clear();
            List<DBEntity> fullList = DBHelper.getInstance(null).getAllEntities(ClassFactory.getInstance());
            for(DBEntity e : fullList) {
                classesById.put(e.getId(), (Class)e);
                classesByName.put(e.getName(), (Class)e);
            }
        }
        return classesByName.get(name);
    }

    private synchronized Class getClass(long id) {
        if(classesById.size() == 0) {
            classesByName.clear();
            List<DBEntity> fullList = DBHelper.getInstance(null).getAllEntities(ClassFactory.getInstance());
            for(DBEntity e : fullList) {
                classesById.put(e.getId(), (Class)e);
                classesByName.put(e.getName(), (Class)e);
            }
        }
        return classesById.get(id);
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
                        "%s integer, %s text, %s integer, %s integer" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_CLASS, COLUMN_CONDITIONS, COLUMN_LEVEL, COLUMN_AUTOMATIC);
        return query;
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
        return String.format("SELECT %s,%s,%s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_CLASS, COLUMN_LEVEL, COLUMN_AUTOMATIC, getTableName(), filters, COLUMN_NAME);
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof ClassFeature)) {
            return null;
        }
        ClassFeature classFeature = (ClassFeature) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassFeatureFactory.COLUMN_NAME, classFeature.getName());
        contentValues.put(ClassFeatureFactory.COLUMN_DESC, classFeature.getDescription());
        contentValues.put(ClassFeatureFactory.COLUMN_REFERENCE, classFeature.getReference());
        contentValues.put(ClassFeatureFactory.COLUMN_SOURCE, classFeature.getSource());
        contentValues.put(ClassFeatureFactory.COLUMN_CLASS, classFeature.getClass_().getId());
        contentValues.put(ClassFeatureFactory.COLUMN_CONDITIONS, classFeature.getConditions());
        contentValues.put(ClassFeatureFactory.COLUMN_LEVEL, classFeature.getLevel());
        contentValues.put(ClassFeatureFactory.COLUMN_AUTOMATIC, classFeature.isAuto() ? 1 : 0);
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        ClassFeature classFeature = new ClassFeature();

        classFeature.setId(resource.getLong(resource.getColumnIndex(ClassFeatureFactory.COLUMN_ID)));
        classFeature.setName(extractValue(resource, ClassFeatureFactory.COLUMN_NAME));
        classFeature.setDescription(extractValue(resource, ClassFeatureFactory.COLUMN_DESC));
        classFeature.setReference(extractValue(resource, ClassFeatureFactory.COLUMN_REFERENCE));
        classFeature.setSource(extractValue(resource, ClassFeatureFactory.COLUMN_SOURCE));
        classFeature.setClass(getClass(extractValueAsInt(resource, ClassFeatureFactory.COLUMN_CLASS)));
        classFeature.setConditions(extractValue(resource, ClassFeatureFactory.COLUMN_CONDITIONS));
        classFeature.setLevel(extractValueAsInt(resource, ClassFeatureFactory.COLUMN_LEVEL));
        classFeature.setAuto(extractValueAsBoolean(resource, ClassFeatureFactory.COLUMN_AUTOMATIC));
        return classFeature;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        ClassFeature classFeature = new ClassFeature();
        classFeature.setName((String)attributes.get((String)YAML_NAME));
        classFeature.setDescription((String)attributes.get(YAML_DESC));
        classFeature.setReference((String)attributes.get(YAML_REFERENCE));
        classFeature.setSource((String)attributes.get(YAML_SOURCE));
        classFeature.setClass(getClass((String)attributes.get(YAML_CLASS)));
        classFeature.setConditions((String)attributes.get(YAML_CONDITIONS));
        classFeature.setLevel(Integer.parseInt((String)attributes.get(YAML_LEVEL)));
        classFeature.setAuto("True".equals((String)attributes.get(YAML_AUTO)));
        return classFeature.isValid() ? classFeature : null;
    }



    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof ClassFeature)) {
            return "";
        }
        ClassFeature classFeature = (ClassFeature)entity;
        StringBuffer buf = new StringBuffer();
        String source = classFeature.getSource() == null ? null : getTranslatedText("source." + classFeature.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_CLASS, classFeature.getClass_().getName()));
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        buf.append(generateItemDetail(templateItem, YAML_CONDITIONS, classFeature.getConditions()));
        buf.append(generateItemDetail(templateItem, YAML_LEVEL, String.valueOf(classFeature.getLevel())));
        return String.format(templateList,buf.toString());
    }
}
