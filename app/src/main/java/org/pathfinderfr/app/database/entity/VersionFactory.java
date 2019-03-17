package org.pathfinderfr.app.database.entity;

import org.pathfinderfr.app.util.StringUtil;

public class VersionFactory {

    private static final String TABLENAME         = "versions";
    private static final String COLUMN_DATAID     = "dataid";
    private static final String COLUMN_VERSION    = "version";


    private static VersionFactory instance;

    private VersionFactory() {
    }

    /**
     * @return then unique instance of that factory
     */
    public static synchronized VersionFactory getInstance() {
        if (instance == null) {
            instance = new VersionFactory();
        }
        return instance;
    }

    public String getTableName() {
        return VersionFactory.TABLENAME;
    }

    public String getQueryCreateTable() {
        String query = String.format( "CREATE TABLE IF NOT EXISTS %s (%s text, %s integer)",
                TABLENAME, COLUMN_DATAID, COLUMN_VERSION);
        return query;
    }

    /**
     * @return the query to fetch a version
     */
    public String getQueryFetchVersion(String dataId) {
        String filters = "";
        return String.format("SELECT %s as version FROM %s WHERE %s='%s'",
                COLUMN_VERSION, TABLENAME, COLUMN_DATAID, dataId);
    }

    /**
     * @return the query to insert a version
     */
    public String getQueryInsertVersion(String dataId, int version) {
        String filters = "";
        return String.format("INSERT INTO %s (%s, %s) VALUES('%s', %d)",
                TABLENAME, COLUMN_DATAID, COLUMN_VERSION, dataId, version);
    }

    /**
     * @return the query to update a version
     */
    public String getQueryUpdateVersion(String dataId, int version) {
        String filters = "";
        return String.format("UPDATE %s SET %s=%d WHERE %s='%s'",
                TABLENAME, COLUMN_VERSION, version, COLUMN_DATAID, dataId);
    }
}
