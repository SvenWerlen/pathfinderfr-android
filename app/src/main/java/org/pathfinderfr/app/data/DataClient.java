package org.pathfinderfr.app.data;

import android.os.AsyncTask;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.SpellFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DataClient extends AsyncTask<String, Void, ArrayList<DBEntity>> {


    @Override
    protected ArrayList<DBEntity> doInBackground(String... urls) {
        DBHelper dbHelper = DBHelper.getInstance(null);
        dbHelper.clear();

        ArrayList<DBEntity> entities = new ArrayList<>();

        for(String u: urls) {

            URL url = null;
            HttpsURLConnection urlConnection = null;
            try {
                url = new URL(u);
                urlConnection = (HttpsURLConnection) url.openConnection();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                SpellFactory factory = SpellFactory.getInstance();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                YamlReader reader = new YamlReader(new InputStreamReader(in, "UTF-8"));
                ArrayList<Object> list  = reader.read(ArrayList.class);
                for(Object obj : list) {
                    if(obj instanceof Map) {
                        DBEntity entity = factory.generateEntity((Map<String,String>)obj);
                        if(entity != null) {
                            entities.add(entity);
                            dbHelper.insertEntity(entity);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        }
        return entities;
    }

    @Override
    protected void onPostExecute(ArrayList<DBEntity> entities) {
        System.out.println("Number of entries downloaded: " + entities.size());
    }

}
