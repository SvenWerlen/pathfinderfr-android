package org.pathfinderfr.app.util;

import org.json.JSONException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SomeTest {

    static void main(String[] args){

        System.out.println("Hello World");

    }

    public void test() throws Exception {
//        String text = "Le . \n\nLorsqu’tion.\n\n Le barbare peut.";
//        String text2 = text.replaceAll("\n","<br />");
//        System.out.println(text2);
//        System.out.println(text2.equals(text));

        FireMessage msg = new FireMessage("Titre", "Message");
        msg.sendToTopic("Test");
    }


}
