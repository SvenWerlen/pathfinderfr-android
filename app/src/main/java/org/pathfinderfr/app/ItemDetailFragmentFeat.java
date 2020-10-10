package org.pathfinderfr.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.character.CharacterSheetActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FeatsUtil;
import org.pathfinderfr.app.util.FragmentUtil;

import java.util.List;
import java.util.Properties;

/**
 * A fragment representing a single feat.
 */
public class ItemDetailFragmentFeat extends Fragment implements View.OnClickListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID             = "item_id";
    public static final String ARG_ITEM_SEL_CHARACTER  = "item_selected";
    public static final String ARG_ITEM_MESSAGE        = "item_message";

    private Properties templates = new Properties();

    private String text;
    private WebView content;

    // View components
    private FlowLayout reqLayout;
    private TextView featReqTV;
    private TextView featReq2TV;
    private TextView sepReqTV;
    private FlowLayout unlockLayout;
    private TextView featUnlockLabel;
    private TextView featUnlTV;
    private ImageView externalLink;
    private ImageView addToCharacter;
    private ImageView addFavorite;
    private TextView message;

    /**
     * The item that this view is presenting
     */
    private Feat feat;
    private boolean isFavorite;
    private Character character;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragmentFeat() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        templates = ConfigurationUtil.getInstance(getContext()).getProperties();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            DBHelper dbhelper = DBHelper.getInstance(getContext());
            long itemID = getArguments().getLong(ARG_ITEM_ID);

            if(itemID >0) {
                feat = (Feat)dbhelper.fetchEntity(itemID, FeatFactory.getInstance());

                // Change menu title by entity name
                Activity activity = this.getActivity();
                CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                if (appBarLayout != null && feat != null) {
                    appBarLayout.setTitle(feat.getName());
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_description_feat, container, false);

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

        reqLayout = rootView.findViewById(R.id.feat_requires);
        featReqTV = rootView.findViewById(R.id.feat_requires_example);
        featReq2TV = rootView.findViewById(R.id.feat_requires_example2);
        sepReqTV = rootView.findViewById(R.id.feat_requires_separator);
        unlockLayout = rootView.findViewById(R.id.feat_unlocks);
        featUnlockLabel = rootView.findViewById(R.id.feat_unlocks_label);
        featUnlTV = rootView.findViewById(R.id.feat_unlocks_example);
        externalLink = rootView.findViewById(R.id.actionExternalLink);
        addToCharacter = rootView.findViewById(R.id.actionAddToCharacter);
        addFavorite = rootView.findViewById(R.id.actionFavorite);
        message = rootView.findViewById(R.id.item_alert_message);
        content = rootView.findViewById(R.id.item_full_description);

        updateRequires();
        updateUnlocks();
        updateContent(rootView);
        updateActionIcons(rootView);

        // Add to character
        addToCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(feat == null) {
                    return;
                }

                boolean success = false;
                String message = getResources().getString(R.string.generic_failed);
                String cName = character == null || character.getName() == null ? "??" : character.getName();

                if(character == null) {
                    message = getResources().getString(R.string.nocharacter_selected_failed);
                }
                else  {
                    Feat feat = (Feat) ItemDetailFragmentFeat.this.feat;
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

                if (feat != null) {
                    if (!isFavorite) {
                        success = dbHelper.insertFavorite(feat);
                        message = success ?
                                getResources().getString(R.string.favorite_added_success) :
                                getResources().getString(R.string.favorite_added_failed);
                    } else {
                        success = dbHelper.deleteFavorite(feat);
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

                    if (FavoriteFactory.FACTORY_ID.equalsIgnoreCase(curViewFactoryId) || CharacterFactory.FACTORY_ID.equalsIgnoreCase(curViewFactoryId)) {
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
                if(feat != null) {
                    try {
                        String url = feat.getReference();
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

    private void updateActionIcons(View view) {
        int colorDisabled = view.getContext().getResources().getColor(R.color.colorDisabled);
        int colorEnabled = view.getContext().getResources().getColor(R.color.colorPrimaryDark);

        boolean isAddedToCharacter = false;
        if(feat != null && character != null) {
            isAddedToCharacter = character.hasFeat((Feat) feat);
        }
        ImageView addToCharacter = (ImageView)view.findViewById(R.id.actionAddToCharacter);
        if(addToCharacter.getBackground() != null) {
            addToCharacter.getBackground().setColorFilter(isAddedToCharacter ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
        } else {
            addToCharacter.setImageResource(isAddedToCharacter ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        }
        if(feat == null || character == null) {
            addToCharacter.setVisibility(View.GONE);
        } else {
            addToCharacter.setVisibility(View.VISIBLE);
        }

        ImageView addFavorite = view.findViewById(R.id.actionFavorite);
        addFavorite.getBackground().setColorFilter(isFavorite ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
    }

    private void updateRequires() {
        // Display "requires" path
        if (feat != null && feat.getRequires().size() > 0) {
            List<Feat> requires = FeatsUtil.getRequiredFeatsPath(feat);
            reqLayout.removeAllViews();
            if(requires != null) {
                // display list
                for (Feat f : requires) {
                    TextView reqFeat = FragmentUtil.copyExampleTextFragment(featReqTV);
                    reqFeat.setText(f.getName());
                    reqFeat.setTag(f.getId());
                    reqLayout.addView(reqFeat);
                    reqLayout.addView(FragmentUtil.copyExampleTextFragment(sepReqTV));
                    reqFeat.setOnClickListener(this);
                }
                TextView reqFeat = FragmentUtil.copyExampleTextFragment(featReq2TV);
                reqFeat.setText(feat.getName());
                reqLayout.addView(reqFeat);
            }
            reqLayout.setVisibility(View.VISIBLE);
        } else {
            reqLayout.setVisibility(View.GONE);
        }
    }

    private void updateUnlocks() {
        // Display "unlocks" list
        List<Feat> unlocks = FeatsUtil.getUnlockedFeats(feat);
        if (feat != null && unlocks != null && unlocks.size() > 0) {
            unlockLayout.removeAllViews();
            unlockLayout.addView(featUnlockLabel);
            for(Feat f : unlocks) {
                TextView unlFeat = FragmentUtil.copyExampleTextFragment(featUnlTV);
                unlFeat.setText(f.getName());
                unlFeat.setTag(f.getId());
                unlockLayout.addView(unlFeat);
                unlFeat.setOnClickListener(this);
            }
            unlockLayout.setVisibility(View.VISIBLE);
        } else {
            unlockLayout.setVisibility(View.GONE);
        }
    }

    private void updateContent(View view) {
        // Show the content as text in a TextView.
        if (feat != null) {

            text = feat.getFactory().generateHTMLContent(feat);
            text = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" /><body><div class=\"pfDescr\">" + text + "</div></body>";
            content.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
            content.setBackgroundColor(Color.TRANSPARENT);

            isFavorite = DBHelper.getInstance(view.getContext()).isFavorite(feat.getFactory().getFactoryId(), feat.getId());
        }
    }


    @Override
    public void onClick(View v) {
        Long featId = (Long)v.getTag();
        if(featId != null) {
            Feat feat = (Feat)DBHelper.getInstance(getContext()).fetchEntity(featId, FeatFactory.getInstance());
            if(feat != null) {
                this.feat = feat;
                updateRequires();
                updateUnlocks();
                updateContent(getView());
                updateActionIcons(getView());
            }
        }
    }
}
