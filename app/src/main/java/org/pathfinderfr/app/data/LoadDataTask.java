package org.pathfinderfr.app.data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.pathfinderfr.app.LoadDataActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SpellFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class LoadDataTask extends AsyncTask<Pair<String,DBEntityFactory>, LoadDataTask.UpdateStatus, List<Integer>> {

    public static interface IDataUI {
        public void onProgressUpdate(UpdateStatus... progress);
        public void onProgressCompleted(Integer... counts);
        public void onOptimisation();
    };

    public static class UpdateStatus {

        public static final int STATUS_ERROR = -1;
        public static final int STATUS_PENDING = 0;
        public static final int STATUS_NOTCHANGED = 1;
        public static final int STATUS_CHANGED = 2;
        public static final int STATUS_NOTFOUND = 3;
        public static final int STATUS_DELETED = 4;

        private String factoryId;
        private int countProcessed;
        private int countTotal;
        private List<Pair<DBEntity,Integer>> favorite;
        private Integer oldVersion;
        private Integer newVersion;
        private boolean ended;

        public UpdateStatus(String factoryId) {
            this.factoryId = factoryId;
            this.countProcessed = -1;
            this.countTotal = 0;
            this.favorite = new ArrayList<>();
            this.ended = false;
            this.oldVersion = null;
            this.newVersion = null;
        }

        public String getFactoryId() { return factoryId; }
        public int getCountProcessed() { return countProcessed; }
        public void setCountProcessed(int countProcessed) { this.countProcessed = countProcessed; }
        public int getCountTotal() { return countTotal; }
        public void setCountTotal(int countTotal) { this.countTotal = countTotal; }
        public List<Pair<DBEntity,Integer>> getFavoriteStatus() { return new ArrayList<>(this.favorite); }
        public void addFavoriteStatus(DBEntity fav, Integer status) { favorite.add(new Pair<DBEntity, Integer>(fav,status));}
        public void setHasEnded(boolean ended) { this.ended = ended; }
        public boolean hasEnded() { return this.ended; }
        public Integer getOldVersion() { return this.oldVersion; }
        public void setOldVersion(Integer version) { this.oldVersion = version; }
        public Integer getNewVersion() { return this.newVersion; }
        public void setNewVersion(Integer version) { this.newVersion = version; }
    }

    private IDataUI caller;
    private boolean deleteOrpheans;

    public LoadDataTask(@NonNull IDataUI caller, boolean deleteOrpheans) {
        this.caller = caller;
        this.deleteOrpheans = deleteOrpheans;
    }

    @Override
    protected List<Integer> doInBackground(Pair<String,DBEntityFactory>... sources) {
        DBHelper dbHelper = DBHelper.getInstance(null);

        // initialize progresses
        UpdateStatus progresses[] = new UpdateStatus[sources.length];
        int idx = 0;
        for(Pair<String,DBEntityFactory> source: sources) {
            progresses[idx] = new UpdateStatus(source.second.getFactoryId());
            idx++;
        }

        Integer[] count = new Integer[sources.length];

        // retrieve versions
        Map<String,Integer> versions = new HashMap<>();
        try {
            URL url = new URL(LoadDataActivity.VERSION);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            YamlReader reader = new YamlReader(new InputStreamReader(in, "UTF-8"));
            Map map = (Map)reader.read();
            List<Map> list = (List<Map>)map.get("Versions");
            for(Map ver: list) {
                versions.put((String)ver.keySet().iterator().next(), Integer.parseInt((String)ver.values().iterator().next()));
            }
        } catch (Exception e) {
            // versions couldn't be found???
            e.printStackTrace();
            return Arrays.asList(count);
        }

        boolean reIndexingRequired = false;

        idx = 0;
        for(Pair<String,DBEntityFactory> source: sources) {

            count[idx] = 0;

            // check if update is required
            String dataId = source.second.getFactoryId().toLowerCase();
            Integer oldVersion = dbHelper.getVersion(dataId);
            Integer newVersion = versions.get(dataId);
            progresses[idx].setOldVersion(oldVersion);
            progresses[idx].setNewVersion(newVersion);

            //System.out.println("Version " + dataId + ": " + oldVersion + " => " + newVersion);

            // no update required => skip
            if(oldVersion != null && newVersion != null && oldVersion.intValue() >= newVersion.intValue()) {
                progresses[idx].setCountProcessed(0);
                progresses[idx].setCountTotal(-1);
                progresses[idx].setHasEnded(true);
                idx++;
                continue;
            }

            String address = source.first;
            DBEntityFactory factory = source.second;
            dbHelper.clear(factory);

            if(factory == SpellFactory.getInstance()) {
                reIndexingRequired = true;
            }

            // ==============================================
            // First part : load data from GitHub repository
            // ==============================================

            URL url = null;
            HttpsURLConnection urlConnection = null;
            try {
                url = new URL(address);
                urlConnection = (HttpsURLConnection) url.openConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (isCancelled()) { progresses[idx].setHasEnded(true); break; }
                publishProgress(progresses);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (isCancelled()) { progresses[idx].setHasEnded(true); break; }
                YamlReader reader = new YamlReader(new InputStreamReader(in, "UTF-8"));
                ArrayList<Object> list  = reader.read(ArrayList.class);
                progresses[idx].setCountTotal(list.size());

                int lastPercentage = 0;
                for(int i=0; i<list.size(); i++) {
                    if (isCancelled()) { progresses[idx].setHasEnded(true); break; }
                    if(list.get(i) instanceof Map) {
                        DBEntity entity = factory.generateEntity((Map<String,Object>)list.get(i));
                        if(entity != null) {
                            long id = dbHelper.insertEntity(entity);
                            if(id >= 0) {
                                count[idx]++;
                            }
                        }
                    }

                    progresses[idx].setCountProcessed(i+1);
                    int percentage = (int)((progresses[idx].getCountProcessed() / (float)progresses[idx].getCountTotal()) * 100);

                    // avoid publishing progress for too small steps
                    if(percentage != lastPercentage) {
                        publishProgress(progresses);
                        lastPercentage = percentage;
                    }
                }
                progresses[idx].setHasEnded(true);

                // update version into database
                dbHelper.updateVersion(dataId, newVersion);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            // ==============================================
            // Second part : migrate favorites
            // ==============================================

            List<DBEntity> favorites = dbHelper.getAllEntities(FavoriteFactory.getInstance());
            for(DBEntity fav: favorites) {
                if(!(fav.getFactory() == source.second)) {
                    continue;
                }
                if (isCancelled()) { break; }
                // TODO: find a way to better handle this special case.
                // Favorites stores long names as names because details are not available
                String name = fav.getName();
                if(!(fav instanceof Skill)) {
                    int index = fav.getName().indexOf('(');
                    if(index > 0) {
                        name = name.substring(0, index-1);
                    }
                }
                int status = LoadDataTask.UpdateStatus.STATUS_PENDING;
                try {
                    DBEntity found = dbHelper.fetchEntityByName(name, fav.getFactory());
                    if(found == null) {
                        if(deleteOrpheans) {
                            dbHelper.deleteFavorite(fav);
                            status = UpdateStatus.STATUS_DELETED;
                        } else {
                            status = UpdateStatus.STATUS_NOTFOUND;
                        }
                    } else {
                        if(fav.getId() == found.getId()) {
                            status = UpdateStatus.STATUS_NOTCHANGED;
                        } else {
                            status = UpdateStatus.STATUS_CHANGED;
                            dbHelper.deleteFavorite(fav);
                            dbHelper.insertFavorite(found);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    status = LoadDataTask.UpdateStatus.STATUS_ERROR;
                }
                progresses[idx].addFavoriteStatus(fav, status);
            }

            idx++;
        }

        // ==============================================
        // Third part : migrate favorites
        // ==============================================
        if(reIndexingRequired) {
            caller.onOptimisation();
            dbHelper.fillSpellClassLevel();
        }

        if(isCancelled()) {
            caller.onProgressUpdate(progresses);
        } else {
            publishProgress(progresses);
        }

        return Arrays.asList(count);
    }

    @Override
    protected void onProgressUpdate(UpdateStatus... values) {
        caller.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(List<Integer> counts) {
        caller.onProgressCompleted((Integer[])counts.toArray());
    }

    @Override
    protected void onCancelled(List<Integer> counts) {
        if(counts != null) {
            caller.onProgressCompleted((Integer[]) counts.toArray());
        }
    }

}
