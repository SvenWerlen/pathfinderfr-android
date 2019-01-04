package org.pathfinderfr.app.data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DataClient extends AsyncTask<Pair<String,DBEntityFactory>, Pair<Integer,Integer>, List<Integer>> {

    public static interface IDataUI {
        public void onProgressUpdate(Pair<Integer,Integer>... progress);
        public void onProgressCompleted(Integer... counts);
    };

    private IDataUI caller;

    public DataClient(@NonNull IDataUI caller) {
        this.caller = caller;
    }

    @Override
    protected List<Integer> doInBackground(Pair<String,DBEntityFactory>... sources) {
        DBHelper dbHelper = DBHelper.getInstance(null);
        dbHelper.clear();

        Pair<Integer,Integer>[] progresses = new Pair[sources.length];
        Integer[] count = new Integer[sources.length];

        int idx = 0;
        for(Pair<String,DBEntityFactory> source: sources) {

            progresses[idx] = new Pair<>(0,0);
            count[idx] = 0;

            String address = source.first;
            DBEntityFactory factory = source.second;

            URL url = null;
            HttpsURLConnection urlConnection = null;
            try {
                url = new URL(address);
                urlConnection = (HttpsURLConnection) url.openConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                publishProgress(progresses);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                YamlReader reader = new YamlReader(new InputStreamReader(in, "UTF-8"));
                ArrayList<Object> list  = reader.read(ArrayList.class);

                int lastPercentage = 0;
                for(int i=0; i<list.size(); i++) {
                    if (isCancelled()) break;
                    if(list.get(i) instanceof Map) {
                        DBEntity entity = factory.generateEntity((Map<String,String>)list.get(i));
                        if(entity != null) {
                            boolean success = dbHelper.insertEntity(entity);
                            if(success) {
                                count[idx]++;
                            }
                        }
                    }

                    progresses[idx] = new Pair<>(i,list.size());

                    int percentage = (int)((progresses[idx].first / (float)progresses[idx].second) * 100);

                    // avoid publishing progress for too small steps
                    if(percentage != lastPercentage) {
                        publishProgress(progresses);
                        lastPercentage = percentage;
                    }
                }

                // file completed => update progress
                progresses[idx] = new Pair<>(count[idx],count[idx]);
                publishProgress(progresses);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            idx++;
        }
        return Arrays.asList(count);
    }

    @Override
    protected void onProgressUpdate(Pair<Integer,Integer>... values) {
        caller.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(List<Integer> counts) {
        caller.onProgressCompleted((Integer[])counts.toArray());
    }

    @Override
    protected void onCancelled(List<Integer> counts) {
        caller.onProgressCompleted((Integer[])counts.toArray());
    }

}
