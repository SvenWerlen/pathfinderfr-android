package org.pathfinderfr.app.database.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.util.Map;
import java.util.Properties;

public class MagicItemFactory extends DBEntityFactory {

    public static final String FACTORY_ID             = "MAGIC";

    private static final String TABLENAME             = "magic";
    private static final String COLUMN_TYPE           = "type";
    private static final String COLUMN_COST           = "cost";
    private static final String COLUMN_LOCATION       = "location";
    private static final String COLUMN_WEIGHT         = "weight";
    private static final String COLUMN_AURA           = "aura";
    private static final String COLUMN_SCL            = "SCL";
    private static final String COLUMN_MANUF_REQ      = "mreq";
    private static final String COLUMN_MANUF_COST     = "mcost";

    private static final String YAML_NAME           = "Nom";
    private static final String YAML_DESC           = "Description";
    private static final String YAML_REFERENCE      = "Référence";
    private static final String YAML_SOURCE         = "Source";
    private static final String YAML_TYPE           = "Type";
    private static final String YAML_COST           = "Prix";
    private static final String YAML_LOCATION       = "Emplacement";
    private static final String YAML_WEIGHT         = "Poids";
    private static final String YAML_AURA           = "Aura";
    private static final String YAML_SCL            = "NLS";
    private static final String YAML_MANUF_REQ      = "Conditions";
    private static final String YAML_MANUF_COST     = "Coût";


    private static MagicItemFactory instance;

    private MagicItemFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized MagicItemFactory getInstance() {
        if (instance == null) {
            instance = new MagicItemFactory();
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
                        "%s integer, %s text, %s text, %s text, " +
                        "%s text, %s integer, %s text, %s text " +
                        ")",
                TABLENAME, COLUMN_ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_REFERENCE, COLUMN_SOURCE,
                COLUMN_TYPE, COLUMN_COST, COLUMN_LOCATION, COLUMN_WEIGHT,
                COLUMN_AURA, COLUMN_SCL, COLUMN_MANUF_REQ, COLUMN_MANUF_COST);

        return query;
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
        return String.format("SELECT %s,%s,%s FROM %s %s ORDER BY %s COLLATE UNICODE",
                COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, getTableName(), filters, COLUMN_NAME);
    }

    @Override
    public ContentValues generateContentValuesFromEntity(@NonNull DBEntity entity) {
        if (!(entity instanceof MagicItem)) {
            return null;
        }
        MagicItem item = (MagicItem) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MagicItemFactory.COLUMN_NAME, item.getName());
        contentValues.put(MagicItemFactory.COLUMN_DESC, item.getDescription());
        contentValues.put(MagicItemFactory.COLUMN_REFERENCE, item.getReference());
        contentValues.put(MagicItemFactory.COLUMN_SOURCE, item.getSource());
        contentValues.put(MagicItemFactory.COLUMN_TYPE, item.getType());
        contentValues.put(MagicItemFactory.COLUMN_COST, item.getCost());
        contentValues.put(MagicItemFactory.COLUMN_LOCATION, item.getLocation());
        contentValues.put(MagicItemFactory.COLUMN_WEIGHT, item.getWeight());
        contentValues.put(MagicItemFactory.COLUMN_AURA, item.getAura());
        contentValues.put(MagicItemFactory.COLUMN_SCL, item.getSpellCasterLevel());
        contentValues.put(MagicItemFactory.COLUMN_MANUF_REQ, item.getManufacturingReq());
        contentValues.put(MagicItemFactory.COLUMN_MANUF_COST, item.getManufacturingCost());

