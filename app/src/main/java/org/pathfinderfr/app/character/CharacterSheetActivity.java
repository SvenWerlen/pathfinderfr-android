package org.pathfinderfr.app.character;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import org.pathfinderfr.R;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.util.SpellFilter;

import java.util.List;

public class CharacterSheetActivity extends AppCompatActivity {

    public static final String PREF_SELECTED_CHARACTER_ID = "pref_characterId";
    public static final String PREF_CHARACTER_MODIF_STATES = "pref_characterModifStates";

    private static final int TAB_HOME = 0;
    private static final int TAB_SKILLS = 1;
    private static final int TAB_FEATS = 2;
    private static final int TAB_SPELLS = 3;

    private int currentTab;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sheet_home:
                    currentTab = TAB_HOME;
                    break;
                case R.id.sheet_skills:
                    currentTab = TAB_SKILLS;
                    break;
                case R.id.sheet_feats:
                    currentTab = TAB_FEATS;
                    break;
                case R.id.sheet_spells:
                    currentTab = TAB_SPELLS;
                    break;
            }
            return showTab();
        }
    };

    private boolean showTab() {
        DBHelper helper = DBHelper.getInstance(getBaseContext());
        Character character;
        long characterId = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .getLong(PREF_SELECTED_CHARACTER_ID, 0L);
        character = (Character)helper.fetchEntity(characterId, CharacterFactory.getInstance());

        // if no character created yet, search for existing characters
        if(character == null) {
            List<DBEntity> list = DBHelper.getInstance(getBaseContext()).getAllEntities(CharacterFactory.getInstance());
            if (list != null && list.size() > 0) {
                // reload with all data
                character = (Character)helper.fetchEntity(((Character)list.get(0)).getId(), CharacterFactory.getInstance());
                characterId = character.getId();
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().
                        putLong(PREF_SELECTED_CHARACTER_ID, characterId).apply();
            }
        }

        if(currentTab != TAB_HOME) {
            if (character == null) {
                View root = findViewById(R.id.sheet_container);
                if (root != null) {
                    Snackbar.make(root, getResources().getString(R.string.character_tab_failed),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                return false;
            } else if (character.getClassesCount() == 0) {
                View root = findViewById(R.id.sheet_container);
                if (root != null) {
                    Snackbar.make(root, getResources().getString(R.string.character_tab_failed_class),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                return false;
            }
        }

        String baseText = getResources().getString(R.string.sheet_menu_activity) + " - ";
        switch (currentTab) {
            case TAB_HOME:
                setTitle(baseText + getResources().getString(R.string.sheet_menu_main));
                showFragment(SheetMainFragment.newInstance(characterId));
                return true;
            case TAB_SKILLS:
                setTitle(baseText + getResources().getString(R.string.sheet_menu_skills));
                showFragment(SheetSkillFragment.newInstance(characterId));
                return true;
            case TAB_FEATS:
                setTitle(baseText + getResources().getString(R.string.sheet_menu_feats));
                showFragment(SheetFeatFragment.newInstance(characterId));
                return true;
            case TAB_SPELLS:
                setTitle(baseText + getResources().getString(R.string.sheet_menu_spells));
                showFragment(SheetSpellFragment.newInstance(characterId));
                return true;
        }
        return false;
    }

    private void showFragment(Fragment newFragment) {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.sheet_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showTooltip(String title, String text) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("tooltip");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment newFragment = FragmentToolTip.newInstance(title, text);
        newFragment.show(ft, "tooltip");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_sheet);

        String baseText = getResources().getString(R.string.sheet_menu_activity) + " - ";
        setTitle(baseText + getResources().getString(R.string.sheet_menu_main));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.sheet_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // search for already created characters
        long characterId = 0;
        List<DBEntity> list = DBHelper.getInstance(getBaseContext()).getAllEntities(CharacterFactory.getInstance());
        if(list != null && list.size() > 0) {
            characterId = list.get(0).getId();
        }
        // keep selected character in preferences
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().
                putLong(PREF_SELECTED_CHARACTER_ID, characterId).apply();

        showTab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh (in case some data changed)

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean reloadRequired = prefs.getBoolean(MainActivity.KEY_RELOAD_REQUIRED, false);
        if(reloadRequired) {
            prefs.edit().putBoolean(MainActivity.KEY_RELOAD_REQUIRED, false).apply();
            showTab();
        }

    }
}
