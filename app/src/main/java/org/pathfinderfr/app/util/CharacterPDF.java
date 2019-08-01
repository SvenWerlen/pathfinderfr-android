package org.pathfinderfr.app.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.BorderCollapsePropertyValue;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import org.pathfinderfr.app.database.entity.Character;

import java.io.IOException;
import java.io.OutputStream;

public class CharacterPDF {

    private Character character;
    private static final int LOGO_WIDTH = 185;
    private static final int STATS_CELL_SPACING = 3;
    private static final int STATS_CELL_WIDTH = 22;
    private static final Style STYLE_CELL_DEFAULT;
    private static final Style STYLE_HEADER;
    private static final Style STYLE_TEXT;
    private static final Style STYLE_TEXT_TOTAL;
    private static final Style STYLE_LABEL_TOP;
    private static final Style STYLE_LABEL_BOTTOM;
    private PdfFont FONT_BOLD;

    static {
        STYLE_CELL_DEFAULT = new Style().setFontSize(8);
        STYLE_HEADER = new Style().setFontSize(4).setFontColor(ColorConstants.DARK_GRAY);
        STYLE_TEXT = new Style().setFontSize(8);
        STYLE_TEXT_TOTAL = new Style().setFontSize(10);
        STYLE_LABEL_TOP = new Style().setFontSize(8);
        STYLE_LABEL_BOTTOM = new Style().setFontSize(4);
    }

    public CharacterPDF(Character character) {
        this.character = character;

        try {
            FONT_BOLD = PdfFontFactory.createRegisteredFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            FONT_BOLD = null;
        }
    }

    public Cell createHeader(String text) {
        return new Cell()
                .addStyle(STYLE_HEADER)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
                .setMargin(0)
                .setPadding(0)
                .add(new Paragraph(text.toUpperCase())
                    .setTextAlignment(TextAlignment.CENTER).setFixedLeading(5).setPaddingTop(1));
    }

    public Paragraph createLabelTop(String text) {
        Paragraph p = (new Paragraph(text))
                .addStyle(STYLE_LABEL_TOP)
                .setFixedLeading(6)
                .setPaddingTop(2)
                .setTextAlignment(TextAlignment.CENTER);
        if(FONT_BOLD != null) {
            p.setFont(FONT_BOLD);
        }
        return p;
    }

    public Paragraph createLabelBottom(String text) {
        Paragraph p = new Paragraph(text)
                .addStyle(STYLE_LABEL_BOTTOM)
                .setFixedLeading(8)
                .setTextAlignment(TextAlignment.CENTER);
        return p;
    }

    public Cell createLabel(String text1, String text2) {
        return new Cell()
                .setPaddingLeft(2)
                .setPaddingRight(2)
                .setPaddingTop(0)
                .setPaddingBottom(0)
                .setMargin(0)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(ColorConstants.BLACK)
                .setFontColor(ColorConstants.WHITE)
                .add(createLabelTop(text1.toUpperCase()))
                .add(createLabelBottom(text2.toUpperCase()));
    }

    public Cell createValueCell(Integer value) {
        return createValueCell(value, false);
    }

    public Cell createValueCell(Integer value, boolean total) {
        Cell c = new Cell()
                .setPadding(0)
                .setMargin(0)
                .addStyle(total ? STYLE_TEXT_TOTAL : STYLE_CELL_DEFAULT)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add((new Paragraph(value == null ? "" : String.format("%d", value)))
                        .setWidth(STATS_CELL_WIDTH)
                        .setTextAlignment(TextAlignment.CENTER));
        if(total) {
            c.setFont(FONT_BOLD);
        }
        return c;
    }

    public Cell createBonusCell(Integer bonus) {
        return new Cell()
                .setPadding(0)
                .setMargin(0)
                .addStyle(STYLE_CELL_DEFAULT)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add((new Paragraph(bonus == null ? "" : String.format("%+d", bonus)))
                        .setWidth(STATS_CELL_WIDTH)
                        .setTextAlignment(TextAlignment.CENTER));
    }

