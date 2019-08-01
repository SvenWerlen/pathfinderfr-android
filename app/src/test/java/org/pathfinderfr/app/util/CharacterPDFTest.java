package org.pathfinderfr.app.util;

import com.itextpdf.io.image.ImageDataFactory;

import org.junit.Test;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterImportExport;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.Race;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CharacterPDFTest {

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
        cl.getLevels().add(new Class.Level(1, new int[]{1}, 0,2,0,0));
        cl.getLevels().add(new Class.Level(2, new int[]{2}, 0,3,0,0));
        cl.getLevels().add(new Class.Level(7, new int[]{7, 2}, 2,5,2,0));
        c.addOrSetClass(cl, null, 7);
        Race r = new Race();
        r.setName("Humain");
        c.setRace(r);
        c.setSpeed(6);
        c.setHitpoints(125);

        String logoPath = "/home/sven/AndroidStudioProjects/PathfinderFR/app/src/main/assets/pdf-logo.png";

        File file = new File("/tmp/test.pdf");
        FileOutputStream fos = new FileOutputStream(file);
        (new CharacterPDF(c)).generatePDF(fos, ImageDataFactory.create(logoPath));
        fos.close();
    }
}
