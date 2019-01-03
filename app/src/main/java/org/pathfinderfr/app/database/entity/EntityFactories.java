package org.pathfinderfr.app.database.entity;

import java.util.List;

public class EntityFactories {

    public static final DBEntityFactory[] FACTORIES = new DBEntityFactory[] {
            FeatFactory.getInstance(),
            SpellFactory.getInstance(),
            FavoriteFactory.getInstance(),
    };


    public static DBEntityFactory getFactoryById(String id) {
        for(DBEntityFactory f: FACTORIES) {
            if(f.getFactoryId().equalsIgnoreCase(id)) {
                return f;
            }
        }
        // not found
        return null;
    }
}