        return contentValues;
    }


    @Override
    public DBEntity generateEntity(@NonNull Cursor resource) {
        MagicItem item = new MagicItem();

        item.setId(resource.getLong(resource.getColumnIndex(MagicItemFactory.COLUMN_ID)));
        item.setName(extractValue(resource, MagicItemFactory.COLUMN_NAME));
        item.setDescription(extractValue(resource, MagicItemFactory.COLUMN_DESC));
        item.setReference(extractValue(resource, MagicItemFactory.COLUMN_REFERENCE));
        item.setSource(extractValue(resource, MagicItemFactory.COLUMN_SOURCE));
        item.setType(extractValueAsInt(resource, MagicItemFactory.COLUMN_TYPE));
        item.setCost(extractValue(resource, MagicItemFactory.COLUMN_COST));
        item.setLocation(extractValue(resource, MagicItemFactory.COLUMN_LOCATION));
        item.setWeight(extractValue(resource, MagicItemFactory.COLUMN_WEIGHT));
        item.setAura(extractValue(resource, MagicItemFactory.COLUMN_AURA));
        item.setSpellCasterLevel(extractValueAsInt(resource, MagicItemFactory.COLUMN_SCL));
        item.setManufacturingReq(extractValue(resource, MagicItemFactory.COLUMN_MANUF_REQ));
        item.setManufacturingCost(extractValue(resource, MagicItemFactory.COLUMN_MANUF_COST));
        return item;
    }

    @Override
    public DBEntity generateEntity(@NonNull Map<String, Object> attributes) {
        MagicItem item = new MagicItem();
        item.setName((String)attributes.get(YAML_NAME));
        item.setDescription((String)attributes.get(YAML_DESC));
        item.setReference((String)attributes.get(YAML_REFERENCE));
        item.setSource((String)attributes.get(YAML_SOURCE));
        String type = (String)attributes.get(YAML_TYPE);
        if("Armure/Bouclier".equals(type)) {
            item.setType(MagicItem.TYPE_ARMOR_SHIELD);
        } else if("Arme".equals(type)) {
            item.setType(MagicItem.TYPE_WEAPON);
        } else if("Potion".equals(type)) {
            item.setType(MagicItem.TYPE_POTION);
        } else if("Anneau".equals(type)) {
            item.setType(MagicItem.TYPE_RING);
        } else if("Sceptre".equals(type)) {
            item.setType(MagicItem.TYPE_SCEPTER);
        } else if("Parchemin".equals(type)) {
            item.setType(MagicItem.TYPE_PARCHMENT);
        } else if("Bâton".equals(type)) {
            item.setType(MagicItem.TYPE_STAFF);
        } else if("Baguette".equals(type)) {
            item.setType(MagicItem.TYPE_WAND);
        } else if("Objet merveilleux".equals(type)) {
            item.setType(MagicItem.TYPE_OBJECT);
        }
        item.setCost((String)attributes.get(YAML_COST));
        item.setLocation((String)attributes.get(YAML_LOCATION));
        item.setWeight((String)attributes.get(YAML_WEIGHT));
        item.setAura((String)attributes.get(YAML_AURA));
        if(attributes.containsKey(YAML_SCL)) {
            item.setSpellCasterLevel(Integer.parseInt((String) attributes.get(YAML_SCL)));
        }
        item.setManufacturingReq((String)attributes.get(YAML_MANUF_REQ));
        item.setManufacturingCost((String)attributes.get(YAML_MANUF_COST));

        return item.isValid() ? item : null;
    }

    @Override
    public String generateDetails(@NonNull DBEntity entity, @NonNull String templateList, @NonNull String templateItem) {
        return "";
    }

    @Override
    public String gerenateHTMLContent(@NonNull DBEntity entity) {
        if(!(entity instanceof MagicItem)) {
            return "";
        }
        MagicItem item = (MagicItem)entity;
        StringBuffer buf = new StringBuffer();
        String source = item.getSource() == null ? null : getTranslatedText("source." + item.getSource().toLowerCase());

        Properties cfg =  ConfigurationUtil.getInstance(null).getProperties();
        String tmplName = cfg.getProperty("template.magicitem.name");
        String tmplSect = cfg.getProperty("template.magicitem.section");
        String tmplProp = cfg.getProperty("template.magicitem.prop");

        // prepare header + props
        StringBuffer props = new StringBuffer();
        if(item.getAura() != null) {
            props.append(String.format(tmplProp, MagicItemFactory.YAML_AURA, item.getAura()));
        }
        if(item.getSpellCasterLevel() > 0) {
            props.append(String.format(tmplProp, MagicItemFactory.YAML_SCL, item.getSpellCasterLevel()));
        }
        if(item.getLocation() != null) {
            props.append(String.format(tmplProp, MagicItemFactory.YAML_LOCATION, item.getLocation()));
        }
        if(item.getCost() != null) {
            props.append(String.format(tmplProp, MagicItemFactory.YAML_COST, item.getCost()));
        }
        if(item.getWeight() != null) {
            props.append(String.format(tmplProp, MagicItemFactory.YAML_WEIGHT,  item.getWeight()));
        }
        buf.append(String.format(tmplName, item.getName(), cfg.getProperty("magicitem.type." + item.getType()), props.toString()));

        // prepare description
        if(item.getDescription() != null && item.getDescription().length() > 0) {
            buf.append(String.format(tmplSect, "DESCRIPTION", item.getDescription().replaceAll("\n", "<br />")));
        }

        // prepare manufacturing details
        StringBuffer manuf = new StringBuffer();
        if(item.getManufacturingReq() != null) {
            manuf.append(String.format(tmplProp, MagicItemFactory.YAML_MANUF_REQ, item.getManufacturingReq()));
            manuf.append("<br />");
        }
        if(item.getManufacturingCost() != null) {
            manuf.append(String.format(tmplProp, MagicItemFactory.YAML_MANUF_COST,  item.getManufacturingCost()));
        }
        if(manuf.length() > 0) {
            buf.append(String.format(tmplSect, "FABRICATION", manuf.toString()));
        }

        return buf.toString();
    }
}
