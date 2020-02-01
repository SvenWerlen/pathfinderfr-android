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

import org.pathfinderfr.app.database.entity.Spell;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.List;

public class CardsPDF {

    private static final int MARGIN = 30;
    private static final int CARD_WIDTH = 175;
    private static final int CARD_HEIGHT = 245;
    private static final int CARD_TITLE = CARD_HEIGHT - 19;
    private static final int CARD_TITLE_MARGIN = 8;
    private static final int CARD_TITLE_WIDTH = 130;
    private static final int CARD_PROP = 200;
    private static final int CARD_PROP_SIZE = 16;
    private static final int CARD_SUBTITLE = CARD_HEIGHT - 29;
    private static final int CARD_SUBTITLE_MARGIN = 8;
    private static final int CARD_SUBTITLE_WIDTH = 130;
    private static final int CARD_CONTENT = 13;
    private static final int CARD_CONTENT_MARGIN = 18;
    private static final int CARD_CONTENT_WIDTH = 145;
    private static final int CARD_CONTENT_MAXHEIGHT = CARD_HEIGHT - 45;
    private static final int CARD_FOOTER = 2;
    private static final int CARD_FOOTER_MARGIN = 17;
    private static final int CARD_FOOTER_WIDTH = 130;

    private static final String TEMPLATE_PROP = "<b><i>PROPNAME:</i></b> PROPVALUE<br/>";
    private static final String TEMPLATE = "CASTINGTIME DURATION RANGE AREA TARGET SAVINGTHROW RESISTANCE<br/>DESCRIPTION";

    private static final Style STYLE_TITLE;
    private static final Style STYLE_SUBTITLE;
    private static final Style STYLE_FOOTER;
    private static final Style STYLE_COMPONENT;
    private static final int FONTSIZE_CONTENT = 6;

    private List<Spell> spells;
    private Params params;
    private Image cardImg[];
    private Image propImg[];
    private Image backImg;

    private static Document simulDoc;

    static {
        STYLE_TITLE = new Style().setFontSize(10).setItalic().setFontColor(ColorConstants.WHITE);
        STYLE_SUBTITLE = new Style().setFontSize(7).setItalic().setFontColor(ColorConstants.WHITE);
        STYLE_FOOTER = new Style().setFontSize(6).setItalic().setFontColor(ColorConstants.WHITE);
        STYLE_COMPONENT = new Style().setFontSize(6).setBold().setFontColor(ColorConstants.WHITE);
        simulDoc = new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream())));
    }

    public static class Params {
        public PdfFont titleFont;
        public ImageData[] cardFront;
        public ImageData[] cardProp;
        public ImageData cardBack;
    }

    public CardsPDF(List<Spell> spells, Params params) {
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
        backImg = (new Image(this.params.cardBack)).setWidth(CARD_WIDTH);
    }

    private static String prepareText(String s, int maxLength, boolean stripAccents, boolean upperCase) {
        if(maxLength > 0 && s.length() > maxLength) {
            s = s.substring(0, maxLength);
        }
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



    public void addCard(Document document, int left, int bottom, Spell spell) {
        // backgrounds
        int idx = 7;
        if(spell.getSchool().startsWith("Universel")) { idx = 0; }
        else if(spell.getSchool().startsWith("Abjuration")) { idx = 1; }
        else if(spell.getSchool().startsWith("Divination")) { idx = 2; }
        else if(spell.getSchool().startsWith("Enchantement")) { idx = 3; }
        else if(spell.getSchool().startsWith("Évocation")) { idx = 4; }
        else if(spell.getSchool().startsWith("Illusion")) { idx = 5; }
        else if(spell.getSchool().startsWith("Invocation")) { idx = 6; }
        else if(spell.getSchool().startsWith("Nécromancie")) { idx = 7; }
        else if(spell.getSchool().startsWith("Transmutation")) { idx = 8; }
        cardImg[idx].setFixedPosition(left, bottom);
        backImg.setFixedPosition(left + CARD_WIDTH, bottom);
        document.add(cardImg[idx]);
        document.add(backImg);
        // texts
        document.add((new Paragraph(prepareText(spell.getName(), 20, true, true)))
                .addStyle(STYLE_TITLE).setFont(params.titleFont)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMaxHeight(15)
                .setFixedPosition(left + CARD_TITLE_MARGIN, bottom + CARD_TITLE, CARD_TITLE_WIDTH));
        document.add((new Paragraph(prepareText(spell.getSchool(), 0, false, false)))
                .addStyle(STYLE_SUBTITLE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_SUBTITLE_MARGIN, bottom + CARD_SUBTITLE, CARD_SUBTITLE_WIDTH));
        document.add((new Paragraph(prepareText(spell.getLevel(), 45, false, false)))
                .addStyle(STYLE_FOOTER)
                .setMaxHeight(12)
                .setFixedPosition(left + CARD_FOOTER_MARGIN, bottom + CARD_FOOTER, CARD_FOOTER_WIDTH));
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
        String html = TEMPLATE.replaceAll("CASTINGTIME", castingTime )
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

    public void generatePDF(OutputStream output) {
        PageSize pageSize = PageSize.A4.rotate();
        PdfDocument pdf = new PdfDocument(new PdfWriter(output));
        Document document = new Document(pdf, pageSize);
        document.setMargins(20, 20,20,20);

        int posY1 = (int)Math.round(pageSize.getHeight() / 2);
        int posY2 = (int)Math.round(pageSize.getHeight() / 2) - CARD_HEIGHT;

        for(int i = 0; i<spells.size(); i++) {
            int posY = i % 4 == 0 || i % 4 == 1 ? posY1 : posY2;
            addCard(document, 2* CARD_WIDTH * (i % 2) + MARGIN, posY, spells.get(i));
            if(i % 4 == 3) {
                document.add(new AreaBreak());
            }
        }
        document.close();
    }
}

