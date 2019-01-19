package org.pathfinderfr.app.data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Skill;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class LoadDataTask extends AsyncTask<Pair<String,DBEntityFactory>, LoadDataTask.UpdateStatus, List<Integer>> {

    public static interface IDataUI {
        public void onProgressUpdate(UpdateStatus... progress);
        public void onProgressCompleted(Integer... counts);
    };

    public static class UpdateStatus {

        public static final int STATUS_ERROR = -1;
        public static final int STATUS_PENDING = 0;
        public static final int STATUS_NOTCHANGED = 1;
        public static final int STATUS_CHANGED = 2;
        public static final int STATUS_NOTFOUND = 3;
        public static final int STATUS_DELETED = 4;

        private int countProcessed;
        private int countTotal;
        private List<Pair<DBEntity,Integer>> favorite;
        private boolean ended;

        public UpdateStatus() {
            this.countProcessed = 0;
            this.countTotal = 0;
            this.favorite = new ArrayList<>();
            this.ended = false;
        }

        public int getCountProcessed() { return countProcessed; }
        public void setCountProcessed(int countProcessed) { this.countProcessed = countProcessed; }
        public int getCountTotal() { return countTotal; }
        public void setCountTotal(int countTotal) { this.countTotal = countTotal; }
        public List<Pair<DBEntity,Integer>> getFavoriteStatus() { return new ArrayList<>(this.favorite); }
        public void addFavoriteStatus(DBEntity fav, Integer status) { favorite.add(new Pair<DBEntity, Integer>(fav,status));}
        public void setHasEnded(boolean ended) { this.ended = ended; }
        public boolean hasEnded() { return this.ended; }
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
        dbHelper.clear();

        UpdateStatus progresses[] = new UpdateStatus[sources.length];
        Integer[] count = new Integer[sources.length];


        int idx = 0;
        for(Pair<String,DBEntityFactory> source: sources) {

            progresses[idx] = new UpdateStatus();
            count[idx] = 0;

            String address = source.first;
            DBEntityFactory factory = source.second;

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
                            boolean success = dbHelper.insertEntity(entity);
                            if(success) {
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
        caller.onProgressCompleted((Integer[])counts.toArray());
    }

}
