package org.pathfinderfr.app.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.ArmorFactory;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassArchetypesFactory;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Equipment;
import org.pathfinderfr.app.database.entity.EquipmentFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.MagicItem;
import org.pathfinderfr.app.database.entity.MagicItemFactory;
import org.pathfinderfr.app.database.entity.Modification;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.database.entity.TraitFactory;
import org.pathfinderfr.app.database.entity.Weapon;
import org.pathfinderfr.app.database.entity.WeaponFactory;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.Triplet;

import java.util.ArrayList;
import java.util.List;

public class MigrationHelper {

    /**
     * Executes sql queries for migrating Character.inventory => CharacterItem[]
     */
    public static void migrateCharacterItems(SQLiteDatabase db) {
        Cursor res =  db.rawQuery( String.format("SELECT id, inventory FROM characters"), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return;
        }
        res.moveToFirst();
        while (!res.isAfterLast()) {
            int index = 0;
            long characterId = res.getLong(res.getColumnIndex("id"));
            String inventory = res.getString(res.getColumnIndex("inventory"));
            if(inventory != null && inventory.length() > 0 ) {
                String[] items = inventory.split("#");
                for(String item : items) {
                    String[] props = item.split("\\|");
                    if (props.length >= 2) {
                        String name = props[0];
                        int weight = 0;
                        try {
                            weight = Integer.parseInt(props[1]);
                        } catch (NumberFormatException nfe) {
                            Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory weight '" + props[1] + "' is invalid (NFE)!");
                        }
                        // item reference was introduced later
                        long objId = 0;
                        if (props.length >= 3) {
                            try {
                                objId = Long.parseLong(props[2]);
                            } catch (NumberFormatException nfe) {
                                Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory référence '" + props[2] + "' is invalid (NFE)!");
                            }
                        }
                        // item additional info (ex: ammo)
                        String infos = null;
                        if (props.length >= 4) {
                            infos = props[3];
                        }
                        // item cost was introduced later
                        long price = 0;
                        if (props.length >= 5) {
                            try {
                                price = Long.parseLong(props[4]);
                            } catch (NumberFormatException nfe) {
                                Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory price '" + props[4] + "' is invalid (NFE)!");
                            }
                        }
                        CharacterItem cItem = new CharacterItem();
                        cItem.setCharacterId(characterId);
                        cItem.setOrder(index);
                        cItem.setName(name);
                        cItem.setPrice(price);
                        cItem.setWeight(weight);
                        cItem.setItemRef(objId);
                        cItem.setAmmo(infos);
                        cItem.setLocation(CharacterItem.LOCATION_NOLOC);
                        ContentValues contentValues = cItem.getFactory().generateContentValuesFromEntity(cItem);
                        db.insert(cItem.getFactory().getTableName(), null, contentValues);
                        index++;
                    }
                }
            }
            res.moveToNext();
        }
        res.close();
    }


    /**
     * Executes sql queries for migrating Character.modifs => Modification[]
     */
    public static void migrateModifs(SQLiteDatabase db) {
        Cursor res =  db.rawQuery( String.format("SELECT id, inventory, modifs FROM characters"), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return;
        }
        res.moveToFirst();
        while (!res.isAfterLast()) {
            int index = 0;
            long characterId = res.getLong(res.getColumnIndex("id"));
            String modifsValue = res.getString(res.getColumnIndex("modifs"));
            String inventoryValue = res.getString(res.getColumnIndex("inventory"));
            // build weapon list
            List<CharacterItem> weapons = new ArrayList<>();
            String[] items = inventoryValue == null ? new String[0] : inventoryValue.split("#");
            for(String item : items) {
                String[] props = item.split("\\|");
                if (props.length >= 3) {
                    try {
                        CharacterItem weapon = new CharacterItem();
                        weapon.setName(props[0]);
                        weapon.setItemRef(Long.parseLong(props[2]));
                        if(weapon.isWeapon()) {
                            weapons.add(weapon);
                        }
                    } catch (NumberFormatException nfe) {
                        Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory référence '" + props[2] + "' is invalid (NFE)!");
                    }
                }
            }
            if (modifsValue != null && modifsValue.length() > 0) {
                for (String modif : modifsValue.split("#")) {
                    String[] modElements = modif.split(":");
                    if (modElements.length >= 3) {
                        String source = modElements[0];
                        String icon = modElements[2];
                        int linkToWeapon = modElements.length >= 4 ? Integer.parseInt(modElements[3]) : 0;
                        List<Pair<Integer, Integer>> bonuses = new ArrayList<>();
                        for (String bonusVal : modElements[1].split(",")) {
                            String[] bonusElements = bonusVal.split("\\|");
                            if (bonusElements.length == 2) {
                                try {
                                    Integer bonusIdx = Integer.parseInt(bonusElements[0]);
                                    Integer bonusValue = Integer.parseInt(bonusElements[1]);
                                    bonuses.add(new Pair<>(bonusIdx, bonusValue));
                                } catch (NumberFormatException nfe) {
                                    Log.e(CharacterFactory.class.getSimpleName(), "Stored modif '" + bonusVal + "' is invalid (NFE)!");
                                }
                            }
                        }
                        Modification modification = new Modification(source, bonuses, icon, false);
                        modification.setCharacterId(characterId);
                        if(linkToWeapon > 0) {
                            // find item ID based on name
                            long itemId = 0;
                            if(linkToWeapon <= weapons.size()) {
                                Cursor resW = db.rawQuery("SELECT id FROM characitems WHERE characterid=? AND name=?", new String[] { String.valueOf(characterId), weapons.get(linkToWeapon-1).getName()});
                                // not found?
                                if(resW.getCount()>=1) {
                                    resW.moveToFirst();
                                    itemId = resW.getLong(res.getColumnIndex("id"));
                                }
                                resW.close();
                            }
                            modification.setItemId(itemId);
                        }
                        ContentValues contentValues = modification.getFactory().generateContentValuesFromEntity(modification);
                        db.insert(modification.getFactory().getTableName(), null, contentValues);
                    }
                }
            }
            res.moveToNext();
        }
        res.close();
    }

