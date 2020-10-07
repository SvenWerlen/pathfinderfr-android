package org.pathfinderfr.app.util;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassArchetypesFactory;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.database.entity.TraitFactory;

import java.util.ArrayList;
import java.util.List;

public class CharacterMigrationUtil {

    public static List<DBEntity> convert(Character c) {
        List<DBEntity> unmatched = new ArrayList<>();
        DBHelper helper = DBHelper.getInstance(null);
        // ABILITIES
        List<Long> ids = new ArrayList<>(c.getSkills()); // avoid ConcurrentModificationException
        for(long id : ids) {
            Skill s = (Skill) helper.fetchEntity(id, SkillFactory.getInstance());
            if(s == null) {
                // something went wrong!! remove
                c.setSkillRank(id, 0);
                continue;
            }
            Skill skillNew = (Skill) helper.fetchEntityByName(s.getName(), SkillFactory.getInstance());
            if(skillNew != null) {
                int rank = c.getSkillRank(id);
                c.setSkillRank(id, 0);
                c.setSkillRank(skillNew.getId(), rank);
            } else {
                unmatched.add(s);
            }
        }

        // FEATS
        int featVersion = helper.getVersion(FeatFactory.FACTORY_ID.toLowerCase());
        for(Feat f : c.getFeats()) {
            if(f.getVersion() != featVersion) {
                Feat newFeat = (Feat) helper.fetchEntityByName(f.getName(), FeatFactory.getInstance());
                if(newFeat != null) {
                    f.setId(newFeat.getId());
                } else {
                    unmatched.add(f);
                }
            }
        }
        // RACE
        if(c.getRace() != null) {
            int raceVersion = helper.getVersion(RaceFactory.FACTORY_ID.toLowerCase());
            Race newRace = (Race) helper.fetchEntityByName(c.getRaceName(), RaceFactory.getInstance());
            if(newRace != null) {
                c.getRace().setId(newRace.getId());
            } else {
                unmatched.add(c.getRace());
            }
        }
        // TRAITS
        int traitVersion = helper.getVersion(TraitFactory.FACTORY_ID.toLowerCase());
        for(Trait t : c.getTraits()) {
            if(t.getVersion() != traitVersion) {
                Trait newTrait = (Trait) helper.fetchEntityByName(t.getName(), TraitFactory.getInstance());
                if(newTrait != null) {
                    t.setId(newTrait.getId());
                } else {
                    unmatched.add(t);
                }
            }
        }
        // CLASSES && CLASS ARCHETYPE
        int classVersion = helper.getVersion(ClassFactory.FACTORY_ID.toLowerCase());
        int classesCount = c.getClassesCount();
        for(int i=0; i<classesCount; i++) {
            Triplet<Class, ClassArchetype, Integer> class_ = c.getClass(i);
            Class newClass = (Class) helper.fetchEntityByName(class_.first.getName(), ClassFactory.getInstance());
            if(newClass != null) {
                class_.first.setId(newClass.getId());
            } else {
                unmatched.add(class_.first);
            }
            if(class_.second != null) {
                List<DBEntity> archetypes = helper.fetchAllEntitiesByName(class_.second.getName(), ClassArchetypesFactory.getInstance());
                ClassArchetype newArchetype = null;
                for (DBEntity e : archetypes) {
                    if (class_.second.equals(e)) {
                        newArchetype = (ClassArchetype) e;
                        break;
                    }
                }
                if (newArchetype != null) {
                    class_.second.setId(newArchetype.getId());
                } else {
                    unmatched.add(class_.second);
                }
            }
        }
        // SPELLS
        int spellVersion = helper.getVersion(SpellFactory.FACTORY_ID.toLowerCase());
        for(Spell s : c.getSpells()) {
            if(s.getVersion() != spellVersion) {
                Spell newSpell = (Spell)helper.fetchEntityByName(s.getName(), SpellFactory.getInstance());
                if(newSpell != null) {
                    s.setId(newSpell.getId());
                } else {
                    unmatched.add(s);
                }
            }
        }
        // CLASSFEATURES
        int classFeatureVersion = DBHelper.getInstance(null).getVersion(ClassFeatureFactory.FACTORY_ID.toLowerCase());
        for(ClassFeature cf : c.getClassFeatures()) {
            if(cf.getVersion() != classFeatureVersion) {
                List<DBEntity> features = helper.fetchAllEntitiesByName(cf.getName(), ClassFeatureFactory.getInstance());
                ClassFeature newFeature = null;
                for(DBEntity e : features) {
                    if(cf.equals(e)) {
                        newFeature = (ClassFeature)e;
                        break;
                    }
                }
                if(newFeature != null) {
                    cf.setId(newFeature.getId());
                } else {
                    unmatched.add(cf);
                }
            }
        }
        return unmatched;
    }
}
