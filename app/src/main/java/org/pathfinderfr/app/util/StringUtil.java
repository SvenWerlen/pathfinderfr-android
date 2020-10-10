package org.pathfinderfr.app.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * Extract the domain name from given URL.
     * Ex: http://www.pathfinder-fr.org/... => www.pathfinder-fr.org
     * @param url full address (URL)
     * @return website (domain name)
     */
    public static final String extractWebSite(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return "??";
        }

    }

    /**
     * Transforms a String array into a String (same as join in Java8)
     * @param sep separator (ex: ',')
     * @param values list of String
     * @return String of sep separated values
     */
    public static final String listToString(String[] values, String sep, Character quote) {
        if(values == null) {
            return null;
        }
        StringBuffer sourceList = new StringBuffer();
        for(String s : values) {
            if(quote != null) {
                sourceList.append(quote).append(s).append(quote);
            } else {
                sourceList.append(s);
            }
            if(sep != null) {
                sourceList.append(sep);
            }
        }
        if(values.length>0 && sep != null) {
            sourceList.delete(sourceList.length()-sep.length(),sourceList.length());
        }
        return sourceList.toString();
    }

    public static final String listToString(String[] values, Character sep, Character quote) {
        return listToString(values, sep != null ? Character.toString(sep) : null, quote);
    }

    public static final String listToString(String[] values, String sep) {
        return listToString(values, sep, null);
    }

    public static final String listToString(String[] values, Character sep) {
        return listToString(values, sep, null);
    }

    public static final String listToString(Integer[] values, String sep) {
        List<String> list = new ArrayList<>();
        for(Integer i : values) {
            list.add(String.valueOf(i));
        }
        return listToString(list, sep);
    }

    public static final String listToString(int[] values, String sep) {
        String[] list = new String[values.length];
        for(int i = 0; i<values.length; i++) {
            list[i] = String.valueOf(values[i]);
        }
        return listToString(list, sep, null);
    }

    public static final String listToString(int[] values, Character sep) {
        return listToString(values, sep != null ? Character.toString(sep) : null);
    }

    public static final String listToString(long[] values, String sep) {
        String[] list = new String[values.length];
        for(int i = 0; i<values.length; i++) {
            list[i] = String.valueOf(values[i]);
        }
        return listToString(list, sep, null);
    }

    public static final String listToString(long[] values, Character sep) {
        return listToString(values, sep != null ? Character.toString(sep) : null);
    }

    public static final String listToString(Long[] values, String sep) {
        String[] list = new String[values.length];
        for(int i = 0; i<values.length; i++) {
            list[i] = String.valueOf(values[i]);
        }
        return listToString(list, sep, null);
    }

    public static final String listToString(List<String> values, String sep) {
        String[] list = values == null || values.size() == 0 ? null : values.toArray(new String[]{});
        return listToString(list, sep, null);
    }

    public static final String listToString(Long[] values, Character sep) {
        return listToString(values, sep != null ? Character.toString(sep) : null);
    }

    public static final int[] stringListToIntList(String[] values) {
        int[] list = new int[values.length];
        for(int i = 0; i<values.length; i++) {
            list[i] = Integer.parseInt(values[i]);
        }
        return list;
    }

    public static int parseWeight(String weight) {
        if(weight == null || weight.length() == 0) {
            return 0;
        }
        weight = weight.replaceAll("  ", " ");

        try {
            // check format: 500 g
            Pattern p = Pattern.compile("^(\\d+) g$");
            Matcher m = p.matcher(weight.trim());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            // check format: 1,5 kg
            p = Pattern.compile("^([\\d,]+) kg$");
            m = p.matcher(weight.trim());
            if (m.find()) {
                return Math.round(1000 * Float.parseFloat(m.group(1).replaceAll(",","\\.")));
            }
        } catch(Exception e) {}
        return 0;
    }

    /**
     * Converts a string such "Cost" as value
     * @return
     */
    public static long string2Cost(String txt) {
        // remove space ("10 000 po" => "10000po")
        txt = txt.replace(" ", "");
        Pattern pattern = Pattern.compile("^(\\d*)p([cao])");
        Matcher matcher = pattern.matcher(txt);
        if (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group(1));
                switch(matcher.group(2)) {
                    case "c": return value;
                    case "a": return value*10;
                    case "o": return value*100;
                }
            } catch(NumberFormatException nfe) {}
        }
        return 0;
    }

    /**
     * Returns a value (total money) as text representation
     * @param total
     * @return
     */
    public static String cost2String(long total) {
        String text = "";

        long po = total/100;
        long pa = (total-po*100)/10;
        long pc = total % 10;

        if(po > 0) {
            NumberFormat df = DecimalFormat.getInstance(Locale.CANADA_FRENCH);
            text += df.format(po) + " po";
        }
        if(pa > 0) {
            text += (text.length() > 0 ? ", " : "") + String.format("%d pa", pa);
        }
        if(pc > 0) {
            text += (text.length() > 0 ? ", " : "") + String.format("%d pc", pc);
        }
        return text;
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    }

    public static String generate(int count, String val) {
        StringBuffer buf = new StringBuffer();
        for(int i=0; i<count; i++) {
            buf.append(val);
        }
        return buf.toString();
    }

    /**
     * Cut string in order to make it fit the maximum length
     * Try to cut at a space
     */
    public static String smartSubstring(String s, int maxLength) {
        if(maxLength > 0 && s.length() >  maxLength) {
            int idx = s.substring(0, maxLength).lastIndexOf(' ');
            if(idx > 0) {
                return s.substring(0, idx);
            } else {
                return s.substring(0, maxLength);
            }
        }
        return s;
    }

    public static int[] extractCritical(String critical) {
        Pattern pattern = Pattern.compile("(?:(\\d{2})-\\d{2}/)?×(\\d)(?:/×(\\d))?");
        Matcher m = pattern.matcher(critical);
        if(m.matches()) {
            int[] result = new int[3];
            result[0] = m.group(1) == null ? 20 : Integer.parseInt(m.group(1));
            result[1] = m.group(2) == null ? 2 : Integer.parseInt(m.group(2));
            result[2] = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
            return result;
        }
        return null;
    }

    public static String cleanText(String text) {
        if(text.trim().startsWith("\\.")) {
            return text.trim().substring(0,1).trim();
        } else {
            return text.trim();
        }
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
