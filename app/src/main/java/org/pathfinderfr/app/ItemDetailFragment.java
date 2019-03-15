package org.pathfinderfr.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.character.CharacterSheetActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.util.ConfigurationUtil;

import java.util.Properties;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_FACTORY_ID = "item_factoryid";
    public static final String ARG_ITEM_SHOWDETAILS = "item_showdetails";

    private Properties templates = new Properties();

    private String text;
    private TextView textview;

    /**
     * The item that this view is presenting
     */
    private DBEntity mItem;
    private boolean isFavorite;
    private Character character;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        templates = ConfigurationUtil.getInstance(getContext()).getProperties();

        if (getArguments().containsKey(ARG_ITEM_ID) && getArguments().containsKey(ARG_ITEM_FACTORY_ID)) {
            DBHelper dbhelper = DBHelper.getInstance(getContext());
            long itemID = getArguments().getLong(ARG_ITEM_ID);
            String factoryID = getArguments().getString(ARG_ITEM_FACTORY_ID);;

            if(itemID >0 && factoryID != null) {
                mItem = dbhelper.fetchEntity(itemID, EntityFactories.getFactoryById(factoryID));

                // Change menu title by entity name
                Activity activity = this.getActivity();
                CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                if (appBarLayout != null && mItem != null) {
                    appBarLayout.setTitle(mItem.getName());
                }
            }
        }
    }

    private void updateActionIcons(View view) {
        int colorDisabled = view.getContext().getResources().getColor(R.color.colorDisabled);
        int colorEnabled = view.getContext().getResources().getColor(R.color.colorPrimaryDark);

        boolean isAddedToCharacter = false;
        if(mItem != null && (character != null)) {
            if(mItem instanceof Feat) {
                isAddedToCharacter = character.hasFeat((Feat) mItem);
            }
            else if(mItem instanceof ClassFeature) {
                isAddedToCharacter = character.hasClassFeature((ClassFeature) mItem);
            }
        }
        ImageView addToCharacter = (ImageView)view.findViewById(R.id.actionAddToCharacter);
        addToCharacter.getBackground().setColorFilter(isAddedToCharacter ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);

        ImageView addFavorite = (ImageView)view.findViewById(R.id.actionFavorite);
        addFavorite.getBackground().setColorFilter(isFavorite ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_description, container, false);

        final DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());
        long characterId = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L);
        if(characterId != 0) {
            character = (Character)dbHelper.fetchEntity(characterId,CharacterFactory.getInstance());
        }

        // Show the content as text in a TextView.
        if (mItem != null) {

            boolean showDetails = getArguments().getBoolean(ARG_ITEM_SHOWDETAILS);

            Log.d(ItemDetailFragment.class.getSimpleName(), "onCreateView " + showDetails);

            // if no description is available, details must always be visible
            if(mItem.getDescription() == null) {
                showDetails = true;
                text = "";
            } else {
                text = mItem.getDescription().replaceAll("\n","<br />");
            }

            if(showDetails) {
                String detail = mItem.getFactory().generateDetails(mItem,
                        templates.getProperty("template.spell.details"),
                        templates.getProperty("template.spell.detail"));
                text = detail + String.format(
                        templates.getProperty("template.spell.description"),text);

            }
            textview = (TextView) rootView.findViewById(R.id.item_full_description);

            isFavorite = dbHelper.isFavorite(mItem.getFactory().getFactoryId(), mItem.getId());
        }

        ImageView externalLink = (ImageView)rootView.findViewById(R.id.actionExternalLink);
        ImageView addToCharacter = (ImageView)rootView.findViewById(R.id.actionAddToCharacter);
        ImageView addFavorite = (ImageView)rootView.findViewById(R.id.actionFavorite);
        updateActionIcons(rootView);

        if(character == null || mItem == null || !(mItem instanceof Feat || mItem instanceof ClassFeature) ) {
            addToCharacter.setVisibility(View.GONE);
        }

        // Add to character
        addToCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItem == null) {
                    return;
                }

                boolean success = false;
                String message = getResources().getString(R.string.generic_failed);

                if(mItem instanceof Feat) {
                    Feat feat = (Feat)mItem;
                    if(character.hasFeat(feat)) {
                        character.removeFeat(feat);
                        if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = getResources().getString(R.string.feat_removed_success);
                        } else {
                            character.addFeat(feat); // rollback
                            message = getResources().getString(R.string.feat_removed_failed);
                        }
                    } else {
                        character.addFeat(feat);
                        if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = getResources().getString(R.string.feat_added_success);
                        } else {
                            character.removeFeat(feat); // rollback
                            message = getResources().getString(R.string.feat_added_failed);
                        }
                    }
                } else if (mItem instanceof ClassFeature) {
                    ClassFeature classFeature = (ClassFeature)mItem;
                    if(classFeature.isAuto()) {
                        message = getResources().getString(R.string.ability_auto);
                    } else if(character.hasClassFeature(classFeature)) {
                        character.removeClassFeature(classFeature);
                        if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = getResources().getString(R.string.ability_removed_success);
                        } else {
                            character.addClassFeature(classFeature); // rollback
                            message = getResources().getString(R.string.ability_removed_failed);
                        }
                    } else {
                        character.addClassFeature(classFeature);
                        if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = getResources().getString(R.string.ability_added_success);
                        } else {
                            character.removeClassFeature(classFeature); // rollback
                            message = getResources().getString(R.string.ability_added_failed);
                        }
                    }
                }
                updateActionIcons(getView());
                Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        // Add to favorites
        addFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success = false;
                String message = getResources().getString(R.string.generic_failed);

                if (mItem != null) {
                    if (!isFavorite) {
                        success = dbHelper.insertFavorite(mItem);
                        message = success ?
                                getResources().getString(R.string.favorite_added_success) :
                                getResources().getString(R.string.favorite_added_failed);
                    } else {
                        success = dbHelper.deleteFavorite(mItem);
                        message = success ?
                                getResources().getString(R.string.favorite_removed_success) :
                                getResources().getString(R.string.favorite_removed_failed);
                    }
                }

                if (success) {
                    isFavorite = !isFavorite;
                    updateActionIcons(getView());

                    // update list if currently viewing favorites
                    String curViewFactoryId = PreferenceManager.getDefaultSharedPreferences(
                            getView().getContext()).getString(MainActivity.KEY_CUR_FACTORY, null);

                    if(FavoriteFactory.FACTORY_ID.equalsIgnoreCase(curViewFactoryId) || CharacterFactory.FACTORY_ID.equalsIgnoreCase(curViewFactoryId)) {
                        PreferenceManager.getDefaultSharedPreferences(getView().getContext()).edit()
                                .putBoolean(MainActivity.KEY_RELOAD_REQUIRED, true).apply();
                    }
                    Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        // Open external link button
        externalLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mItem != null) {
                    String url = mItem.getReference();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            }
        });


        return rootView;
    }


    @Override
    public void onStart(){
        super.onStart();
        textview.setText(Html.fromHtml(text));
    }
}
