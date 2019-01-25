package org.pathfinderfr.app.character;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.util.SpellFilter;

import java.util.List;

public class CharacterSheetActivity extends AppCompatActivity {

    private long characterId;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // search for already created characters
            characterId = 0;
            List<DBEntity> list = DBHelper.getInstance(getBaseContext()).getAllEntities(CharacterFactory.getInstance());
            if(list != null && list.size() > 0) {
                characterId = list.get(0).getId();
            }

            if(characterId == 0) {
                return false;
            }

            String baseText = getResources().getString(R.string.sheet_menu_activity) + " - ";
            switch (item.getItemId()) {
                case R.id.sheet_home:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_main));
                    showFragment(SheetMainFragment.newInstance(characterId));
                    return true;
                case R.id.sheet_skills:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_skills));
                    showFragment(SheetSkillFragment.newInstance(characterId));
                    return true;
                case R.id.sheet_feats:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_feats));
                    showFragment(SheetFeatFragment.newInstance(characterId));
                    return true;
                case R.id.sheet_spells:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_spells));
                    return true;
            }
            return false;
        }
    };

    private void showFragment(Fragment newFragment) {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.sheet_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
        characterId = 0;
        List<DBEntity> list = DBHelper.getInstance(getBaseContext()).getAllEntities(CharacterFactory.getInstance());
        if(list != null && list.size() > 0) {
            characterId = list.get(0).getId();
        }

        showFragment(SheetMainFragment.newInstance(characterId));
    }

}
