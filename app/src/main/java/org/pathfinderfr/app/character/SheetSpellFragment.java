package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
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
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.SpellFilter;
import org.pathfinderfr.app.util.SpellTable;
import org.pathfinderfr.app.util.StringUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * spell tab on character sheet
 */
public class SheetSpellFragment extends Fragment implements FragmentSpellFilter.OnFragmentInteractionListener {

    private static final String ARG_CHARACTER_ID = "character_id";

    private Character character;
    private long characterId;

    //private List<Pair<TableRow, spell>> spells;

    public SheetSpellFragment() {
        // Required empty public constructor
    }

    /**
     * @param characterId character id to display or 0 if new character
     * @return A new instance of fragment SheetMainFragment.
     */
    public static SheetSpellFragment newInstance(long characterId) {
        SheetSpellFragment fragment = new SheetSpellFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // fetch character
        if(characterId > 0) {
            character = (Character)DBHelper.getInstance(getContext()).fetchEntity(characterId, CharacterFactory.getInstance());
        }
        if(character == null) {
            character = new Character();
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sheet_spells, container, false);

        // References
        TableLayout table = view.findViewById(R.id.sheet_spells_table);
        TextView exampleLevel = view.findViewById(R.id.sheet_spells_example_level);
        TextView exampleSchool = view.findViewById(R.id.sheet_spells_example_school);
        TextView exampleName = view.findViewById(R.id.sheet_spells_example_name);
        ImageView exampleFav = view.findViewById(R.id.sheet_spells_example_fav);
        view.findViewById(R.id.sheet_spells_row).setVisibility(View.GONE);
        view.findViewById(R.id.sheet_spells_row_level).setVisibility(View.GONE);
        view.findViewById(R.id.sheet_spells_row_school).setVisibility(View.GONE);

        // determine size
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 25, view.getResources().getDisplayMetrics());

        // extract class names
        List<String> classNames = new ArrayList<>();
        List<String> classNamesShort = new ArrayList<>();
        for(int i =0; i<character.getClassesCount(); i++) {
            classNames.add(character.getClass(i).first.getName());
            classNamesShort.add(character.getClass(i).first.getShortName());
        }

        String templateLevel = ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("template.sheet.spells.level");
        String templateSpell = ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("template.sheet.spell");

        // filters / preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        boolean filterOnlyFav = prefs.getBoolean(FragmentSpellFilter.KEY_SPELLFILTER_FAV, false);
        int filterMode = prefs.getInt(FragmentSpellFilter.KEY_SPELLFILTER_MODE, FragmentSpellFilter.SPELLFILTER_MODE_SCHOOL);

        Set<Long> favorites = null;
        favorites = new HashSet<>();
        for(DBEntity e : DBHelper.getInstance(view.getContext()).getAllEntities(FavoriteFactory.getInstance())) {
            if(e instanceof Spell) {
                favorites.add(e.getId());
                System.out.println("Favorite: " + e.getId());
            }
        }

        boolean filtersApplied = filterOnlyFav || filterMode != FragmentSpellFilter.SPELLFILTER_MODE_SCHOOL;
        ImageView iv = view.findViewById(R.id.sheet_spells_filters);
        iv.setImageDrawable(ContextCompat.getDrawable(view.getContext(),
                (filtersApplied ? R.drawable.ic_filtered : R.drawable.ic_filter)));

        SpellTable sTable = new SpellTable(classNames);
        final DBHelper dbHelper = DBHelper.getInstance(view.getContext());
        List<DBEntity> spells = dbHelper.getAllEntities(SpellFactory.getInstance(), PreferenceUtil.getSources(view.getContext()));
        for(DBEntity entity : spells) {
            // only add if favorite (or filtering disabled)
            if(!filterOnlyFav || favorites.contains(entity.getId())) {
                sTable.addSpell((Spell) entity);
            }
        }

        final int colorDisabled = view.getContext().getResources().getColor(R.color.colorDisabled);
        final int colorEnabled = view.getContext().getResources().getColor(R.color.colorPrimaryDark);

