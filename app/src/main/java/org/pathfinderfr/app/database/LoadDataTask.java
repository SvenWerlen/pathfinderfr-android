package org.pathfinderfr.app.database;

import android.os.AsyncTask;
import androidx.annotation.NonNull;

import android.util.Log;
import android.util.Pair;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.pathfinderfr.app.LoadDataActivity;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
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

        // status for items migration
        public static final int STATUS_MIGR_ERROR = -1;
        public static final int STATUS_MIGR_PENDING = 0;
        public static final int STATUS_MIGR_NOTCHANGED = 1;
        public static final int STATUS_MIGR_CHANGED = 2;
        public static final int STATUS_MIGR_NOTFOUND = 3;
        public static final int STATUS_MIGR_DELETED = 4;

        // status for update
        public static final int STATUS_NOTSTARTED = -1;
        public static final int STATUS_DOWNLOADING = 0;
        public static final int STATUS_NOUPDATE_REQUIRED = 1;
        public static final int STATUS_INPROGRESS = 2;
        public static final int STATUS_CANCELLED = 3;
        public static final int STATUS_ENDED = 4;

        private String factoryId;
        private int countProcessed;
        private int countTotal;
        private int status;
        private List<Pair<DBEntity,Integer>> favorite;
        private Integer oldVersion;
        private Integer newVersion;

        public UpdateStatus(String factoryId) {
            this.factoryId = factoryId;
            this.countProcessed = -1;
            this.countTotal = 0;
            this.favorite = new ArrayList<>();
            this.oldVersion = null;
            this.newVersion = null;
            status = STATUS_NOTSTARTED;
        }

        public String getFactoryId() { return factoryId; }
        public int getCountProcessed() { return countProcessed; }
        public void setCountProcessed(int countProcessed) { this.countProcessed = countProcessed; }
        public int getCountTotal() { return countTotal; }
        public void setCountTotal(int countTotal) { this.countTotal = countTotal; }
        public List<Pair<DBEntity,Integer>> getFavoriteStatus() { return new ArrayList<>(this.favorite); }
        public void addFavoriteStatus(DBEntity fav, Integer status) { favorite.add(new Pair<DBEntity, Integer>(fav,status));}
        public void setStatus(int status) { this.status = status; }
        public int getStatus() { return status; }
        public boolean hasEnded() { return status == STATUS_ENDED || status == STATUS_NOUPDATE_REQUIRED || status == STATUS_CANCELLED; }
        public Integer getOldVersion() { return this.oldVersion; }
        public void setOldVersion(Integer version) { this.oldVersion = version; }
        public Integer getNewVersion() { return this.newVersion; }
        public void setNewVersion(Integer version) { this.newVersion = version; }
    }

    private IDataUI caller;
    private boolean forceUpdate;

    public LoadDataTask(@NonNull IDataUI caller, boolean forceUpdate) {
        this.caller = caller;
        this.forceUpdate = forceUpdate;
    }

    public static Map<String,Integer> getLatestVersion() {
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
            return versions;
        } catch (Exception e) {
            // versions couldn't be found???
            e.printStackTrace();
            return null;
        }
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

        // check database (just in case)
        dbHelper.checkDatabase();

        // retrieve versions
        Map<String,Integer> versions = getLatestVersion();
        if(versions == null) {
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

            System.out.println("Version " + dataId + ": " + oldVersion + " => " + newVersion);

            // no update required => skip
            if(!forceUpdate && oldVersion != null && newVersion != null && oldVersion >= newVersion) {
                progresses[idx].setStatus(UpdateStatus.STATUS_NOUPDATE_REQUIRED);
                idx++;
                continue;
            }

            // clear data of same version


            String address = source.first;
            DBEntityFactory factory = source.second;
            // dbHelper.clear(factory); // since v4, tables are not cleared any more
            // clean any existing data with that version to avoid duplicates (data may have been forced!)
            dbHelper.clearDataWithVersion(factory, newVersion);

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

            Map<String, DBEntity> referencesMap = new HashMap<>();

            try {
                if (isCancelled()) { progresses[idx].setStatus(UpdateStatus.STATUS_CANCELLED); break; }
                progresses[idx].setStatus(UpdateStatus.STATUS_DOWNLOADING);
                publishProgress(progresses);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (isCancelled()) { progresses[idx].setStatus(UpdateStatus.STATUS_CANCELLED); break; }
                YamlReader reader = new YamlReader(new InputStreamReader(in, "UTF-8"));
                ArrayList<Object> list  = reader.read(ArrayList.class);
                progresses[idx].setCountTotal(list.size());
                progresses[idx].setStatus(UpdateStatus.STATUS_INPROGRESS);

                int lastPercentage = 0;
                for(int i=0; i<list.size(); i++) {
                    if (isCancelled()) { progresses[idx].setStatus(UpdateStatus.STATUS_CANCELLED); break; }
                    if(list.get(i) instanceof Map) {
                        DBEntity entity = factory.generateEntity((Map<String,Object>)list.get(i));
                        if(entity != null) {
                            // for feats, update "requires" before inserting into DB
                            // assuming that required feats have already been processed
                            if(entity instanceof Feat) {
                                Feat f = (Feat)entity;
                                for(String required : f.getRequiresRef()) {
                                    if(referencesMap.containsKey(required)) {
                                        f.getRequires().add(((Feat)referencesMap.get(required)).getId());
                                    } else {
                                        Log.w(LoadDataTask.class.getSimpleName(), "Couldn't find required feat for " + f.getName());
                                    }
                                }
                            }

                            entity.setVersion(newVersion);
                            long id = dbHelper.insertEntity(entity);
                            if(id >= 0) {
                                entity.setId(id);
                                referencesMap.put(entity.getReference(), entity);
                                count[idx]++;
                            }
                        } else {
                            System.out.println("Cannot be imported!!");
                        }
                    }

                    progresses[idx].setCountProcessed(i+1);
                    int percentage = (int)((progresses[idx].getCountProcessed() / (float)progresses[idx].getCountTotal()) * 100);

                    // avoid publishing progress for too small steps
                    if(percentage >= lastPercentage + 5) {
                        publishProgress(progresses);
                        lastPercentage = percentage;
                    }
                }
                progresses[idx].setStatus(UpdateStatus.STATUS_ENDED);

                // update version into database
                dbHelper.updateVersion(dataId, newVersion);

                // cleanup factory
                factory.cleanup();

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
                int migrStatus;
                try {
                    DBEntity found = dbHelper.fetchEntityByName(name, fav.getFactory());
                    if(found == null) {
                        dbHelper.deleteFavorite(fav);
                        migrStatus = UpdateStatus.STATUS_MIGR_DELETED;
                    } else {
                        if(fav.getId() == found.getId()) {
                            migrStatus = UpdateStatus.STATUS_MIGR_NOTCHANGED;
                        } else {
                            migrStatus = UpdateStatus.STATUS_MIGR_CHANGED;
                            dbHelper.deleteFavorite(fav);
                            dbHelper.insertFavorite(found);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    migrStatus = LoadDataTask.UpdateStatus.STATUS_MIGR_ERROR;
                }
                progresses[idx].addFavoriteStatus(fav, migrStatus);
            }

            idx++;
        }

        // ==============================================
        // Last part : update spell class indexes
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
