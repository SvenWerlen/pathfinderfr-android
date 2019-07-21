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
import android.support.design.widget.Snackbar;
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
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.ArmorFactory;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.ConditionFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.Equipment;
import org.pathfinderfr.app.database.entity.EquipmentFactory;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.MagicItemFactory;
import org.pathfinderfr.app.database.entity.RaceAlternateTrait;
import org.pathfinderfr.app.database.entity.RaceAlternateTraitFactory;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.database.entity.WeaponFactory;
import org.pathfinderfr.app.util.ClassFeatureFilter;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.EquipmentFilter;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.RaceAlternateTraitFilter;
import org.pathfinderfr.app.util.SpellFilter;
import org.pathfinderfr.app.util.StringUtil;
import org.pathfinderfr.app.character.CharacterSheetActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, FilterSpellFragment.OnFragmentInteractionListener,
        FilterClassFeaturesFragment.OnFragmentInteractionListener, FilterEquipmentFragment.OnFragmentInteractionListener,
        FilterRaceAlternateTraitFragment.OnFragmentInteractionListener {

    // preference for showing long or short name
    private static final String PREF_SHOW_NAMELONG    = "general_list_namelong";
    // preference for showing disclaimer on welcome page
    private static final String PREF_SHOW_DISCLAIMER  = "general_show_disclaimer";
    // preference for line height (in lists)
    public final static String PREF_LINEHEIGHT        = "general_lineheight";
    // preference for larger clickable zones
    public final static String PREF_FATFINGERS        = "general_fatfingers";


    // current factory (which list is currently been displayed)
    public static final String KEY_CUR_FACTORY = "current_factory";
    // list must be refreshed (something has been done outside of main activity)
    public static final String KEY_RELOAD_REQUIRED = "refresh_required";
    // spell filters
    public static final String KEY_SPELL_FILTERS = "filter_spells";
    public static final String KEY_ABILITY_FILTERS = "filter_classfeatures";
    public static final String KEY_TRAIT_FILTERS = "filter_racetraits";
    public static final String KEY_EQUIPMENT_FILTERS = "filter_equipment";

    public static final String DIALOG_FILTER = "dialog-filter";
    public static final String KEY_SEARCH_VISIBLE = "search-visible";

    DBHelper dbhelper;

    long totalCount;
    // list that is being displayed
    List<DBEntity> listCur = new ArrayList<>();
    // list from database (including filters)
    List<DBEntity> listFull = new ArrayList<>();
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

        // listen to welcome page
        findViewById(R.id.welcome_sheets).setOnClickListener(this);
        findViewById(R.id.welcome_selchar).setOnClickListener(this);
        findViewById(R.id.welcome_favorites).setOnClickListener(this);
        findViewById(R.id.welcome_skills).setOnClickListener(this);
        findViewById(R.id.welcome_feats).setOnClickListener(this);
        findViewById(R.id.welcome_abilities).setOnClickListener(this);
        findViewById(R.id.welcome_traits).setOnClickListener(this);
        findViewById(R.id.welcome_spells).setOnClickListener(this);
        findViewById(R.id.welcome_equipment).setOnClickListener(this);
        findViewById(R.id.welcome_magic).setOnClickListener(this);
        findViewById(R.id.welcome_condition).setOnClickListener(this);
        findViewById(R.id.welcome_generator).setOnClickListener(this);

        // hide selected character if no character selected
        Character character = null;
        long characterId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L);
        if(characterId > 0) {
            character = (Character) DBHelper.getInstance(getApplicationContext()).fetchEntity(characterId, CharacterFactory.getInstance());
        }
        if(character != null) {
            String charName = character.getName() == null ? "-" : character.getName();
            if(charName.indexOf(' ') > 0) {
                charName = charName.substring(0,charName.indexOf(' '));
            }
            if(charName.length() > 15) {
                charName = charName.substring(0,15);
                System.out.println(charName);
            }
            ((TextView)findViewById(R.id.welcome_selchar_text)).setText(charName);
        } else {
            findViewById(R.id.welcome_selchar).setVisibility(View.INVISIBLE);
        }

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
                applySearch();
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
                applySearch();
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

        // filter button
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // remove preselected character
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().remove(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID).apply();
                // open character sheet
                Intent intent = new Intent(MainActivity.this, CharacterSheetActivity.class);
                startActivity(intent);
            }
        });

        // navigation
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateWelcomeAndNavigation();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = (RecyclerView) findViewById(R.id.item_list);
        recyclerView.setAdapter(new ItemListRecyclerViewAdapter(this, listCur, false));

        // Disclaimer / copyright
        boolean showDisclaimer = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(PREF_SHOW_DISCLAIMER, true);
        findViewById(R.id.welcome_copyright).setVisibility(showDisclaimer ? View.VISIBLE : View.GONE);
        findViewById(R.id.welcome_copyright).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                findViewById(R.id.welcome_copyright).setVisibility(View.GONE);
            }
        });

        // reset list
        String factoryId = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(KEY_CUR_FACTORY, null);
        MenuItem selItem;
        if(CharacterFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_sheet);
        } else if(FavoriteFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_favorites);
        } else if(SkillFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_skills);
        } else if(FeatFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_feats);
        } else if(ClassFeatureFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_abilities);
        } else if(SpellFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_spells);
        } else if(ConditionFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_conditions);
        } else if(EquipmentFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_equipment);
        } else if(MagicItemFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_magic);
        } else if(RaceAlternateTraitFactory.FACTORY_ID.equals(factoryId)) {
            selItem = navigationView.getMenu().findItem(R.id.nav_traits);
        } else {
            selItem = navigationView.getMenu().findItem(R.id.nav_home);
        }
        if(selItem != null) {
            navigationView.setCheckedItem(selItem.getItemId());
            onNavigationItemSelected(selItem);
        }

        if(factoryId != null) {
            DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_FILTER);
            if(fragment instanceof FilterSpellFragment) {
                FilterSpellFragment fragSpellFilter = (FilterSpellFragment)fragment;
                if (fragSpellFilter != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    fragSpellFilter.setFilter(new SpellFilter(prefs.getString(KEY_SPELL_FILTERS, null)));
                }
            } else if(fragment instanceof FilterClassFeaturesFragment) {
                FilterClassFeaturesFragment fragAbilityFilter = (FilterClassFeaturesFragment)fragment;
                if (fragAbilityFilter != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    fragAbilityFilter.setFilter(new ClassFeatureFilter(prefs.getString(KEY_ABILITY_FILTERS, null)));
                }
            } else if(fragment instanceof FilterEquipmentFragment) {
                FilterEquipmentFragment fragEquipmentFilter = (FilterEquipmentFragment)fragment;
                if (fragEquipmentFilter != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    fragEquipmentFilter.setFilter(new EquipmentFilter(prefs.getString(KEY_EQUIPMENT_FILTERS, null),
                            getResources().getString(R.string.home_item_armors),
                            getResources().getString(R.string.home_item_weapons)));
                }
            } else if(fragment instanceof FilterRaceAlternateTraitFragment) {
                FilterRaceAlternateTraitFragment fragTraitFilter = (FilterRaceAlternateTraitFragment)fragment;
                if (fragTraitFilter != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    fragTraitFilter.setFilter(new RaceAlternateTraitFilter(prefs.getString(KEY_TRAIT_FILTERS, null)));
                }
            }

            updateTitle(factoryId);

            if(savedInstanceState != null) {
                if (savedInstanceState.getByte(KEY_SEARCH_VISIBLE, (byte) 0) == 1) {
                    searchButton.performClick();
                }
            }
        }
    }

    private void updateWelcomeAndNavigation() {
        // Welcome screen
        //TextView textview = (TextView) findViewById(R.id.welcome_screen);
        Properties props = ConfigurationUtil.getInstance(getBaseContext()).getProperties();

        String[] sources = PreferenceUtil.getSources(getBaseContext());
        if(sources.length == ConfigurationUtil.getInstance().getAvailableSources().length) {
            sources = new String[0];
        }

        long countCharacters = dbhelper.getCountEntities(CharacterFactory.getInstance());
        long countFavorites = dbhelper.getCountEntities(FavoriteFactory.getInstance());
        long countSkills = dbhelper.getCountEntities(SkillFactory.getInstance());
        long countFeats = dbhelper.getCountEntities(FeatFactory.getInstance());
        long countAbilities = dbhelper.getCountEntities(ClassFeatureFactory.getInstance());
        long countSpells = dbhelper.getCountEntities(SpellFactory.getInstance());
        long countRaces = dbhelper.getCountEntities(RaceFactory.getInstance());
        long countClasses = dbhelper.getCountEntities(ClassFactory.getInstance());
        long countWeapons = dbhelper.getCountEntities(WeaponFactory.getInstance());
        long countArmors = dbhelper.getCountEntities(ArmorFactory.getInstance());
        long countEquipment = dbhelper.getCountEntities(EquipmentFactory.getInstance());
        long countMagic = dbhelper.getCountEntities(MagicItemFactory.getInstance());
        long countConditions = dbhelper.getCountEntities(ConditionFactory.getInstance());
        long countTraits = dbhelper.getCountEntities(RaceAlternateTraitFactory.getInstance());

        long countFeatsFiltered = dbhelper.getCountEntities(FeatFactory.getInstance(), sources);
        long countAbilitiesFiltered = dbhelper.getCountEntities(ClassFeatureFactory.getInstance(), sources);
        long countSpellsFiltered = dbhelper.getCountEntities(SpellFactory.getInstance(), sources);
        long countRacesFiltered = dbhelper.getCountEntities(RaceFactory.getInstance(), sources);
        long countClassesFiltered = dbhelper.getCountEntities(ClassFactory.getInstance(), sources);

        long countSources = sources.length == 0 ? ConfigurationUtil.getInstance().getAvailableSources().length : sources.length;
        long countSourcesTotal = ConfigurationUtil.getInstance().getAvailableSources().length;


        String welcomeText = String.format(props.getProperty("template.welcome"),
                countRacesFiltered, countRaces,
                countClassesFiltered, countClasses,
                countSkills,
                countFeatsFiltered, countFeats,
                countAbilitiesFiltered, countAbilities,
                countSpellsFiltered, countSpells,
                countCharacters,
                countFavorites,
                countSources, countSourcesTotal);
        if (countSkills == 0 && countFeats == 0 && countAbilities == 0 && countSpells == 0) {
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

        //textview.setText(Html.fromHtml(welcomeText));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(countFavorites > 0);
        navigationView.getMenu().findItem(R.id.nav_traits).setVisible(countTraits > 0
                && PreferenceUtil.sourceIsActive(getBaseContext(),"MR")); // hide alternate traits if MR is not active
        navigationView.getMenu().findItem(R.id.nav_skills).setVisible(countSkills > 0);
        navigationView.getMenu().findItem(R.id.nav_feats).setVisible(countFeats > 0);
        navigationView.getMenu().findItem(R.id.nav_abilities).setVisible(countAbilities > 0);
        navigationView.getMenu().findItem(R.id.nav_spells).setVisible(countSpells > 0);
        navigationView.getMenu().findItem(R.id.nav_sheet).setVisible(countClasses > 0 && countRaces > 0);
        navigationView.getMenu().findItem(R.id.nav_equipment).setVisible(countEquipment + countWeapons + countArmors > 0);
        navigationView.getMenu().findItem(R.id.nav_magic).setVisible(countMagic > 0);
        navigationView.getMenu().findItem(R.id.nav_conditions).setVisible(countConditions > 0);

    }

    private void updateTitle(String factoryId) {
        if(factoryId != null) {
            // Change menu title by factory (ie. type) name
            Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
            String title = ConfigurationUtil.getInstance().getProperties().getProperty("template.title." + factoryId.toLowerCase());
            if (listCur.size() == totalCount) {
                title += String.format(" (%d)", listFull.size());
            } else {
                title += String.format(" (%d/%d)", listCur.size(), totalCount);
            }
            if (toolBar != null && title != null) {
                toolBar.setTitle(title);
            }
        }
    }

    /**
     * This function applies filter and search, then refreshes recycler view
     */
    private void applySearch() {
        // pick filtered list or whole list
        List<DBEntity> list = listFull;

        // update line height
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            int lineHeight = Integer.parseInt(preferences.getString(PREF_LINEHEIGHT, "0"));
            lineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineHeight, getResources().getDisplayMetrics());
            ((ItemListRecyclerViewAdapter) recyclerView.getAdapter()).setMinimumLineHeight(lineHeight);
        } catch(NumberFormatException nfe) {}

        listCur.clear();
        for (DBEntity el : list) {
            if (search == null || search.length() < 3 || (el.getName() != null && el.getName().toLowerCase().indexOf(search) >= 0)) {
                listCur.add(el);
            }
        }

        String factoryId = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(KEY_CUR_FACTORY, null);

        boolean showNameLong;
        if(CharacterFactory.FACTORY_ID.equals(factoryId)) {
            showNameLong = true;
        } else {
            showNameLong = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(PREF_SHOW_NAMELONG, true);
        }

        ((ItemListRecyclerViewAdapter)recyclerView.getAdapter()).setFactoryId(factoryId);
        ((ItemListRecyclerViewAdapter)recyclerView.getAdapter()).setShowNameLong(showNameLong);
        recyclerView.getAdapter().notifyDataSetChanged();

        updateTitle(factoryId);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            String factoryId = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(KEY_CUR_FACTORY, null);
            if(factoryId != null) {
                NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
                nav.setCheckedItem(R.id.nav_home);
                onNavigationItemSelected(nav.getMenu().findItem(R.id.nav_home));
            } else {
                super.onBackPressed();
            }
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

        boolean filterActive = false;

        List<DBEntity> newEntities = null;
        if (id == R.id.nav_home && factoryId != null) {
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().remove(KEY_CUR_FACTORY).apply();
            factoryId = null;
        } else if (id == R.id.nav_favorites) {
            newEntities = dbhelper.getAllEntities(FavoriteFactory.getInstance());
            totalCount = newEntities.size();
            factoryId = FavoriteFactory.FACTORY_ID;
        } else if (id == R.id.nav_sheet) {
            newEntities = dbhelper.getAllEntities(CharacterFactory.getInstance());
            totalCount = newEntities.size();
            factoryId = CharacterFactory.FACTORY_ID;
        } else if (id == R.id.nav_skills) {
            newEntities = dbhelper.getAllEntities(SkillFactory.getInstance());
            totalCount = newEntities.size();
            factoryId = SkillFactory.FACTORY_ID;
        } else if (id == R.id.nav_feats) {
            newEntities = dbhelper.getAllEntities(FeatFactory.getInstance(),
                    sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources);
            totalCount = newEntities.size();
            factoryId = FeatFactory.FACTORY_ID;
        } else if (id == R.id.nav_abilities) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            ClassFeatureFilter filter = new ClassFeatureFilter(prefs.getString(KEY_ABILITY_FILTERS, null));
            filterActive = filter.hasAnyFilter();
            filterClassFeatures(filter);
            newEntities = new ArrayList<>(listFull);
            totalCount = dbhelper.getCountEntities(ClassFeatureFactory.getInstance(),
                    sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources) ;
            factoryId = ClassFeatureFactory.FACTORY_ID;
        } else if (id == R.id.nav_spells) {
            // check that spell indexes are available!
            if(!dbhelper.hasSpellIndexes()) {
                Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.warning_missing_indexes),
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return false;
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SpellFilter filter = new SpellFilter(prefs.getString(KEY_SPELL_FILTERS, null));
            filterActive = filter.hasAnyFilter();
            newEntities = (List<DBEntity>)(List<?>)dbhelper.getSpells(filter, sources);
            totalCount = newEntities.size();
            if(filterActive) {
                totalCount = dbhelper.getCountEntities(SpellFactory.getInstance(), sources);
            }
            factoryId = SpellFactory.FACTORY_ID;
        } else if (id == R.id.nav_conditions) {
            newEntities = dbhelper.getAllEntities(ConditionFactory.getInstance(),
                    sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources);
            totalCount = newEntities.size();
            factoryId = ConditionFactory.FACTORY_ID;
        } else if (id == R.id.nav_equipment) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            EquipmentFilter filter = new EquipmentFilter(prefs.getString(KEY_EQUIPMENT_FILTERS, null),
                    getResources().getString(R.string.home_item_armors),
                    getResources().getString(R.string.home_item_weapons));
            filterActive = filter.hasAnyFilter();
            filterEquipment(filter);
            newEntities = new ArrayList<>(listFull);
            totalCount = dbhelper.getCountEntities(EquipmentFactory.getInstance(),
                    sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources) ;
            factoryId = EquipmentFactory.FACTORY_ID;
        } else if (id == R.id.nav_magic) {
            newEntities = dbhelper.getAllEntities(MagicItemFactory.getInstance(),
                    sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources);
            totalCount = newEntities.size();
            factoryId = MagicItemFactory.FACTORY_ID;
        } else if (id == R.id.nav_traits) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            RaceAlternateTraitFilter filter = new RaceAlternateTraitFilter(prefs.getString(KEY_TRAIT_FILTERS, null));
            filterActive = filter.hasAnyFilter();
            filterRaceAlternateTraits(filter);
            newEntities = new ArrayList<>(listFull);
            totalCount = dbhelper.getCountEntities(RaceAlternateTraitFactory.getInstance(),
                    sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources) ;
            factoryId = RaceAlternateTraitFactory.FACTORY_ID;
        } else if (id == R.id.nav_magic_generator) {
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().remove(KEY_CUR_FACTORY).apply();
            Intent intent = new Intent(this, TreasureActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_refresh_data) {
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().remove(KEY_CUR_FACTORY).apply();
            Intent intent = new Intent(this, LoadDataActivity.class);
            startActivity(intent);
        }

        if (factoryId == null) {
            // reset activity
            findViewById(R.id.welcomeScroller).setVisibility(View.VISIBLE);
            findViewById(R.id.item_list).setVisibility(View.GONE);
            boolean showDisclaimer = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(PREF_SHOW_DISCLAIMER, true);
            findViewById(R.id.welcome_copyright).setVisibility(showDisclaimer ? View.VISIBLE : View.GONE);
            findViewById(R.id.closeSearchButton).setVisibility(View.GONE);
            findViewById(R.id.searchButton).setVisibility(View.GONE);
            findViewById(R.id.filterButton).setVisibility(View.GONE);
            findViewById(R.id.addButton).setVisibility(View.GONE);
            Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
            if (toolBar != null) {
                toolBar.setTitle(getResources().getString(R.string.title_activity_main));
            }
        }
        else if (newEntities != null) {
            boolean filterEnabled = (SpellFactory.FACTORY_ID.equalsIgnoreCase(factoryId)
                    || ClassFeatureFactory.FACTORY_ID.equalsIgnoreCase(factoryId)
                    || EquipmentFactory.FACTORY_ID.equalsIgnoreCase(factoryId)
                    || RaceAlternateTraitFactory.FACTORY_ID.equalsIgnoreCase(factoryId));
            // reset activity
            findViewById(R.id.welcomeScroller).setVisibility(View.GONE);
            findViewById(R.id.item_list).setVisibility(View.VISIBLE);
            findViewById(R.id.welcome_copyright).setVisibility(View.GONE);
            findViewById(R.id.closeSearchButton).setVisibility(View.GONE);
            findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
            findViewById(R.id.filterButton).setVisibility(filterEnabled ? View.VISIBLE : View.GONE);
            findViewById(R.id.addButton).setVisibility(CharacterFactory.FACTORY_ID.equalsIgnoreCase(factoryId) ? View.VISIBLE : View.GONE);
            EditText searchInput = (EditText) findViewById(R.id.searchinput);
            searchInput.setText("");
            searchInput.setVisibility(View.GONE);

            // Update preferences
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().
                    putString(KEY_CUR_FACTORY,factoryId).apply();

            // Update view
            listFull.clear();
            listFull.addAll(newEntities);
            search = null;
            applySearch();

            // change icon if filter applied
            FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
            int filterButtonId = filterActive  ? R.drawable.ic_filtered : R.drawable.ic_filter;
            filterButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), filterButtonId));
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

        updateWelcomeAndNavigation();
        updateTitle(factory);

        try {
            int oldLineHeight = ((ItemListRecyclerViewAdapter) recyclerView.getAdapter()).getMinimumLineHeight();
            int newLineHeight = Integer.parseInt(prefs.getString(PREF_LINEHEIGHT, "0"));
            newLineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newLineHeight, getResources().getDisplayMetrics());
            if(oldLineHeight != newLineHeight) {
                ((ItemListRecyclerViewAdapter) recyclerView.getAdapter()).setMinimumLineHeight(newLineHeight);
                // force refresh
                recyclerView.setAdapter(recyclerView.getAdapter());
            }
        } catch( NumberFormatException nfe) {}


        if(reloadRequired) {
            if(FavoriteFactory.FACTORY_ID.equalsIgnoreCase(factory) ||
                    CharacterFactory.FACTORY_ID.equalsIgnoreCase(factory)) {
                List<DBEntity> entities = dbhelper.getAllEntities(EntityFactories.getFactoryById(factory));
                listFull.clear();
                listFull.addAll(entities);
                totalCount = listFull.size();
                applySearch();
            }
            prefs.edit().putBoolean(MainActivity.KEY_RELOAD_REQUIRED, false).apply();
        }
    }


    void showDialog() {
        ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(DIALOG_FILTER);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        String factory = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext()).getString(KEY_CUR_FACTORY, null);

        if(SpellFactory.FACTORY_ID.equals(factory)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            DialogFragment newFragment = FilterSpellFragment.newInstance(
                    new SpellFilter(prefs.getString(KEY_SPELL_FILTERS, null)));
            newFragment.show(ft, DIALOG_FILTER);
        } else if(ClassFeatureFactory.FACTORY_ID.equals(factory)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            FilterClassFeaturesFragment newFragment = FilterClassFeaturesFragment.newInstance();
            newFragment.setFilter(new ClassFeatureFilter(prefs.getString(KEY_ABILITY_FILTERS, null)));
            newFragment.show(ft, DIALOG_FILTER);
        } else if(RaceAlternateTraitFactory.FACTORY_ID.equals(factory)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            FilterRaceAlternateTraitFragment newFragment = FilterRaceAlternateTraitFragment.newInstance();
            newFragment.setFilter(new RaceAlternateTraitFilter(prefs.getString(KEY_TRAIT_FILTERS, null)));
            newFragment.show(ft, DIALOG_FILTER);
        } else if(EquipmentFactory.FACTORY_ID.equals(factory)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            FilterEquipmentFragment newFragment = FilterEquipmentFragment.newInstance();
            newFragment.setFilter(new EquipmentFilter(prefs.getString(KEY_EQUIPMENT_FILTERS, null),
                    getResources().getString(R.string.home_item_armors),
                    getResources().getString(R.string.home_item_weapons)));
            newFragment.show(ft, DIALOG_FILTER);
        }

    }

    @Override
    public void onApplyFilter(SpellFilter filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String[] sources = PreferenceUtil.getSources(getBaseContext());
        prefs.edit().putString(MainActivity.KEY_SPELL_FILTERS, filter.generatePreferences()).apply();
        listFull = (List<DBEntity>)(List<?>)dbhelper.getSpells(filter,
                sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources);
        applySearch();

        // change icon if filter applied
        FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
        int filterButtonId = filter.hasAnyFilter() ? R.drawable.ic_filtered : R.drawable.ic_filter;
        filterButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), filterButtonId));
    }

    private void filterClassFeatures(ClassFeatureFilter filter) {
        String[] sources = PreferenceUtil.getSources(getBaseContext());
        List<DBEntity> classFeatures = dbhelper.getAllEntities(ClassFeatureFactory.getInstance(),
                sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources);

        listFull = new ArrayList<>();
        for(DBEntity e : classFeatures) {
            ClassFeature a = (ClassFeature)e;
            // check level max && class
            if(a.getLevel() <= filter.getFilterMaxLevel() &&
                    (!filter.hasFilterClass() || filter.isFilterClassEnabled(a.getClass_().getId()))) {
                listFull.add(e);
            }
        }
    }

    @Override
    public void onApplyFilter(ClassFeatureFilter filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString(MainActivity.KEY_ABILITY_FILTERS, filter.generatePreferences()).apply();
        filterClassFeatures(filter);
        applySearch();

        // change icon if filter applied
        FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
        int filterButtonId = filter.hasAnyFilter() ? R.drawable.ic_filtered : R.drawable.ic_filter;
        filterButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), filterButtonId));
    }

    private void filterRaceAlternateTraits(RaceAlternateTraitFilter filter) {
        String[] sources = PreferenceUtil.getSources(getBaseContext());
        List<DBEntity> raceTraits = dbhelper.getAllEntities(RaceAlternateTraitFactory.getInstance(),
                sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources);

        listFull = new ArrayList<>();
        for(DBEntity e : raceTraits) {
            RaceAlternateTrait t = (RaceAlternateTrait)e;
            // check race
            if(!filter.hasFilterRace() || filter.isFilterRaceEnabled(t.getRace().getId())) {
                listFull.add(e);
            }
        }
    }

    @Override
    public void onApplyFilter(RaceAlternateTraitFilter filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString(MainActivity.KEY_TRAIT_FILTERS, filter.generatePreferences()).apply();
        filterRaceAlternateTraits(filter);
        applySearch();

        // change icon if filter applied
        FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
        int filterButtonId = filter.hasAnyFilter() ? R.drawable.ic_filtered : R.drawable.ic_filter;
        filterButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), filterButtonId));
    }

    /**
     * Combines weapons, armors, and other equipment
     */
    private void filterEquipment(EquipmentFilter filter) {
        String[] sources = PreferenceUtil.getSources(getBaseContext());
        List<DBEntity> equipment = new ArrayList<>();
        equipment.addAll(dbhelper.getAllEntities(WeaponFactory.getInstance(),
                sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources));
        equipment.addAll(dbhelper.getAllEntities(ArmorFactory.getInstance(),
                sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources));
        equipment.addAll(dbhelper.getAllEntities(EquipmentFactory.getInstance(),
                sources.length == ConfigurationUtil.getInstance().getAvailableSources().length ? null : sources));

        listFull = new ArrayList<>();
        for(DBEntity e : equipment) {
            if(!filter.hasAnyFilter() || !filter.isFiltered(e)) {
                listFull.add(e);
            }
        }
    }

    @Override
    public void onApplyFilter(EquipmentFilter filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString(MainActivity.KEY_EQUIPMENT_FILTERS, filter.generatePreferences()).apply();
        filterEquipment(filter);
        applySearch();

        // change icon if filter applied
        FloatingActionButton filterButton = (FloatingActionButton) findViewById(R.id.filterButton);
        int filterButtonId = filter.hasAnyFilter() ? R.drawable.ic_filtered : R.drawable.ic_filter;
        filterButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), filterButtonId));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(findViewById(R.id.closeSearchButton).getVisibility() == View.VISIBLE) {
            outState.putByte(KEY_SEARCH_VISIBLE, (byte)1);
        }
    }

    @Override
    public void onClick(View v) {
        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);

        int navId = 0;
        switch(v.getId()) {
            case R.id.welcome_sheets:
                navId = R.id.nav_sheet; break;
            case R.id.welcome_favorites:
                navId = R.id.nav_favorites; break;
            case R.id.welcome_skills:
                navId = R.id.nav_skills; break;
            case R.id.welcome_feats:
                navId = R.id.nav_feats; break;
            case R.id.welcome_abilities:
                navId = R.id.nav_abilities; break;
            case R.id.welcome_traits:
                navId = R.id.nav_traits; break;
            case R.id.welcome_spells:
                navId = R.id.nav_spells; break;
            case R.id.welcome_equipment:
                navId = R.id.nav_equipment; break;
            case R.id.welcome_magic:
                navId = R.id.nav_magic; break;
            case R.id.welcome_condition:
                navId = R.id.nav_conditions; break;
            case R.id.welcome_generator:
                navId = R.id.nav_magic_generator; break;
            // special case (open selected character)
            case R.id.welcome_selchar:
                long characterId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L);
                if(characterId <= 0) {
                    return;
                }
                Context context = getApplicationContext();
                Intent intent = new Intent(this, CharacterSheetActivity.class);
                intent.putExtra(CharacterSheetActivity.SELECTED_CHARACTER_ID, characterId);
                context.startActivity(intent);
                return;
        }

        if(navId > 0) {
            nav.setCheckedItem(navId);
            onNavigationItemSelected(nav.getMenu().findItem(navId));
        }
    }
}