        int rowId = 0;
        for(SpellTable.SpellLevel level : sTable.getLevels()) {
            TableRow rowLevel = new TableRow(view.getContext());
            TextView levelTv = FragmentUtil.copyExampleTextFragment(exampleLevel);
            if(classNames.size() > 1) {
                levelTv.setText(String.format(templateLevel, StringUtil.listToString(classNamesShort.toArray(new String[0]), '/'), level.getLevel()));
            } else {
                levelTv.setText(String.format(templateLevel, classNames.get(0), level.getLevel()));
            }
            rowLevel.addView(levelTv);
            table.addView(rowLevel);

            // LEVEL
            // SCHOOL
            //   SPELL
            if(filterMode == FragmentSpellFilter.SPELLFILTER_MODE_SCHOOL) {
                for (SpellTable.SpellSchool school : level.getSchools()) {
                    TableRow rowSchool = new TableRow(view.getContext());
                    TextView schoolTv = FragmentUtil.copyExampleTextFragment(exampleSchool);
                    schoolTv.setText(school.getSchoolName());
                    rowSchool.addView(schoolTv);
                    table.addView(rowSchool);

                    for (final Spell spell : school.getSpells()) {
                        // spell
                        TableRow rowSpell = new TableRow(view.getContext());
                        rowSpell.setMinimumHeight(height);
                        rowSpell.setGravity(Gravity.CENTER_VERTICAL);
                        rowSpell.setBackgroundColor(ContextCompat.getColor(getContext(),
                                rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
                        TextView spellTv = FragmentUtil.copyExampleTextFragment(exampleName);
                        if (classNames.size() > 1) {
                            Pair<String, Integer> infos = SpellFilter.getLevel(classNames, spell);
                            spellTv.setText(String.format(templateSpell, spell.getName(), infos.first));
                        } else {
                            spellTv.setText(spell.getName());
                        }
                        rowSpell.addView(spellTv);
                        // favorite icon
                        final ImageView spellFavTv = FragmentUtil.copyExampleImageFragment(exampleFav);
                        spellFavTv.setImageResource(R.drawable.ic_link_favorite);
                        if (filterOnlyFav) {
                            spellFavTv.setVisibility(View.INVISIBLE);
                        } else {
                            boolean isFav = favorites.contains(spell.getId());
                            spellFavTv.setColorFilter(isFav ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
                            spellFavTv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boolean isFav = dbHelper.isFavorite(SpellFactory.FACTORY_ID, spell.getId());
                                    if(isFav) {
                                        dbHelper.deleteFavorite(spell);
                                    } else {
                                        dbHelper.insertFavorite(spell);
                                    }
                                    spellFavTv.setColorFilter(!isFav ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
                                }
                            });
                        }
                        rowSpell.addView(spellFavTv);
                        table.addView(rowSpell);
                        rowId++;

                        spellTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Context context = SheetSpellFragment.this.getContext();
                                Intent intent = new Intent(context, ItemDetailActivity.class);
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, spell.getId());
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, spell.getFactory().getFactoryId());
                                context.startActivity(intent);
                            }
                        });
                    }
                }
            }
            // LEVEL
            //   SPELL
            else {
                for (final Spell spell : level.getSpells()) {
                    // spell
                    TableRow rowSpell = new TableRow(view.getContext());
                    rowSpell.setMinimumHeight(height);
                    rowSpell.setGravity(Gravity.CENTER_VERTICAL);
                    rowSpell.setBackgroundColor(ContextCompat.getColor(getContext(),
                            rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
                    TextView spellTv = FragmentUtil.copyExampleTextFragment(exampleName);
                    if (classNames.size() > 1) {
                        Pair<String, Integer> infos = SpellFilter.getLevel(classNames, spell);
                        spellTv.setText(String.format(templateSpell, spell.getName(), infos.first));
                    } else {
                        spellTv.setText(spell.getName());
                    }
                    rowSpell.addView(spellTv);
                    // favorite icon
                    final ImageView spellFavTv = FragmentUtil.copyExampleImageFragment(exampleFav);
                    spellFavTv.setImageResource(R.drawable.ic_link_favorite);
                    if (filterOnlyFav) {
                        spellFavTv.setVisibility(View.INVISIBLE);
                    } else {
                        boolean isFav = favorites.contains(spell.getId());
                        spellFavTv.setColorFilter(isFav ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
                        spellFavTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean isFav = dbHelper.isFavorite(SpellFactory.FACTORY_ID, spell.getId());
                                if(isFav) {
                                    dbHelper.deleteFavorite(spell);
                                } else {
                                    dbHelper.insertFavorite(spell);
                                }
                                spellFavTv.setColorFilter(!isFav ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
                            }
                        });
                    }
                    rowSpell.addView(spellFavTv);
                    table.addView(rowSpell);
                    rowId++;

                    spellTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = SheetSpellFragment.this.getContext();
                            Intent intent = new Intent(context, ItemDetailActivity.class);
                            intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, spell.getId());
                            intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, spell.getFactory().getFactoryId());
                            context.startActivity(intent);
                        }
                    });
                }
            }
        }

        view.findViewById(R.id.spells_table_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = SheetSpellFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = SheetSpellFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag("spells-filter");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentSpellFilter.newInstance(SheetSpellFragment.this);
                newFragment.show(ft, "spells-filter");
            }
        });

        view.findViewById(R.id.sheet_spells_empty_list).setVisibility(rowId == 0 && !filtersApplied ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.sheet_spells_filter_empty).setVisibility(rowId == 0 && filtersApplied ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onFilterApplied() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.sheet_container,
                SheetSpellFragment.newInstance(characterId)).commit();
    }

}
