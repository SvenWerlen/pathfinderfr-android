package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraitFactory extends DBEntityFactory {

    public static final String FACTORY_ID         = "TRAITS";

    private static final String TABLENAME         = "racealttraits"; // old name (cannot be rename due for compatibility reasons)
    private static final String COLUMN_RACE       = "race";
    private static final String COLUMN_REPLACES   = "replaces";
    private static final String COLUMN_ALTERS     = "alters";

    private static final String YAML_NAME         = "Nom";
    private static final String YAML_DESC         = "Description";
    private static final String YAML_REFERENCE    = "Référence";
    private static final String YAML_SOURCE       = "Source";
    private static final String YAML_RACE         = "Race";
    private static final String YAML_REPLACES     = "Remplace";
    private static final String YAML_ALTERS       = "Modifie";

    private static TraitFactory instance;

    private Map<Long, Race> racesById;
    private Map<String, Race> racesByName;

    private TraitFactory() {
        racesById = new HashMap<>();
        racesByName = new HashMap<>();
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized TraitFactory getInstance() {
        if (instance == null) {
            instance = new TraitFactory();
        }
        return instance;
    }

    private synchronized Race getRace(String name) {
        if(racesByName.size() == 0) {
            racesById.clear();
            List<DBEntity> fullList = DBHelper.getInstance(null).getAllEntities(RaceFactory.getInstance());
            for(DBEntity e : fullList) {
                racesById.put(e.getId(), (Race)e);
                racesByName.put(e.getName(), (Race)e);
            }
        }
        return racesByName.get(name);
    }

    private synchronized Race getRace(long id) {
        if(racesById.size() == 0) {
            racesByName.clear();
            List<DBEntity> fullList = DBHelper.getInstance(null).getAllEntities(RaceFactory.getInstance());
            for(DBEntity e : fullList) {
                racesById.put(e.getId(), (Race)e);
                racesByName.put(e.getName(), (Race)e);
            }
        }
        return racesById.get(id);
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
                        "%s integer, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID, COLUMN_VERSION,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_RACE, COLUMN_REPLACES, COLUMN_ALTERS);
        return query;
    }

    /**
     * @return the query to fetch all entities (including fields required for filtering)
     */
    @Override
    public String getQueryFetchAll(Integer version, String... sources) {
        return String.format("SELECT %s,%s,%s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_RACE, COLUMN_REPLACES, COLUMN_ALTERS, getTableName(), getFilters(version, sources), COLUMN_NAME);
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof Trait)) {
            return null;
        }
        Trait raceAltTrait = (Trait) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassArchetypesFactory.COLUMN_VERSION, raceAltTrait.getVersion());
        contentValues.put(TraitFactory.COLUMN_NAME, raceAltTrait.getName());
        contentValues.put(TraitFactory.COLUMN_DESC, raceAltTrait.getDescription());
        contentValues.put(TraitFactory.COLUMN_REFERENCE, raceAltTrait.getReference());
        contentValues.put(TraitFactory.COLUMN_SOURCE, raceAltTrait.getSource());
        if(raceAltTrait.getRace() != null) {
            contentValues.put(TraitFactory.COLUMN_RACE, raceAltTrait.getRace().getId());
        }
        contentValues.put(TraitFactory.COLUMN_REPLACES, StringUtil.listToString(raceAltTrait.getReplaces(),"#"));
        contentValues.put(TraitFactory.COLUMN_ALTERS, StringUtil.listToString(raceAltTrait.getAlters(),"#"));
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        Trait trait = new Trait();

        trait.setId(resource.getLong(resource.getColumnIndex(TraitFactory.COLUMN_ID)));
        trait.setVersion(extractValueAsInt(resource, TraitFactory.COLUMN_VERSION));
        trait.setName(extractValue(resource, TraitFactory.COLUMN_NAME));
        trait.setDescription(extractValue(resource, TraitFactory.COLUMN_DESC));
        trait.setReference(extractValue(resource, TraitFactory.COLUMN_REFERENCE));
        trait.setSource(extractValue(resource, TraitFactory.COLUMN_SOURCE));
        trait.setRace(getRace(extractValueAsInt(resource, TraitFactory.COLUMN_RACE)));

        String replaces = extractValue(resource, TraitFactory.COLUMN_REPLACES);
        String[] replacesList = replaces == null ? new String[]{} : replaces.split("#");
        for(String repEl : replacesList) {
            trait.replaces(repEl);
        }

        String alters = extractValue(resource, TraitFactory.COLUMN_ALTERS);
        String[] altersList = alters == null ? new String[]{} : alters.split("#");
        for(String altEl : altersList) {
            trait.alters(altEl);
        }
        return trait;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        Trait trait = new Trait();
        trait.setName((String)attributes.get((String)YAML_NAME));
        trait.setDescription((String)attributes.get(YAML_DESC));
        trait.setReference((String)attributes.get(YAML_REFERENCE));
        trait.setSource((String)attributes.get(YAML_SOURCE));
        trait.setRace(getRace((String)attributes.get(YAML_RACE)));

        Object replaces = attributes.get(YAML_REPLACES);
        if(replaces instanceof List) {
            List<Object> list = (List<Object>)replaces;
            for(Object t : list) {
                trait.replaces(t.toString());
            }
        }

        Object alters = attributes.get(YAML_ALTERS);
        if(alters instanceof List) {
            List<Object> list = (List<Object>)alters;
            for(Object t : list) {
                trait.alters(t.toString());
            }
        }

        return trait.isValid() ? trait : null;
    }



    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof Trait)) {
            return "";
        }
        Trait trait = (Trait)entity;
        StringBuffer buf = new StringBuffer();
        String source = trait.getSource() == null ? null : getTranslatedText("source." + trait.getSource().toLowerCase());
        if(trait.getRace() != null) {
            buf.append(generateItemDetail(templateItem, YAML_RACE, trait.getRace().getName()));
        }
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        if(trait.getReplaces().size()>0) {
            buf.append(generateItemDetail(templateItem, YAML_REPLACES, trait.getReplaces()));
        }
        if(trait.getAlters().size()>0) {
            buf.append(generateItemDetail(templateItem, YAML_ALTERS, trait.getAlters()));
        }
        return String.format(templateList,buf.toString());
    }
}