    /**
     * Tries to migrate a character data on the latest version (based on names)
     */
    public static List<DBEntity> convert(Character c) {
        List<DBEntity> unmatched = new ArrayList<>();
        DBHelper helper = DBHelper.getInstance(null);
        // ABILITIES
        int skillVersion = helper.getVersion(SkillFactory.FACTORY_ID.toLowerCase());
        List<Long> ids = new ArrayList<>(c.getSkills()); // avoid ConcurrentModificationException
        for(long id : ids) {
            Skill s = (Skill) helper.fetchEntity(id, SkillFactory.getInstance());
            if(s == null) {
                // something went wrong!! remove
                c.setSkillRank(id, 0);
                continue;
            }
            if(s.getVersion() != skillVersion) {
                Skill skillNew = (Skill) helper.fetchEntityByName(s.getName(), SkillFactory.getInstance());
                if (skillNew != null) {
                    int rank = c.getSkillRank(id);
                    c.setSkillRank(id, 0);
                    c.setSkillRank(skillNew.getId(), rank);
                } else {
                    unmatched.add(s);
                }
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
        int raceVersion = helper.getVersion(RaceFactory.FACTORY_ID.toLowerCase());
        if(c.getRace() != null) {
            if(c.getRace().getVersion() != raceVersion) {
                Race newRace = (Race) helper.fetchEntityByName(c.getRaceName(), RaceFactory.getInstance());
                if (newRace != null) {
                    c.getRace().setId(newRace.getId());
                } else {
                    unmatched.add(c.getRace());
                }
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
            if(class_.first.getVersion() != classVersion) {
                Class newClass = (Class) helper.fetchEntityByName(class_.first.getName(), ClassFactory.getInstance());
                if (newClass != null) {
                    class_.first.setId(newClass.getId());
                } else {
                    unmatched.add(class_.first);
                }
                if (class_.second != null) {
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
                // linkedTo??
                if(cf.getLinkedTo() != null) {

                }
            }
        }
        // ITEMS REF
        int armorVersion = DBHelper.getInstance(null).getVersion(ArmorFactory.FACTORY_ID.toLowerCase());
        int weaponVersion = DBHelper.getInstance(null).getVersion(WeaponFactory.FACTORY_ID.toLowerCase());
        int equipVersion = DBHelper.getInstance(null).getVersion(EquipmentFactory.FACTORY_ID.toLowerCase());
        int magicVersion = DBHelper.getInstance(null).getVersion(MagicItemFactory.FACTORY_ID.toLowerCase());
        for(CharacterItem i : c.getInventoryItems()) {
            DBEntity e = helper.fetchObjectEntity(i);
            if(e != null) {
                if(e instanceof Armor) {
                    if(e.getVersion() != armorVersion) {
                        Armor newArmor = (Armor) helper.fetchEntityByName(e.getName(), ArmorFactory.getInstance());
                        if (newArmor != null) {
                            i.setItemRef(CharacterItem.IDX_ARMORS + newArmor.getId());
                        } else {
                            unmatched.add(i);
                        }
                    }
                }
                else if(e instanceof Weapon) {
                    if(e.getVersion() != weaponVersion) {
                        Weapon newWeapon = (Weapon) helper.fetchEntityByName(e.getName(), WeaponFactory.getInstance());
                        if (newWeapon != null) {
                            i.setItemRef(CharacterItem.IDX_WEAPONS + newWeapon.getId());
                        } else {
                            unmatched.add(i);
                        }
                    }
                }
                else if(e instanceof Equipment) {
                    if(e.getVersion() != equipVersion) {
                        Equipment newEquip = (Equipment) helper.fetchEntityByName(e.getName(), EquipmentFactory.getInstance());
                        if (newEquip != null) {
                            i.setItemRef(CharacterItem.IDX_EQUIPMENT + newEquip.getId());
                        } else {
                            unmatched.add(i);
                        }
                    }
                }
                else if(e instanceof MagicItem) {
                    if(e.getVersion() != magicVersion) {
                        MagicItem newMagic = (MagicItem) helper.fetchEntityByName(e.getName(), MagicItemFactory.getInstance());
                        if (newMagic != null) {
                            i.setItemRef(CharacterItem.IDX_MAGICITEM + newMagic.getId());
                        } else {
                            unmatched.add(i);
                        }
                    }
                }
                else {
                    throw new IllegalStateException("Invalid entity : " + e.getClass().getSimpleName());
                }
            }
        }
        return unmatched;
    }
}
