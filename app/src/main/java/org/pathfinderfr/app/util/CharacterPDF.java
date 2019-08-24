package org.pathfinderfr.app.util;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.BorderCollapsePropertyValue;
import com.itextpdf.layout.property.BorderRadius;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.database.entity.Weapon;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CharacterPDF {

    private static final int LOGO_WIDTH = 185;
    private static final int STATS_CELL_SPACING = 3;
    private static final int STATS_CELL_WIDTH = 22;
    private static final Style STYLE_CELL_DEFAULT;
    private static final Style STYLE_HEADER;
    private static final Style STYLE_XP;
    private static final Style STYLE_TEXT;
    private static final Style STYLE_TEXT_TOTAL;
    private static final Style STYLE_LABEL_TOP;
    private static final Style STYLE_LABEL_BOTTOM;
    private static final Color COLOR_LIGHT_GRAY;
    private static final DecimalFormat WEIGHT_FORMAT = new DecimalFormat("0.#");
    private static final DecimalFormat BIGNUM_FORMAT = new DecimalFormat("###,###");

    private static final String TEXT_LONG_TEST = "Ceci est un vraiment long texte pour tester dans les champs qui font vraiment plus que 100 charactères de long";

    private Character character;
    private List<DBEntity> skills;
    private List<Weapon> weapons;
    private List<Armor> armors;
    private Options options;

    static {
        STYLE_CELL_DEFAULT = new Style().setFontSize(8);
        STYLE_HEADER = new Style().setFontSize(4).setFontColor(ColorConstants.DARK_GRAY);
        STYLE_TEXT = new Style().setFontSize(8);
        STYLE_XP = new Style().setFontSize(14);
        STYLE_TEXT_TOTAL = new Style().setFontSize(10);
        STYLE_LABEL_TOP = new Style().setFontSize(8);
        STYLE_LABEL_BOTTOM = new Style().setFontSize(4);
        COLOR_LIGHT_GRAY = new DeviceRgb(230, 230, 230);
    }

    public static class Options {
        public boolean printInkSaving = false;
        public boolean printLogo = true;
        public boolean showWeaponsInInventory = false;
        public boolean showArmorsInInventory = false;
    }

    public CharacterPDF(Options options, Character character, List<DBEntity> skills, List<Weapon> weapons, List<Armor> armors) {
        this.character = character;
        this.skills = skills;
        this.weapons = weapons;
        this.armors = armors;
        this.options = options;
    }

    public Cell createHeader(String text) {
        return createHeader(text,TextAlignment.CENTER,1,1);
    }

    public Cell createHeader(String text, TextAlignment align) {
        return createHeader(text, align, 1,1);
    }

    public Cell createHeader(String text, int rowspan, int colspan) {
        return createHeader(text, TextAlignment.CENTER, rowspan, colspan);
    }

    public Cell createHeader(String text, TextAlignment align, int rowspan, int colspan) {
        return new Cell(rowspan, colspan)
                .addStyle(STYLE_HEADER)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
                .setMargin(0)
                .setPadding(0)
                .add(new Paragraph(text.toUpperCase())
                    .setTextAlignment(align).setFixedLeading(5).setPaddingTop(1));
    }

    public Paragraph createLabelTop(String text) {
        Paragraph p = (new Paragraph(text))
                .addStyle(STYLE_LABEL_TOP)
                .setFixedLeading(6)
                .setPaddingTop(2)
                .setTextAlignment(TextAlignment.CENTER);
        p.setBold();
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
        return createLabel(text1, text2,1,1);
    }

    public Cell createLabel(String text1, String text2, int rowspan, int colspan) {
        return new Cell(rowspan, colspan)
                .setPaddingLeft(2)
                .setPaddingRight(2)
                .setPaddingTop(0)
                .setPaddingBottom(0)
                .setMargin(0)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(options.printInkSaving ? COLOR_LIGHT_GRAY : ColorConstants.BLACK)
                .setFontColor(options.printInkSaving ? ColorConstants.BLACK : ColorConstants.WHITE)
                .add(createLabelTop(text1.toUpperCase()))
                .add(createLabelBottom(text2.toUpperCase()));
    }

    public Cell createValueCell(Integer value) {
        return createValueCell(value, false);
    }

    public Cell createValueCell(Integer value, boolean total) {
        return createValueCell(value == null ? "" : String.format("%d", value), total, 1, 1);
    }

    public Cell createValueCell(String value, boolean total, int rowspan, int colspan) {
        Cell c = new Cell(rowspan, colspan)
                .setPadding(0)
                .setMargin(0)
                .addStyle(total ? STYLE_TEXT_TOTAL : STYLE_CELL_DEFAULT)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        if(colspan == 1) {
            c.add((new Paragraph(value))
                    .setWidth(STATS_CELL_WIDTH)
                    .setTextAlignment(TextAlignment.CENTER));
        } else {
            c.add((new Paragraph(value))
                    .setTextAlignment(TextAlignment.CENTER));
        }
        if(total) {
            c.setBold();
        }
        return c;
    }

    public Cell createWeightCell(int value, boolean total) {
        Cell c = new Cell()
                .setPadding(0)
                .setPaddingRight(3)
                .setMargin(0)
                .setMinHeight(13)
                .addStyle(total ? STYLE_TEXT_TOTAL : STYLE_CELL_DEFAULT)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        String weight = "";
        if(total) {
            weight = String.format("%d", Math.round(value / 1000f));
        }
        else if (value >= 1000) {
            weight = WEIGHT_FORMAT.format(value / 1000f) + " kg";
        } else if(value > 0) {
            weight = String.format("%d g", value);
        } else if(value < 0) {
            weight = "";
        } else {
            weight = "-";
        }

        c.add((new Paragraph(weight))
                    .setTextAlignment(TextAlignment.RIGHT));
        if(total) {
            c.setBold();
        }
        return c;
    }

    public Cell createValueGold(int value) {
        Cell c = new Cell()
                .setPadding(0)
                .setMargin(0)
                .addStyle(STYLE_CELL_DEFAULT)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        c.add((new Paragraph(BIGNUM_FORMAT.format(value)))
                .setTextAlignment(TextAlignment.RIGHT));
        return c;
    }

    public Cell createValueExperience(int value, TextAlignment align) {
        Cell c = new Cell()
                .setPadding(0)
                .setMargin(0)
                .addStyle(STYLE_XP)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        c.add((new Paragraph(BIGNUM_FORMAT.format(value)))
                .setTextAlignment(align));
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
        return createInfoText(text, colspan, TextAlignment.LEFT);
    }

    public Cell createInfoText(String text, int colspan, TextAlignment align) {
        return createInfoText(text, colspan, align, false);
    }

    public Cell createInfoText(String text, int colspan, TextAlignment align, boolean border) {
        text = text == null ? "" : text;
        Cell c = new Cell(1, colspan)
                .setPadding(0)
                .setPaddingTop(3)
                .setMargin(0)
                .addStyle(STYLE_TEXT)
                .setMinHeight(10)
                .setMinWidth(colspan*40)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add((new Paragraph(text).setTextAlignment(align).setFixedLeading(9)));
        if(!border) {
            c.setBorderLeft(Border.NO_BORDER);
            c.setBorderRight(Border.NO_BORDER);
            c.setBorderTop(Border.NO_BORDER);
        }
        return c;
    }

    public Cell createFeatureText(String text, boolean title) {
        text = text == null ? "" : text;
        Cell c = new Cell()
                .setPadding(0)
                .setMargin(0)
                .addStyle(STYLE_TEXT)
                .setMinHeight(10)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add((new Paragraph(text).setTextAlignment(TextAlignment.LEFT).setFixedLeading(9)));
        c.setBorder(Border.NO_BORDER);
        if(title) {
            c.setBold();
        }
        return c;
    }

    /**
     * Returns the french translation for the alignment
     */
    public static String alignment2Text(int alignment) {
        switch(alignment) {
            case Character.ALIGN_LG: return "LB";
            case Character.ALIGN_NG: return "NB";
            case Character.ALIGN_CG: return "CB";
            case Character.ALIGN_LN: return "LN";
            case Character.ALIGN_N: return "N";
            case Character.ALIGN_CN: return "CN";
            case Character.ALIGN_LE: return "LM";
            case Character.ALIGN_NE: return "NM";
            case Character.ALIGN_CE: return "CM";
            default: return null;
        }
    }

    /**
     * Returns the french translation for the alignment
     */
    public static int text2alignment(String alignment) {
        if(alignment == null) {
            return 0;
        }
        switch(alignment) {
            case "LB": return Character.ALIGN_LG;
            case "NB": return Character.ALIGN_NG;
            case "CB": return Character.ALIGN_CG;
            case "LN": return Character.ALIGN_LN;
            case "N": return Character.ALIGN_N;
            case "CN": return Character.ALIGN_CN;
            case "LM": return Character.ALIGN_LE;
            case "NM": return Character.ALIGN_NE;
            case "CM": return Character.ALIGN_CE;
            default: return 0;
        }
    }

    /**
     * Returns the french translation for the sex
     */
    public static String sex2text(int sex) {
        switch(sex) {
            case Character.SEX_M: return "M";
            case Character.SEX_F: return "F";
            default: return null;
        }
    }

    /**
     * Returns the french translation for the sex
     */
    public static int text2sex(String sex) {
        if(sex == null) {
            return 0;
        }
        switch(sex) {
            case "M": return Character.SEX_M;
            case "F": return Character.SEX_F;
            default: return 0;
        }
    }

    /**
     * Returns the french translation for the alignment
     */
    public static String size2Text(int size) {
        switch(size) {
            case Character.SIZE_FINE: return "I";
            case Character.SIZE_DIMINUTIVE: return "Min";
            case Character.SIZE_TINY: return "TP";
            case Character.SIZE_SMALL: return "P";
            case Character.SIZE_MEDIUM: return "M";
            case Character.SIZE_LARGE_TALL: return "G";
            case Character.SIZE_LARGE_LONG: return "G";
            case Character.SIZE_HUGE_TALL: return "TG";
            case Character.SIZE_HUGE_LONG: return "TG";
            case Character.SIZE_GARG_TALL: return "Gig";
            case Character.SIZE_GARG_LONG: return "Gig";
            case Character.SIZE_COLO_TALL: return "C";
            case Character.SIZE_COLO_LONG: return "C";
            default: return null;
        }
    }

    /**
     * Returns the french translation for the alignment
     */
    public static int text2Size(String size) {
        if(size == null) {
            return 0;
        }
        switch(size) {
            case "I": return Character.SIZE_FINE;
            case "Min": return Character.SIZE_DIMINUTIVE;
            case "TP": return Character.SIZE_TINY;
            case "P": return Character.SIZE_SMALL;
            case "M": return Character.SIZE_MEDIUM;
            case "G": return Character.SIZE_LARGE_TALL;
            //case "G": return Character.SIZE_LARGE_LONG;
            case "TG": return Character.SIZE_HUGE_TALL;
            //case "TG": return Character.SIZE_HUGE_LONG;
            case "Gig": return Character.SIZE_GARG_TALL;
            //case "Gig": return Character.SIZE_GARG_LONG;
            case "C": return Character.SIZE_COLO_TALL;
            //case "C": return Character.SIZE_COLO_LONG;
            default: return 0;
        }
    }

    /**
     * Returns a string with max characters
     */
    public String stringMax(String str, int maxChars) {
        if(str == null) {
            return "-";
        } else if(str.length() <= maxChars) {
            return str;
        } else {
            return str.substring(0, maxChars) + "…";
        }
    }

    public Table createSectionInfos() {
        Table table = new Table(8);
        table.setFixedPosition(225, 760, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);

        table.addCell(createInfoText(stringMax(character.getName(), 30), 3));
        table.addCell(createInfoText(alignment2Text(character.getAlignment()), 1));
        table.addCell(createInfoText(stringMax(character.getPlayer(), 35), 4));
        table.addCell(createInfo("Nom du personnage", 3));
        table.addCell(createInfo("Alignement", 1));
        table.addCell(createInfo("Joueur", 4));

        table.addCell(createInfoText(character.getClassNames(), 4));
        table.addCell(createInfoText(stringMax(character.getDivinity(), 18), 2));
        table.addCell(createInfoText(stringMax(character.getOrigin(), 18), 2));
        table.addCell(createInfo("Classe et niveau", 4));
        table.addCell(createInfo("Divinité", 2));
        table.addCell(createInfo("Origine", 2));

        table.addCell(createInfoText(character.getRaceName(), 1));
        table.addCell(createInfoText(size2Text(character.getSizeType()), 1));
        table.addCell(createInfoText(sex2text(character.getSex()), 1));
        table.addCell(createInfoText(String.format("%d ans", character.getAge()), 1));
        table.addCell(createInfoText(String.format("%d cm", character.getHeight()), 1));
        table.addCell(createInfoText(String.format("%d kg", character.getWeight()), 1));
        table.addCell(createInfoText(stringMax(character.getHair(), 8), 1));
        table.addCell(createInfoText(stringMax(character.getEyes(), 8), 1));
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

    /**
     * Returns the french translation for the alignment
     */
    public static String flyManeuverability2Text(int fly, int man) {
        if(fly == 0 || man == 0) {
            return "";
        }
        switch(man) {
            case Character.SPEED_MANEUV_CLUMBSY: return "Déplorable";
            case Character.SPEED_MANEUV_POOR: return "Médiocre";
            case Character.SPEED_MANEUV_GOOD: return "Bonne";
            case Character.SPEED_MANEUV_PERFECT: return "Parfaite";
            default: return "Moyenne";
        }
    }

    /**
     * Returns the french translation for the alignment
     */
    public static int text2flyManeuverability(String man) {
        if(man == null) {
            return 0;
        }
        switch(man) {
            case "Déplorable": return Character.SPEED_MANEUV_CLUMBSY;
            case "Médiocre": return Character.SPEED_MANEUV_POOR;
            case "Moyenne": return Character.SPEED_MANEUV_AVERAGE;
            case "Bonne": return Character.SPEED_MANEUV_GOOD;
            case "Parfaite": return Character.SPEED_MANEUV_PERFECT;
            default: return 0;
        }
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
            if(value1 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_TEXT).setPadding(0).setMargin(0).setWidth(20)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(value1)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
            if(label1 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_HEADER).setPadding(0).setMargin(0).setWidth(15)
                        .setVerticalAlignment(VerticalAlignment.BOTTOM).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(label1)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
            if(value2 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_TEXT).setPadding(0).setMargin(0).setWidth(20)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(value2)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
            if(label2 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_HEADER).setPadding(0).setMargin(0).setWidth(15)
                        .setVerticalAlignment(VerticalAlignment.BOTTOM).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(label2)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
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

    public String formatSpeed(float speed, String unit) {
        if(speed <= 0) {
            return "-";
        } else {
            return new DecimalFormat("#.#" + unit).format(speed);
        }
    }

    public Table createSectionSpeed() {
        Table table = new Table(6);
        table.setFixedPosition(310, 705, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("VD", "Déplacement"));
        table.addCell(createSpeed("Mètres", "Cases", String.valueOf(character.getSpeed()*1.5), String.valueOf(character.getSpeed())));
        table.addCell(createSpeed("Mètres", "Cases", String.valueOf(character.getSpeedWithArmor()*1.5), String.valueOf(character.getSpeedWithArmor())));
        table.addCell(createCell("Mod. Temporaires", TextAlignment.CENTER, 3, 1, 58, 0));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Vitesse de déplacement", TextAlignment.CENTER, 1, 2, 0, 0).setBorder(Border.NO_BORDER).setPaddingBottom(2));
        table.addCell(createCell("Avec armure", TextAlignment.CENTER, 1, 2, 0, 0).setBorder(Border.NO_BORDER).setPaddingBottom(2));
        table.addCell(createSpeed("Mètres", null,formatSpeed(character.getBaseSpeedFly(), ""),flyManeuverability2Text(character.getBaseSpeedFly(), character.getBaseSpeedManeuverability())));
        table.addCell(createInfoText(formatSpeed(character.getSpeedSwimming(), " m"),1, TextAlignment.CENTER, true).setMinWidth(0));
        table.addCell(createInfoText(formatSpeed(character.getSpeedClimbing(), " m"), 1, TextAlignment.CENTER, true).setMinWidth(0));
        table.addCell(createInfoText(formatSpeed(character.getBaseSpeedDig(), " m"), 1, TextAlignment.CENTER, true).setMinWidth(0));
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
            p.setBold();
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
        table.addCell(createCell(String.valueOf(character.getHitpoints()), TextAlignment.LEFT, 1, 3, 120, 40));
        table.addCell(createCell("Dégâts non-létaux", TextAlignment.LEFT, 1, 3, 0, 0).setBorder(Border.NO_BORDER).setPaddingTop(5));
        table.addCell(createCell(String.valueOf(character.getHitpointsTemp()), TextAlignment.LEFT, 1, 3, 120, 20));
        return table;
    }

    public Table createSectionInitiative() {
        Table table = new Table(6);
        table.setFixedPosition(175, 607, 100);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("INI", "Initiative").setMinWidth(28));
        table.addCell(createValueCell(character.getInitiative()));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 0, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 0, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_INI)));
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
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_ARMOR)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_SHIELD)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSizeModifierArmorClass()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_NATURAL)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_PARADE)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC)));
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
        table.addCell(createValueCell(character.getArmorClassContact()));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(new Cell(1,7).setPadding(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(ColorConstants.BLACK).add(createLabel("Pris au dépourvu", "Classe d'armure")));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getArmorClassFlatFooted()));
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
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_MAG_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_MAG_REF)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_REF)));
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
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_MAG_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_MAG_FOR)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_FOR)));
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
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_MAG_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_MAG_WIL)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_WIL)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));

        return table;
    }


    public Table createSectionAttackDefense() {
        Table table = new Table(18);
        table.setFixedPosition(21, 375, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);

        table.addCell(createLabel("BBA", "Bonus de base à l'attaque").setMinWidth(70));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getBaseAttackBonusAsString(), false, 1, 7));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createLabel("Résistance", "à la magie", 1, 6).setMinWidth(67));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getMagicResistance(), false));

        table.addCell(new Cell(1,18).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("BMO", "Bonus de manœuvre offensive").setMinWidth(70));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getCombatManeuverBonus(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getBaseAttackBonusBest()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getStrengthModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSizeModifierManeuver()));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createCell("Mod.", TextAlignment.RIGHT, 2, 8, 0, 0));

        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Total").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("BBA").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de force"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de taille"));
        table.addCell(createHeader(""));

        table.addCell(new Cell(1,18).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("DMD", "Degré de manœuvre défensive").setMinWidth(70));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getCombatManeuverDefense(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getBaseAttackBonusBest()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getStrengthModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSizeModifierManeuver()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createValueCell(10).setPadding(0).setMinWidth(9).setBorder(Border.NO_BORDER));
        table.addCell(createHeader("").setMinWidth(35));
        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader(""));

        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Total").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("BBA").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de force"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de Dex"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de taille"));
        table.addCell(createHeader(""));

        return table;
    }


    public Table createSectionWeapon(int left, int bottom, int weaponIdx) {
        Weapon w = weapons != null && weapons.size() > weaponIdx ? weapons.get(weaponIdx) : null;
        Table table = new Table(5);
        table.setFixedPosition(left, bottom, 0);
        table.addCell(createLabel("Arme", "", 2,3)
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTopLeftRadius(new BorderRadius(5))
                .setBorderTopRightRadius(new BorderRadius(5)).setMinWidth(170));
        table.addCell(createHeader("").setMinHeight(5));
        table.addCell(createHeader(""));
        table.addCell(createLabel("","Bonus à l'attaque").setMinWidth(58).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
        table.addCell(createLabel("","Critique").setMinWidth(45).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));

        table.addCell(createInfoText(w == null ? "" : w.getName(), 3, TextAlignment.LEFT, true).setMinHeight(15).setPaddingLeft(3));
        String attackBonus = w == null ? "" : (w.isRanged() ? character.getAttackBonusRangeAsString(weaponIdx+1) : character.getAttackBonusMeleeAsString(weaponIdx+1));
        table.addCell(createInfoText(attackBonus, 1, TextAlignment.CENTER, true));
        table.addCell(createInfoText(w == null ? "" : w.getCritical(), 1, TextAlignment.CENTER, true));
        table.addCell(createLabel("","Type").setWidth(20));
        table.addCell(createLabel("","Portée"));
        table.addCell(createLabel("","Munitions").setMinWidth(90));
        table.addCell(createLabel("","Dégâts", 1,2));
        table.addCell(createInfoText(w == null ? "" : w.getType(), 1, TextAlignment.CENTER, true).setMinHeight(15));
        table.addCell(createInfoText(w == null ? "" : w.getRangeInMeters(), 1, TextAlignment.CENTER, true));
        table.addCell(createInfoText(w == null ? "" : (w.isRanged() ? stringMax(w.getDescription(), 20) : "-"), 1, TextAlignment.CENTER, true));
        String damage = w == null ? "" : character.getDamage(w, weaponIdx+1);
        table.addCell(createInfoText(damage, 2, TextAlignment.CENTER, true));

        return table;
    }


    public Table createSectionArmors() {
        Table table = new Table(7);
        //table.setFixedPosition(left, bottom, 0);
        table.addCell(createLabel("Protections", "", 2,1)
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTopLeftRadius(new BorderRadius(5))
                .setBorderTopRightRadius(new BorderRadius(5)).setMinWidth(128));
        table.addCell(createHeader("",1,6).setMinHeight(5));
        table.addCell(createLabel("","Bonus").setMinWidth(30).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
        table.addCell(createLabel("","Type").setMinWidth(30).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
        table.addCell(createLabel("","Pénalité").setMinWidth(30).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
        table.addCell(createLabel("","Échec de sorts").setMinWidth(30).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
        table.addCell(createLabel("","Poids").setMinWidth(30).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
        table.addCell(createLabel("","Propriétés").setMinWidth(30).setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));

        for(int i=0; i<5; i++) {
            Armor armor = armors == null || i >= armors.size() ? new Armor() : armors.get(i);
            table.addCell(createInfoText(armor.getName(), 1, TextAlignment.LEFT, true).setMinHeight(15).setPaddingLeft(3));
            table.addCell(createInfoText(armor.getBonus(), 1, TextAlignment.CENTER, true));
            String category = armor.getCategory() == null ? "" : armor.getCategory();
            if(category.length() >= 4) {
                category = category.substring(0,4);
            }
            table.addCell(createInfoText(stringMax(category, 4), 1, TextAlignment.CENTER, true));
            table.addCell(createInfoText(armor.getMalus(), 1, TextAlignment.CENTER, true));
            table.addCell(createInfoText(armor.getCastFail(), 1, TextAlignment.CENTER, true));
            table.addCell(createInfoText(armor.getWeight(), 1, TextAlignment.CENTER, true));
            table.addCell(createInfoText("", 1, TextAlignment.CENTER, true));

        }

        return table;
    }

    public Cell createCheckBox(boolean checked) {
        Color checkedBgd = options.printInkSaving ? ColorConstants.GRAY : ColorConstants.BLACK;
        return new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph().setBackgroundColor(checked ? checkedBgd: ColorConstants.WHITE)
                .setMinHeight(6).setMinWidth(6)
                .setBorder(new SolidBorder(1)));
    }

    public Table createSectionSkills() {
        Table table = new Table(9);
        table.setFixedPosition(313, 145, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);
        table.addCell(createLabel("Compétences", "", 1,9).setMinWidth(251).setMinHeight(12));
        table.addCell(createHeader("").setMinWidth(10));
        table.addCell(createHeader("Nom de la compétence",TextAlignment.LEFT).setMinWidth(120));
        table.addCell(createHeader("Bonus total"));
        table.addCell(createHeader("Mod. carac",1,2));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Rang"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. divers"));
        int idx = 0;
        for(DBEntity skill : skills) {
            if(skill instanceof Skill) {
                Color backgnd = idx++ % 2 == 0 ? COLOR_LIGHT_GRAY : ColorConstants.WHITE;
                Skill s = (Skill)skill;
                String name = s.getName() + ( s.requiresTraining() ? "*" : "" );
                name = name.replaceAll("Connaissances", "Conn." );
                table.addCell(createCheckBox(character.isClassSkill(s)).setBackgroundColor(backgnd));
                table.addCell(createInfoText(name,1).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd).setPaddingBottom(2).setPaddingLeft(1));
                table.addCell(createValueCell(character.getSkillTotalBonus(s),true).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createInfoText(s.getAbilityId(),1, TextAlignment.CENTER).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd).setMinWidth(20).setPaddingBottom(2));
                table.addCell(createValueCell(character.getSkillAbilityMod(s)).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createValueCell(character.getSkillRank(s.getId())).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createValueCell(character.getSkillModBonus(s)).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
            }
        }
        return table;
    }


    public Table createSectionOthers() {
        Table table = new Table(1);
        table.setFixedPosition(313, 50, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);

        table.addCell(createInfoText(stringMax(character.getModifsAsString(), 200), 1).setMinWidth(256).setVerticalAlignment(VerticalAlignment.BOTTOM));
        table.addCell(createInfo("Modificateurs particuliers", 1));

        table.addCell(createInfoText(stringMax(character.getLanguages(), 200), 1).setMinWidth(256).setMinHeight(30).setVerticalAlignment(VerticalAlignment.BOTTOM));
        table.addCell(createInfo("Langues", 1));
        return table;
    }


    public Table createSectionInventory() {
        Table table = new Table(2);
        table.setFixedPosition(21, 200, 0);
        //table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);
        table.addCell(createLabel("Équipement", "", 1,2).setMinWidth(150).setMinHeight(12));
        table.addCell(createHeader("Objet",TextAlignment.CENTER).setMinWidth(120));
        table.addCell(createHeader("Poids").setMinWidth(30));

        int totalWeight = 0;
        List<Character.InventoryItem> items = new ArrayList<>();
        for(Character.InventoryItem item : character.getInventoryItems()) {
            boolean add = true;
            if(item.isWeapon()) {
                add = options.showWeaponsInInventory;
            } else if(item.isArmor()) {
                add = options.showArmorsInInventory;
            }

            if(add) {
                items.add(item);
            }
            totalWeight += item.getWeight();
        }

        for(int i = 0; i<35; i++) {
            String itemName = "";
            int itemWeight = -1;
            if(i < items.size()) {
                Character.InventoryItem item = items.get(i);
                itemName = stringMax(item.getName(),25);
                itemWeight = item.getWeight();
            }
            table.addCell(createInfoText(itemName, 1, TextAlignment.LEFT, true).setPaddingBottom(0).setPaddingTop(0).setPaddingLeft(3));
            table.addCell(createWeightCell(itemWeight, false));
        }
        table.addCell(createLabel("Poids total (kg)", "", 1,1));
        table.addCell(createWeightCell(totalWeight, true));
        return table;
    }

    public static float[] getLoad(int strength) {
        if(strength < 0) {
            return new float[] { 0, 0, 0};
        }
        switch(strength) {
            case 1: return new float[] { 1.5f, 3, 5 };
            case 2: return new float[] { 3, 6.5f, 10 };
            case 3: return new float[] { 5, 10, 15 };
            case 4: return new float[] { 6.5f, 13, 20 };
            case 5: return new float[] { 8, 16.5f, 25 };
            case 6: return new float[] { 10, 20, 30 };
            case 7: return new float[] { 11.5f, 23, 35 };
            case 8: return new float[] { 13, 26.5f, 40 };
            case 9: return new float[] { 15, 30, 45 };
            case 10: return new float[] { 16.5f, 33, 50 };
            case 11: return new float[] { 19, 38, 57.5f };
            case 12: return new float[] { 21.5f, 43, 65 };
            case 13: return new float[] { 25, 50, 75 };
            case 14: return new float[] { 29, 58, 87.5f };
            case 15: return new float[] { 33, 66.5f, 100 };
            case 16: return new float[] { 38, 76.5f, 115 };
            case 17: return new float[] { 43, 86.5f, 130 };
            case 18: return new float[] { 50, 100, 150 };
            case 19: return new float[] { 58, 116.5f, 175 };
            case 20: return new float[] { 66.5f, 133, 200 };
            case 21: return new float[] { 76.5f, 153, 230 };
            case 22: return new float[] { 86.5f, 173, 260 };
            case 23: return new float[] { 100, 200, 300 };
            case 24: return new float[] { 116.5f, 233, 350 };
            case 25: return new float[] { 133, 266.5f, 400 };
            case 26: return new float[] { 153, 306.5f, 460 };
            case 27: return new float[] { 173, 346.5f, 520 };
            case 28: return new float[] { 200, 400, 600 };
            case 29: return new float[] { 233, 466.5f, 700 };
        }
        float[] result = getLoad(strength-10);
        return new float[] { result[0] * 4, result[1] * 4, result[2] * 4};
    }

    public Table createSectionWeights() {
        Table table = new Table(4);
        table.setMinWidth(155);
        table.setFixedPosition(18, 140, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(STATS_CELL_SPACING);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        float loads[] = getLoad(character.getStrength());
        table.addCell(createInfo("Charge légère",1).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(createValueCell(Math.round(loads[0])));
        table.addCell(createInfo("Porter au-dessus de la tête",1).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(createValueCell(Math.round(loads[2])));
        table.addCell(createInfo("Charge intermédiaire",1).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(createValueCell(Math.round(loads[1])));
        table.addCell(createInfo("Décoller du sol",1).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(createValueCell(Math.round(loads[2]*2)));
        table.addCell(createInfo("Charge lourde",1).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(createValueCell(Math.round(loads[2])));
        table.addCell(createInfo("Pousser ou tirer",1).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(createValueCell(Math.round(loads[2]*5)));

        return table;
    }


    public Table createSectionRichness() {
        Table table = new Table(3);
        table.setFixedPosition(21, 50, 0);
        table.setBorder(new SolidBorder(1));
        //table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);
        table.addCell(createLabel("Richesses", "", 1,3).setMinWidth(143).setMinHeight(12));
        table.addCell(createHeader("PC", TextAlignment.RIGHT).setPadding(5));
        table.addCell(createValueGold(character.getMoneyCP()).setBorder(Border.NO_BORDER).setMinWidth(30));
        table.addCell(new Cell().setMinWidth(103).setBorder(Border.NO_BORDER));
        table.addCell(createHeader("PA", TextAlignment.RIGHT).setPadding(5));
        table.addCell(createValueGold(character.getMoneySP()).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createHeader("PO", TextAlignment.RIGHT).setPadding(5));
        table.addCell(createValueGold(character.getMoneyGP()).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createHeader("PP", TextAlignment.RIGHT).setPadding(5));
        table.addCell(createValueGold(character.getMoneyPP()).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));

        return table;
    }

    public String getTextFromTraitFeatFeature(Object element) {
        String text = "";
        if(element instanceof Feat) {
            Feat f = (Feat)element;
            text = String.format("[D] %s", f.getName());
        } else if(element instanceof Race.Trait) {
            Race.Trait t = (Race.Trait)element;
            text = String.format("[T] %s (%s)", t.getName(), character.getRaceName());
            if(character.traitIsAltered(t.getName()) != null) {
                text += " (!)";
            }
        } else if(element instanceof Trait) {
            Trait t = (Trait)element;
            if(t.getRace() == null) {
                text = String.format("[T] %s", t.getName());
            } else {
                text = String.format("[T] %s (%s)", t.getName(), t.getRace().getName());
            }
        }  else if(element instanceof ClassFeature) {
            ClassFeature f = (ClassFeature)element;
            text = String.format("[C] %s %d: %s", f.getClass_().getShortName(), f.getLevel(), f.getNameLong());
        }
        return text;
    }

    public Table createSectionFeatsAndFeatures() {
        Table table;
        List<Object> entities = new ArrayList<>();

        // racial traits
        for(Race.Trait t : character.getRace().getTraits()) {
            if(character.traitIsReplaced(t.getName()) == null) {
                entities.add(t);
            }
        }
        // traits (racial and regular)
        for(Trait t : character.getTraits()) {
            entities.add(t);
        }
        // feats
        for(Feat f : character.getFeats()) {
            entities.add(f);
        }
        // class features
        for(ClassFeature cl : character.getClassFeatures()) {
            entities.add(cl);
        }

        // normal display
        if(entities.size() <=27) {
            table = new Table(1);
            table.setFixedPosition(190, 100, 0);
            table.setVerticalBorderSpacing(0);
            table.setHorizontalBorderSpacing(0);
            table.addCell(createLabel("[T]raits, [D]ons et [C]apacités", "", 1,1).setMinWidth(200).setMinHeight(12));

            for (int i = 0; i < 27; i++) {
                Object element = i < entities.size() ? entities.get(i) : null;
                String text = getTextFromTraitFeatFeature(element);
                table.addCell(createFeatureText(stringMax(text,50), false).setBorderBottom(new SolidBorder(1)).setPaddingTop(4).setPaddingBottom(4).setMinHeight(13).setPaddingLeft(2));
            }
        }
        // normal display
        else if(entities.size() <=42) {
            table = new Table(1);
            table.setFixedPosition(190, 106, 0);
            table.setVerticalBorderSpacing(0);
            table.setHorizontalBorderSpacing(0);
            table.addCell(createLabel("[T]raits, [D]ons et [C]apacités", "", 1,1).setMinWidth(200).setMinHeight(12));

            for (int i = 0; i < 42; i++) {
                Object element = i < entities.size() ? entities.get(i) : null;
                String text = getTextFromTraitFeatFeature(element);
                table.addCell(createFeatureText(stringMax(text,50), false).setBorderBottom(new SolidBorder(1)).setMinHeight(13).setPaddingLeft(2));
            }
        }
        // dense display
        else {
            table = new Table(1);
            table.setFixedPosition(190, 100, 0);
            table.setVerticalBorderSpacing(0);
            table.setHorizontalBorderSpacing(0);
            table.addCell(createLabel("[T]raits, [D]ons et [C]apacités", "", 1,1).setMinWidth(200).setMinHeight(12));

            for (int i = 0; i < 66; i++) {
                Color backgnd = i % 2 == 1 ? COLOR_LIGHT_GRAY : ColorConstants.WHITE;
                Object element = i < entities.size() ? entities.get(i) : null;
                String text = getTextFromTraitFeatFeature(element);
                table.addCell(createFeatureText(stringMax(text,50), false).setBackgroundColor(backgnd).setMinHeight(9).setPaddingLeft(2));
            }
        }
        return table;
    }


    public Table createSectionSpells() {
        Table table;
        List<Triplet<Class, ClassArchetype,Integer>> spellClasses = new ArrayList<>();
        for(int i=0; i<character.getClassesCount(); i++) {
            SpellFilter filter = new SpellFilter(null);
            Triplet<Class, ClassArchetype,Integer> classLvl = character.getClass(i);
            // increase spell caster level
            classLvl = new Triplet<>(classLvl.first, classLvl.second, classLvl.third + character.getAdditionalBonus(Character.MODIF_COMBAT_MAG_LVL));
            filter.addFilterClass(classLvl.first.getId());
            Class.Level lvl = classLvl.first.getLevel(classLvl.third);
            if(lvl != null && lvl.getMaxSpellLvl() > 0) {
                filter.setFilterMaxLevel(lvl.getMaxSpellLvl());
                spellClasses.add(classLvl);
            }
        }

        SpellTable sTable = new SpellTable(spellClasses);
        for(Spell s : character.getSpells()) {
            sTable.addSpell(s);
        }

        List<String> spellTexts = new ArrayList<>();
        for(SpellTable.SpellLevel lvl : sTable.getLevels()) {
            spellTexts.add("Niveau " + lvl.getLevel());
            for(SpellTable.SpellAndClass s : lvl.getSpells()) {
                String school = s.getSpell().getSchool();
                if(school != null && school.length() > 3) {
                    school = school.substring(0,3);
                    school = String.valueOf(school.charAt(0)).toUpperCase() + school.substring(1);
                } else {
                    school = "   ";
                }
                spellTexts.add(String.format("[%s] %s", school, s.getSpell().getName()));
            }
            spellTexts.add("");
        }


        // normal display
        if(spellTexts.size() <= 34) {
            table = new Table(1);
            table.setFixedPosition(410, 55, 0);
            table.setVerticalBorderSpacing(0);
            table.setHorizontalBorderSpacing(0);
            table.addCell(createLabel("Sorts", "", 1,1).setMinWidth(155).setMinHeight(12));

            for (int i = 0; i < 34; i++) {
                String text = i < spellTexts.size() ? spellTexts.get(i) : "";
                table.addCell(createFeatureText(stringMax(text,40), text.startsWith("Niveau")).setBorderBottom(new SolidBorder(1)).setPaddingTop(4).setPaddingBottom(4).setMinHeight(13).setPaddingLeft(2));
            }
        } else if(spellTexts.size() <= 54) {
            table = new Table(1);
            table.setFixedPosition(410, 55, 0);
            table.setVerticalBorderSpacing(0);
            table.setHorizontalBorderSpacing(0);
            table.addCell(createLabel("Sorts", "", 1,1).setMinWidth(155).setMinHeight(12));

            for (int i = 0; i < 54; i++) {
                String text = i < spellTexts.size() ? spellTexts.get(i) : "";
                table.addCell(createFeatureText(stringMax(text,40), text.startsWith("Niveau")).setBorderBottom(new SolidBorder(1)).setMinHeight(13).setPaddingLeft(2));
            }
        } else {
            table = new Table(1);
            table.setFixedPosition(410, 55, 0);
            table.setVerticalBorderSpacing(0);
            table.setHorizontalBorderSpacing(0);
            table.addCell(createLabel("Sorts", "", 1,1).setMinWidth(155).setMinHeight(12));

            for (int i = 0; i < 84; i++) {
                Color backgnd = i % 2 == 1 ? COLOR_LIGHT_GRAY : ColorConstants.WHITE;
                String text = i < spellTexts.size() ? spellTexts.get(i) : "";
                table.addCell(createFeatureText(stringMax(text,40), text.startsWith("Niveau")).setBackgroundColor(backgnd).setMinHeight(9).setPaddingLeft(2));
            }
        }
        return table;
    }

    public Table createSectionExperience() {
        Table table = new Table(2);
        table.setFixedPosition(190, 50, 0);
        //table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);
        table.addCell(createLabel("Points d'expérience", "", 1,1).setMinWidth(155).setMinHeight(12));
        table.addCell(createLabel("Niveau", "", 1,1).setMinWidth(40).setMinHeight(12));
        table.addCell(createValueExperience(character.getExperience(), TextAlignment.RIGHT).setPaddingRight(5));
        table.addCell(createValueExperience(character.getLevel(), TextAlignment.CENTER));
        return table;
    }



    public void generatePDF(OutputStream output, ImageData logo) {
        PdfDocument pdf = new PdfDocument(new PdfWriter(output));
        Document document = new Document(pdf);
        document.setMargins(20, 20,20,20);

        // show logo
        if(options.printLogo) {
            document.add((new Image(logo)).setWidth(LOGO_WIDTH));
            document.add(new Paragraph("Feuille de Personnage")
                    .addStyle(STYLE_CELL_DEFAULT)
                    .setFixedPosition(70, 760, 100));
        } else {
            document.add(new Paragraph("")
                    .addStyle(STYLE_CELL_DEFAULT)
                    .setHeight(60)
                    .setFixedPosition(22, 760, 200)
                    .setBorder(new SolidBorder(0.5f)));
        }
        document.add(createSectionStats());
        document.add(createSectionInfos());
        document.add(createSectionHitpoints());
        document.add(createSectionSpeed());
        document.add(createSectionInitiative());
        document.add(createSectionArmorClass());
        document.add(createSectionResistances());
        document.add(createSectionAttackDefense());
        document.add(createSectionWeapon(21,310, 0));
        document.add(createSectionWeapon(21,245, 1));
        document.add(createSectionWeapon(21,180, 2));
        document.add(createSectionWeapon(21,115, 3));
        document.add(createSectionWeapon(21,50, 4));
        document.add(createSectionSkills());
        document.add(createSectionOthers());
        document.add(new AreaBreak());

        // page 2
        document.add(createSectionArmors());
        document.add(createSectionInventory());
        document.add(createSectionWeights());
        document.add(createSectionRichness());
        document.add(createSectionFeatsAndFeatures());
        document.add(createSectionExperience());
        document.add(createSectionSpells());

        document.close();
    }
}

