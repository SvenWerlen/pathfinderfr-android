package org.pathfinderfr.app.database.entity;

import android.content.Context;
import android.view.View;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import org.pathfinderfr.app.character.FragmentHitPointsPicker;
import org.pathfinderfr.app.character.FragmentInfosPicker;
import org.pathfinderfr.app.character.FragmentMoneyPicker;
import org.pathfinderfr.app.character.FragmentSpeedPicker;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.util.CharacterPDF;
import org.pathfinderfr.app.util.Pair;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CharacterImportExport {

    private static final String YAML_UUID            = "Id";
    private static final String YAML_NAME            = "Nom";
    private static final String YAML_RACE            = "Race";
    private static final String YAML_HP              = "PointsVie";
    private static final String YAML_HP_TEMP         = "PointsVieTemp";
    private static final String YAML_SPEED           = "Vitesse";
    private static final String YAML_CLASSES         = "Classes";
    private static final String YAML_ARCHETYPE       = "Archetype";
    private static final String YAML_LEVEL           = "Niveau";
    private static final String YAML_TRAITS          = "TraitsAlternatifs";
    private static final String YAML_ABILITIES       = "Caracs";
    private static final String YAML_ABILITY_STR     = "Force";
    private static final String YAML_ABILITY_DEX     = "Dextérité";
    private static final String YAML_ABILITY_CON     = "Constitution";
    private static final String YAML_ABILITY_INT     = "Intelligence";
    private static final String YAML_ABILITY_WIS     = "Sagesse";
    private static final String YAML_ABILITY_CHA     = "Charisme";
    private static final String YAML_SKILLS          = "Compétences";
    private static final String YAML_RANK            = "Rang";
    private static final String YAML_FEATS           = "Dons";
    private static final String YAML_CLASSFEATURES   = "Aptitudes";
    private static final String YAML_LINKEDTO        = "LiéÀ";
    private static final String YAML_LINKEDNAME      = "LiéNom";
    private static final String YAML_MODIFS          = "Modifs";
    private static final String YAML_MODIF_SOURCE    = "Source";
    private static final String YAML_MODIF_ICON      = "Icône";
    private static final String YAML_MODIF_LINKTO    = "Lien";
    private static final String YAML_MODIF_BONUS     = "Bonus";
    private static final String YAML_BONUS_ID        = "Id";
    private static final String YAML_BONUS_VALUE     = "Valeur";
    private static final String YAML_INVENTORY       = "Inventaire";
    private static final String YAML_WEIGHT          = "Poids";
    private static final String YAML_PRICE           = "Prix";
    private static final String YAML_AMMO            = "Munitions";
    private static final String YAML_REFERENCE       = "Référence";
    private static final String YAML_REF_TYPE_W      = "Arme";
    private static final String YAML_REF_TYPE_A      = "Armure";
    private static final String YAML_REF_TYPE_E      = "Équipement";
    private static final String YAML_REF_TYPE_M      = "Objet magique";
    private static final String YAML_SPEED_ARMOR     = "VitesseAvecArmure";
    private static final String YAML_SPEED_DIG       = "VitesseCreusement";
    private static final String YAML_SPEED_FLY       = "VitesseVol";
    private static final String YAML_SPEED_FLYM      = "VolManoeuvrabilité";
    private static final String YAML_PLAYER          = "Joueur";
    private static final String YAML_ALIGNMENT       = "Alignement";
    private static final String YAML_DIVINITY        = "Divinité";
    private static final String YAML_ORIGIN          = "Origine";
    private static final String YAML_SIZETYPE        = "TypeTaille";
    private static final String YAML_SEX             = "Sexe";
    private static final String YAML_AGE             = "Âge";
    private static final String YAML_HEIGHT          = "Taille";
    private static final String YAML_HAIR            = "Cheveux";
    private static final String YAML_EYES            = "Yeux";
    private static final String YAML_LANG            = "Langues";
    private static final String YAML_MONEY           = "Richesses";
    private static final String YAML_CP              = "pc";
    private static final String YAML_SP              = "pa";
    private static final String YAML_GP              = "po";
    private static final String YAML_PP              = "pp";
    private static final String YAML_XP              = "Expérience";
    private static final String YAML_SPELLS          = "Sorts";


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
    public static final int ERROR_INVENTORY_REFERENCE  = 26;
    public static final int ERROR_PLAYER_TOOLONG       = 27;
    public static final int ERROR_DIVINITY_TOOLONG     = 28;
    public static final int ERROR_ORIGIN_TOOLONG       = 29;
    public static final int ERROR_AGE_FORMAT           = 30;
    public static final int ERROR_AGE_INVALID          = 31;
    public static final int ERROR_HEIGHT_FORMAT        = 32;
    public static final int ERROR_HEIGHT_INVALID       = 33;
    public static final int ERROR_WEIGHT_FORMAT        = 34;
    public static final int ERROR_WEIGHT_INVALID       = 35;
    public static final int ERROR_HAIR_TOOLONG         = 36;
    public static final int ERROR_EYES_TOOLONG         = 37;
    public static final int ERROR_LANG_TOOLONG         = 38;
    public static final int ERROR_XP_FORMAT            = 39;
    public static final int ERROR_XP_INVALID           = 40;
    public static final int ERROR_SPELL_NOMATCH        = 41;
    public static final int ERROR_SPELLS_EXCEPTION     = 42;
    public static final int ERROR_MONEY_FORMAT         = 43;
    public static final int ERROR_MONEY_INVALID        = 44;


    public static String exportCharacterAsYML(Character c, Context ctx) {
        DBHelper dbHelper = DBHelper.getInstance(ctx);

        if(c == null) {
            return null;
        }

        // basics
        Map<String, Object> data = new LinkedHashMap<>();
        if(c.hasUUID()) {
            data.put(YAML_UUID, c.getUniqID());
        }
        data.put(YAML_NAME, c.getName());
        data.put(YAML_PLAYER, c.getPlayer());
        data.put(YAML_SEX, CharacterPDF.sex2text(c.getSex()));
        data.put(YAML_ALIGNMENT, CharacterPDF.alignment2Text(c.getAlignment()));
        data.put(YAML_DIVINITY, c.getDivinity());
        data.put(YAML_ORIGIN, c.getOrigin());
        data.put(YAML_RACE, c.getRace() == null ? null : c.getRace().getName());

        // classes
        List<Map> classes = new ArrayList();
        for(int idx = 0; idx < c.getClassesCount(); idx++) {
            Map<String, Object> cl = new LinkedHashMap();
            cl.put(YAML_NAME, c.getClass(idx).first.getName());
            if(c.getClass(idx).second != null) {
                cl.put(YAML_ARCHETYPE, c.getClass(idx).second.getName());
            }
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
        data.put(YAML_XP, c.getExperience());
        data.put(YAML_SIZETYPE, CharacterPDF.size2Text(c.getSizeType()));
        data.put(YAML_AGE, c.getAge());
        data.put(YAML_HEIGHT, c.getHeight());
        data.put(YAML_WEIGHT, c.getWeight());
        data.put(YAML_HAIR, c.getHair());
        data.put(YAML_EYES, c.getEyes());
        data.put(YAML_LANG, c.getLanguages());
        data.put(YAML_HP, c.getHitpoints());
        data.put(YAML_HP_TEMP, c.getHitpointsTemp());
        data.put(YAML_SPEED, c.getBaseSpeed());
        data.put(YAML_SPEED_ARMOR, c.getBaseSpeedWithArmor());
        data.put(YAML_SPEED_DIG, c.getBaseSpeedDig());
        data.put(YAML_SPEED_FLY, c.getBaseSpeedFly());
        data.put(YAML_SPEED_FLYM, CharacterPDF.flyManeuverability2Text(c.getBaseSpeedFly(), c.getBaseSpeedManeuverability()));

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
            Map<String, Object> featObj = new LinkedHashMap();
            featObj.put(YAML_NAME, f.getName());
            if(f.isAuto() && f.getLinkedTo() != null) {
                featObj.put(YAML_LINKEDTO, f.getLinkedTo().getName());
            }
            if(f.getLinkedName() != null) {
                featObj.put(YAML_LINKEDNAME, f.getLinkedName());
            }
            features.add(featObj);
        }
        data.put(YAML_CLASSFEATURES, features);

        // spells
        List<Map> spells = new ArrayList();
        for(Spell s : c.getSpells()) {
            Map<String, Object> spellObj = new LinkedHashMap();
            spellObj.put(YAML_NAME, s.getName());
            spells.add(spellObj);
        }
        data.put(YAML_SPELLS, spells);

        // modifs
        List<Map> modifs = new ArrayList<>();
        for(Modification modif : c.getModifications()) {
            Map<String, Object> modifObj = new LinkedHashMap<>();
            modifObj.put(YAML_MODIF_SOURCE, modif.getSource());
            modifObj.put(YAML_MODIF_ICON, modif.getIcon());
            modifObj.put(YAML_MODIF_LINKTO, modif.getItemId());
            List<Map> bonuses = new ArrayList<>();
            for(int idx = 0; idx < modif.getModifCount(); idx++) {
                Map<String, Object> bonus = new LinkedHashMap<>();
                bonus.put(YAML_BONUS_ID, modif.getModif(idx).first);
                bonus.put(YAML_BONUS_VALUE, modif.getModif(idx).second);
                bonuses.add(bonus);
            }
            modifObj.put(YAML_MODIF_BONUS, bonuses);
            modifs.add(modifObj);
        }
        data.put(YAML_MODIFS, modifs);

        // money
        Map<String, Object> money = new LinkedHashMap<>();
        money.put(YAML_CP, c.getMoneyCP());
        money.put(YAML_SP, c.getMoneySP());
        money.put(YAML_GP, c.getMoneyGP());
        money.put(YAML_PP, c.getMoneyPP());
        data.put(YAML_MONEY, money);

        // inventory
        List<Map> inventory = new ArrayList();
        for(CharacterItem item : c.getInventoryItems()) {
            Map<String, Object> itemObj = new LinkedHashMap();
            itemObj.put(YAML_NAME, item.getName());
            itemObj.put(YAML_WEIGHT, item.getWeight());
            itemObj.put(YAML_PRICE, item.getPrice());
            if(item.getItemRef() > 0) {
                DBEntity e = dbHelper.fetchObjectEntity(item);
                if(e != null) {
                    if(e instanceof Weapon) {
                        itemObj.put(YAML_REFERENCE, YAML_REF_TYPE_W + " " + e.getName());
                    } else if(e instanceof Armor) {
                        itemObj.put(YAML_REFERENCE, YAML_REF_TYPE_A + " " + e.getName());
                    } else if(e instanceof Equipment) {
                        itemObj.put(YAML_REFERENCE, YAML_REF_TYPE_E + " " + e.getName());
                    } else if(e instanceof MagicItem) {
                        itemObj.put(YAML_REFERENCE, YAML_REF_TYPE_M + " " + e.getName());
                    }
                }
            }
            if(item.getAmmo() != null && item.getAmmo().length() > 0) {
                itemObj.put(YAML_AMMO, item.getAmmo());
            }
            inventory.add(itemObj);
        }
        data.put(YAML_INVENTORY, inventory);

        // racial traits
        List<Map> traits = new ArrayList();
        for(Trait t : c.getTraits()) {
            Map<String, Object> traitObj = new LinkedHashMap();
            traitObj.put(YAML_NAME, t.getName());
            if(t.getRace() != null) {
                traitObj.put(YAML_RACE, t.getRace().getName());
            }
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

    public static String cleanText(String text, List<Integer> errors, int maxLen, int maxLenError) {
        if(text != null && text.length() > maxLen) {
            text = text.substring(0, maxLen);
            errors.add(maxLenError);
        }
        if(text != null) {
            text.replace('\n', ' ').replace('\r', ' ');
        }
        return text;
    }

    public static int cleanNumber(String text, List<Integer> errors, int maxValue, int maxValueError, int formatError) {
        try {
            String value = text;
            if(value != null) {
                int val = Integer.parseInt(value);
                if(val >= 0 && val <= maxValue) {
                    return val;
                } else {
                    errors.add(maxValueError);
                }
            }
        } catch(NumberFormatException e) {
            errors.add(formatError);
        }
        return 0;
    }


    public static Pair<Character,List<Integer>> importCharacterAsYML(String yml, View view) {
        DBHelper dbHelper = DBHelper.getInstance(view.getContext());
        List<Integer> errors = new ArrayList<>();

        Character c = new Character();

        try {
            YamlReader reader = new YamlReader(yml);
            Map map = (Map) reader.read();

            c.setUniqID((String)map.get(YAML_UUID));
            c.setName(cleanText((String)map.get(YAML_NAME), errors, 30, ERROR_NAME_TOOLONG));
            c.setPlayer(cleanText((String)map.get(YAML_PLAYER), errors, 30, ERROR_PLAYER_TOOLONG));
            if(map.containsKey(YAML_SEX)) {
                c.setSex(CharacterPDF.text2sex((String) map.get(YAML_SEX)));
            }
            if(map.containsKey(YAML_ALIGNMENT)) {
                c.setAlignment(CharacterPDF.text2alignment((String)map.get(YAML_ALIGNMENT)));
            }
            c.setDivinity(cleanText((String)map.get(YAML_DIVINITY), errors, 30, ERROR_DIVINITY_TOOLONG));
            c.setOrigin(cleanText((String)map.get(YAML_ORIGIN), errors, 30, ERROR_ORIGIN_TOOLONG));


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
            c.setHitpoints(cleanNumber((String)map.get(YAML_HP), errors,
                    FragmentHitPointsPicker.MAX_HITPOINTS,
                    ERROR_HITPOINTS_INVALID, ERROR_HITPOINTS_FORMAT));

            // hit points temp
            c.setHitpointsTemp(cleanNumber((String)map.get(YAML_HP_TEMP), errors,
                    FragmentHitPointsPicker.MAX_HITPOINTS,
                    ERROR_HITPOINTS_INVALID, ERROR_HITPOINTS_FORMAT));

            // experience
            c.setExperience(cleanNumber((String)map.get(YAML_XP), errors,
                    99999999,
                    ERROR_XP_INVALID, ERROR_XP_FORMAT));

            // size type
            if(map.containsKey(YAML_SIZETYPE)) {
                c.setSizeType(CharacterPDF.text2Size((String)map.get(YAML_SIZETYPE)));
            }

            // age
            c.setAge(cleanNumber((String)map.get(YAML_AGE), errors,
                    FragmentInfosPicker.MAX_VALUE,
                    ERROR_AGE_INVALID, ERROR_AGE_FORMAT));

            // height
            c.setHeight(cleanNumber((String)map.get(YAML_HEIGHT), errors,
                    FragmentInfosPicker.MAX_VALUE,
                    ERROR_HEIGHT_INVALID, ERROR_HEIGHT_FORMAT));

            // weight
            c.setWeight(cleanNumber((String)map.get(YAML_WEIGHT), errors,
                    FragmentInfosPicker.MAX_VALUE,
                    ERROR_WEIGHT_INVALID, ERROR_WEIGHT_FORMAT));

            c.setHair(cleanText((String)map.get(YAML_HAIR), errors, 10, ERROR_HAIR_TOOLONG));
            c.setEyes(cleanText((String)map.get(YAML_EYES), errors, 10, ERROR_EYES_TOOLONG));
            c.setLanguages(cleanText((String)map.get(YAML_LANG), errors, 200, ERROR_LANG_TOOLONG));

            // speed
            c.setSpeed(cleanNumber((String)map.get(YAML_SPEED), errors,
                    FragmentSpeedPicker.MAX_SPEED,
                    ERROR_SPEED_INVALID, ERROR_SPEED_FORMAT));

            c.setSpeedWithArmor(cleanNumber((String)map.get(YAML_SPEED_ARMOR), errors,
                    FragmentSpeedPicker.MAX_SPEED,
                    ERROR_SPEED_INVALID, ERROR_SPEED_FORMAT));

            c.setSpeedDig(cleanNumber((String)map.get(YAML_SPEED_DIG), errors,
                    FragmentSpeedPicker.MAX_SPEED,
                    ERROR_SPEED_INVALID, ERROR_SPEED_FORMAT));

            c.setSpeedFly(cleanNumber((String)map.get(YAML_SPEED_FLY), errors,
                    FragmentSpeedPicker.MAX_SPEED,
                    ERROR_SPEED_INVALID, ERROR_SPEED_FORMAT));

            c.setSpeedManeuverability(CharacterPDF.text2flyManeuverability((String)map.get(YAML_SPEED_FLYM)));

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

            // spells
            try {
                Object spells = map.get(YAML_SPELLS);
                if (spells instanceof List) {
                    List<Object> sList = (List<Object>) spells;
                    for (Object s : sList) {
                        if (s instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) s;
                            if(values.containsKey(YAML_NAME)) {
                                Spell spell = (Spell)dbHelper.fetchEntityByName(values.get(YAML_NAME).toString(), SpellFactory.getInstance());
                                if(spell == null) {
                                    errors.add(ERROR_SPELL_NOMATCH);
                                } else {
                                    c.addSpell(spell);
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_SPELLS_EXCEPTION);
            }

            // classfeatures
            try {
                Object features = map.get(YAML_CLASSFEATURES);
                if (features instanceof List) {
                    List<Object> fList = (List<Object>) features;
                    List<String> linkedTo = new ArrayList<>();
                    for (Object f : fList) {
                        if (f instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) f;
                            if(values.containsKey(YAML_NAME)) {
                                List<DBEntity> featList = dbHelper.fetchAllEntitiesByName(values.get(YAML_NAME).toString(), ClassFeatureFactory.getInstance());
                                if(featList == null || featList.size() == 0) {
                                    errors.add(ERROR_FEATURE_NOMATCH);
                                }
                                ClassFeature feature = null;
                                // find best match
                                for(DBEntity entity : featList) {
                                    ClassFeature cf = (ClassFeature)entity;
                                    // class matches
                                    for(int i = 0; i<c.getClassesCount(); i++) {
                                        if(c.isValidClassFeature(cf)) {
                                            feature = cf;
                                            break;
                                        }
                                    }
                                    if(feature != null) {
                                        break;
                                    }
                                }
                                // take first match (best guess)
                                if(feature == null) {
                                    feature = (ClassFeature)featList.get(0);
                                }
                                c.addClassFeature(feature);
                                if(values.containsKey(YAML_LINKEDNAME)) {
                                    feature.setLinkedName(values.get(YAML_LINKEDNAME).toString());
                                }
                                if(feature.isAuto() && values.containsKey(YAML_LINKEDTO)) {
                                    linkedTo.add(values.get(YAML_LINKEDTO).toString());
                                } else {
                                    linkedTo.add("");
                                }
                            }
                        }
                    }
                    // update linkedTo (if any)
                    for(int idx = 0; idx < linkedTo.size(); idx++) {
                        if(linkedTo.get(idx).length() > 0) {
                            String name = linkedTo.get(idx);
                            // search corresponding
                            for(ClassFeature cf : c.getClassFeatures()) {
                                if(cf.getName().equals(name)) {
                                    c.getClassFeatures().get(idx).setLinkedTo(cf);
                                    cf.setLinkedTo(c.getClassFeatures().get(idx));
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_FEATURES_EXCEPTION);
            }

            // money
            Object money = map.get(YAML_MONEY);
            if (money instanceof Map) {
                Map<String, Object> pieces = (Map<String, Object>) money;
                if (pieces.containsKey(YAML_CP)) {
                    c.setMoneyCP(cleanNumber((String)pieces.get(YAML_CP), errors, FragmentMoneyPicker.MAX_MONEY, ERROR_MONEY_INVALID, ERROR_MONEY_FORMAT));
                }
                if (pieces.containsKey(YAML_SP)) {
                    c.setMoneySP(cleanNumber((String)pieces.get(YAML_SP), errors, FragmentMoneyPicker.MAX_MONEY, ERROR_MONEY_INVALID, ERROR_MONEY_FORMAT));
                }
                if (pieces.containsKey(YAML_GP)) {
                    c.setMoneyGP(cleanNumber((String)pieces.get(YAML_GP), errors, FragmentMoneyPicker.MAX_MONEY, ERROR_MONEY_INVALID, ERROR_MONEY_FORMAT));
                }
                if (pieces.containsKey(YAML_PP)) {
                    c.setMoneyPP(cleanNumber((String)pieces.get(YAML_PP), errors, FragmentMoneyPicker.MAX_MONEY, ERROR_MONEY_INVALID, ERROR_MONEY_FORMAT));
                }
            }

            // inventory (must be imported BEFORE modifs because of linkto)
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
                                int itemPrice = 0;
                                if(values.containsKey(YAML_PRICE)) {
                                    try {
                                        itemPrice = Integer.parseInt((String) values.get(YAML_PRICE));
                                    } catch (NumberFormatException nfe) {
                                        errors.add(ERROR_INVENTORY_FORMAT);
                                    }
                                }
                                long objectId = 0L;
                                if(values.containsKey(YAML_REFERENCE)) {
                                    DBEntity object = null;
                                    String reference = (String)values.get(YAML_REFERENCE);
                                    if(reference.startsWith(YAML_REF_TYPE_W)) {
                                        object = dbHelper.fetchEntityByName(reference.substring(YAML_REF_TYPE_W.length()+1), WeaponFactory.getInstance());
                                        //objectId = object == null ? 0L : CharacterItem.IDX_WEAPONS + object.getId();
                                    }
                                    else if(reference.startsWith(YAML_REF_TYPE_A)) {
                                        object = dbHelper.fetchEntityByName(reference.substring(YAML_REF_TYPE_A.length()+1), ArmorFactory.getInstance());
                                        //objectId = object == null ? 0L : Character.InventoryItem.IDX_ARMORS + object.getId();
                                    }
                                    else if(reference.startsWith(YAML_REF_TYPE_E)) {
                                        object = dbHelper.fetchEntityByName(reference.substring(YAML_REF_TYPE_E.length()+1), EquipmentFactory.getInstance());
                                        //objectId = object == null ? 0L : Character.InventoryItem.IDX_EQUIPMENT + object.getId();
                                    }
                                    else if(reference.startsWith(YAML_REF_TYPE_M)) {
                                        object = dbHelper.fetchEntityByName(reference.substring(YAML_REF_TYPE_M.length()+1), MagicItemFactory.getInstance());
                                        //objectId = object == null ? 0L : Character.InventoryItem.IDX_MAGICITEM + object.getId();
                                    }
                                    if(object == null) {
                                        errors.add(ERROR_INVENTORY_REFERENCE);
                                    }
                                }
                                //c.addInventoryItem(new CharacterItem(0L, itemName, itemWeight, itemPrice, objectId, (String)values.get(YAML_AMMO), CharacterItem.LOCATION_BAG));
                            }
                        }
                    }
                }
            } catch(Exception e) {
                errors.add(ERROR_INVENTORY_EXCEPTION);
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
                                int linkTo = 0;
                                if(values.containsKey(YAML_MODIF_LINKTO)) {
                                    linkTo = Integer.parseInt(values.get(YAML_MODIF_LINKTO).toString());
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
                                //c.addModif(new Character.CharacterModif(source, bonusList, icon, linkTo));
                            }
                        }
                    }
                }
            } catch(NumberFormatException e) {
                errors.add(ERROR_MODIFS_FORMAT);
            } catch(Exception e) {
                errors.add(ERROR_MODIFS_EXCEPTION);
            }

            // traits
            try {
                List<DBEntity> allTraits = dbHelper.getAllEntities(TraitFactory.getInstance());
                Object traits = map.get(YAML_TRAITS);
                if (traits instanceof List) {
                    List<Object> fList = (List<Object>) traits;
                    for (Object f : fList) {
                        if (f instanceof Map) {
                            Map<String, Object> values = (Map<String, Object>) f;
                            if(values.containsKey(YAML_NAME)) {
                                String tName = values.get(YAML_NAME).toString();
                                String rName = values.containsKey(YAML_RACE) ? values.get(YAML_RACE).toString() : null;
                                Trait trait = null;
                                for(DBEntity t : allTraits) {
                                    Trait tObj = (Trait)t;
                                    if(tObj.getName().equals(tName) && ((tObj.getRace() == null && rName == null) || (tObj.getRace().getName().equals(rName)))) {
                                        trait = (Trait)t;
                                        break;
                                    }
                                }
                                if(trait == null) {
                                    errors.add(ERROR_TRAIT_NOMATCH);
                                } else {
                                    c.addTrait(trait);
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
