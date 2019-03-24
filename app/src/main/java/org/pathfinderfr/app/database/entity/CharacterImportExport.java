package org.pathfinderfr.app.database.entity;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;

import org.pathfinderfr.app.database.DBHelper;

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
    private static final String YAML_LEVEL         = "Niveau";
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


    public static String exportCharacterAsYML(Character c) {
        DBHelper dbHelper = DBHelper.getInstance(null);

        // basics
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(YAML_NAME, c.getName());
        data.put(YAML_RACE, c.getRace() == null ? null : c.getRace().getName());

        // classes
        List<Map> classes = new ArrayList();
        for(int idx = 0; idx < c.getClassesCount(); idx++) {
            Map<String, Object> cl = new LinkedHashMap();
            cl.put(YAML_NAME, c.getClass(idx).first.getName());
            cl.put(YAML_LEVEL, c.getClass(idx).second);
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
}
