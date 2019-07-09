package org.pathfinderfr.app.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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
}
