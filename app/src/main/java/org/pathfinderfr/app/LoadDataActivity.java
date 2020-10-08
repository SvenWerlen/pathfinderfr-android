package org.pathfinderfr.app;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.LoadDataTask;
import org.pathfinderfr.app.database.entity.ArmorFactory;
import org.pathfinderfr.app.database.entity.ClassArchetypesFactory;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.ConditionFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.EquipmentFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.MagicItemFactory;
import org.pathfinderfr.app.database.entity.TraitFactory;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.database.entity.WeaponFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoadDataActivity extends AppCompatActivity implements LoadDataTask.IDataUI {

    public static final String SOURCE = "https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/master";
    public static final String VERSION = SOURCE + "/data/versions.yml";

    private LoadDataTask loadTaskInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button button = findViewById(R.id.loaddataButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadTaskInProgress == null) {
                    // disable buttons to force user to stay
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    Button button = findViewById(R.id.loaddataButton);
                    button.setText(getResources().getString(R.string.loaddata_stop));
                    findViewById(R.id.loaddataProgressBar).setVisibility(View.VISIBLE);
                    findViewById(R.id.loaddataInfos).setVisibility(View.VISIBLE);
                    findViewById(R.id.migrationRequiredMessage).setVisibility(View.GONE);
                    boolean forceUpdate = ((CheckBox)findViewById(R.id.forceupdate)).isChecked();

                    loadTaskInProgress = new LoadDataTask(LoadDataActivity.this, forceUpdate);
                    loadTaskInProgress.execute(
                            new Pair(SOURCE + "/data/races.yml", RaceFactory.getInstance()),
                            new Pair(SOURCE + "/data/classes.yml", ClassFactory.getInstance()),
                            new Pair(SOURCE + "/data/class-archetypes.yml", ClassArchetypesFactory.getInstance()),
                            new Pair(SOURCE + "/data/traits.yml", TraitFactory.getInstance()),
                            new Pair(SOURCE + "/data/competences.yml", SkillFactory.getInstance()),
                            new Pair(SOURCE + "/data/dons.yml", FeatFactory.getInstance()),
                            new Pair(SOURCE + "/data/classfeatures.yml", ClassFeatureFactory.getInstance()),
                            new Pair(SOURCE + "/data/spells.yml", SpellFactory.getInstance()),
                            new Pair(SOURCE + "/data/conditions.yml", ConditionFactory.getInstance()),
                            new Pair(SOURCE + "/data/armes.yml", WeaponFactory.getInstance()),
                            new Pair(SOURCE + "/data/armures.yml", ArmorFactory.getInstance()),
                            new Pair(SOURCE + "/data/equipement.yml", EquipmentFactory.getInstance()),
                            new Pair(SOURCE + "/data/magic.yml", MagicItemFactory.getInstance())
                            );

                } else {
                    Button button = findViewById(R.id.loaddataButton);
                    button.setText(getResources().getString(R.string.loaddata_stopping));
                    button.setEnabled(false);
                    if(loadTaskInProgress != null) {
                        loadTaskInProgress.cancel(false);
                    }

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (loadTaskInProgress == null) {
            super.onBackPressed();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getResources().getString(R.string.loaddata_pleasewait), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onProgressUpdate(LoadDataTask.UpdateStatus... progresses) {
        int totalProgress = 0;
        for(LoadDataTask.UpdateStatus p : progresses) {
            if(p != null && p.getCountTotal() > 0) {
                totalProgress += (int) ((p.getCountProcessed() / (float) p.getCountTotal()) * 100);
            }
        }

        // update information
        boolean completed = true;
        String text = "";
        for(int i=0; i<progresses.length; i++) {
            String status;
            if(progresses[i].getStatus() == LoadDataTask.UpdateStatus.STATUS_NOTSTARTED) {
                status = getResources().getString(R.string.loaddata_waiting);
            } else if(progresses[i].getStatus() == LoadDataTask.UpdateStatus.STATUS_NOUPDATE_REQUIRED) {
                status = String.format(getResources().getString(R.string.loaddata_noupdate_required), progresses[i].getOldVersion());
                completed = completed && progresses[i].hasEnded();
            } else if(progresses[i].getStatus() == LoadDataTask.UpdateStatus.STATUS_DOWNLOADING) {
                status = getResources().getString(R.string.loaddata_downloading);
                completed = completed && progresses[i].hasEnded();
            } else {
                String done = String.valueOf(progresses[i].getCountProcessed());
                String total = String.valueOf(progresses[i].getCountTotal());
                String percentage = String.valueOf((int)((progresses[i].getCountProcessed() / (float)progresses[i].getCountTotal()) * 100));
                String template = getResources().getString(R.string.loaddata_inprogress);
                status = String.format(template,done,total,percentage + '%');
                completed = completed && progresses[i].hasEnded();
            }

            String sourceName = ConfigurationUtil.getInstance(getApplicationContext()).getProperties().getProperty("template.title." + progresses[i].getFactoryId().toLowerCase());
            text += "<b>" + sourceName + "</b>: " + status + "<br/>";
        }
        //if(completed) {
        //    text += "<br/>" + favoriteMigrationText(progresses);
        //}

        final int progress = progresses.length == 0 ? 100 : totalProgress / progresses.length;
        final String message = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) findViewById(R.id.loaddataProgressBar)).setProgress(progress);
                ((TextView)findViewById(R.id.loaddataInfos)).setText(Html.fromHtml(message));
            }
        });
    }

    @Override
    public void onOptimisation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.optimizeMessage).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onProgressCompleted(Integer... counts) {
        findViewById(R.id.optimizeMessage).setVisibility(View.GONE);

        // change status migrationRequiredMessage
        loadTaskInProgress = null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String buttonText = getResources().getString(R.string.loaddata_start);
        final int progress = 100;

        // migrate characters
        Map<String, Integer> unmatched = DBHelper.getInstance(this.getBaseContext()).migrateCharacters(false);
        if(unmatched.size() > 0) {
            StringBuffer buf = new StringBuffer();
            for(String name: unmatched.keySet()) {
                buf.append(String.format(Locale.CANADA, ", %s (%d)", name, unmatched.get(name)));
            }
            if( buf.length() > 0 ) buf.delete(0, 2);
            TextView tv = findViewById(R.id.migrationRequiredMessage);
            tv.setText(String.format(ConfigurationUtil.getInstance(getBaseContext()).getProperties().getProperty("warning.migrationrequired"), buf.toString()));
            tv.setVisibility(View.VISIBLE);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((Button)findViewById(R.id.loaddataButton)).setText(buttonText);
                ((Button)findViewById(R.id.loaddataButton)).setEnabled(true);
                ((ProgressBar) findViewById(R.id.loaddataProgressBar)).setProgress(progress);
            }
        });
    }

}
