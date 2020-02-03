package org.pathfinderfr.app.util;

import android.util.Log;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.pdf.CardsPDF;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class CardsPDFTest {

    @Before
    public void init() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void test() throws IOException {

        int FEATS = 3;
        int SPELLS = 3;

        String base = "/home/sven/AndroidStudioProjects/PathfinderFR/app/src/main/assets/cards";

        List<String> classes = Arrays.asList("Bar", "Ens");

        File file = new File("/tmp/cards.pdf");
        FileOutputStream fos = new FileOutputStream(file);

        // load feats from GIT
        File fileFeats = new File("/tmp/dons.yml");
        if(!fileFeats.exists()) {
            URL website = new URL("https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/Feature/3.6/data/dons.yml");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fosSpells = new FileOutputStream("/tmp/dons.yml");
            fosSpells.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }

        // load spells from GIT
        File fileSpells = new File("/tmp/spells.yml");
        if(!fileSpells.exists()) {
            URL website = new URL("https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/Feature/3.6/data/spells.yml");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fosSpells = new FileOutputStream("/tmp/spells.yml");
            fosSpells.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }

        // get feats
        List<Feat> feats = new ArrayList<>();
        FeatFactory featFactory = FeatFactory.getInstance();
        YamlReader featReader = new YamlReader(new InputStreamReader(new FileInputStream(fileFeats), "UTF-8"));
        List featList  = (List)featReader.read();
        for(int i=0; i<FEATS; i++) {
            Feat feat = (Feat)featFactory.generateEntity((Map<String,Object>)featList.get(i));
            feats.add(feat);
        }

        // get class features
        List<ClassFeature> features = new ArrayList<>();
        Class cl = new Class();
        cl.setName("Ensorceleur");
        ClassFeature feature = new ClassFeature();
        feature.setName("Lignage Aquatique: Soulèvement des profondeurs (Mag)");
        feature.setLevel(15);
        feature.setDescription("Au niveau 15, l’ensorceleur peut soulever de l’eau comme avec un" +
                "sort de contrôle de l’eau sans que la présence d’eau ne soit nécessaire. L’eau" +
                "ainsi créée est stationnaire et ne coule pas hors de la zone où elle est créée." +
                "Elle reste en place 1 round par niveau d’ensorceleur puis disparaît. Au niveau" +
                "20, les dimensions de cet effet sont doublées. L’ensorceleur peut utiliser cette" +
                "capacité une fois par jour.");
        feature.setClass(cl);
        features.add(feature);

        cl = new Class();
        cl.setName("Roublard");
        feature = new ClassFeature();
        feature.setName("Talent de roublard #2");
        feature.setLevel(6);
        feature.setDescription("Aux niveaux 3, 6 et 9, le maître des ombres gagne une capacité qui" +
                "lui permet de confondre ses adversaires. Cela fonctionne comme les talents de" +
                "la classe de roublard. Il ne peut sélectionner un talent plus d''une fois. S''il" +
                "a accès aux talents de maître roublard, il peut faire son choix dans cette liste.");
        feature.setClass(cl);
        ClassFeature featureLinked = new ClassFeature();
        featureLinked.setName("Talent: Acrobate inégalable (Ext)");
        featureLinked.setDescription("Une fois par jour, un roublard disposant de ce talent peut lancer" +
                "deux dés lors d’un test d’Acrobaties et choisir le résultat le plus avantageux." +
                "Il doit décider d’utiliser cette capacité avant le test. Il peut utiliser cette" +
                "capacité une fois de plus par jour par tranche de 5 niveaux de roublard.");
        featureLinked.setClass(cl);
        featureLinked.setLevel(3);
        feature.setLinkedTo(featureLinked);
        features.add(feature);


        // get spells
        List<Spell> spells = new ArrayList<>();
        SpellFactory spellFactory = SpellFactory.getInstance();
        YamlReader spellReader = new YamlReader(new InputStreamReader(new FileInputStream(fileSpells), "UTF-8"));
        List spelllist  = (List)spellReader.read();
        for(int i=0; i<SPELLS; i++) {
            Spell spell = (Spell)spellFactory.generateEntity((Map<String,Object>)spelllist.get(i));
            spells.add(spell);
        }

        PdfFont font = PdfFontFactory.createFont(AssetUtil.assetToBytes(new FileInputStream(new File(base + "/FOY1REG.TTF"))), "UTF-8", true);
        CardsPDF.Params params = new CardsPDF.Params();
        // full colors
        if(false) {
            params.cardFront = new ImageData[9];
            for (int i = 1; i < 10; i++) {
                params.cardFront[i - 1] = ImageDataFactory.create(String.format("%s/card%d.png", base, i));
            }
            params.cardProp = new ImageData[9];
            for (int i = 1; i < 10; i++) {
                params.cardProp[i - 1] = ImageDataFactory.create(String.format("%s/comp%d.png", base, i));
            }
        } else {
            params.cardFront = new ImageData[] { ImageDataFactory.create(String.format("%s/card%d.png", base, 8)) };
            params.cardProp = new ImageData[] { ImageDataFactory.create(String.format("%s/comp%d.png", base, 8)) };
        }
        params.cardBack = ImageDataFactory.create(String.format("%s/back.png", base));
        params.titleFont = font;
        params.printBack = true;
        params.feats = true;
        params.features = true;
        params.spells = true;
        (new CardsPDF(classes, feats, features, spells, params)).generatePDF(fos);
        fos.close();
    }
}
