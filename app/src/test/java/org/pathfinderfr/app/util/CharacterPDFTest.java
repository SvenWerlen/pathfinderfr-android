package org.pathfinderfr.app.util;

import android.util.Log;

import com.itextpdf.io.image.ImageDataFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.Modification;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.database.entity.Weapon;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class CharacterPDFTest {

    public static int skillId;

    @Before
    public void init() {
        PowerMockito.mockStatic(Log.class);
    }

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
        cl.setName("Barde");
        cl.getSkills().add("Acrobatie");
        cl.getLevels().add(new Class.Level(1, new int[]{1}, 0,2,0,2));
        cl.getLevels().add(new Class.Level(2, new int[]{2}, 0,3,0,3));
        cl.getLevels().add(new Class.Level(7, new int[]{7, 2}, 2,5,2,4));
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

        List<Modification> modifications = c.getModifications();
        Modification modif = new Modification("Race", new ArrayList<Pair<Integer, Integer>>(), "-", true);
        modifications.add(modif);

        List<Pair<Integer, Integer>> modifs = new ArrayList<Pair<Integer, Integer>>();
        modifs.add(new Pair<>(Modification.MODIF_COMBAT_AC_ARMOR, 7));
        modif = new Modification("Armure de plates", modifs, "-", true);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_COMBAT_AC_SHIELD, 2));
        modif = new Modification("Écu", modifs, "-", true);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_COMBAT_AC_NATURAL, 3));
        modif = new Modification("Peau d'écorce", modifs, "-", true);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_COMBAT_AC_PARADE, 1));
        modif = new Modification("Bracelets", modifs, "-", true);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_COMBAT_AC, -2));
        modif = new Modification("Bague", modifs, "-", true);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_SAVES_ALL, 1));
        modifs.add(new Pair<>(Modification.MODIF_SAVES_MAG_ALL, 2));
        modifs.add(new Pair<>(Modification.MODIF_SAVES_MAG_REF, 1));
        modif = new Modification("Objet réflexes", modifs, "-", false);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_SAVES_ALL, 1));
        modifs.add(new Pair<>(Modification.MODIF_SAVES_MAG_ALL, 2));
        modifs.add(new Pair<>(Modification.MODIF_SAVES_MAG_FOR, 2));
        modif = new Modification("Objet réflexes", modifs, "-", false);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_SAVES_ALL, 1));
        modifs.add(new Pair<>(Modification.MODIF_SAVES_MAG_ALL, 2));
        modifs.add(new Pair<>(Modification.MODIF_SAVES_MAG_WIL, 3));
        modif = new Modification("Objet réflexes", modifs, "-", false);
        modifications.add(modif);

        modifs = new ArrayList<>();
        modifs.add(new Pair<>(Modification.MODIF_COMBAT_MAG, 1));
        modifs.add(new Pair<>(Modification.MODIF_COMBAT_INI, 2));
        modif = new Modification("Objet combat", modifs, "-", true);
        modifications.add(modif);

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

        List<CharacterItem> inventory = c.getInventoryItems();
        List<Weapon> weapons = new ArrayList<>();
        Weapon w = new Weapon();
        w.setName("Arc long");
        w.setDamageSmall("1d6");
        w.setDamageMedium("1d8");
        w.setCritical("x3");
        w.setRange("30 m (20 c)");
        w.setType("P");
        weapons.add(w);
        inventory.add(new CharacterItem(c.getId(), "Arc long", 1500, 1000L, CharacterItem.IDX_WEAPONS + 5, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));

        w = new Weapon();
        w.setName("Arc court composite");
        w.setDamageSmall("1d4");
        w.setDamageMedium("1d6");
        w.setCritical("19-20/x2");
        w.setRange("20 m (15 c)");
        w.setType("P");
        weapons.add(w);
        inventory.add(new CharacterItem(c.getId(), "Arc long composite", 1500, 1000L, CharacterItem.IDX_WEAPONS + 5, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));

        w = new Weapon();
        w.setName("Hache d'armes");
        w.setDamageSmall("1d6");
        w.setDamageMedium("1d8");
        w.setCritical("x3");
        w.setRange("--");
        w.setType("T");
        weapons.add(w);
        inventory.add(new CharacterItem(c.getId(), "Hache d'armes", 3000, 1000L, CharacterItem.IDX_WEAPONS + 5, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));


        List<Armor> armors = new ArrayList<>();
        Armor a = new Armor();
        a.setName("Armure de cuir en peau de dragon");
        a.setCategory("Armure légère");
        a.setCost("10 po");
        a.setBonus("+2");
        a.setBonusDexMax("+6");
        a.setMalus("0");
        a.setCastFail("10%");
        a.setWeight("7,5 kg");
        armors.add(a);
        inventory.add(new CharacterItem(c.getId(), "Armure de cuir en peau de dragon", 7500, 1000L, CharacterItem.IDX_ARMORS + 5, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));


        inventory.add(new CharacterItem(c.getId(), "Corde en chanvre, 15 m", 5000, 1000L, 0, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));
        inventory.add(new CharacterItem(c.getId(), "Cadenas (bon)", 500, 1000L, 0, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));
        inventory.add(new CharacterItem(c.getId(), "1234567890 1234567890 1234567890 123", 1250, 1000L, 0, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));

        for(int i = 1; i<= 40; i++) {
            inventory.add(new CharacterItem(c.getId(), String.format("Fill %02d", i), 100, 1000L, 0, null, CharacterItem.CATEGORY_UNCLASSIFIED, CharacterItem.LOCATION_NOLOC));
        }

        c.getRace().getTraits().add(new Race.Trait("Taille moyenne",""));
        c.getRace().getTraits().add(new Race.Trait("Vitesse normale",""));
        c.getRace().getTraits().add(new Race.Trait("Don en bonus",""));
        c.getRace().getTraits().add(new Race.Trait("Compétent",""));
        c.getRace().getTraits().add(new Race.Trait("Langues supplémentaires",""));

        Trait t = new Trait();
        t.setName("Trait de combat: Courageux");
        c.addTrait(t);
        t = new Trait();
        t.setName("Trait de foi: Érudit du Grand Au-Delà");
        c.addTrait(t);

        Feat f = new Feat();
        f.setName("Tours de magie ou oraisons supplémentaires");
        f.setSummary("Gain de 2 tours de magie ou oraisons connus");
        c.addFeat(f);
        f = new Feat();
        f.setName("Derviche dimensionnel");
        f.setSummary("Pendant une action complexe, le personnage peut lancer porte dimensionnelle ou utiliser pas chassé par une action rapide");
        c.addFeat(f);
        f = new Feat();
        f.setName("Manœuvres dimensionnelles");
        c.addFeat(f);
        f = new Feat();
        f.setName("Savant dimensionnel");
        c.addFeat(f);
        f = new Feat();
        f.setName("Aisance");
        c.addFeat(f);
        f = new Feat();
        f.setName("Déplacement acrobatique");
        c.addFeat(f);
        f = new Feat();
        f.setName("Pas léger");
        c.addFeat(f);
        f = new Feat();
        f.setName("Déplacement mystique");
        c.addFeat(f);

        for(int i = 0; i < 20; i++) {
            c.addFeat(f);
        }

        ClassFeature cf = new ClassFeature();
        cf.setClass(cl);
        cf.setLevel(1);
        cf.setName("Armes et armures");
        c.addClassFeature(cf);

        cf = new ClassFeature();
        cf.setClass(cl);
        cf.setLevel(2);
        cf.setName("Érudition (Ext)");
        c.addClassFeature(cf);

        Spell s = new Spell();
        s.setName("Convocation d'instrument");
        s.setLevel("Bar 2");
        s.setId(2);
        s.setSchool("Invocation");
        c.addSpell(s);

        s = new Spell();
        s.setId(0);
        s.setName("Aspect de fée hantée");
        s.setLevel("Bar 0");
        s.setSchool("Invocation");
        c.addSpell(s);

        s = new Spell();
        s.setId(1);
        s.setName("Abondance de munitions - très long texte à couper");
        s.setLevel("Bar 1");
        s.setSchool("Expiration");
        c.addSpell(s);

        for(int i = 0; i<50; i++) {
            s = new Spell();
            s.setId(i+10);
            s.setName("Sort " + i);
            s.setLevel("Bar 3");
            s.setSchool("Test");
            c.addSpell(s);
        }

        File file = new File("/tmp/test.pdf");
        FileOutputStream fos = new FileOutputStream(file);
        CharacterPDF.Options opts = new CharacterPDF.Options();
        opts.printInkSaving = false;
        opts.printLogo = true;
        opts.showWeaponsInInventory = true;
        (new CharacterPDF(opts, c, skills, weapons, armors)).generatePDF(fos, ImageDataFactory.create(logoPath));
        fos.close();
    }
}
