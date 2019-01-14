package org.pathfinderfr.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.EntityFactories;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;

import java.io.IOException;
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

    /**
     * The item that this view is presenting
     */
    private DBEntity mItem;

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
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            DBHelper dbhelper = DBHelper.getInstance(getContext());
            long itemID = getArguments().getLong(ARG_ITEM_ID);
            String factoryID = getArguments().getString(ARG_ITEM_FACTORY_ID);;

            System.out.println("Fragment - FactoryId (" + factoryID + ") => " + itemID);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_description, container, false);

        // Show the content as text in a TextView.
        if (mItem != null) {

            boolean showDetails = getArguments().getBoolean(ARG_ITEM_SHOWDETAILS);

            String text;
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
            TextView textview = (TextView) rootView.findViewById(R.id.item_description);
            textview.setText(Html.fromHtml(text));
        }

        return rootView;
    }
}
