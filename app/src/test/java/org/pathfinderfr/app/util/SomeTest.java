package org.pathfinderfr.app.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SomeTest {

    @Test
    public void test() {
        String text = "Le . \n\nLorsqu’tion.\n\n Le barbare peut.";
        String text2 = text.replaceAll("\n","<br />");
        System.out.println(text2);
        System.out.println(text2.equals(text));
    }
}
