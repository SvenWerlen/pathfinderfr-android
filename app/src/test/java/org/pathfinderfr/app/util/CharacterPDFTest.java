package org.pathfinderfr.app.util;

import com.itextpdf.io.image.ImageDataFactory;

import org.junit.Test;
import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterImportExport;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.Weapon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterPDFTest {

    public static int skillId;

    public static Skill createSkill(String name, String ability, boolean trainingReq) {
        Skill s = new Skill();
        s.setId(++skillId);
        s.setName(name);
        s.setAbility(ability);
        s.setTraining(trainingReq ? "Oui" : "Non");
        return s;
    }

    @Test
    public void test() throws IOException {

        Character c = new Character();
        c.setStrength(15);
        c.setDexterity(12);
        c.setConstitution(13);
        c.setIntelligence(14);
        c.setWisdom(15);
        c.setCharisma(16);
        c.setName("Lana");
        Class cl = new Class();
        cl.setName("Barbare");
        cl.getSkills().add("Acrobatie");
        cl.getLevels().add(new Class.Level(1, new int[]{1}, 0,2,0,0));
        cl.getLevels().add(new Class.Level(2, new int[]{2}, 0,3,0,0));
        cl.getLevels().add(new Class.Level(7, new int[]{7, 2}, 2,5,2,0));
        c.addOrSetClass(cl, null, 7);
        Race r = new Race();
        r.setName("Humain");
        c.setRace(r);
        c.setSpeed(6);
        c.setSpeedWithArmor(4);
        c.setSpeedDig(3);
        c.setSpeedFly(10);
        c.setSpeedManeuverability(Character.SPEED_MANEUV_AVERAGE);
        c.setHitpoints(125);
        c.setHitpointsTemp(25);
        c.setSkillRank(1, 3);

        c.setAlignment(Character.ALIGN_CN);
        c.setPlayer("Émilie");
        c.setDivinity("Dieu du vin");
        c.setOrigin("Varisie");
        c.setSizeType(Character.SIZE_SMALL);
        c.setSex(Character.SEX_F);
        c.setAge(17);
        c.setHeight(150);
        c.setWeight(57);
        c.setHair("Noirs");
        c.setEyes("Bruns");
        c.setLanguages("Commun, gobelin, elfique, draconique");



        Character.CharacterModif modif = new Character.CharacterModif("Race", new ArrayList<Pair<Integer, Integer>>(), "-", 0,true);
        c.addModif(modif);

        List<Pair<Integer, Integer>> modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_COMBAT_AC_ARMOR, 7));
        modif = new Character.CharacterModif("Armure de plates", modifs, "-",  0,true);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_COMBAT_AC_SHIELD, 2));
        modif = new Character.CharacterModif("Écu", modifs, "-",  0,true);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_COMBAT_AC_NATURAL, 3));
        modif = new Character.CharacterModif("Peau d'écorce", modifs, "-",  0,true);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_COMBAT_AC_PARADE, 1));
        modif = new Character.CharacterModif("Bracelets", modifs, "-",  0,true);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_COMBAT_AC, -2));
        modif = new Character.CharacterModif("Bague", modifs, "-",  0,true);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_ALL, 1));
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_MAG_ALL, 2));
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_MAG_REF, 1));
        modif = new Character.CharacterModif("Objet réflexes", modifs, "-",  0,false);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_ALL, 1));
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_MAG_ALL, 2));
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_MAG_FOR, 2));
        modif = new Character.CharacterModif("Objet réflexes", modifs, "-",  0,false);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_ALL, 1));
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_MAG_ALL, 2));
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_SAVES_MAG_WIL, 3));
        modif = new Character.CharacterModif("Objet réflexes", modifs, "-",  0,false);
        c.addModif(modif);

        modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_COMBAT_MAG, 1));
        modifs.add(new Pair<Integer, Integer>(Character.MODIF_COMBAT_INI, 2));
        modif = new Character.CharacterModif("Objet combat", modifs, "-",  0,true);
        c.addModif(modif);


        List<DBEntity> skills = new ArrayList<>();
        skills.add(createSkill("Acrobatie", "Dex", false));
        skills.add(createSkill("Art de la magie", "Int", true));
        skills.add(createSkill("Artisanat", "Int", false));
        skills.add(createSkill("Bluff", "Cha", false));
        skills.add(createSkill("Connaissances (exploration souterraine)", "Int", true));
        skills.add(createSkill("Connaissances (folklore local)", "Int", true));
        skills.add(createSkill("Connaissances (géographie)", "Int", true));
        skills.add(createSkill("Connaissances (histoire)", "Int", true));
        skills.add(createSkill("Connaissances (ingénierie)", "Int", true));
        skills.add(createSkill("Connaissances (mystères)", "Int", true));
        skills.add(createSkill("Connaissances (nature)", "Int", true));
        skills.add(createSkill("Connaissances (noblesse)", "Int", true));
        skills.add(createSkill("Connaissances (plans)", "Int", true));
        skills.add(createSkill("Connaissances (religion)", "Int", true));
        skills.add(createSkill("Déguisement", "Cha", false));
        skills.add(createSkill("Diplomatie", "Cha", false));
        skills.add(createSkill("Discrétion", "Dex", false));
        skills.add(createSkill("Dressage", "Cha", true));
        skills.add(createSkill("Équitation", "Dex", false));
        skills.add(createSkill("Escalade", "For", false));
        skills.add(createSkill("Escamotage", "Dex", true));
        skills.add(createSkill("Estimation", "Int", false));
        skills.add(createSkill("Évasion", "Dex", false));
        skills.add(createSkill("Intimidation", "Cha", false));
        skills.add(createSkill("Linguistique", "Int", true));
        skills.add(createSkill("Natation", "For", false));
        skills.add(createSkill("Perception", "Sag", false));
        skills.add(createSkill("Premiers secours", "Sag", false));
        skills.add(createSkill("Profession", "Sag", true));
        skills.add(createSkill("Psychologie", "Sag", false));
        skills.add(createSkill("Représentation", "Sag", false));
        skills.add(createSkill("Sabotage", "Dex", true));
        skills.add(createSkill("Survie", "Sag", false));
        skills.add(createSkill("Utilisation d'objets magiques", "Cha", true));
        skills.add(createSkill("Vol", "Dex", false));

        String logoPath = "/home/sven/AndroidStudioProjects/PathfinderFR/app/src/main/assets/pdf-logo.png";

        List<Weapon> weapons = new ArrayList<>();
        Weapon w = new Weapon();
        w.setName("Arc long");
        w.setDamageSmall("1d6");
        w.setDamageMedium("1d8");
        w.setCritical("x3");
        w.setRange("30 m (20 c)");
        w.setType("P");
        weapons.add(w);

        w = new Weapon();
        w.setName("Arc court composite");
        w.setDamageSmall("1d4");
        w.setDamageMedium("1d6");
        w.setCritical("19-20/x2");
        w.setRange("20 m (15 c)");
        w.setType("P");
        weapons.add(w);

        w = new Weapon();
        w.setName("Hache d'armes");
        w.setDamageSmall("1d6");
        w.setDamageMedium("1d8");
        w.setCritical("x3");
        w.setRange("--");
        w.setType("T");
        weapons.add(w);

        List<Armor> armors = new ArrayList<>();
        Armor a = new Armor();
        a.setName("Armure de cuir en peau de dragon");
        a.setCost("10 po");
        a.setBonus("+2");
        a.setBonusDexMax("+6");
        a.setMalus("0");
        a.setCastFail("10%");
        a.setWeight("7,5 kg");
        armors.add(a);

        File file = new File("/tmp/test.pdf");
        FileOutputStream fos = new FileOutputStream(file);
        CharacterPDF.Options opts = new CharacterPDF.Options();
        opts.printInkSaving = false;
        opts.printLogo = true;
        (new CharacterPDF(opts, c, skills, weapons, armors)).generatePDF(fos, ImageDataFactory.create(logoPath));
        fos.close();
    }
}
