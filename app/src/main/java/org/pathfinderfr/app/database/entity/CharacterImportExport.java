package org.pathfinderfr.app.database.entity;

import android.content.Context;
import android.view.View;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import org.pathfinderfr.app.character.FragmentHitPointsPicker;
import org.pathfinderfr.app.character.FragmentSpeedPicker;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.Pair;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CharacterImportExport {

    private static final String YAML_NAME          = "Nom";
    private static final String YAML_RACE          = "Race";
    private static final String YAML_HP            = "PointsVie";
    private static final String YAML_SPEED         = "Vitesse";
    private static final String YAML_CLASSES       = "Classes";
    private static final String YAML_ARCHETYPE     = "Archetype";
    private static final String YAML_LEVEL         = "Niveau";
    private static final String YAML_TRAITS        = "TraitsAlternatifs";
    private static final String YAML_ABILITIES     = "Caracs";
    private static final String YAML_ABILITY_STR   = "Force";
    private static final String YAML_ABILITY_DEX   = "Dextérité";
    private static final String YAML_ABILITY_CON   = "Constitution";
    private static final String YAML_ABILITY_INT   = "Intelligence";
    private static final String YAML_ABILITY_WIS   = "Sagesse";
    private static final String YAML_ABILITY_CHA   = "Charisme";
    private static final String YAML_SKILLS        = "Compétences";
    private static final String YAML_RANK          = "Rang";
    private static final String YAML_FEATS         = "Dons";
    private static final String YAML_CLASSFEATURES = "Aptitudes";
    private static final String YAML_MODIFS        = "Modifs";
    private static final String YAML_MODIF_SOURCE  = "Source";
    private static final String YAML_MODIF_ICON    = "Icône";
    private static final String YAML_MODIF_BONUS   = "Bonus";
    private static final String YAML_BONUS_ID      = "Id";
    private static final String YAML_BONUS_VALUE   = "Valeur";
    private static final String YAML_INVENTORY     = "Inventaire";
    private static final String YAML_WEIGHT        = "Poids";

    public static final int ERROR_NAME_TOOLONG         = 1;
    public static final int ERROR_RACE_NOMATCH         = 2;
    public static final int ERROR_CLASS_NOMATCH        = 3;
    public static final int ERROR_CLASS_EXCEPTION      = 4;
    public static final int ERROR_ABILITIES_FORMAT     = 5;
    public static final int ERROR_ABILITIES_EXCEPTION  = 6;
    public static final int ERROR_HITPOINTS_FORMAT     = 7;
    public static final int ERROR_HITPOINTS_INVALID    = 8;
    public static final int ERROR_SPEED_FORMAT         = 9;
    public static final int ERROR_SPEED_INVALID        = 10;
    public static final int ERROR_SKILLS_FORMAT        = 11;
    public static final int ERROR_SKILL_NOMATCH        = 12;
    public static final int ERROR_SKILLS_EXCEPTION     = 13;
    public static final int ERROR_FEAT_NOMATCH         = 14;
    public static final int ERROR_FEATS_EXCEPTION      = 15;
    public static final int ERROR_FEATURE_NOMATCH      = 16;
    public static final int ERROR_FEATURES_EXCEPTION   = 17;
    public static final int ERROR_MODIF_SOURCE_TOOLONG = 18;
    public static final int ERROR_MODIFS_FORMAT        = 19;
    public static final int ERROR_MODIFS_EXCEPTION     = 20;
    public static final int ERROR_MODIF_ICON_NOTFOUND  = 21;
    public static final int ERROR_INVENTORY_FORMAT     = 22;
    public static final int ERROR_INVENTORY_EXCEPTION  = 23;
    public static final int ERROR_TRAIT_NOMATCH        = 24;
    public static final int ERROR_TRAITS_EXCEPTION     = 25;


    public static String exportCharacterAsYML(Character c, Context ctx) {
        DBHelper dbHelper = DBHelper.getInstance(ctx);

        // basics
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(YAML_NAME, c.getName());
        data.put(YAML_RACE, c.getRace() == null ? null : c.getRace().getName());

        // classes
        List<Map> classes = new ArrayList();
        for(int idx = 0; idx < c.getClassesCount(); idx++) {
            Map<String, Object> cl = new LinkedHashMap();
            cl.put(YAML_NAME, c.getClass(idx).first.getName());
            cl.put(YAML_ARCHETYPE, c.getClass(idx).second.getName());
            cl.put(YAML_LEVEL, c.getClass(idx).third);
            classes.add(cl);
        }
        data.put(YAML_CLASSES, classes);

        // abilities
        List<Map> abilities = new ArrayList();
        Map<String, Object> ability = new LinkedHashMap();
        ability.put(YAML_ABILITY_STR, c.getAbilityValue(Character.ABILITY_STRENGH, false));
        abilities.add(ability);
        ability = new LinkedHashMap();
        ability.put(YAML_ABILITY_DEX, c.getAbilityValue(Character.ABILITY_DEXTERITY, false));
        abilities.add(ability);
        ability = new LinkedHashMap();
        ability.put(YAML_ABILITY_CON, c.getAbilityValue(Character.ABILITY_CONSTITUTION, false));
        abilities.add(ability);
        ability = new LinkedHashMap();
        ability.put(YAML_ABILITY_INT, c.getAbilityValue(Character.ABILITY_INTELLIGENCE, false));
        abilities.add(ability);
        ability = new LinkedHashMap();
        ability.put(YAML_ABILITY_WIS, c.getAbilityValue(Character.ABILITY_WISDOM, false));
        abilities.add(ability);
        ability = new LinkedHashMap();
        ability.put(YAML_ABILITY_CHA, c.getAbilityValue(Character.ABILITY_CHARISMA, false));
        abilities.add(ability);
        data.put(YAML_ABILITIES, abilities);

        // others
        data.put(YAML_HP, c.getHitpoints());
        data.put(YAML_SPEED, c.getBaseSpeed());

        // skills
        List<Map> skills = new ArrayList();
        for(Long skillId : c.getSkills()) {
            Map<String, Object> skillObj = new LinkedHashMap();
            Skill skill = (Skill)dbHelper.fetchEntity(skillId, SkillFactory.getInstance());
            skillObj.put(YAML_NAME, skill.getName());
            skillObj.put(YAML_RANK, c.getSkillRank(skillId));
            skills.add(skillObj);
        }
        data.put(YAML_SKILLS, skills);

        // feats
        List<Map> feats = new ArrayList();
        for(Feat f : c.getFeats()) {
            Map<String, Object> featObj = new LinkedHashMap();
            featObj.put(YAML_NAME, f.getName());
            feats.add(featObj);
        }
        data.put(YAML_FEATS, feats);

        // class features
        List<Map> features = new ArrayList();
        for(ClassFeature f : c.getClassFeatures()) {
            if(f.isAuto()) {
                continue;
            }
            Map<String, Object> featObj = new LinkedHashMap();
            featObj.put(YAML_NAME, f.getName());
            features.add(featObj);
        }
        data.put(YAML_CLASSFEATURES, features);

        // modifs
        List<Map> modifs = new ArrayList();
        for(Character.CharacterModif modif : c.getModifs()) {
            Map<String, Object> modifObj = new LinkedHashMap();
            modifObj.put(YAML_MODIF_SOURCE, modif.getSource());
            modifObj.put(YAML_MODIF_ICON, modif.getIcon());
            List<Map> bonuses = new ArrayList<>();
            for(int idx = 0; idx < modif.getModifCount(); idx++) {
                Map<String, Object> bonus = new LinkedHashMap();
                bonus.put(YAML_BONUS_ID, modif.getModif(idx).first);
                bonus.put(YAML_BONUS_VALUE, modif.getModif(idx).second);
                bonuses.add(bonus);
            }
            modifObj.put(YAML_MODIF_BONUS, bonuses);
            modifs.add(modifObj);
        }
        data.put(YAML_MODIFS, modifs);

        // inventory
        List<Map> inventory = new ArrayList();
        for(Character.InventoryItem item : c.getInventoryItems()) {
            Map<String, Object> itemObj = new LinkedHashMap();
            itemObj.put(YAML_NAME, item.getName());
            itemObj.put(YAML_WEIGHT, item.getWeight());
            inventory.add(itemObj);
        }
        data.put(YAML_INVENTORY, inventory);

        // racial traits
        List<Map> traits = new ArrayList();
        for(RaceAlternateTrait t : c.getAlternateTraits()) {
            Map<String, Object> traitObj = new LinkedHashMap();
            traitObj.put(YAML_NAME, t.getName());
            traitObj.put(YAML_RACE, t.getRace().getName());
            traits.add(traitObj);
        }
        data.put(YAML_TRAITS, traits);

        try {
            StringWriter exportData = new StringWriter();
            YamlConfig config = new YamlConfig();
            config.writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER);
            config.writeConfig.setEscapeUnicode(false);
            YamlWriter writer = new YamlWriter(exportData, config);
            writer.write(data);
            writer.close();
            return exportData.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Pair<Character,List<Integer>> importCharacterAsYML(String yml, View view) {
        DBHelper dbHelper = DBHelper.getInstance(view.getContext());
        List<Integer> errors = new ArrayList<>();

        Character c = new Character();

        try {
            YamlReader reader = new YamlReader(yml);
            Map map = (Map) reader.read();

            // name
            String name = (String)map.get(YAML_NAME);
            if(name != null && name.length() > 30) {
                name = name.substring(0, 30);
                errors.add(ERROR_NAME_TOOLONG);
            }
            if(name != null) {
                name.replace('\n', ' ').replace('\r', ' ');
            }
            c.setName(name);

            // race
            String raceName = (String)map.get(YAML_RACE);
            if(raceName != null) {
                Race race = (Race)dbHelper.fetchEntityByName(raceName, RaceFactory.getInstance());
                if(race == null) {
                    errors.add(ERROR_RACE_NOMATCH);
                }
                c.setRace(race);
            }

            // classes
            try {
                Object classes = map.get(YAML_CLASSES);
                if (classes instanceof List) {
                    List<Object> clList = (List<Object>) classes;
                    for (Object cl : clList) {
                        if (cl instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) cl;
                            if (values.containsKey(YAML_NAME) && values.containsKey(YAML_LEVEL)) {
                                Class class_ = (Class) dbHelper.fetchEntityByName(values.get(YAML_NAME).toString(), ClassFactory.getInstance());
                                ClassArchetype arch = null;
                                if(values.containsKey(YAML_ARCHETYPE)) {
                                    List<DBEntity> list = dbHelper.fetchAllEntitiesByName(values.get(YAML_ARCHETYPE).toString(), ClassArchetypesFactory.getInstance());
                                    for(DBEntity e : list) {
                                        ClassArchetype a = (ClassArchetype)e;
                                        if(a.getClass_().getId() == class_.getId()) {
                                            arch = a;
                                            break;
                                        }
                                    }
                                }

                                if (class_ == null) {
                                    errors.add(ERROR_CLASS_NOMATCH);
                                } else {
                                    c.addOrSetClass(class_, arch, Integer.parseInt(values.get(YAML_LEVEL).toString()));
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_CLASS_EXCEPTION);
            }

            // abilities
            try {
                Object abilities = map.get(YAML_ABILITIES);
                if (abilities instanceof List) {
                    List<Object> aList = (List<Object>) abilities;
                    for (Object a : aList) {
                        if (a instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) a;
                            for(String abilityName : values.keySet()) {
                                if(abilityName.equals(YAML_ABILITY_STR)) {
                                    c.setStrength(Integer.parseInt(values.get(YAML_ABILITY_STR).toString()));
                                } else if(abilityName.equals(YAML_ABILITY_DEX)) {
                                    c.setDexterity(Integer.parseInt(values.get(YAML_ABILITY_DEX).toString()));
                                } else if(abilityName.equals(YAML_ABILITY_CON)) {
                                    c.setConstitution(Integer.parseInt(values.get(YAML_ABILITY_CON).toString()));
                                } else if(abilityName.equals(YAML_ABILITY_INT)) {
                                    c.setIntelligence(Integer.parseInt(values.get(YAML_ABILITY_INT).toString()));
                                } else if(abilityName.equals(YAML_ABILITY_WIS)) {
                                    c.setWisdom(Integer.parseInt(values.get(YAML_ABILITY_WIS).toString()));
                                } else if(abilityName.equals(YAML_ABILITY_CHA)) {
                                    c.setCharisma(Integer.parseInt(values.get(YAML_ABILITY_CHA).toString()));
                                }
                            }
                        }
                    }
                }
            } catch(NumberFormatException e) {
                errors.add(ERROR_ABILITIES_FORMAT);
            } catch(Exception e) {
                errors.add(ERROR_ABILITIES_EXCEPTION);
            }

            // hit points
            try {
                String hitpoint = (String)map.get(YAML_HP);
                if(hitpoint != null) {
                    int hp = Integer.parseInt(hitpoint);
                    if(hp >= 0 && hp <= FragmentHitPointsPicker.MAX_HITPOINTS) {
                        c.setHitpoints(hp);
                    } else {
                        errors.add(ERROR_HITPOINTS_INVALID);
                    }
                }
            } catch(NumberFormatException e) {
                errors.add(ERROR_HITPOINTS_FORMAT);
            }

            // speed
            try {
                String speed = (String)map.get(YAML_SPEED);
                if(speed != null) {
                    int sp = Integer.parseInt(speed);
                    if(sp >= 0 && sp <= FragmentSpeedPicker.MAX_SPEED) {
                        c.setSpeed(sp);
                    } else {
                        errors.add(ERROR_SPEED_INVALID);
                    }
                }
            } catch(NumberFormatException e) {
                errors.add(ERROR_SPEED_FORMAT);
            }

            // skills
            try {
                Object skills = map.get(YAML_SKILLS);
                if (skills instanceof List) {
                    List<Object> sList = (List<Object>) skills;
                    for (Object s : sList) {
                        if (s instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) s;
                            if(values.containsKey(YAML_NAME) && values.containsKey(YAML_RANK)) {
                                Skill skill = (Skill)dbHelper.fetchEntityByName(values.get(YAML_NAME).toString(), SkillFactory.getInstance());
                                if(skill == null) {
                                    errors.add(ERROR_SKILL_NOMATCH);
                                } else {
                                    int rank = Integer.parseInt(values.get(YAML_RANK).toString());
                                    c.setSkillRank(skill.getId(), rank);
                                }
                            }
                        }
                    }
                }
            } catch(NumberFormatException e) {
                errors.add(ERROR_SKILLS_FORMAT);
            } catch(Exception e) {
                errors.add(ERROR_SKILLS_EXCEPTION);
            }

            // feats
            try {
                Object feats = map.get(YAML_FEATS);
                if (feats instanceof List) {
                    List<Object> fList = (List<Object>) feats;
                    for (Object f : fList) {
                        if (f instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) f;
                            if(values.containsKey(YAML_NAME)) {
                                Feat feat = (Feat)dbHelper.fetchEntityByName(values.get(YAML_NAME).toString(), FeatFactory.getInstance());
                                if(feat == null) {
                                    errors.add(ERROR_FEAT_NOMATCH);
                                } else {
                                    c.addFeat(feat);
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_FEATS_EXCEPTION);
            }

            // classfeatures
            try {
                Object features = map.get(YAML_CLASSFEATURES);
                if (features instanceof List) {
                    List<Object> fList = (List<Object>) features;
                    for (Object f : fList) {
                        if (f instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) f;
                            if(values.containsKey(YAML_NAME)) {
                                ClassFeature feature = (ClassFeature)dbHelper.fetchEntityByName(values.get(YAML_NAME).toString(), ClassFeatureFactory.getInstance());
                                if(feature == null) {
                                    errors.add(ERROR_FEATURE_NOMATCH);
                                } else if(!feature.isAuto()) {
                                    c.addClassFeature(feature);
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_FEATURES_EXCEPTION);
            }

            // modifs
            try {
                Object modifs = map.get(YAML_MODIFS);
                if (modifs instanceof List) {
                    List<Object> mList = (List<Object>) modifs;
                    for (Object m : mList) {
                        if (m instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) m;
                            if(values.containsKey(YAML_MODIF_SOURCE) && values.containsKey(YAML_MODIF_ICON) && values.containsKey(YAML_MODIF_BONUS)) {
                                String source = (String)values.get(YAML_MODIF_SOURCE);
                                if(source != null && source.length() > 20) {
                                    source = source.substring(0, 20);
                                    errors.add(ERROR_MODIF_SOURCE_TOOLONG);
                                }
                                if(source != null) {
                                    source.replace('\n', ' ').replace('\r', ' ');
                                }
                                String icon = (String)values.get(YAML_MODIF_ICON);
                                int resourceId = view.getResources().getIdentifier("modif_" + icon, "drawable",
                                        view.getContext().getPackageName());
                                if(resourceId == 0) {
                                    errors.add(ERROR_MODIF_ICON_NOTFOUND);
                                    continue;
                                }
                                Object bonus = values.get(YAML_MODIF_BONUS);
                                List<Pair<Integer,Integer>> bonusList = new ArrayList<>();
                                if(bonus instanceof List) {
                                    List<Object> bList = (List<Object>) bonus;
                                    for (Object b : bList) {
                                        if (b instanceof Map) {
                                            Map<String, Object> bValues = (Map<String, Object>) b;
                                            if(bValues.containsKey(YAML_BONUS_ID) && bValues.containsKey(YAML_BONUS_VALUE)) {
                                                int id = Integer.parseInt(bValues.get(YAML_BONUS_ID).toString());
                                                int value = Integer.parseInt(bValues.get(YAML_BONUS_VALUE).toString());
                                                if(id>0 && Math.abs(value) < 100) {
                                                    Pair<Integer,Integer> pair = new Pair<>(id,value);
                                                    bonusList.add(pair);
                                                }
                                            }
                                        }
                                    }
                                }
                                c.addModif(new Character.CharacterModif(source, bonusList, icon));
                            }
                        }
                    }
                }
            } catch(NumberFormatException e) {
                errors.add(ERROR_MODIFS_FORMAT);
            } catch(Exception e) {
                errors.add(ERROR_MODIFS_EXCEPTION);
            }

            // inventory
            try {
                Object inventory = map.get(YAML_INVENTORY);
                if (inventory instanceof List) {
                    List<Object> fList = (List<Object>) inventory;
                    for (Object f : fList) {
                        if (f instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) f;
                            if(values.containsKey(YAML_NAME) && values.containsKey(YAML_WEIGHT)) {
                                String itemName = (String)values.get(YAML_NAME);
                                int itemWeight = 0;
                                try {
                                    itemWeight = Integer.parseInt((String)values.get(YAML_WEIGHT));
                                } catch(NumberFormatException nfe) {
                                    errors.add(ERROR_INVENTORY_FORMAT);
                                }
                                c.addInventoryItem(new Character.InventoryItem(itemName, itemWeight));
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_INVENTORY_EXCEPTION);
            }

            // racial alternate traits
            try {
                List<DBEntity> allTraits = dbHelper.getAllEntities(RaceAlternateTraitFactory.getInstance());
                Object traits = map.get(YAML_TRAITS);
                if (traits instanceof List) {
                    List<Object> fList = (List<Object>) traits;
                    for (Object f : fList) {
                        if (f instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) f;
                            if(values.containsKey(YAML_NAME) && values.containsKey(YAML_RACE)) {
                                String tName = values.get(YAML_NAME).toString();
                                String rName = values.get(YAML_RACE).toString();
                                RaceAlternateTrait trait = null;
                                for(DBEntity t : allTraits) {
                                    RaceAlternateTrait tObj = (RaceAlternateTrait)t;
                                    if(tObj.getName().equals(tName) && tObj.getRace().getName().equals(rName)) {
                                        trait = (RaceAlternateTrait)t;
                                        break;
                                    }
                                }
                                if(trait == null) {
                                    errors.add(ERROR_TRAIT_NOMATCH);
                                } else {
                                    c.addAlternateTrait(trait);
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_TRAITS_EXCEPTION);
            }

            //System.out.println(exportCharacterAsYML(c, view.getContext()));

            return new Pair<>(c, errors);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
