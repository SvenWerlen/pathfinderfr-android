package org.pathfinderfr.app.pdf;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.IRenderer;

import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardsPDF {

    private static final int MARGIN = 30;
    private static final int CARD_WIDTH = 175;
    private static final int CARD_HEIGHT = 245;
    private static final int CARD_TITLE = CARD_HEIGHT - 19;
    private static final int CARD_TITLE_MARGIN = 10;
    private static final int CARD_TITLE_WIDTH = 130;
    private static final int CARD_TITLE_MAXLENGTH = 25;
    private static final int CARD_PROP = 200;
    private static final int CARD_PROP_SIZE = 16;
    private static final int CARD_SUBTITLE = CARD_HEIGHT - 29;
    private static final int CARD_SUBTITLE_MARGIN = 10;
    private static final int CARD_SUBTITLE_WIDTH = 130;
    private static final int CARD_CONTENT = 13;
    private static final int CARD_CONTENT_MARGIN = 18;
    private static final int CARD_CONTENT_WIDTH = 145;
    private static final int CARD_CONTENT_MAXHEIGHT = CARD_HEIGHT - 45;
    private static final float CARD_FOOTER = 1.5f;
    private static final int CARD_FOOTER_MARGIN = 17;
    private static final int CARD_FOOTER_WIDTH = 145;
    private static final int CARD_BOX_X = CARD_WIDTH - 31;
    private static final int CARD_BOX_Y = CARD_HEIGHT - 27;
    private static final int CARD_BOX_WIDTH = 20;

    private static final String TEMPLATE_PROP = "<b><i>PROPNAME:</i></b> PROPVALUE<br/>";
    private static final String TEMPLATE_FEAT = "CONDITIONS NORMAL<br/>DESCRIPTION";
    private static final String TEMPLATE_SPELL = "CASTINGTIME DURATION RANGE AREA TARGET SAVINGTHROW RESISTANCE<br/>DESCRIPTION";

    private static final int BACK_FEAT = 0;
    private static final int BACK_FEATURE = 1;
    private static final int BACK_SPELL = 2;
    private static final String[] BACKLABELS = { "Don", "Aptitude", "Sort" };

    private static final Style STYLE_TITLE;
    private static final Style STYLE_SUBTITLE;
    private static final Style STYLE_FOOTER;
    private static final Style STYLE_BOX;
    private static final Style STYLE_COMPONENT;
    private static final Style STYLE_BACK;
    private static final int FONTSIZE_CONTENT = 6;

    List<String> classes;
    private List<Feat> feats;
    private List<ClassFeature> features;
    private List<Spell> spells;
    private Params params;
    private Image cardImg[];
    private Image propImg[];
    private Image backImg[];

    private static Document simulDoc;

    static {
        STYLE_TITLE = new Style().setFontSize(10).setItalic().setFontColor(ColorConstants.WHITE);
        STYLE_SUBTITLE = new Style().setFontSize(7).setItalic().setFontColor(ColorConstants.WHITE);
        STYLE_FOOTER = new Style().setFontSize(6).setItalic().setFontColor(ColorConstants.WHITE);
        STYLE_BOX = new Style().setFontSize(16).setBold().setFontColor(ColorConstants.DARK_GRAY);
        STYLE_COMPONENT = new Style().setFontSize(6).setBold().setFontColor(ColorConstants.WHITE);
        STYLE_BACK = new Style().setFontSize(15).setFontColor(ColorConstants.WHITE);
        simulDoc = new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream())));
    }

    public static class Params {
        public PdfFont titleFont;
        public ImageData[] cardFront;
        public ImageData[] cardProp;
        public ImageData[] cardBack;
        public boolean printBack = true;
        public boolean feats;
        public boolean features;
        public boolean spells;
    }

    public CardsPDF(List<String> classes, List<Feat> feats, List<ClassFeature> features, List<Spell> spells, Params params) {
        this.classes = classes;
        this.feats = feats;
        this.features = features;
        this.spells = spells;
        this.params = params;
        cardImg = new Image[this.params.cardFront.length];
        for(int i=0; i<this.params.cardFront.length; i++) {
            cardImg[i] = (new Image(this.params.cardFront[i])).setWidth(CARD_WIDTH);
        }
        propImg = new Image[this.params.cardProp.length];
        for(int i=0; i<this.params.cardProp.length; i++) {
            propImg[i] = (new Image(this.params.cardProp[i])).setWidth(CARD_PROP_SIZE);
        }
        backImg = new Image[this.params.cardBack.length];
        for(int i=0; i<this.params.cardBack.length; i++) {
            backImg[i] = (new Image(this.params.cardBack[i])).setWidth(CARD_WIDTH);
        }
    }

    private static String prepareText(String s, int maxLength, boolean stripAccents, boolean upperCase) {
        s = StringUtil.smartSubstring(s, maxLength);
        if(stripAccents) {
            s = Normalizer.normalize(s, Normalizer.Form.NFD);
            s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        }
        if(upperCase) {
            s = s.toUpperCase();
        }
        return s;
    }

    private String generateProp(String key, String value) {
        return value == null ? "" : TEMPLATE_PROP.replaceAll( "PROPVALUE", value).replaceAll("PROPNAME", key );
    }

    private Paragraph generateHTMLElement(String html) {
        try {
            while(true) {
                List<IElement> elements = HtmlConverter.convertToElements(html);
                if (elements.size() > 0) {
                    Paragraph paragraph = (Paragraph) elements.get(0);
                    for (IElement el : paragraph.getChildren()) {
                        ((Text) el).setFontSize(FONTSIZE_CONTENT);
                    }
                    paragraph.setMultipliedLeading(0.9f)
                            .setWidth(CARD_CONTENT_WIDTH)
                            .setVerticalAlignment(VerticalAlignment.TOP);
                    // check if fits size
                    IRenderer paragraphRenderer = paragraph.createRendererSubTree();
                    LayoutResult result = paragraphRenderer.setParent(simulDoc.getRenderer()).
                            layout(new LayoutContext(new LayoutArea(1, new Rectangle(CARD_CONTENT_WIDTH, 1000))));
                    if(result.getOccupiedArea().getBBox().getHeight() > CARD_CONTENT_MAXHEIGHT) {
                        html = html.substring(0, html.length() - 20) + "...";
                        continue;
                    }
                    return paragraph.setHeight(CARD_CONTENT_MAXHEIGHT);
                }
            }
        } catch(Exception e) {}
        return null;
    }


    /**
     * Generate a card for a Feat
     */
    private void addCard(Document document, int left, int bottom, Feat feat) {
        // backgrounds
        int idx = 7;
        if(cardImg.length == 1) { idx = 0; }
        cardImg[idx].setFixedPosition(left, bottom);
        document.add(cardImg[idx]);
        // texts
        String title = prepareText(feat.getName(), CARD_TITLE_MAXLENGTH, false, false);
        String subtitle = feat.getName().length() > CARD_TITLE_MAXLENGTH ? feat.getName().substring(title.length()) : "";
        document.add((new Paragraph(title))
                .addStyle(STYLE_TITLE) //.setFont(params.titleFont)
                .setTextAlignment(TextAlignment.LEFT)
                .setMaxHeight(15)
                .setFixedPosition(left + CARD_TITLE_MARGIN, bottom + CARD_TITLE, CARD_TITLE_WIDTH));
        document.add((new Paragraph(subtitle))
                .addStyle(STYLE_SUBTITLE)
                .setTextAlignment(TextAlignment.LEFT)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_SUBTITLE_MARGIN, bottom + CARD_SUBTITLE, CARD_SUBTITLE_WIDTH));
        document.add((new Paragraph(prepareText(feat.getCategory(), 45, false, false)))
                .addStyle(STYLE_FOOTER)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_FOOTER_MARGIN, bottom + CARD_FOOTER, CARD_FOOTER_WIDTH));

        // description
        String html = TEMPLATE_FEAT.replaceAll("CONDITIONS", generateProp("Condition", feat.getConditions()))
                .replaceAll("NORMAL", generateProp("Normal", feat.getNormal()))
                .replaceAll("DESCRIPTION", feat.getDescription().replaceAll("\n", "<br/>"));
        document.add(generateHTMLElement(html)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setFixedPosition(left + CARD_CONTENT_MARGIN -7, bottom + CARD_CONTENT, CARD_CONTENT_WIDTH +8));
    }


    /**
     * Generate a card for a Feature
     */
    private void addCard(Document document, int left, int bottom, ClassFeature feature) {
        // backgrounds
        int idx = 7;
        if(cardImg.length == 1) { idx = 0; }
        cardImg[idx].setFixedPosition(left, bottom);
        document.add(cardImg[idx]);
        // linkedTo ?
        String linkedTo = null;
        if(feature.getLinkedTo() != null) {
            linkedTo = feature.getName();
            feature = feature.getLinkedTo();
        }
        // split name (ex: Exploitation: Contresort => Exploitation AND Contresort)
        String name = feature.getName();
        String type = feature.getClass_().getName();
        int idxType = name.indexOf(": ");
        if(idxType > 0) {
            type += ": " + name.substring(0, idxType);
            name = name.substring(idxType + 2);
        }
        if(linkedTo != null) {
            type = feature.getClass_().getName() + ": " + linkedTo;
        }
        if(feature.getLinkedName() != null && feature.getLinkedName().length() > 0) {
            type = feature.getClass_().getName() + ": " + name;
            name = feature.getLinkedName();
        }
        // texts
        String title = prepareText(name, CARD_TITLE_MAXLENGTH, false, false);
        String subtitle = name.length() > CARD_TITLE_MAXLENGTH ? name.substring(title.length()) : "";
        document.add((new Paragraph(title))
                .addStyle(STYLE_TITLE) //.setFont(params.titleFont)
                .setTextAlignment(TextAlignment.LEFT)
                .setMaxHeight(15)
                .setFixedPosition(left + CARD_TITLE_MARGIN, bottom + CARD_TITLE, CARD_TITLE_WIDTH));
        document.add((new Paragraph(subtitle))
                .addStyle(STYLE_SUBTITLE)
                .setTextAlignment(TextAlignment.LEFT)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_SUBTITLE_MARGIN, bottom + CARD_SUBTITLE, CARD_SUBTITLE_WIDTH));
        document.add((new Paragraph(prepareText(type, 45, false, false)))
                .addStyle(STYLE_FOOTER)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_FOOTER_MARGIN, bottom + CARD_FOOTER, CARD_FOOTER_WIDTH));
        document.add((new Paragraph(String.valueOf(feature.getLevel())))
                .addStyle(STYLE_BOX)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(left + CARD_BOX_X, bottom + CARD_BOX_Y, CARD_BOX_WIDTH));

        // description
        document.add(generateHTMLElement(feature.getDescription())
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setFixedPosition(left + CARD_CONTENT_MARGIN -7, bottom + CARD_CONTENT, CARD_CONTENT_WIDTH +8));
    }

    /**
     * Generate a card for a Spell
     */
    private void addCard(Document document, int left, int bottom, Spell spell) {
        // backgrounds
        int idx = 7;
        if(cardImg.length == 1) { idx = 0; }
        else if(spell.getSchool().startsWith("Universel")) { idx = 0; }
        else if(spell.getSchool().startsWith("Abjuration")) { idx = 1; }
        else if(spell.getSchool().startsWith("Divination")) { idx = 2; }
        else if(spell.getSchool().startsWith("Enchantement")) { idx = 3; }
        else if(spell.getSchool().startsWith("Évocation")) { idx = 4; }
        else if(spell.getSchool().startsWith("Illusion")) { idx = 5; }
        else if(spell.getSchool().startsWith("Invocation")) { idx = 6; }
        else if(spell.getSchool().startsWith("Nécromancie")) { idx = 7; }
        else if(spell.getSchool().startsWith("Transmutation")) { idx = 8; }
        cardImg[idx].setFixedPosition(left, bottom);
        document.add(cardImg[idx]);
        // texts
        String title = prepareText(spell.getName(), CARD_TITLE_MAXLENGTH, false, false);
        String subtitle = spell.getName().length() > CARD_TITLE_MAXLENGTH ? spell.getName().substring(title.length()) : "";
        Integer[] levels = retrieveLevels(spell.getLevel());
        Integer minLvl = null;
        StringBuffer levelString = new StringBuffer();
        for(int i=0; i<levels.length; i++) {
            if(levels[i] != null) {
                levelString.append(classes.get(i)).append(' ').append(levels[i]).append(", ");
                if(minLvl == null || levels[i] < minLvl) {
                    minLvl = levels[i];
                }
            }
        }
        if(levelString.length() > 0) {
            levelString.delete(levelString.length()-2, levelString.length());
        }
        document.add((new Paragraph(title))
                .addStyle(STYLE_TITLE) //.setFont(params.titleFont)
                .setTextAlignment(TextAlignment.LEFT)
                .setMaxHeight(15)
                .setFixedPosition(left + CARD_TITLE_MARGIN, bottom + CARD_TITLE, CARD_TITLE_WIDTH));
        document.add((new Paragraph(subtitle))
                .addStyle(STYLE_SUBTITLE)
                .setTextAlignment(TextAlignment.LEFT)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_SUBTITLE_MARGIN, bottom + CARD_SUBTITLE, CARD_SUBTITLE_WIDTH));
        document.add((new Paragraph(prepareText(spell.getSchool(), 22, false, false)))
                .addStyle(STYLE_FOOTER)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_FOOTER_MARGIN, bottom + CARD_FOOTER, CARD_FOOTER_WIDTH));
        document.add((new Paragraph(prepareText(levelString.toString(), 22, false, false)))
                .addStyle(STYLE_FOOTER)
                .setMaxHeight(12)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFixedPosition(left + CARD_FOOTER_MARGIN, bottom + CARD_FOOTER, CARD_FOOTER_WIDTH));
        if(minLvl != null) {
            document.add((new Paragraph(String.valueOf(minLvl)))
                    .addStyle(STYLE_BOX)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFixedPosition(left + CARD_BOX_X, bottom + CARD_BOX_Y, CARD_BOX_WIDTH));
        }
        // components
        List<String> components = spell.getComponentList();
        for(int i = 0; i<components.size(); i++) {
            propImg[idx].setFixedPosition(left + 1, bottom + CARD_PROP - (CARD_PROP_SIZE) * i );
            document.add(propImg[idx]);
            String comp = components.get(i);
            document.add((new Paragraph(prepareText(comp, 0, false, false)))
                    .addStyle(STYLE_COMPONENT)
                    .setFontSize(comp.length() <= 2 ? 6 : 4)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setHeight(CARD_PROP_SIZE)
                    .setFixedPosition(left + 1, bottom + 0.5f + CARD_PROP - (CARD_PROP_SIZE) * i, CARD_PROP_SIZE-2));
        }

        // description
        String castingTime = generateProp("Temps d'incantation", spell.getCastingTime());
        String duration = generateProp("Durée", spell.getDuration());
        String range = generateProp("Portée", spell.getRange());
        String area = generateProp("Zone d'effet", spell.getArea());
        String target = generateProp("Cible", spell.getTarget());
        String saving = generateProp("Jet de sauvegarde", spell.getSavingThrow());
        String resistance = generateProp("Résistance à la magie", spell.getSpellResistance());
        String html = TEMPLATE_SPELL.replaceAll("CASTINGTIME", castingTime )
                .replaceAll( "DURATION", duration)
                .replaceAll( "RANGE", range)
                .replaceAll( "AREA", area)
                .replaceAll( "TARGET", target)
                .replaceAll( "SAVINGTHROW", saving)
                .replaceAll( "RESISTANCE", resistance)
                .replaceAll( "DESCRIPTION", spell.getDescription().replaceAll("\n", "<br/>"));
        document.add(generateHTMLElement(html)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setFixedPosition(left + CARD_CONTENT_MARGIN, bottom + CARD_CONTENT, CARD_CONTENT_WIDTH));
    }

    /**
     * Retrieves the spell level for each class
     *
     * @param levels spell levels (ex: Alc 6, Bar 5, Ens/Mag 6, Hyp 5, Spi 6)
     * @return min level (ex: [5, 6] for a [Bar, Mag] or [6, null] for a [Spi, Brb])
     */
    private Integer[] retrieveLevels(String levels) {
        Integer[] results = new Integer[classes.size()];
        for(int i=0; i<classes.size(); i++) {
            String cl = classes.get(i);
            Pattern pattern = Pattern.compile(".*" + cl + "(?:/.{3})? (\\d).*");
            Matcher m = pattern.matcher(levels);
            if(m.matches()) {
                results[i] = Integer.parseInt(m.group(1));
            } else {
                results[i] = null;
            }
        }
        return results;
    }

    public void addBack(Document document, int left, int bottom, int backType) {
        backImg[backType].setFixedPosition(left, bottom);
        document.add(backImg[backType]);
        // shadow
        document.add((new Paragraph(BACKLABELS[backType]))
                .addStyle(STYLE_BACK)
                .setFont(params.titleFont)
                .setFontColor(ColorConstants.BLACK)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(left+1, bottom + 34, CARD_WIDTH));
        document.add((new Paragraph(BACKLABELS[backType]))
                .addStyle(STYLE_BACK)
                .setFont(params.titleFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(left, bottom + 35, CARD_WIDTH));
    }


    private boolean generateSection(Document document, PageSize pageSize, int backType, List<Object> objects) {
        int posY1 = (int) Math.round(pageSize.getHeight() / 2);
        int posY2 = (int) Math.round(pageSize.getHeight() / 2) - CARD_HEIGHT - 5;

        if(objects.size() == 0) {
            return false;
        }

        if(params.printBack) {
            for (int i = 0; i < objects.size(); i++) {
                int posY = i % 4 <= 1 ? posY1 : posY2;
                int left = (2 * CARD_WIDTH + 5) * (i % 2) + MARGIN;
                Object obj = objects.get(i);
                if(obj instanceof Spell) {
                    addCard(document, left, posY, (Spell) obj);
                } else if (obj instanceof Feat) {
                    addCard(document, left, posY, (Feat) obj);
                } else if (obj instanceof ClassFeature) {
                    addCard(document, left, posY, (ClassFeature) obj);
                } else {
                    throw new IllegalStateException("Not supported: " + obj.getClass().getSimpleName());
                }
                addBack(document, left + CARD_WIDTH, posY, backType);
                if (i % 4 == 3) {
                    document.add(new AreaBreak());
                }
            }
        } else {
            for (int i = 0; i < objects.size(); i++) {
                int posY = i % 8 <= 3 ? posY1 : posY2;
                int left = (CARD_WIDTH + 5) * (i % 4) + MARGIN;
                Object obj = objects.get(i);
                if(obj instanceof Spell) {
                    addCard(document, left, posY, (Spell) obj);
                } else if (obj instanceof Feat) {
                    addCard(document, left, posY, (Feat) obj);
                } else if (obj instanceof ClassFeature) {
                    addCard(document, left, posY, (ClassFeature) obj);
                } else {
                    throw new IllegalStateException("Not supported: " + obj.getClass().getSimpleName());
                }
                if (i % 8 == 7) {
                    document.add(new AreaBreak());
                }
            }
            // print back
            document.add(new AreaBreak());
            for(int j = 0; j < 8; j++) {
                addBack(document, (CARD_WIDTH + 5) * (j % 4) + MARGIN, j % 8 <= 3 ? posY1 : posY2, backType);
            }
        }
        return true;
    }

    public void generatePDF(OutputStream output) {
        PageSize pageSize = PageSize.A4.rotate();
        PdfDocument pdf = new PdfDocument(new PdfWriter(output));
        Document document = new Document(pdf, pageSize);
        document.setMargins(20, 20,20,20);

        boolean addBreak = false;
        if(params.feats && feats.size() > 0) {
            if(addBreak) { document.add(new AreaBreak()); }
            List<Object> objects = new ArrayList<>(); objects.addAll(feats);
            addBreak = generateSection(document, pageSize, BACK_FEAT, objects);
        }
        if(params.features && features.size() > 0) {
            if(addBreak) { document.add(new AreaBreak()); }
            List<Object> objects = new ArrayList<>(); objects.addAll(features);
            // remove all features that are linked and not auto (to avoid duplicated)
            for(ClassFeature cl : features) {
                if(cl.getLinkedTo() != null && !cl.isAuto()) {
                    objects.remove(cl);
                }
            }
            addBreak = generateSection(document, pageSize, BACK_FEATURE, objects);
        }
        if(params.spells && spells.size() > 0) {
            if(addBreak) { document.add(new AreaBreak()); }
            List<Object> objects = new ArrayList<>(); objects.addAll(spells);
            addBreak = generateSection(document, pageSize, BACK_SPELL, objects);
        }

        document.close();
    }
}

