package org.pathfinderfr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.data.DataClient;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DBHelper dbhelper;
    List<DBEntity> list = new ArrayList<>();
    List<DBEntity> listFull = new ArrayList<>();

    RecyclerView recyclerView;


    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // search button appears only for item-list view
        ImageButton searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.searchButton).setVisibility(View.GONE);
                findViewById(R.id.closeSearchButton).setVisibility(View.VISIBLE);

                EditText input = (EditText) findViewById(R.id.searchinput);
                input.setVisibility(View.VISIBLE);
                input.requestFocus();

                //Snackbar.make(view, "Search filter", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        // search input appears when user clicks on search button
        EditText searchInput = (EditText) findViewById(R.id.searchinput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String criteria = s.toString().toLowerCase();
                if(criteria.length() >= 3) {
                    list.clear();
                    for(DBEntity el : listFull) {
                        if(el.getName().toLowerCase().indexOf(criteria)>=0) {
                            list.add(el);
                        }
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });

        ImageButton closeSearchButton = (ImageButton) findViewById(R.id.closeSearchButton);
        closeSearchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Clear search input and close and make search visible again
                findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
                findViewById(R.id.closeSearchButton).setVisibility(View.GONE);
                EditText searchInput = (EditText) findViewById(R.id.searchinput);
                searchInput.setText("");
                searchInput.setVisibility(View.GONE);
                list.clear();
                list.addAll(listFull);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        dbhelper = DBHelper.getInstance(this);

        recyclerView = (RecyclerView)findViewById(R.id.item_list);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, list, mTwoPane));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        boolean dataChanged = false;
        if (id == R.id.nav_spells) {
            List<DBEntity> entities = dbhelper.getAllEntities(SpellFactory.getInstance());
            list.clear();
            listFull.clear();
            list.addAll(entities);
            listFull.addAll(entities);
            dataChanged = true;
        } else if (id == R.id.nav_skills) {
            list.clear();
            listFull.clear();
            dataChanged = true;
        } else if (id == R.id.nav_feats) {
            list.clear();
            listFull.clear();
            dataChanged = true;
        } else if (id == R.id.nav_share) {
            new DataClient().execute("https://raw.githubusercontent.com/SvenWerlen/pathfinderfr-data/master/data/spells.yml");

        } else if (id == R.id.nav_send) {

        }

        if (dataChanged) {
            findViewById(R.id.closeSearchButton).setVisibility(View.GONE);
            EditText searchInput = (EditText) findViewById(R.id.searchinput);
            searchInput.setText("");
            searchInput.setVisibility(View.GONE);
            findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
