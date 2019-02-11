package org.pathfinderfr.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.character.FragmentAbilityPicker;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.SpellFilter;
import org.pathfinderfr.app.util.StringUtil;
import org.pathfinderfr.app.character.CharacterSheetActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FilterSpellFragment.OnFragmentInteractionListener {

    // preference for showing long or short name
    private static final String PREF_SHOW_NAMELONG = "general_list_namelong";
    // preference for showing disclaimer on welcome page
    private static final String PREF_SHOW_DISCLAIMER = "general_show_disclaimer";

    // current factory (which list is currently been displayed)
    public static final String KEY_CUR_FACTORY = "current_factory";
    // list must be refreshed (something has been done outside of main activity)
    public static final String KEY_RELOAD_REQUIRED = "refresh_required";
    // spell filters
    public static final String KEY_SPELL_FILTERS = "filter_spells";

    public static final String DIALOG_SPELL_FILTER = "spells-filter";
    public static final String KEY_SEARCH_VISIBLE = "search-visible";

    DBHelper dbhelper;

    // list that is displayed
    List<DBEntity> listCur = new ArrayList<>();
    // complete list (from database)
    List<DBEntity> listFull = new ArrayList<>();
    // filtered list
    List<DBEntity> listFiltered = null;
    // search criteria
    String search = null;

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

        dbhelper = DBHelper.getInstance(getBaseContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // search button appears only for item-list view
        ImageButton searchButton = findViewById(R.id.searchButton);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.searchButton).setVisibility(View.GONE);
                findViewById(R.id.closeSearchButton).setVisibility(View.VISIBLE);

                EditText input = (EditText) findViewById(R.id.searchinput);
                input.setVisibility(View.VISIBLE);
                input.requestFocus();
                ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input, 0);
            }
        };
        // enable search when user clicks on search button or toolbar
        searchButton.setOnClickListener(listener);

        EditText searchInput = findViewById(R.id.searchinput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                search = s.toString().toLowerCase();
                applyFiltersAndSearch();
            }
        });


        ImageButton closeSearchButton = (ImageButton) findViewById(R.id.closeSearchButton);
        closeSearchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
                // Clear search input and close and make search visible again
                findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
                findViewById(R.id.closeSearchButton).setVisibility(View.GONE);
                EditText searchInput = (EditText) findViewById(R.id.searchinput);
                searchInput.setText("");
                searchInput.setVisibility(View.GONE);
                search = null;
                applyFiltersAndSearch();
            }
        });


        // filter button
        FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        // Welcome screen
        TextView textview = (TextView) findViewById(R.id.welcome_screen);
        Properties props = ConfigurationUtil.getInstance(getBaseContext()).getProperties();

        String[] sources = PreferenceUtil.getSources(getBaseContext());
        if(sources.length == ConfigurationUtil.getInstance().getSources().length) {
            sources = new String[0];
        }

        long countFavorites = dbhelper.getCountEntities(FavoriteFactory.getInstance());
        long countSkills = dbhelper.getCountEntities(SkillFactory.getInstance());
        long countFeats = dbhelper.getCountEntities(FeatFactory.getInstance());
        long countSpells = dbhelper.getCountEntities(SpellFactory.getInstance());
        long countRaces = dbhelper.getCountEntities(RaceFactory.getInstance());
        long countClasses = dbhelper.getCountEntities(ClassFactory.getInstance());

        long countFeatsFiltered = dbhelper.getCountEntities(FeatFactory.getInstance(), sources);
        long countSpellsFiltered = dbhelper.getCountEntities(SpellFactory.getInstance(), sources);
        long countRacesFiltered = dbhelper.getCountEntities(RaceFactory.getInstance(), sources);
        long countClassesFiltered = dbhelper.getCountEntities(ClassFactory.getInstance(), sources);

        long countSources = sources.length;
        long countSourcesTotal = ConfigurationUtil.getInstance().getSources().length;

        // search for already created characters
        long characterId = 0;
        List<DBEntity> list = DBHelper.getInstance(getBaseContext()).getAllEntities(CharacterFactory.getInstance());
        if(list != null && list.size() > 0) {
            characterId = list.get(0).getId();
        }


        String welcomeText = String.format(props.getProperty("template.welcome"),
                countSkills, countFeatsFiltered, countFeats, countSpellsFiltered, countSpells,
                countRacesFiltered, countRaces, countClassesFiltered, countClasses,
                countFavorites, countSources, countSourcesTotal);
        if (countSkills == 0 && countFeats == 0 && countSpells == 0) {
            welcomeText += props.getProperty("template.welcome.first");
        } else {
            welcomeText += props.getProperty("template.welcome.second");
        }
        welcomeText += props.getProperty("template.welcome.userdoc");

        // version
        PackageManager manager = getBaseContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getBaseContext().getPackageName(), 0);
            welcomeText += String.format(props.getProperty("template.welcome.version"),info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            welcomeText += String.format(props.getProperty("template.welcome.version"),"??");
        }

        textview.setText(Html.fromHtml(welcomeText));

        // Disclaimer / copyright
        boolean showDisclaimer = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(PREF_SHOW_DISCLAIMER, true);
        findViewById(R.id.welcome_copyright).setVisibility(showDisclaimer ? View.VISIBLE : View.GONE);
        findViewById(R.id.welcome_copyright).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                findViewById(R.id.welcome_copyright).setVisibility(View.GONE);
            }
        });

        // Navigation

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
        recyclerView.setAdapter(new ItemListRecyclerViewAdapter(this, listCur, mTwoPane));

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
        if(countClasses == 0 || countRaces == 0) {
            navigationView.getMenu().findItem(R.id.nav_sheet).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(this);

        // reset list after screen rotation
        if(savedInstanceState != null) {
            String factoryId = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(KEY_CUR_FACTORY, null);

            if(FavoriteFactory.FACTORY_ID.equals(factoryId)) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_favorites));
            } else if(SkillFactory.FACTORY_ID.equals(factoryId)) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_skills));
            } else if(FeatFactory.FACTORY_ID.equals(factoryId)) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_feats));
            } else if(SpellFactory.FACTORY_ID.equals(factoryId)) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_spells));
            }

            FilterSpellFragment fragSpellFilter = (FilterSpellFragment)getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_SPELL_FILTER);
            if (fragSpellFilter != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                List<Spell> spellList = (List<Spell>)(List<?>)listFull;
                fragSpellFilter.setFilter(new SpellFilter(spellList, prefs.getString(KEY_SPELL_FILTERS, null)));
            }

            if(savedInstanceState.getByte(KEY_SEARCH_VISIBLE, (byte)0) == 1) {
                searchButton.performClick();
            }
        }
    }

    /**
     * This function applies filter and search, then refreshes recycler view
     */
    private void applyFiltersAndSearch() {
        // pick filtered list or whole list
        List<DBEntity> list = (listFiltered == null ? listFull : listFiltered);

        listCur.clear();
        for (DBEntity el : list) {
            if (search == null || search.length() < 3 || el.getName().toLowerCase().indexOf(search) >= 0) {
                listCur.add(el);
            }
        }

        String factoryId = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext()).getString(KEY_CUR_FACTORY, null);

        boolean showNameLong = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(PREF_SHOW_NAMELONG, true);

        ((ItemListRecyclerViewAdapter)recyclerView.getAdapter()).setFactoryId(factoryId);
        ((ItemListRecyclerViewAdapter)recyclerView.getAdapter()).setShowNameLong(showNameLong);
        recyclerView.getAdapter().notifyDataSetChanged();

        if(factoryId != null) {
            // Change menu title by factory (ie. type) name
            Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
            String title = ConfigurationUtil.getInstance().getProperties().getProperty("template.title." + factoryId.toLowerCase());
            if (listFiltered == null) {
                title += String.format(" (%d)", listFull.size());
            } else {
                title += String.format(" (%d/%d)", listCur.size(), listFull.size());
            }
            if (toolBar != null && title != null) {
                toolBar.setTitle(title);
            }
        }
    }

    /**
     * Applies the filters on list and updates the listFiltered variable
     */
    private void generateFilteredList() {
        String factoryId = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext()).getString(KEY_CUR_FACTORY, null);

        if(SpellFactory.FACTORY_ID.equals(factoryId)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            List<Spell> spells = (List<Spell>)(List<?>)listFull;
            SpellFilter filter = new SpellFilter(spells,prefs.getString(KEY_SPELL_FILTERS, null));
            listFiltered = (List<DBEntity>)(List<?>)filter.getFilteredList();
            if(listFiltered.size() == listFull.size()) {
                listFiltered = null;
            }
        }

        // change icon if filter applied
        FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
        int filterButtonId = listFiltered == null ? R.drawable.ic_filter : R.drawable.ic_filtered;
        filterButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), filterButtonId));
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
        ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String factoryId = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext()).getString(KEY_CUR_FACTORY, null);

        String[] sources = PreferenceUtil.getSources(getBaseContext());
        Log.i(MainActivity.class.getSimpleName(), "Sources enabled: " + StringUtil.listToString(sources, ','));

        List<DBEntity> newEntities = null;
        if (id == R.id.nav_home && factoryId != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                    .putString(KEY_CUR_FACTORY,null).apply();
            factoryId = null;
        } else if (id == R.id.nav_favorites) {
            newEntities = dbhelper.getAllEntities(FavoriteFactory.getInstance());
            factoryId = FavoriteFactory.FACTORY_ID;
        } else if (id == R.id.nav_sheet) {
            Intent intent = new Intent(this, CharacterSheetActivity.class);
            startActivity(intent);
            factoryId = CharacterFactory.FACTORY_ID;
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().
                    putString(KEY_CUR_FACTORY,factoryId).apply();
        } else if (id == R.id.nav_skills) {
            newEntities = dbhelper.getAllEntities(SkillFactory.getInstance());
            factoryId = SkillFactory.FACTORY_ID;
        } else if (id == R.id.nav_feats) {
            if(sources.length == ConfigurationUtil.getInstance().getSources().length) {
                newEntities = dbhelper.getAllEntities(FeatFactory.getInstance());
            } else {
                newEntities = dbhelper.getAllEntities(FeatFactory.getInstance(), sources);
            }
            factoryId = FeatFactory.FACTORY_ID;
        } else if (id == R.id.nav_spells) {
            if(sources.length == ConfigurationUtil.getInstance().getSources().length) {
                newEntities = dbhelper.getAllEntities(SpellFactory.getInstance());
            } else {
                newEntities = dbhelper.getAllEntities(SpellFactory.getInstance(), sources);
            }
            factoryId = SpellFactory.FACTORY_ID;
        } else if (id == R.id.nav_refresh_data) {
            Intent intent = new Intent(this, LoadDataActivity.class);
            startActivity(intent);
        }

        if (newEntities != null) {
            boolean filterEnabled = SpellFactory.FACTORY_ID.equalsIgnoreCase(factoryId);
            // reset activity
            findViewById(R.id.welcomeScroller).setVisibility(View.GONE);
            findViewById(R.id.welcome_copyright).setVisibility(View.GONE);
            findViewById(R.id.closeSearchButton).setVisibility(View.GONE);
            findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
            findViewById(R.id.filterButton).setVisibility(filterEnabled ? View.VISIBLE : View.GONE);
            EditText searchInput = (EditText) findViewById(R.id.searchinput);
            searchInput.setText("");
            searchInput.setVisibility(View.GONE);

            // Update preferences
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().
                    putString(KEY_CUR_FACTORY,factoryId).apply();

            // Update view
            listFull.clear();
            listFull.addAll(newEntities);
            listFiltered = null;
            search = null;
            generateFilteredList();
            applyFiltersAndSearch();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean reloadRequired = prefs.getBoolean(MainActivity.KEY_RELOAD_REQUIRED, false);
        String factory = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(KEY_CUR_FACTORY, null);

        if(reloadRequired) {
            if(FavoriteFactory.FACTORY_ID.equalsIgnoreCase(factory)) {
                List<DBEntity> entities = dbhelper.getAllEntities(FavoriteFactory.getInstance());
                listFull.clear();
                listFull.addAll(entities);
                generateFilteredList();
                applyFiltersAndSearch();
            }
            prefs.edit().putBoolean(MainActivity.KEY_RELOAD_REQUIRED, false).apply();
        }
    }


    void showDialog() {
        ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(DIALOG_SPELL_FILTER);
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
            DialogFragment newFragment = FilterSpellFragment.newInstance(
                    new SpellFilter(spellList, prefs.getString(KEY_SPELL_FILTERS, null)));
            newFragment.show(ft, DIALOG_SPELL_FILTER);
        }

    }

    @Override
    public void onApplyFilter(SpellFilter filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString(MainActivity.KEY_SPELL_FILTERS, filter.generatePreferences()).apply();
        generateFilteredList();
        applyFiltersAndSearch();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(findViewById(R.id.closeSearchButton).getVisibility() == View.VISIBLE) {
            outState.putByte(KEY_SEARCH_VISIBLE, (byte)1);
        }
    }
}