    public Table createSectionStats() {
        Table table = new Table(5);
        table.setFixedPosition(18, 615, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(STATS_CELL_SPACING);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createHeader("Caractéristique"));
        table.addCell(createHeader("Valeur"));
        table.addCell(createHeader("Modif."));
        table.addCell(createHeader("Valeur temporaire"));
        table.addCell(createHeader("mod. temporaire"));
        table.addCell(createLabel("FOR", "Force"));
        table.addCell(createValueCell(character.getStrength()));
        table.addCell(createBonusCell(character.getStrengthModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("DEX", "Dextérité"));
        table.addCell(createValueCell(character.getDexterity()));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("CON", "Constitution"));
        table.addCell(createValueCell(character.getConstitution()));
        table.addCell(createBonusCell(character.getConstitutionModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("INT", "Intelligence"));
        table.addCell(createValueCell(character.getIntelligence()));
        table.addCell(createBonusCell(character.getIntelligenceModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("SAG", "Sagesse"));
        table.addCell(createValueCell(character.getWisdom()));
        table.addCell(createBonusCell(character.getWisdomModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("CHA", "Charisme"));
        table.addCell(createValueCell(character.getCharisma()));
        table.addCell(createBonusCell(character.getCharismaModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        return table;
    }

    public Cell createInfo(String label, int colspan) {
        return new Cell(1, colspan)
                .setPadding(0)
                .setMargin(0)
                .addStyle(STYLE_HEADER)
                .setBorder(Border.NO_BORDER)
                .setMinWidth(colspan*40)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .add((new Paragraph(label.toUpperCase()).setFixedLeading(5).setPaddingTop(1)));
    }

    public Cell createInfoText(String text, int colspan) {
        return new Cell(1, colspan)
                .setPadding(0)
                .setPaddingTop(3)
                .setMargin(0)
                .addStyle(STYLE_TEXT)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setMinHeight(10)
                .setMinWidth(colspan*40)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add((new Paragraph(text).setTextAlignment(TextAlignment.LEFT)));
    }

    public Table createSectionInfos() {
        Table table = new Table(8);
        table.setFixedPosition(225, 760, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);

        table.addCell(createInfoText(character.getName() == null ? "" : character.getName(), 3));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfoText("", 4));
        table.addCell(createInfo("Nom du personnage", 3));
        table.addCell(createInfo("Alignement", 1));
        table.addCell(createInfo("Joueur", 4));

        table.addCell(createInfoText(character.getClassNames(), 4));
        table.addCell(createInfoText("", 2));
        table.addCell(createInfoText("", 2));
        table.addCell(createInfo("Classe et niveau", 4));
        table.addCell(createInfo("Divinité", 2));
        table.addCell(createInfo("Origine", 2));

        table.addCell(createInfoText(character.getRaceName(), 1));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfoText("", 1));
        table.addCell(createInfo("Race", 1));
        table.addCell(createInfo("Catégorie de taille", 1));
        table.addCell(createInfo("Sexe", 1));
        table.addCell(createInfo("Âge", 1));
        table.addCell(createInfo("Taille", 1));
        table.addCell(createInfo("Poids", 1));
        table.addCell(createInfo("Cheveux", 1));
        table.addCell(createInfo("Yeux", 1));
        return table;
    }

    public Cell createSpeed(String label1, String label2, String value1, String value2) {
        Cell c = new Cell(1, label1 == null ? 1 : 2)
                .setPadding(0)
                .setMargin(0)
                .setMinWidth(label1 == null ? 35 : 70)
                .setMinHeight(15)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM);
        if(label1 != null) {
            Table t = new Table(4).setWidth(70);
            t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_TEXT).setPadding(0).setMargin(0).setWidth(20)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(value1)));
            t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_HEADER).setPadding(0).setMargin(0).setWidth(15)
                    .setVerticalAlignment(VerticalAlignment.BOTTOM).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(label1)));
            t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_TEXT).setPadding(0).setMargin(0).setWidth(20)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(value2)));
            t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_HEADER).setPadding(0).setMargin(0).setWidth(15)
                    .setVerticalAlignment(VerticalAlignment.BOTTOM).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(label2)));
            c.add(t);
        }
        return c;
    }

    public Cell createCell(String label, TextAlignment align, int rowspan, int colspan, int minWidth, int minHeight) {
        Cell c = new Cell(rowspan, colspan)
                .setPadding(0)
                .setPaddingLeft(2)
                .setPaddingRight(2)
                .setMargin(0)
                .setMinWidth(minWidth)
                .setMinHeight(minHeight)
                .addStyle(STYLE_HEADER)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .add(new Paragraph(label.toUpperCase()).setTextAlignment(align).setFixedLeading(5).setPaddingTop(1));
        return c;
    }

    public Table createSectionSpeed() {
        Table table = new Table(6);
        table.setFixedPosition(310, 705, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("VD", "Déplacement"));
        table.addCell(createSpeed("Mètres", "Cases", String.valueOf(character.getSpeed()*1.5), String.valueOf(character.getSpeed())));
        table.addCell(createSpeed("Mètres", "Cases","",""));
        table.addCell(createCell("Mod. Temporaires", TextAlignment.CENTER, 3, 1, 58, 0));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Vitesse de déplacement", TextAlignment.CENTER, 1, 2, 0, 0).setBorder(Border.NO_BORDER).setPaddingBottom(2));
        table.addCell(createCell("Avec armure", TextAlignment.CENTER, 1, 2, 0, 0).setBorder(Border.NO_BORDER).setPaddingBottom(2));
        table.addCell(createSpeed("Mètres", "","",""));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 0, 0));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 0, 0));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 0, 0));
        table.addCell(createCell("Vol", TextAlignment.LEFT, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Manœuvr.", TextAlignment.RIGHT, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Natation", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Escalade", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Creusement", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        return table;
    }

    public Paragraph createValue(String value) {
        return createValue(value, false);
    }

    public Paragraph createValue(String value, boolean total) {
        Paragraph p = new Paragraph(value)
                .addStyle(total ? STYLE_TEXT_TOTAL : STYLE_TEXT)
                .setPadding(0)
                .setMargin(0)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedLeading(8);
        if(total) {
            p.setFont(FONT_BOLD);
        }
        return p;
    }

    public Table createSectionHitpoints() {
        Table table = new Table(3);
        table.setFixedPosition(175, 651, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("PV", "Points de vie"));
        table.addCell(createCell("Total", TextAlignment.LEFT, 1, 1, 0, 0).add(createValue(String.valueOf(character.getHitpoints()),true)));
        table.addCell(createCell("RD", TextAlignment.LEFT, 1, 1, 0, 0));
        table.addCell(createCell("Blessures/Points de vie actuels", TextAlignment.LEFT, 1, 3, 0, 0).setBorder(Border.NO_BORDER).setPaddingTop(5));
        table.addCell(createCell("", TextAlignment.LEFT, 1, 3, 120, 40));
        table.addCell(createCell("Dégâts non-létaux", TextAlignment.LEFT, 1, 3, 0, 0).setBorder(Border.NO_BORDER).setPaddingTop(5));
        table.addCell(createCell("", TextAlignment.LEFT, 1, 3, 120, 20));
        return table;
    }

    public Table createSectionInitiative() {
        Table table = new Table(6);
        table.setFixedPosition(175, 607, 100);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("INI", "Initiative").setMinWidth(28));
        table.addCell(createCell("", TextAlignment.LEFT, 1, 1, 20, 0));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 0, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("", TextAlignment.LEFT, 1, 1, 20, 0));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 0, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("", TextAlignment.LEFT, 1, 1, 20, 0));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Total", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod de DEX", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod divers", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        return table;
    }

    public Table createSectionArmorClass() {
        Table table = new Table(19);
        table.setFixedPosition(21, 550, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);
        table.addCell(createLabel("CA", "Classe d'armure").setMinWidth(41));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getArmorClass(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createValueCell(10).setPadding(0).setMinWidth(9).setBorder(Border.NO_BORDER));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Total", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Bonus armure", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Bonus bouclier", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER).setPadding(0));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. de DEX", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. de taille", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Armure naturelle", TextAlignment.CENTER, 1, 1, 24, 0).setBorder(Border.NO_BORDER).setPadding(0));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. de parade", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. divers", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell(1,19).setBorder(Border.NO_BORDER));
        table.addCell(createLabel("Contact", "Classe d'armure"));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 20, 0));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(new Cell(1,7).setBorder(Border.NO_BORDER).setPadding(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(ColorConstants.BLACK).add(createLabel("Pris au dépourvu", "Classe d'armure")));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 20, 0));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(createCell("Mod.", TextAlignment.RIGHT, 1, 5, 0, 0));
        return table;
    }



    public Table createSectionResistances() {
        Table table = new Table(15);
        table.setFixedPosition(21, 465, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);

        table.addCell(createHeader("Jets de sauvegarde"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Total"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Bonus de base"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de charac"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. magique"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. divers"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. temporaire"));
        table.addCell(new Cell(7,1).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod.", TextAlignment.RIGHT, 7, 1, 55, 0));

        table.addCell(new Cell(1,13).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("Réflexes", "(Dextérité)").setMinWidth(50));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getSavingThrowsReflexesTotal(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSavingThrowsReflexes()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));

        table.addCell(new Cell(1,13).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("Vigueur", "(Constitution)").setMinWidth(50));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getSavingThrowsFortitudeTotal(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSavingThrowsFortitude()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getConstitutionModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));

        table.addCell(new Cell(1,13).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("Volonté", "(Sagesse)").setMinWidth(50));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getSavingThrowsWillTotal(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSavingThrowsWill()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getWisdomModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));

        return table;
    }


    public void generatePDF(OutputStream output, ImageData logo) {
        PdfDocument pdf = new PdfDocument(new PdfWriter(output));
        Document document = new Document(pdf);
        document.setMargins(20, 20,20,20);

        // show logo
        document.add((new Image(logo)).setWidth(LOGO_WIDTH));
        document.add(new Paragraph("Feuille de Personnage")
                .addStyle(STYLE_CELL_DEFAULT)
               .setFixedPosition(70, 760, 100));

        document.add(createSectionStats());
        document.add(createSectionInfos());
        document.add(createSectionHitpoints());
        document.add(createSectionSpeed());
        document.add(createSectionInitiative());
        document.add(createSectionArmorClass());
        document.add(createSectionResistances());
        document.close();
    }
}
