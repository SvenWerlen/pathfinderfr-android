package org.pathfinderfr.app.database.entity;

public class Condition extends DBEntity {

    @Override
    public DBEntityFactory getFactory() {
        return ConditionFactory.getInstance();
    }

}
