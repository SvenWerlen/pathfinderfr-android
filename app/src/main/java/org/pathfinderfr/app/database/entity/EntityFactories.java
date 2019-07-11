package org.pathfinderfr.app.database.entity;

public class EntityFactories {

    public static final DBEntityFactory[] FACTORIES = new DBEntityFactory[] {
            SkillFactory.getInstance(),
            FeatFactory.getInstance(),
            ClassFeatureFactory.getInstance(),
            SpellFactory.getInstance(),
            FavoriteFactory.getInstance(),
            RaceFactory.getInstance(),
            ClassFactory.getInstance(),
            CharacterFactory.getInstance(),
            ConditionFactory.getInstance(),
            WeaponFactory.getInstance(),
            ArmorFactory.getInstance(),
            EquipmentFactory.getInstance(),
            RaceAlternateTraitFactory.getInstance(),
            ClassArchetypesFactory.getInstance()
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
