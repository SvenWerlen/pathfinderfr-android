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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class CardsPDFTest {

    @Before
    public void init() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void test() throws IOException {

        String base = "/home/sven/AndroidStudioProjects/PathfinderFR/app/src/main/assets/cards";

        File file = new File("/tmp/cards.pdf");
        FileOutputStream fos = new FileOutputStream(file);

        // load spells from GIT
        File fileSpells = new File("/tmp/spells.yml");
        if(!fileSpells.exists()) {
            URL website = new URL("https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/Feature/3.6/data/spells.yml");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fosSpells = new FileOutputStream("/tmp/spells.yml");
            fosSpells.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }

        // get spells
        List<Spell> spells = new ArrayList<>();
        SpellFactory factory = SpellFactory.getInstance();
        YamlReader reader = new YamlReader(new InputStreamReader(new FileInputStream(fileSpells), "UTF-8"));
        List list  = (List)reader.read();
        for(int i=0; i<10; i++) {
            Spell spell = (Spell)factory.generateEntity((Map<String,Object>)list.get(i));
            spells.add(spell);
        }

        PdfFont font = PdfFontFactory.createFont(base + "/FOY1REG.TTF", "UTF-8", true);
        CardsPDF.Params params = new CardsPDF.Params();
        params.cardFront = new ImageData[9];
        for(int i=1; i<10; i++) {
            params.cardFront[i-1] = ImageDataFactory.create(String.format("%s/card%d.png", base, i));
        }
        params.cardProp = new ImageData[9];
        for(int i=1; i<10; i++) {
            params.cardProp[i-1] = ImageDataFactory.create(String.format("%s/comp%d.png", base, i));
        }
        params.cardBack = ImageDataFactory.create(String.format("%s/back.png", base));
        params.titleFont = font;
        params.printBack = false;
        (new CardsPDF(spells, params)).generatePDF(fos);
        fos.close();
    }
}
