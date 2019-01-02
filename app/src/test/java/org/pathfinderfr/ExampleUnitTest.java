package org.pathfinderfr;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.junit.Test;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() throws Exception {
        YamlReader reader = new YamlReader(new FileReader("/home/sven/projets/PathfinderVolume/data/spells.yml"));
        SpellFactory factory = SpellFactory.getInstance();
        //ArrayList<Object> list = new ArrayList<>();
        ArrayList<Object> list  = reader.read(ArrayList.class);
        for(Object obj : list) {
            if(obj instanceof Map) {
                DBEntity spell = factory.generateEntity((Map<String,String>)obj);
                System.out.println(spell);
            }
        }


    }
}