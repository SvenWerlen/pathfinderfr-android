package org.pathfinderfr.app.character;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.pathfinderfr.R;
import org.pathfinderfr.app.util.SpellFilter;

public class CharacterSheetActivity extends AppCompatActivity implements SheetMainFragment.OnFragmentInteractionListener {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            String baseText = getResources().getString(R.string.sheet_menu_activity) + " - ";
            switch (item.getItemId()) {
                case R.id.sheet_home:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_main));
                    return true;
                case R.id.sheet_skills:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_skills));
                    return true;
                case R.id.sheet_feats:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_feats));
                    return true;
                case R.id.sheet_spells:
                    setTitle(baseText + getResources().getString(R.string.sheet_menu_spells));
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_sheet);

        String baseText = getResources().getString(R.string.sheet_menu_activity) + " - ";
        setTitle(baseText + getResources().getString(R.string.sheet_menu_main));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.sheet_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Fragment newFragment = new SheetMainFragment();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.sheet_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
