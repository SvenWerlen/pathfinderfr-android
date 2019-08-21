package org.pathfinderfr.app.database.entity;

public class EntityFactories {

    public static final DBEntityFactory[] FACTORIES = new DBEntityFactory[] {
            ArmorFactory.getInstance(),
            CharacterFactory.getInstance(),
            ClassArchetypesFactory.getInstance(),
            ClassFactory.getInstance(),
            ClassFeatureFactory.getInstance(),
            ConditionFactory.getInstance(),
            EquipmentFactory.getInstance(),
            FavoriteFactory.getInstance(),
            FeatFactory.getInstance(),
            MagicItemFactory.getInstance(),
            RaceFactory.getInstance(),
            SkillFactory.getInstance(),
            SpellFactory.getInstance(),
            TraitFactory.getInstance(),
            WeaponFactory.getInstance()
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
