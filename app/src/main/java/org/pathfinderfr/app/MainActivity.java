package org.pathfinderfr.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.SpellFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SpellFilterFragment.OnFragmentInteractionListener {

    // current factory (which list is currently been displayed)
    public static final String KEY_CUR_FACTORY      = "current_factory";
    // list must be refreshed (something has been done outside of main activity)
    public static final String KEY_REFRESH_REQUIRED = "refresh_required";
    // spell filters
    public static final String KEY_SPELL_FILTERS = "filter_spells";

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

        dbhelper = DBHelper.getInstance(this);

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
            }
        });

        // search input appears when user clicks on search button
        EditText searchInput = (EditText) findViewById(R.id.searchinput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String criteria = s.toString().toLowerCase();
                if (criteria.length() >= 3) {
                    list.clear();
                    for (DBEntity el : listFull) {
                        if (el.getName().toLowerCase().indexOf(criteria) >= 0) {
                            list.add(el);
                        }
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else if(list.size() < listFull.size()) {
                    list.clear();
                    list.addAll(listFull);
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
                if(list.size() < listFull.size()) {
                    list.clear();
                    list.addAll(listFull);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });


        // filter button
        FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.filterButton).setVisibility(View.GONE);
                showDialog();
            }
        });

        // Welcome screen
        TextView textview = (TextView) findViewById(R.id.welcome_screen);
        Properties props = ConfigurationUtil.getInstance(getBaseContext()).getProperties();
        long countFavorites = dbhelper.getCountEntities(FavoriteFactory.getInstance());
        long countSkills = dbhelper.getCountEntities(SkillFactory.getInstance());
        long countFeats = dbhelper.getCountEntities(FeatFactory.getInstance());
        long countSpells = dbhelper.getCountEntities(SpellFactory.getInstance());
        String welcomeText = String.format(props.getProperty("template.welcome"),
                countSkills, countFeats, countSpells, countFavorites);
        if (countSkills == 0 && countFeats == 0 && countSpells == 0) {
            welcomeText += props.getProperty("template.welcome.first");
        } else {
            welcomeText += props.getProperty("template.welcome.second");
        }
        textview.setText(Html.fromHtml(welcomeText));

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
            mTwoPane = false; // TODO: enable when two-pane mode will be fixed
        }

        recyclerView = (RecyclerView) findViewById(R.id.item_list);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, list, mTwoPane));

        // Navigation
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if(countFavorites == 0) {
            navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(false);
        }
        if(countSkills == 0) {
            navigationView.getMenu().findItem(R.id.nav_skills).setVisible(false);
        }
        if(countFeats == 0) {
            navigationView.getMenu().findItem(R.id.nav_feats).setVisible(false);
        }
        if(countSpells == 0) {
            navigationView.getMenu().findItem(R.id.nav_spells).setVisible(false);
        }

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
        String factoryId = null;

        String factory = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext()).getString(KEY_CUR_FACTORY, null);

        if (id == R.id.nav_home && factory != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                    .putString(KEY_CUR_FACTORY,null).apply();
        } else if (id == R.id.nav_favorites) {
            List<DBEntity> entities = dbhelper.getAllEntities(FavoriteFactory.getInstance());
            list.clear();
            listFull.clear();
            list.addAll(entities);
            listFull.addAll(entities);
            dataChanged = true;
            factoryId = FavoriteFactory.FACTORY_ID;
        } else if (id == R.id.nav_skills) {
            List<DBEntity> entities = dbhelper.getAllEntities(SkillFactory.getInstance());
            list.clear();
            listFull.clear();
            list.addAll(entities);
            listFull.addAll(entities);
            dataChanged = true;
            factoryId = SkillFactory.FACTORY_ID;
        } else if (id == R.id.nav_feats) {
            List<DBEntity> entities = dbhelper.getAllEntities(FeatFactory.getInstance());
            list.clear();
            listFull.clear();
            list.addAll(entities);
            listFull.addAll(entities);
            dataChanged = true;
            factoryId = FeatFactory.FACTORY_ID;
        } else if (id == R.id.nav_spells) {
            List<DBEntity> entities = dbhelper.getAllEntities(SpellFactory.getInstance());
            list.clear();
            listFull.clear();
            list.addAll(entities);
            listFull.addAll(entities);
            dataChanged = true;
            factoryId = SpellFactory.FACTORY_ID;
        } else if (id == R.id.nav_refresh_data) {
            Intent intent = new Intent(this, LoadDataActivity.class);
            startActivity(intent);
        }

        if (dataChanged) {
            // reset activity
            findViewById(R.id.welcome_screen).setVisibility(View.GONE);
            findViewById(R.id.welcome_copyright).setVisibility(View.GONE);
            findViewById(R.id.closeSearchButton).setVisibility(View.GONE);
            findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
            findViewById(R.id.filterButton).setVisibility(View.VISIBLE);
            EditText searchInput = (EditText) findViewById(R.id.searchinput);
            searchInput.setText("");
            searchInput.setVisibility(View.GONE);

            // Change menu title by factory (ie. type) name
            Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
            String title = ConfigurationUtil.getInstance().getProperties().getProperty("template.title." + factoryId.toLowerCase());
            if (toolBar != null && title != null) {
                toolBar.setTitle(title);
            }

            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().
                    putString(KEY_CUR_FACTORY,factoryId).apply();

            recyclerView.getAdapter().notifyDataSetChanged();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean refreshRequired = prefs.getBoolean(MainActivity.KEY_REFRESH_REQUIRED, false);
        String factory = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(KEY_CUR_FACTORY, null);

        if(refreshRequired) {
            if(FavoriteFactory.FACTORY_ID.equalsIgnoreCase(factory)) {
                List<DBEntity> entities = dbhelper.getAllEntities(FavoriteFactory.getInstance());
                list.clear();
                listFull.clear();
                list.addAll(entities);
                listFull.addAll(entities);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
            prefs.edit().putBoolean(MainActivity.KEY_REFRESH_REQUIRED, false).apply();
        }
    }


    void showDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("filter");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        String factory = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext()).getString(KEY_CUR_FACTORY, null);

        if(SpellFactory.FACTORY_ID.equals(factory)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            List<Spell> spellList = (List<Spell>)(List<?>)listFull;
            DialogFragment newFragment = SpellFilterFragment.newInstance(
                    new SpellFilter(spellList, prefs.getString(KEY_SPELL_FILTERS, null)));
            newFragment.show(ft, "dialog");
        }

    }

    @Override
    public void onApplyFilter(SpellFilter filter) {
        findViewById(R.id.filterButton).setVisibility(View.VISIBLE);
        list.clear();
        list.addAll(filter.getFilteredList());
        recyclerView.getAdapter().notifyDataSetChanged();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString(MainActivity.KEY_SPELL_FILTERS, filter.generatePreferences()).apply();
    }
}
