package org.pathfinderfr.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import android.view.MenuItem;

import org.pathfinderfr.R;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 */
public class ItemDetailActivity extends AppCompatActivity implements ItemDetailFragment.Callbacks {

    private final static String PREF_SHOWDETAILS = "general_showdetails";
    private boolean showDetails;

    public static final String KEY_TOOLBAR_TITLE = "toolbar-title";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        showDetails = preferences.getBoolean(PREF_SHOWDETAILS, true);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0));
            arguments.putLong(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, 0));
            arguments.putString(ItemDetailFragment.ARG_ITEM_FACTORY_ID, getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID));
            arguments.putBoolean(ItemDetailFragment.ARG_ITEM_SHOWDETAILS, getIntent().getBooleanExtra(ItemDetailFragment.ARG_ITEM_SHOWDETAILS, showDetails));
            arguments.putString(ItemDetailFragment.ARG_ITEM_MESSAGE, getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_MESSAGE));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            String title = savedInstanceState.getString(KEY_TOOLBAR_TITLE);
            if(appBarLayout != null && title != null) {
                appBarLayout.setTitle(title);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            //navigateUpTo(new Intent(this, MainActivity.class));
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if(appBarLayout != null) {
            outState.putString(KEY_TOOLBAR_TITLE, appBarLayout.getTitle().toString());
        }
    }

    @Override
    public void onRefreshRequest() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Bundle arguments = new Bundle();
        arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0));
        arguments.putLong(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, 0));
        arguments.putString(ItemDetailFragment.ARG_ITEM_FACTORY_ID, getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID));
        arguments.putBoolean(ItemDetailFragment.ARG_ITEM_SHOWDETAILS, getIntent().getBooleanExtra(ItemDetailFragment.ARG_ITEM_SHOWDETAILS, showDetails));
        arguments.putString(ItemDetailFragment.ARG_ITEM_MESSAGE, getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_MESSAGE));
        ItemDetailFragment fragment = new ItemDetailFragment();
        fragment.setArguments(arguments);
        transaction.addToBackStack(null);
        transaction.replace(R.id.item_detail_container, fragment);
        transaction.commit();
    }
}
