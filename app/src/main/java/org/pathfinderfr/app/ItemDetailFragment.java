package org.pathfinderfr.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.character.CharacterSheetActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.Equipment;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.MagicItem;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.database.entity.Weapon;
import org.pathfinderfr.app.treasure.TreasureUtil;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

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
    public static final String ARG_ITEM_SEL_CHARACTER = "item_selected";
    public static final String ARG_ITEM_MESSAGE = "item_message";

    private Properties templates = new Properties();

    private String text;
    private WebView content;

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
            else if(mItem instanceof Trait) {
                isAddedToCharacter = character.hasTrait((Trait) mItem);
            }
            else if(mItem instanceof Skill) {
                isAddedToCharacter = character.isClassSkill((Skill)mItem);
            }
        }
        ImageView addToCharacter = (ImageView)view.findViewById(R.id.actionAddToCharacter);
        if(addToCharacter.getBackground() != null) {
            addToCharacter.getBackground().setColorFilter(isAddedToCharacter ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
        } else {
            addToCharacter.setImageResource(isAddedToCharacter ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        }

        ImageView addFavorite = (ImageView)view.findViewById(R.id.actionFavorite);
        addFavorite.getBackground().setColorFilter(isFavorite ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_description, container, false);

        final DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());

        long characterId = 0;
        if(getArguments().containsKey(ARG_ITEM_SEL_CHARACTER)) {
            characterId = getArguments().getLong(ARG_ITEM_SEL_CHARACTER);
        }
        if(characterId == 0){
            characterId = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                    .getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L);
        }
        if(characterId != 0) {
            character = (Character)dbHelper.fetchEntity(characterId,CharacterFactory.getInstance());
        }

        // Show the content as text in a TextView.
        if (mItem != null) {

            boolean showDetails = getArguments().getBoolean(ARG_ITEM_SHOWDETAILS);

            Log.d(ItemDetailFragment.class.getSimpleName(), "onCreateView " + showDetails);

            // if full content available, take it
            text = mItem.getFactory().generateHTMLContent(mItem);
            // not available, build it (details + description)
            if( text == null) {
                // if no description is available, details must always be visible
                if (mItem.getDescription() == null) {
                    showDetails = true;
                    text = "";
                } else {
                    text = mItem.getDescription().replaceAll("\n", "<br />");
                }

                if (showDetails) {
                    String detail = mItem.getFactory().generateDetails(mItem,
                            templates.getProperty("template.spell.details"),
                            templates.getProperty("template.spell.detail"));
                    text = detail.replaceAll("\n", "<br />") + String.format(
                            templates.getProperty("template.spell.description"), text);

                }
                text = "<div class=\"main\">" + text  + "</div>";
            }
            content = (WebView) rootView.findViewById(R.id.item_full_description);

            isFavorite = dbHelper.isFavorite(mItem.getFactory().getFactoryId(), mItem.getId());
        }

        ImageView externalLink = (ImageView)rootView.findViewById(R.id.actionExternalLink);
        ImageView addToCharacter = (ImageView)rootView.findViewById(R.id.actionAddToCharacter);
        ImageView addFavorite = (ImageView)rootView.findViewById(R.id.actionFavorite);
        TextView message = (TextView)rootView.findViewById(R.id.item_alert_message);
        updateActionIcons(rootView);


        if(mItem == null || !(mItem instanceof Feat || mItem instanceof ClassFeature || mItem instanceof Trait
                || mItem instanceof Weapon || mItem instanceof Armor || mItem instanceof Equipment || mItem instanceof  MagicItem || mItem instanceof Skill) ) {
            addToCharacter.setVisibility(View.GONE);
        }

        if(mItem instanceof Skill) {
            Skill sk = (Skill)mItem;
            if(character != null && character.isClassSkill(sk)) {
                addToCharacter.setImageResource(R.drawable.ic_checked);
                addToCharacter.setBackground(null);
            } else {
                addToCharacter.setImageResource(R.drawable.ic_unchecked);
                addToCharacter.setBackground(null);
            }
        }

        if(mItem == null || mItem instanceof Race) {
            addFavorite.setVisibility(View.GONE);
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
                String cName = character == null || character.getName() == null ? "??" : character.getName();

                if(character == null) {
                    message = getResources().getString(R.string.nocharacter_selected_failed);
                }
                else if(mItem instanceof Feat) {
                    Feat feat = (Feat) mItem;
                    if (character.hasFeat(feat)) {
                        character.removeFeat(feat);
                        if (DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = String.format(getResources().getString(R.string.feat_removed_success), cName);
                            success = true;
                        } else {
                            character.addFeat(feat); // rollback
                            message = getResources().getString(R.string.feat_removed_failed);
                        }
                    } else {
                        character.addFeat(feat);
                        if (DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = String.format(getResources().getString(R.string.feat_added_success), cName);
                            success = true;
                        } else {
                            character.removeFeat(feat); // rollback
                            message = getResources().getString(R.string.feat_added_failed);
                        }
                    }
                }
                else if(mItem instanceof Trait) {
                        Trait trait = (Trait)mItem;
                        if(character.hasTrait(trait)) {
                            character.removeTrait(trait);
                            if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                                message = String.format(getResources().getString(R.string.trait_removed_success),cName);
                                success = true;
                            } else {
                                character.addTrait(trait); // rollback
                                message = getResources().getString(R.string.trait_removed_failed);
                            }
                        } else {
                            character.addTrait(trait);
                            if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                                message = String.format(getResources().getString(R.string.trait_added_success),cName);
                                success = true;
                            } else {
                                character.removeTrait(trait); // rollback
                                message = getResources().getString(R.string.trait_added_failed);
                            }
                        }
                } else if (mItem instanceof ClassFeature) {
                    ClassFeature classFeature = (ClassFeature)mItem;

                    if(character.hasClassFeature(classFeature)) {
                        character.removeClassFeature(classFeature);
                        if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = String.format(getResources().getString(R.string.ability_removed_success),cName);
                            success = true;
                        } else {
                            character.addClassFeature(classFeature); // rollback
                            message = getResources().getString(R.string.ability_removed_failed);
                        }
                    } else {
                        character.addClassFeature(classFeature);
                        if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = String.format(getResources().getString(R.string.ability_added_success),cName);
                            success = true;
                        } else {
                            character.removeClassFeature(classFeature); // rollback
                            message = getResources().getString(R.string.ability_added_failed);
                        }
                    }
                } else if (mItem instanceof Weapon) {
                    Weapon w = (Weapon)mItem;
                    character.addInventoryItem(new Character.InventoryItem(w.getName(), w.getWeightInGrams(), StringUtil.string2Cost(w.getCost()), Character.InventoryItem.IDX_WEAPONS + w.getId(), null));
                    if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                        message = String.format(getResources().getString(R.string.weapon_added_success), cName);
                        success = true;
                    } else {
                        message = getResources().getString(R.string.weapon_added_failed);
                    }
                } else if (mItem instanceof Armor) {
                    Armor a = (Armor)mItem;
                    character.addInventoryItem(new Character.InventoryItem(a.getName(), a.getWeightInGrams(), StringUtil.string2Cost(a.getCost()), Character.InventoryItem.IDX_ARMORS + a.getId(), null));
                    if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                        message = String.format(getResources().getString(R.string.armor_added_success), cName);
                        success = true;
                    } else {
                        message = getResources().getString(R.string.armor_added_failed);
                    }
                } else if (mItem instanceof Equipment) {
                    Equipment e = (Equipment)mItem;
                    character.addInventoryItem(new Character.InventoryItem(e.getName(), e.getWeightInGrams(), StringUtil.string2Cost(e.getCost()), Character.InventoryItem.IDX_EQUIPMENT + e.getId(), null));
                    if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                        message = String.format(getResources().getString(R.string.equipment_added_success), cName);
                        success = true;
                    } else {
                        message = getResources().getString(R.string.equipment_added_failed);
                    }
                } else if (mItem instanceof MagicItem) {
                    MagicItem m = (MagicItem)mItem;
                    character.addInventoryItem(new Character.InventoryItem(m.getName(), m.getWeightInGrams(), StringUtil.string2Cost(m.getCost()), Character.InventoryItem.IDX_MAGICITEM + m.getId(), null));
                    if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                        message = String.format(getResources().getString(R.string.magicitem_added_success), cName);
                        success = true;
                    } else {
                        message = getResources().getString(R.string.magicitem_added_failed);
                    }
                } else if (mItem instanceof Skill) {
                    Skill sk = (Skill)mItem;
                    if(character.isClassSkillByDefault(sk)) {
                        message = getResources().getString(R.string.skill_added_auto);
                    } else if(character.isClassSkill(sk)) {
                        if(character.setClassSkill(sk, false) && DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = String.format(getResources().getString(R.string.skill_removed_success), cName);
                            success = true;
                        } else {
                            message = getResources().getString(R.string.skill_removed_failed);
                        }
                    } else {
                        if(character.setClassSkill(sk, true) && DBHelper.getInstance(getContext()).updateEntity(character)) {
                            message = String.format(getResources().getString(R.string.skill_added_success), cName);
                            success = true;
                        } else {
                            message = getResources().getString(R.string.skill_added_failed);
                        }
                    }
                }
                // update list if currently viewing character
                if(success) {
                    PreferenceManager.getDefaultSharedPreferences(getView().getContext()).edit()
                            .putBoolean(MainActivity.KEY_RELOAD_REQUIRED, true).apply();
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
                    try {
                        String url = mItem.getReference();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    } catch(Exception e) {
                        Snackbar.make(getView(), getResources().getString(R.string.itemdetails_openurl_failed), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            }
        });


        String warningMessage = getArguments().getString(ARG_ITEM_MESSAGE);
        if(warningMessage == null || warningMessage.length() == 0) {
            message.setVisibility(View.GONE);
        } else {
            message.setText(warningMessage);
        }

        return rootView;
    }


    @Override
    public void onStart(){
        super.onStart();
        text = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + text;
        content.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
        content.setBackgroundColor(Color.TRANSPARENT);
    }
}
