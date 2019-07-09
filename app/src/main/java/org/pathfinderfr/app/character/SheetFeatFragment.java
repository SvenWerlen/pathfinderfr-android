package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.ItemDetailActivity;
import org.pathfinderfr.app.ItemDetailFragment;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.FeatFactory;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Feat tab on character sheet
 */
public class SheetFeatFragment extends Fragment implements FragmentFeatFilter.OnFragmentInteractionListener {

    private static final String ARG_CHARACTER_ID = "character_id";
    private static final String DIALOG_FEAT_FILTER = "feats-filter";

    private Character character;
    private long characterId;

    private List<Pair<TableRow, Feat>> feats;

    public SheetFeatFragment() {
        // Required empty public constructor
    }

    /**
     * @param characterId character id to display or 0 if new character
     * @return A new instance of fragment SheetMainFragment.
     */
    public static SheetFeatFragment newInstance(long characterId) {
        SheetFeatFragment fragment = new SheetFeatFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CHARACTER_ID, characterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            characterId = getArguments().getLong(ARG_CHARACTER_ID);
        }
    }

    private void applyFilters(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        int filterOnlyType = prefs.getInt(FragmentFeatFilter.KEY_FEATFILTER_TYPE, FragmentFeatFilter.FEATFILTER_TYPE_ALL);
        boolean filterOnlyFav = prefs.getBoolean(FragmentFeatFilter.KEY_FEATFILTER_FAV, false);

        boolean filtersApplied = (filterOnlyType != FragmentFeatFilter.FEATFILTER_TYPE_ALL) || filterOnlyFav;
        ImageView iv = view.findViewById(R.id.sheet_feats_filters);
        iv.setImageDrawable(ContextCompat.getDrawable(view.getContext(),
                (filtersApplied ? R.drawable.ic_filtered : R.drawable.ic_filter)));

        Set<Long> favorites = new HashSet<>();
        for(DBEntity e : DBHelper.getInstance(view.getContext()).getAllEntities(FavoriteFactory.getInstance())) {
            if(e instanceof Feat) {
                favorites.add(e.getId());
            }
        }

        int rowId = 0;
        for(Pair<TableRow,Feat> entry : feats) {
            if(filterOnlyType == FragmentFeatFilter.FEATFILTER_TYPE_COMBAT && !entry.second.isCombat()) {
                entry.first.setVisibility(View.GONE);
                continue;
            }
            else if(filterOnlyType == FragmentFeatFilter.FEATFILTER_TYPE_NOCOMBAT && entry.second.isCombat()) {
                entry.first.setVisibility(View.GONE);
                continue;
            }
            else if (filterOnlyFav && !favorites.contains(entry.second.getId())) {
                entry.first.setVisibility(View.GONE);
                continue;
            }
            entry.first.setVisibility(View.VISIBLE);
            entry.first.setBackgroundColor(ContextCompat.getColor(getContext(),
                    rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
            rowId++;
        }

        view.findViewById(R.id.sheet_feats_filter_empty).setVisibility(feats.size() > 0 && rowId == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // fetch character
        if(characterId > 0) {
            character = (Character)DBHelper.getInstance(getContext()).fetchEntity(characterId, CharacterFactory.getInstance());
        }
        if(character == null) {
            throw new IllegalStateException("No character selected!");
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sheet_feats, container, false);

        // References
        TableLayout table = view.findViewById(R.id.sheet_feats_table);
        ImageView exampleIcon = view.findViewById(R.id.sheet_feats_example_icon);
        TextView exampleName = view.findViewById(R.id.sheet_feats_example_name);
        view.findViewById(R.id.sheet_feats_row).setVisibility(View.GONE);

        // determine size
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int lineHeight = Integer.parseInt(prefs.getString(MainActivity.PREF_LINEHEIGHT, "0"));
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, lineHeight, view.getResources().getDisplayMetrics());

        // add all skills
        int rowId = 0;
        feats = new ArrayList<>();

        for(final Feat feat : character.getFeats()) {

            TableRow row = new TableRow(view.getContext());
            row.setMinimumHeight(height);
            row.setGravity(Gravity.CENTER_VERTICAL);
            feats.add(new Pair(row,feat));

            // icon
            ImageView iconIv = FragmentUtil.copyExampleImageFragment(exampleIcon);
            if(!feat.isCombat()) {
                iconIv.setVisibility(View.INVISIBLE);
            }
            iconIv.setColorFilter(exampleName.getCurrentTextColor());
            row.addView(iconIv);
            // name
            TextView nameTv = FragmentUtil.copyExampleTextFragment(exampleName);
            nameTv.setText(feat.getNameLong());
            nameTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = SheetFeatFragment.this.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, feat.getId());
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, feat.getFactory().getFactoryId());
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, character.getId());
                    context.startActivity(intent);
                }
            });
            row.addView(nameTv);

            // add to table
            table.addView(row);

            rowId++;
        }

        view.findViewById(R.id.feats_table_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = SheetFeatFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = SheetFeatFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_FEAT_FILTER);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentFeatFilter.newInstance(SheetFeatFragment.this);
                newFragment.show(ft, DIALOG_FEAT_FILTER);
            }
        });

        if(feats.size() > 0) {
            view.findViewById(R.id.sheet_feats_empty_list).setVisibility(View.GONE);
        }

        applyFilters(view);

        // reset listeners for opened dialogs
        if (savedInstanceState != null) {
            FragmentFeatFilter fragFeatFilter = (FragmentFeatFilter) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_FEAT_FILTER);
            if (fragFeatFilter != null) {
                fragFeatFilter.setListener(this);
            }
        }

        return view;
    }

    @Override
    public void onFilterApplied() {
        applyFilters(getView());
    }
}
