package org.pathfinderfr.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.data.DataClient;
import org.pathfinderfr.app.database.entity.DBEntityFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;

public class LoadDataActivity extends AppCompatActivity implements DataClient.IDataUI {

    private static final String[] SOURCES = new String[]{
            "https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/master/data/competences.yml",
            "https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/master/data/dons.yml",
            "https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/master/data/spells.yml"};

    private static final String[] SOURCES_NAMES = new String[]{"Compétences", "Dons", "Sorts"};
    private DataClient taskInProgress;

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
                if(taskInProgress == null) {
                    // disable buttons to force user to stay
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    Button button = findViewById(R.id.loaddataButton);
                    button.setText(getResources().getString(R.string.loaddata_stop));
                    findViewById(R.id.loaddataProgressBar).setVisibility(View.VISIBLE);
                    findViewById(R.id.loaddataInfos).setVisibility(View.VISIBLE);

                    Pair<String, DBEntityFactory> source0 = new Pair(SOURCES[0], SkillFactory.getInstance());
                    Pair<String, DBEntityFactory> source1 = new Pair(SOURCES[1], FeatFactory.getInstance());
                    Pair<String, DBEntityFactory> source2 = new Pair(SOURCES[2], SpellFactory.getInstance());
                    taskInProgress = new DataClient(LoadDataActivity.this);
                    taskInProgress.execute(source0,source1,source2);
                } else {
                    taskInProgress.cancel(false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (taskInProgress == null) {
            super.onBackPressed();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getResources().getString(R.string.loaddata_pleasewait), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onProgressUpdate(Pair<Integer,Integer>... progress) {
        // update progress bar
        ProgressBar bar = (ProgressBar) findViewById(R.id.loaddataProgressBar);
        int totalProgress = 0;
        for(Pair<Integer,Integer> p : progress) {
            if(p != null) {
                totalProgress += (int) ((p.first / (float) p.second) * 100);
            }
        }
        bar.setProgress(totalProgress / progress.length);

        // update information
        TextView view = findViewById(R.id.loaddataInfos);
        String text = "";
        for(int i = 0; i<progress.length; i++) {
            String status;
            if(progress[i] == null) {
                status = getResources().getString(R.string.loaddata_waiting);
            } else if(progress[i].second == 0) {
                status = getResources().getString(R.string.loaddata_downloading);
            } else {
                String done = String.valueOf(progress[i].first);
                String total = String.valueOf(progress[i].second);
                String percentage = String.valueOf((int)((progress[i].first / (float)progress[i].second) * 100));
                String template = getResources().getString(R.string.loaddata_inprogress);
                status = String.format(template,done,total,percentage + '%');
            }
            text += "<b>" + SOURCES_NAMES[i] + "</b>: " + status + "<br/>";
        }
        view.setText(Html.fromHtml(text));
    }

    @Override
    public void onProgressCompleted(Integer... counts) {
        // change status
        taskInProgress = null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button button = findViewById(R.id.loaddataButton);
        button.setText(getResources().getString(R.string.loaddata_start));

        // update progress bar
        ProgressBar bar = (ProgressBar) findViewById(R.id.loaddataProgressBar);
        bar.setProgress(100);

        // update information
        TextView view = findViewById(R.id.loaddataInfos);
        String text = "";
        for(int i = 0; i<counts.length; i++) {
            text += "<b>" + SOURCES_NAMES[i] + "</b>: " + counts[i] + " importés<br/>";
        }
        view.setText(Html.fromHtml(text));
    }
}
