package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

public class RaceFactory extends DBEntityFactory {

    public static final String FACTORY_ID         = "RACES";

    private static final String TABLENAME         = "races";
    private static final String COLUMN_TRAITS     = "traits";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";
    private static final String YAML_TRAITS       = "Traits";

    private static RaceFactory instance;

    private RaceFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized RaceFactory getInstance() {
        if (instance == null) {
            instance = new RaceFactory();
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
                        "%s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_TRAITS);
        return query;
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Race)) {
            return null;
        }
        Race race = (Race) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(RaceFactory.COLUMN_NAME, race.getName());
        contentValues.put(RaceFactory.COLUMN_DESC, race.getDescription());
        contentValues.put(RaceFactory.COLUMN_REFERENCE, race.getReference());
        contentValues.put(RaceFactory.COLUMN_SOURCE, race.getSource());

        StringBuffer buf = new StringBuffer();
        for(Race.Trait t : race.getTraits()) {
            buf.append(t.getName()).append(':');
            buf.append(t.getDescription());
            buf.append('#');
        }
        if(buf.length() > 0) {
            buf.deleteCharAt(buf.length()-1);
        }
        contentValues.put(RaceFactory.COLUMN_TRAITS, buf.toString());
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Race race = new Race();

        race.setId(resource.getLong(resource.getColumnIndex(RaceFactory.COLUMN_ID)));
        race.setName(extractValue(resource,RaceFactory.COLUMN_NAME));
        race.setDescription(extractValue(resource,RaceFactory.COLUMN_DESC));
        race.setReference(extractValue(resource,RaceFactory.COLUMN_REFERENCE));
        race.setSource(extractValue(resource,RaceFactory.COLUMN_SOURCE));
        String traitsValue = extractValue(resource,RaceFactory.COLUMN_TRAITS);
        if(traitsValue != null && traitsValue.length() > 0) {
            String[] traits = traitsValue.split("#");
            for(String trait : traits) {
                String[] props = trait.split(":");
                if(props.length < 2) {
                    continue;
                }
                // description could have multiple ':'
                StringBuffer descr = new StringBuffer();
                for(int i = 1; i<props.length; i++) {
                    descr.append(props[i]).append(':');
                }
                descr.deleteCharAt(descr.length()-1);
                race.getTraits().add(new Race.Trait(props[0], descr.toString()));
            }
        }
        return race;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Race race = new Race();
        race.setName((String)attributes.get(YAML_NAME));
        race.setDescription((String)attributes.get(YAML_DESC));
        race.setReference((String)attributes.get(YAML_REFERENCE));
        race.setSource((String)attributes.get(YAML_SOURCE));
        Object traits = attributes.get(YAML_TRAITS);
        if(traits instanceof List) {
            List<Object> list = (List<Object>)traits;
            for(Object t : list) {
                Map<String,String> map = (Map<String,String>)t;
                race.getTraits().add(new Race.Trait(map.get(YAML_NAME),map.get(YAML_DESC)));
            }
        }
        return race.isValid() ? race : null;
    }


    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Race)) {
            return "";
        }
        Race race = (Race)entity;
        StringBuffer buf = new StringBuffer();
        String source = race.getSource() == null ? null : getTranslatedText("source." + race.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        for(Race.Trait t : race.getTraits()) {
            buf.append(generateItemDetail(templateItem, t.getName(), t.getDescription()));
        }
        return String.format(templateList,buf.toString());
    }
}
