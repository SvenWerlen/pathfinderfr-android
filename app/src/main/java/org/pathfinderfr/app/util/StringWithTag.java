package org.pathfinderfr.app.util;

public class StringWithTag {
    private String string;
    private Object tag;

    public StringWithTag(String stringPart, Object tagPart) {
        string = stringPart;
        tag = tagPart;
    }

    public String getString() {
        return string;
    }

    public Object getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return string;
    }
}