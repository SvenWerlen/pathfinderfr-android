package org.pathfinderfr.app.util;

import java.net.URI;
import java.net.URISyntaxException;

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
    public static final String listToString(String[] values, Character sep, Character quote) {
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
            sourceList.deleteCharAt(sourceList.length()-1);
        }
        return sourceList.toString();
    }

    public static final String listToString(String[] values, Character sep) {
        return listToString(values, sep, null);
    }
}
