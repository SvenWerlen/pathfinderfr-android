package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassArchetypesFactory extends DBEntityFactory {

    public static final String FACTORY_ID        = "CLASSARCHETYPES";

    private static final String TABLENAME         = "classarchetypes";
    private static final String COLUMN_CLASS      = "class";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";
    private static final String YAML_CLASS        = "Classe";

    private static ClassArchetypesFactory instance;

    private Map<Long, Class> classesById;
    private Map<String, Class> classesByName;

    private ClassArchetypesFactory() {
        classesById = new HashMap<>();
        classesByName = new HashMap<>();
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized ClassArchetypesFactory getInstance() {
        if (instance == null) {
            instance = new ClassArchetypesFactory();
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
                        "%s integer" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_CLASS);
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
        return String.format("SELECT %s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_CLASS, getTableName(), filters, COLUMN_NAME);
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof ClassArchetype)) {
            return null;
        }
        ClassArchetype archetype = (ClassArchetype) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassArchetypesFactory.COLUMN_NAME, archetype.getName());
        contentValues.put(ClassArchetypesFactory.COLUMN_DESC, archetype.getDescription());
        contentValues.put(ClassArchetypesFactory.COLUMN_REFERENCE, archetype.getReference());
        contentValues.put(ClassArchetypesFactory.COLUMN_SOURCE, archetype.getSource());
        contentValues.put(ClassArchetypesFactory.COLUMN_CLASS, archetype.getClass_().getId());
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        ClassArchetype archetype = new ClassArchetype();

        archetype.setId(resource.getLong(resource.getColumnIndex(ClassArchetypesFactory.COLUMN_ID)));
        archetype.setName(extractValue(resource, ClassArchetypesFactory.COLUMN_NAME));
        archetype.setDescription(extractValue(resource, ClassArchetypesFactory.COLUMN_DESC));
        archetype.setReference(extractValue(resource, ClassArchetypesFactory.COLUMN_REFERENCE));
        archetype.setSource(extractValue(resource, ClassArchetypesFactory.COLUMN_SOURCE));
        archetype.setClass(getClass(extractValueAsInt(resource, ClassArchetypesFactory.COLUMN_CLASS)));
        return archetype;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        ClassArchetype archetype = new ClassArchetype();
        archetype.setName((String)attributes.get((String)YAML_NAME));
        archetype.setDescription((String)attributes.get(YAML_DESC));
        archetype.setReference((String)attributes.get(YAML_REFERENCE));
        archetype.setSource((String)attributes.get(YAML_SOURCE));
        archetype.setClass(getClass((String)attributes.get(YAML_CLASS)));
        return archetype.isValid() ? archetype : null;
    }



    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof ClassArchetype)) {
            return "";
        }
        ClassArchetype archetype = (ClassArchetype)entity;
        StringBuffer buf = new StringBuffer();
        String source = archetype.getSource() == null ? null : getTranslatedText("source." + archetype.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_CLASS, archetype.getClass_().getName()));
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        return String.format(templateList,buf.toString());
    }
}
