package org.pathfinderfr.app.character;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.database.entity.TraitFactory;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.StringUtil;
import org.pathfinderfr.app.util.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Feat tab on character sheet
 */
public class SheetClassFeatureFragment extends Fragment implements FragmentClassFeatureFilter.OnFragmentInteractionListener, View.OnClickListener {

    private static final String ARG_CHARACTER_ID = "character_id";
    private static final String DIALOG_CLASSFEATURE_FILTER = "classfeatures-filter";

    private Character character;
    private long characterId;

    private List<TableRow> traits;
    private List<Pair<TableRow, ClassFeature>> features;

    private Callbacks mCallbacks;

    public SheetClassFeatureFragment() {
        // Required empty public constructor
    }

    public interface Callbacks {
        void onRefreshRequest();
    }

    /**
     * @param characterId character id to display or 0 if new character
     * @return A new instance of fragment SheetMainFragment.
     */
    public static SheetClassFeatureFragment newInstance(long characterId) {
        SheetClassFeatureFragment fragment = new SheetClassFeatureFragment();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Activities containing this fragment must implement its callbacks
        if(context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        }
    }

    private void applyFilters(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        boolean filterTraits = prefs.getBoolean(FragmentClassFeatureFilter.KEY_CLASSFEATUREFILTER_TRAITS, true);
        boolean filterAuto = prefs.getBoolean(FragmentClassFeatureFilter.KEY_CLASSFEATUREFILTER_AUTO, true);
        boolean filterOnlyFav = prefs.getBoolean(FragmentClassFeatureFilter.KEY_CLASSFEATUREFILTER_FAV, false);

        boolean filtersApplied = filterOnlyFav || !filterTraits || !filterAuto;
        ImageView iv = view.findViewById(R.id.sheet_classfeatures_filters);
        iv.setImageDrawable(ContextCompat.getDrawable(view.getContext(),
                (filtersApplied ? R.drawable.ic_filtered : R.drawable.ic_filter)));

        Set<Long> favorites = new HashSet<>();
        for(DBEntity e : DBHelper.getInstance(view.getContext()).getAllEntities(FavoriteFactory.getInstance())) {
            if(e instanceof ClassFeature) {
                favorites.add(e.getId());
            }
        }

        int rowId = 0;
        for(TableRow row : traits) {
            row.setVisibility(filterTraits ? View.VISIBLE : View.GONE);
            row.setBackgroundColor(ContextCompat.getColor(getContext(),
                    rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
            rowId++;
        }
        for(Pair<TableRow,ClassFeature> entry : features) {
            if ((!filterAuto && entry.second.isAuto()) || (filterOnlyFav && !favorites.contains(entry.second.getId()))) {
                entry.first.setVisibility(View.GONE);
                continue;
            }
            entry.first.setVisibility(View.VISIBLE);
            entry.first.setBackgroundColor(ContextCompat.getColor(getContext(),
                    rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
            rowId++;
        }

        view.findViewById(R.id.sheet_classfeatures_filter_empty).setVisibility(features.size() > 0 && rowId == 0 ? View.VISIBLE : View.GONE);
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
        View view = inflater.inflate(R.layout.fragment_sheet_classfeatures, container, false);

        // References
        TableLayout table = view.findViewById(R.id.sheet_classfeatures_table);
        ImageView exampleIcon = view.findViewById(R.id.sheet_classfeatures_example_icon);
        TextView exampleName = view.findViewById(R.id.sheet_classfeatures_example_name);
        ImageView exampleLinked = view.findViewById(R.id.sheet_classfeatures_example_linked);
        view.findViewById(R.id.sheet_classfeatures_row).setVisibility(View.GONE);
        TextView messageAdd = view.findViewById(R.id.sheet_classfeatures_add);
        exampleIcon.setColorFilter(view.getResources().getColor(R.color.colorBlack));

        view.findViewById(R.id.classfeatures_add_batch).setOnClickListener(this);
        view.findViewById(R.id.classfeatures_del_batch_all).setOnClickListener(this);
        view.findViewById(R.id.classfeatures_del_batch_base).setOnClickListener(this);

        // determine size
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int lineHeight = Integer.parseInt(prefs.getString(MainActivity.PREF_LINEHEIGHT, "0"));
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, lineHeight, view.getResources().getDisplayMetrics());
        int rowId = 0;


        // add all traits
        traits = new ArrayList<>();
        if(character.getRace() != null) {
            for (final Race.Trait t : character.getRace().getTraits()) {

                TableRow row = new TableRow(view.getContext());
                row.setMinimumHeight(height);
                row.setGravity(Gravity.CENTER_VERTICAL);
                traits.add(row);

                // icon
                ImageView iconIv = FragmentUtil.copyExampleImageFragment(exampleIcon);
                iconIv.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_item_icon_trait));
                iconIv.setColorFilter(view.getResources().getColor(R.color.colorBlack));
                row.addView(iconIv);
                // name
                TextView nameTv = FragmentUtil.copyExampleTextFragment(exampleName);
                String template;
                // replaced or modified?
                Trait replacedBy = character.traitIsReplaced(t.getName());
                Trait alteredBy = character.traitIsAltered(t.getName());
                if(replacedBy == null && alteredBy == null) {
                    template =  ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.racetrait.name");
                } else if(replacedBy != null) {
                    template =  ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.racetrait.replaced.name");
                } else {
                    template =  ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.racetrait.altered.name");
                }
                nameTv.setText(String.format(template, t.getName(), character.getRaceName()));

                nameTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = SheetClassFeatureFragment.this.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, character.getRace().getId());
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, RaceFactory.getInstance().getFactoryId());
                        // highlight if replaced or altered
                        Trait replacedBy = character.traitIsReplaced(t.getName());
                        Trait alteredBy = character.traitIsAltered(t.getName());
                        if(replacedBy != null) {
                            intent.putExtra(ItemDetailFragment.ARG_ITEM_MESSAGE,
                                    String.format(ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("warning.trait.replaced"),
                                            t.getName(), replacedBy.getName()));
                        } else if(alteredBy != null) {
                            intent.putExtra(ItemDetailFragment.ARG_ITEM_MESSAGE,
                                    String.format(ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("warning.trait.altered"),
                                            t.getName(), alteredBy.getName()));
                        }
                        context.startActivity(intent);
                    }
                });
                row.addView(nameTv);

                // add to table
                table.addView(row);

                rowId++;
            }
        }

        // add all alternate traits
        if(character.getTraits() != null) {
            for (final Trait t : character.getTraits()) {

                TableRow row = new TableRow(view.getContext());
                row.setMinimumHeight(height);
                row.setGravity(Gravity.CENTER_VERTICAL);
                traits.add(row);

                // icon
                ImageView iconIv = FragmentUtil.copyExampleImageFragment(exampleIcon);
                iconIv.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_item_icon_trait));
                iconIv.setColorFilter(view.getResources().getColor(R.color.colorBlack));
                row.addView(iconIv);
                // name
                TextView nameTv = FragmentUtil.copyExampleTextFragment(exampleName);
                String templateRace = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.racetrait.name");
                String templateOther = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.trait.name");
                if(t.getRace() == null) {
                    nameTv.setText(String.format(templateOther, t.getName()));
                } else {
                    nameTv.setText(String.format(templateRace, t.getName(), t.getRace().getName()));
                }
                nameTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = SheetClassFeatureFragment.this.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, t.getId());
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, TraitFactory.getInstance().getFactoryId());
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, character.getId());
                        // highlight if invalid trait
                        if(!character.isValidTrait(t)) {
                            intent.putExtra(ItemDetailFragment.ARG_ITEM_MESSAGE,
                                    String.format(ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("warning.trait.incompatible.race"), character.getRaceName()));
                        }
                        // highlight if duplicated traits
                        if(character.isDuplicatedTrait(t)) {
                            intent.putExtra(ItemDetailFragment.ARG_ITEM_MESSAGE,
                                    String.format(ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("warning.trait.incompatible.duplicates")));
                        }
                        context.startActivity(intent);
                    }
                });

                // highlight if invalid trait
                if(!character.isValidTrait(t)) {
                    nameTv.setTextColor(getResources().getColor(R.color.colorWarning));
                }
                // highlight if duplicated traits
                if(character.isDuplicatedTrait(t)) {
                    nameTv.setTextColor(getResources().getColor(R.color.colorWarning));
                }

                row.addView(nameTv);

                // add to table
                table.addView(row);

                rowId++;
            }
        }

        // add all class features
        features = new ArrayList<>();
        for(final ClassFeature classfeature : character.getClassFeatures()) {

            // skip non-auto and linked features
            if(!classfeature.isAuto() && classfeature.getLinkedTo() != null) {
                continue;
            }

            TableRow row = new TableRow(view.getContext());
            row.setMinimumHeight(height);
            row.setGravity(Gravity.CENTER_VERTICAL);
            features.add(new Pair(row,classfeature));

            // icon
            ImageView iconIv = FragmentUtil.copyExampleImageFragment(exampleIcon);
            row.addView(iconIv);
            // name
            TextView nameTv = FragmentUtil.copyExampleTextFragment(exampleName);
            if(classfeature.isAuto()) {
                if(classfeature.getLinkedTo() == null) {
                    String template = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.classfeatures.name");
                    nameTv.setText(String.format(template, classfeature.getClass_().getShortName(), classfeature.getLevel(), classfeature.getNameShort()));
                } else {
                    String template = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.classfeatures.name.linkedTo");
                    nameTv.setText(String.format(template, classfeature.getClass_().getShortName(), classfeature.getLevel(), classfeature.getName(), classfeature.getLinkedTo().getNameShort()));
                }
            } else {
                nameTv.setText(classfeature.getName());
            }
            // highlight if invalid level or class
            if(!character.isValidClassFeature(classfeature)) {
                nameTv.setTextColor(getResources().getColor(R.color.colorWarning));
            }
            nameTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = SheetClassFeatureFragment.this.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, classfeature.getId());
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, classfeature.getFactory().getFactoryId());
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, character.getId());
                    context.startActivity(intent);
                }
            });
            // linkedTo => show icon
            if(classfeature.getLinkedTo() != null) {
                ImageView linkedTo = FragmentUtil.copyExampleImageFragment(exampleLinked);
                LinearLayout layout = new LinearLayout(getContext());
                layout.addView(nameTv);
                layout.addView(linkedTo);
                row.addView(layout);
            } else {
                row.addView(nameTv);
            }

            // add to table
            table.addView(row);

            rowId++;
        }

        view.findViewById(R.id.classfeatures_table_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = SheetClassFeatureFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = SheetClassFeatureFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_CLASSFEATURE_FILTER);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentClassFeatureFilter.newInstance(SheetClassFeatureFragment.this);
                newFragment.show(ft, DIALOG_CLASSFEATURE_FILTER);
            }
        });

        if(rowId > 0) {
            // hide message for empty list
            view.findViewById(R.id.sheet_classfeatures_empty_list).setVisibility(View.GONE);
        } else {
            messageAdd.setVisibility(View.GONE);
        }

        applyFilters(view);

        // reset listeners for opened dialogs
        if (savedInstanceState != null) {
            FragmentClassFeatureFilter fragClassFeatureFilter = (FragmentClassFeatureFilter) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_CLASSFEATURE_FILTER);
            if (fragClassFeatureFilter != null) {
                fragClassFeatureFilter.setListener(this);
            }
        }

        return view;
    }

    @Override
    public void onFilterApplied() {
        applyFilters(getView());
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.classfeatures_add_batch) {
            List<DBEntity> features = DBHelper.getInstance(getContext()).getAllEntities(ClassFeatureFactory.getInstance());
            Map<Long,Integer> classes = new HashMap<>();
            Set<Long> archetypes = new HashSet<>();
            Set<Integer> completedLevel = new HashSet<>();
            Set<Integer> addedLevel = new HashSet<>();
            for(ClassFeature cf : character.getClassFeatures()) {
                completedLevel.add(cf.getLevel());
            }
            for(int i = 0; i < character.getClassesCount(); i++) {
                Triplet<Class, ClassArchetype,Integer> level = character.getClass(i);
                classes.put(level.first.getId(), level.third);
                if(level.second != null) {
                    archetypes.add(level.second.getId());
                }
            }
            // add all automatic class features matching level and archetype
            for(DBEntity f : features) {
                ClassFeature cFeat = (ClassFeature)f;
                if (cFeat.isAuto() && classes.containsKey(cFeat.getClass_().getId())
                        && (cFeat.getClassArchetype() == null || archetypes.contains(cFeat.getClassArchetype().getId()))
                        && cFeat.getLevel() <= classes.get(cFeat.getClass_().getId())) {
                    // in order to avoid previously removed abilities to reappear, only add if no ability exist for that level
                    if(!completedLevel.contains(cFeat.getLevel())) {
                        if(character.addClassFeature(cFeat)) {
                            addedLevel.add(cFeat.getLevel());
                        }
                    }
                }
            }
            // something was added?
            if(addedLevel.size() > 0) {
                DBHelper.getInstance(getContext()).updateEntity(character);
                if (mCallbacks != null) {
                    mCallbacks.onRefreshRequest();
                }

                Integer[] levels = addedLevel.toArray(new Integer[addedLevel.size()]);
                String text = String.format(getResources().getString(R.string.sheet_classfeatures_batch_message), StringUtil.listToString(levels, ", "));
                Snackbar.make(getView(), text,
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                Snackbar.make(getView(), String.format(getResources().getString(R.string.sheet_classfeatures_batch_error)),
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        else if(v.getId() == R.id.classfeatures_del_batch_all || v.getId() == R.id.classfeatures_del_batch_base) {
            character.removeClasseFeatures(v.getId() == R.id.classfeatures_del_batch_base);
            if (mCallbacks != null) {
                mCallbacks.onRefreshRequest();
            }
            if(DBHelper.getInstance(getContext()).updateEntity(character)) {
                Snackbar.make(getView(), getResources().getString(R.string.sheet_classfeatures_batch_del_message),
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                Snackbar.make(getView(), getResources().getString(R.string.sheet_classfeatures_batch_del_error),
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }
}
