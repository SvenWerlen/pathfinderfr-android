package org.pathfinderfr.app.database.entity;

import java.util.List;

public class EntityFactories {

    private static DBEntityFactory[] factories = new DBEntityFactory[] { SpellFactory.getInstance() };


    public static DBEntityFactory getFactoryById(String id) {
        for(DBEntityFactory f: factories) {
            if(f.getFactoryId().equalsIgnoreCase(id)) {
                return f;
            }
        }
        // not found
        return null;
    }
}
