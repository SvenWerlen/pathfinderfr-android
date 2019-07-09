package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaceAlternateTraitFactory extends DBEntityFactory {

    public static final String FACTORY_ID         = "RACEALTTRAITS";

    private static final String TABLENAME         = "racealttraits";
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

    private static RaceAlternateTraitFactory instance;

    private Map<Long, Race> racesById;
    private Map<String, Race> racesByName;

    private RaceAlternateTraitFactory() {
        racesById = new HashMap<>();
        racesByName = new HashMap<>();
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized RaceAlternateTraitFactory getInstance() {
        if (instance == null) {
            instance = new RaceAlternateTraitFactory();
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
                        "%s text, %s text, %s text, %s text," +
                        "%s integer, %s text, %s text" +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_RACE, COLUMN_REPLACES, COLUMN_ALTERS);
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
                COLUMN_ID, COLUMN_NAME, COLUMN_RACE, COLUMN_REPLACES, COLUMN_ALTERS, getTableName(), filters, COLUMN_NAME);
    }


    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof RaceAlternateTrait)) {
            return null;
        }
        RaceAlternateTrait raceAltTrait = (RaceAlternateTrait) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(RaceAlternateTraitFactory.COLUMN_NAME, raceAltTrait.getName());
        contentValues.put(RaceAlternateTraitFactory.COLUMN_DESC, raceAltTrait.getDescription());
        contentValues.put(RaceAlternateTraitFactory.COLUMN_REFERENCE, raceAltTrait.getReference());
        contentValues.put(RaceAlternateTraitFactory.COLUMN_SOURCE, raceAltTrait.getSource());
        contentValues.put(RaceAlternateTraitFactory.COLUMN_RACE, raceAltTrait.getRace().getId());
        contentValues.put(RaceAlternateTraitFactory.COLUMN_REPLACES, StringUtil.listToString(raceAltTrait.getReplaces(),"#"));
        contentValues.put(RaceAlternateTraitFactory.COLUMN_ALTERS, StringUtil.listToString(raceAltTrait.getAlters(),"#"));
        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        RaceAlternateTrait raceAltTrait = new RaceAlternateTrait();

        raceAltTrait.setId(resource.getLong(resource.getColumnIndex(RaceAlternateTraitFactory.COLUMN_ID)));
        raceAltTrait.setName(extractValue(resource, RaceAlternateTraitFactory.COLUMN_NAME));
        raceAltTrait.setDescription(extractValue(resource, RaceAlternateTraitFactory.COLUMN_DESC));
        raceAltTrait.setReference(extractValue(resource, RaceAlternateTraitFactory.COLUMN_REFERENCE));
        raceAltTrait.setSource(extractValue(resource, RaceAlternateTraitFactory.COLUMN_SOURCE));
        raceAltTrait.setRace(getRace(extractValueAsInt(resource, RaceAlternateTraitFactory.COLUMN_RACE)));

        String replaces = extractValue(resource, RaceAlternateTraitFactory.COLUMN_REPLACES);
        String[] replacesList = replaces == null ? new String[]{} : replaces.split("#");
        for(String repEl : replacesList) {
            raceAltTrait.replaces(repEl);
        }

        String alters = extractValue(resource, RaceAlternateTraitFactory.COLUMN_ALTERS);
        String[] altersList = replaces == null ? new String[]{} : replaces.split("#");
        for(String altEl : altersList) {
            raceAltTrait.alters(altEl);
        }
        return raceAltTrait;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        RaceAlternateTrait raceAltTrait = new RaceAlternateTrait();
        raceAltTrait.setName((String)attributes.get((String)YAML_NAME));
        raceAltTrait.setDescription((String)attributes.get(YAML_DESC));
        raceAltTrait.setReference((String)attributes.get(YAML_REFERENCE));
        raceAltTrait.setSource((String)attributes.get(YAML_SOURCE));
        raceAltTrait.setRace(getRace((String)attributes.get(YAML_RACE)));

        Object replaces = attributes.get(YAML_REPLACES);
        if(replaces instanceof List) {
            List<Object> list = (List<Object>)replaces;
            for(Object t : list) {
                raceAltTrait.replaces(t.toString());
            }
        }

        Object alters = attributes.get(YAML_ALTERS);
        if(alters instanceof List) {
            List<Object> list = (List<Object>)alters;
            for(Object t : list) {
                raceAltTrait.alters(t.toString());
            }
        }

        return raceAltTrait.isValid() ? raceAltTrait : null;
    }



    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        if(!(entity instanceof RaceAlternateTrait)) {
            return "";
        }
        RaceAlternateTrait raceAltTrait = (RaceAlternateTrait)entity;
        StringBuffer buf = new StringBuffer();
        String source = raceAltTrait.getSource() == null ? null : getTranslatedText("source." + raceAltTrait.getSource().toLowerCase());
        buf.append(generateItemDetail(templateItem, YAML_RACE, raceAltTrait.getRace().getName()));
        buf.append(generateItemDetail(templateItem, YAML_SOURCE, source));
        buf.append(generateItemDetail(templateItem, YAML_DESC, raceAltTrait.getDescription()));
        return String.format(templateList,buf.toString());
    }
}
