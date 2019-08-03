package org.pathfinderfr.app.util;

import com.itextpdf.io.image.ImageDataFactory;

import org.junit.Test;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterImportExport;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.Skill;

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
        c.setStrength(11);
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
        c.setHitpoints(125);
        c.setSkillRank(1, 3);

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

        File file = new File("/tmp/test.pdf");
        FileOutputStream fos = new FileOutputStream(file);
        (new CharacterPDF(c, skills)).generatePDF(fos, ImageDataFactory.create(logoPath));
        fos.close();
    }
}
