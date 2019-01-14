package org.pathfinderfr.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 */
public class ItemDetailActivity extends AppCompatActivity {

    private final static String PREF_SHOWDETAILS = "general_showdetails";
    private boolean showDetails;


    /**
     * Updates the button icon (showMore, showLess) according to status
     */
    private void updateShowDetailsButtonIcon(boolean showDetails) {
        FloatingActionButton moreDetails = (FloatingActionButton) findViewById(R.id.fabDetails);
        if (showDetails) {
            moreDetails.setImageDrawable(getResources().getDrawable(R.drawable.ic_lessdetails, getApplicationContext().getTheme()));
            findViewById(R.id.fabLinkExternal).setVisibility(View.VISIBLE);
        } else {
            moreDetails.setImageDrawable(getResources().getDrawable(R.drawable.ic_moredetails, getApplicationContext().getTheme()));
            findViewById(R.id.fabLinkExternal).setVisibility(View.GONE);
        }
    }

    /**
     * Updates the button icon (showMore, showLess) according to status
     */
    private void updateFavoriteButtonIcon(boolean isFavorite) {
        ImageButton favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
        int drawableId = isFavorite ? android.R.drawable.star_big_on : android.R.drawable.star_big_off;
        favoriteButton.setImageDrawable(getResources().getDrawable(drawableId, getApplicationContext().getTheme()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        showDetails = preferences.getBoolean(PREF_SHOWDETAILS, false);
        updateShowDetailsButtonIcon(showDetails);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Favorite button
        ImageButton favorite = (ImageButton) findViewById(R.id.favoriteButton);
        long itemID = getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0);
        String factoryID = getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID);

        System.out.println("FactoryId (" + factoryID + ") => " + itemID);

        boolean isFavorite = DBHelper.getInstance(getBaseContext()).isFavorite(factoryID, itemID);
        updateFavoriteButtonIcon(isFavorite);


        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean success = false;
                String message = getResources().getString(R.string.generic_failed);

                long itemID = getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0);
                String factoryID = getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID);
                boolean isFavorite = DBHelper.getInstance(getBaseContext()).isFavorite(factoryID, itemID);
                if (itemID > 0 && factoryID != null) {
                    DBHelper dbhelper = DBHelper.getInstance(getBaseContext());
                    DBEntity entity = dbhelper.fetchEntity(itemID, EntityFactories.getFactoryById(factoryID));
                    if (entity != null && !isFavorite) {
                        success = dbhelper.insertFavorite(entity);
                        message = success ?
                                getResources().getString(R.string.favorite_added_success) :
                                getResources().getString(R.string.favorite_added_failed);
                    } else if (entity != null && isFavorite) {
                        success = dbhelper.deleteFavorite(entity);
                        message = success ?
                                getResources().getString(R.string.favorite_removed_success) :
                                getResources().getString(R.string.favorite_removed_failed);
                    }
                }

                if (success) {
                    isFavorite = !isFavorite;
                    updateFavoriteButtonIcon(isFavorite);
                    // update list if currently viewing favorites

                    String curViewFactoryId = PreferenceManager.getDefaultSharedPreferences(
                            getBaseContext()).getString(MainActivity.KEY_CUR_FACTORY, null);

                    if(FavoriteFactory.FACTORY_ID.equalsIgnoreCase(curViewFactoryId)) {
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                                .putBoolean(MainActivity.KEY_RELOAD_REQUIRED, true).apply();
                    }
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        // Open external link button
        FloatingActionButton externalLink = (FloatingActionButton) findViewById(R.id.fabLinkExternal);
        externalLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long itemID = getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0);
                String factoryID = getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID);
                if (itemID > 0 && factoryID != null) {
                    DBHelper dbhelper = DBHelper.getInstance(getBaseContext());
                    DBEntity entity = dbhelper.fetchEntity(itemID, EntityFactories.getFactoryById(factoryID));
                    if(entity != null) {
                        String url = entity.getReference();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    } else {
                        String message = getResources().getString(R.string.generic_failed);
                        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
            }
        });

        // More details button
        FloatingActionButton moreDetails = (FloatingActionButton) findViewById(R.id.fabDetails);
        moreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDetails = !showDetails;
                updateShowDetailsButtonIcon(showDetails);

                // refresh fragment
                Bundle arguments = new Bundle();
                arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0));
                arguments.putString(ItemDetailFragment.ARG_ITEM_FACTORY_ID, getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID));
                arguments.putBoolean(ItemDetailFragment.ARG_ITEM_SHOWDETAILS, showDetails);

                Fragment frg = getSupportFragmentManager().findFragmentById(R.id.item_detail_container);
                frg.setArguments(arguments);
                getSupportFragmentManager().beginTransaction().detach(frg).attach(frg).commit();
            }
        });

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
            arguments.putString(ItemDetailFragment.ARG_ITEM_FACTORY_ID, getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID));
            arguments.putBoolean(ItemDetailFragment.ARG_ITEM_SHOWDETAILS, getIntent().getBooleanExtra(ItemDetailFragment.ARG_ITEM_SHOWDETAILS, showDetails));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
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
}
