package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
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
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.SpellFilter;
import org.pathfinderfr.app.util.SpellTable;
import org.pathfinderfr.app.util.SpellUtil;
import org.pathfinderfr.app.util.StringUtil;
import org.pathfinderfr.app.util.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * spell tab on character sheet
 */
public class SheetSpellFragment extends Fragment implements FragmentSpellFilter.OnFragmentInteractionListener, View.OnClickListener {

    private static final String ARG_CHARACTER_ID = "character_id";
    private static final String DIALOG_SPELL_FILTER = "spells-filter";

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

    /**
     * Generates a table rows for given spells
     *
     * @param spells list of spells to be rendered
     * @param spellClasses character's classes that have spells
     * @param chosen list of chosen spells
     * @param ctx context for rendering
     * @param height row height
     * @param filterOnlyFav is favorite filter enabled?
     * @param colorEnabled color for favorite (when enabled)
     * @param colorDisabled color for favorite (when disabled)
     * @param exampleName example for spell name
     * @param exampleFav example for favorite icon
     * @param templateSpell template for spell name (text)
     * @param favListener listener when favorite is clicked
     * @param detailsListener listener when details is clicked
     */
    private static int generateTableRows(
            List<SpellTable.SpellAndClass> spells,
            List<Triplet<Class, ClassArchetype,Integer>> spellClasses,
            Set<Long> chosen,
            Context ctx,
            int startIdx,
            int height,
            boolean filterOnlyFav,
            int colorEnabled,
            int colorDisabled,
            TableLayout table,
            TextView exampleName,
            ImageView exampleFav,
            String templateSpell,
            View.OnClickListener favListener,
            View.OnClickListener detailsListener) {

        int rowId = startIdx;
        for (final SpellTable.SpellAndClass spell : spells) {
            int backgroundColor = ContextCompat.getColor(ctx, rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite);
            // spell
            TableRow rowSpell = new TableRow(ctx);
            rowSpell.setMinimumHeight(height);
            rowSpell.setGravity(Gravity.CENTER_VERTICAL);
            rowSpell.setBackgroundColor(backgroundColor);
            TextView spellTv = FragmentUtil.copyExampleTextFragment(exampleName);
            if (spellClasses.size() > 1) {
                spellTv.setText(String.format(templateSpell,
                        spell.getSpell().getName(),
                        StringUtil.listToString(spell.getClasses().toArray(new String[0]), '/')));
            } else {
                spellTv.setText(spell.getSpell().getName());
            }
            rowSpell.addView(spellTv);
            // favorite icon
            final ImageView spellFavTv = FragmentUtil.copyExampleImageFragment(exampleFav);
            spellFavTv.setImageResource(R.drawable.ic_item_icon_sheet);
            spellFavTv.setTag(spell.getSpell().getId());
            if (filterOnlyFav) {
                spellFavTv.setVisibility(View.INVISIBLE);
            } else {
                boolean isSel =  chosen.contains(spell.getSpell().getId());
                spellFavTv.setColorFilter(isSel ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
                spellFavTv.setOnClickListener(favListener);
                spellTv.setTag(spell.getSpell().getId());
            }
            rowSpell.addView(spellFavTv);
            spellTv.setOnClickListener(detailsListener);
            spellTv.setTag(spell.getSpell().getId());
            table.addView(rowSpell);
            rowId++;
        }
        return rowId;
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
        View view = inflater.inflate(R.layout.fragment_sheet_spells, container, false);

        SheetMainFragment.initializeCharacterModifsStates(view.getContext(), character);

        // references
        TableLayout table = view.findViewById(R.id.sheet_spells_table);
        TextView exampleLevel = view.findViewById(R.id.sheet_spells_example_level);
        TextView exampleSchool = view.findViewById(R.id.sheet_spells_example_school);
        TextView exampleName = view.findViewById(R.id.sheet_spells_example_name);
        ImageView exampleFav = view.findViewById(R.id.sheet_spells_example_fav);
        view.findViewById(R.id.sheet_spells_row).setVisibility(View.GONE);
        view.findViewById(R.id.sheet_spells_row_level).setVisibility(View.GONE);
        view.findViewById(R.id.sheet_spells_row_school).setVisibility(View.GONE);

        // determine size
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int lineHeight = Integer.parseInt(prefs.getString(MainActivity.PREF_LINEHEIGHT, "0"));
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, lineHeight, view.getResources().getDisplayMetrics());

        String templateLevel = ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("template.sheet.spells.level");
        String templateSpell = ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("template.sheet.spell");

        // filters / preferences
        boolean filterOnlyFav = prefs.getBoolean(FragmentSpellFilter.KEY_SPELLFILTER_FAV, false);
        int filterMode = prefs.getInt(FragmentSpellFilter.KEY_SPELLFILTER_MODE, FragmentSpellFilter.SPELLFILTER_MODE_SCHOOL);

        Set<Long> chosenSpells = null;
        chosenSpells = new HashSet<>();
        for(Spell spell : character.getSpells()) {
            chosenSpells.add(spell.getId());
        }

        // check if filters have been applied
        boolean filtersApplied = filterOnlyFav || filterMode != FragmentSpellFilter.SPELLFILTER_MODE_SCHOOL;
        ImageView iv = view.findViewById(R.id.sheet_spells_filters);
        iv.setImageDrawable(ContextCompat.getDrawable(view.getContext(),
                (filtersApplied ? R.drawable.ic_filtered : R.drawable.ic_filter)));

        // fetch spells
        final DBHelper dbHelper = DBHelper.getInstance(view.getContext());
        List<Spell> spells = new ArrayList<>();
        List<Triplet<Class,ClassArchetype,Integer>> spellClasses = new ArrayList<>();
        for(int i=0; i<character.getClassesCount(); i++) {
            SpellFilter filter = new SpellFilter(null);
            Triplet<Class, ClassArchetype,Integer> classLvl = character.getClass(i);
            List<Class> classes = SpellUtil.getSpellListFrom(classLvl.first, view.getContext());
            for(Class cl : classes) {
                // increase spell caster level
                classLvl = new Triplet<>(cl, classLvl.second, classLvl.third + character.getAdditionalBonus(Character.MODIF_COMBAT_MAG_LVL));
                filter.addFilterClass(cl.getId());
                Class.Level lvl = classLvl.first.getLevel(classLvl.third);
                if (lvl != null && lvl.getMaxSpellLvl() > 0) {
                    filter.setFilterMaxLevel(lvl.getMaxSpellLvl());
                    spellClasses.add(classLvl);
                    spells.addAll(dbHelper.getSpells(filter, PreferenceUtil.getSources(view.getContext())));
                }
            }
        }

        SpellTable sTable = new SpellTable(spellClasses);
        for(Spell entity : spells) {
            // only add if favorite (or filtering disabled)
            if(!filterOnlyFav || chosenSpells.contains(entity.getId())) {
                sTable.addSpell(entity);
            }
        }

        final int colorDisabled = view.getContext().getResources().getColor(R.color.colorDisabled);
        final int colorEnabled = view.getContext().getResources().getColor(R.color.colorPrimaryDark);

        int rowId = 0;
        for(SpellTable.SpellLevel level : sTable.getLevels()) {
            TableRow rowLevel = new TableRow(view.getContext());
            TextView levelTv = FragmentUtil.copyExampleTextFragment(exampleLevel);
            // list of classes that can have spell at that level
            List<Class> classForThatLevel = new ArrayList<>();
            for(Triplet<Class, ClassArchetype,Integer> pair : spellClasses) {
                Class.Level lvl = pair.first.getLevel(pair.third);
                if(level.getLevel() == 0 || (lvl != null && level.getLevel() <= lvl.getMaxSpellLvl())) {
                    classForThatLevel.add(pair.first);
                }
            }

            if(classForThatLevel.size() > 1) {
                StringBuffer buf = new StringBuffer();
                Set<String> clAdded = new HashSet<>();
                for(Triplet<Class, ClassArchetype,Integer> pair : spellClasses) {
                    String name = pair.first.getShortName(true);
                    if(!clAdded.contains(name)) {
                        clAdded.add(name);
                        buf.append(name).append("/");
                    }
                }
                buf.deleteCharAt(buf.length()-1);
                levelTv.setText(String.format(templateLevel, buf.toString(), level.getLevel()));
            } else if(classForThatLevel.size() == 1) {
                levelTv.setText(String.format(templateLevel, classForThatLevel.get(0).getShortName(true), level.getLevel()));
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

                    rowId = generateTableRows(
                            school.getSpells(),
                            spellClasses,
                            chosenSpells,
                            view.getContext(),
                            rowId,
                            height,
                            filterOnlyFav,
                            colorEnabled,
                            colorDisabled,
                            table,
                            exampleName,
                            exampleFav,
                            templateSpell,
                            this,
                            this);
                }
            }
            // LEVEL
            //   SPELL
            else {
                rowId = generateTableRows(
                        level.getSpells(),
                        spellClasses,
                        chosenSpells,
                        view.getContext(),
                        rowId,
                        height,
                        filterOnlyFav,
                        colorEnabled,
                        colorDisabled,
                        table,
                        exampleName,
                        exampleFav,
                        templateSpell,
                        this,
                        this);
            }
        }

        view.findViewById(R.id.spells_table_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = SheetSpellFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = SheetSpellFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_SPELL_FILTER);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentSpellFilter.newInstance(SheetSpellFragment.this);
                newFragment.show(ft, DIALOG_SPELL_FILTER);
            }
        });

        if(!dbHelper.hasSpellIndexes()) {
            view.findViewById(R.id.sheet_spells_indexes_empty).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.sheet_spells_empty_list).setVisibility(rowId == 0 && !filterOnlyFav ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.sheet_spells_filter_empty).setVisibility(rowId == 0 && filterOnlyFav ? View.VISIBLE : View.GONE);
        }

        // reset listeners for opened dialogs
        if (savedInstanceState != null) {
            FragmentSpellFilter fragSpellFilter = (FragmentSpellFilter) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_SPELL_FILTER);
            if (fragSpellFilter != null) {
                fragSpellFilter.setListener(this);
            }
        }

        return view;
    }

    @Override
    public void onFilterApplied() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.sheet_container,
                SheetSpellFragment.newInstance(characterId)).commit();
    }

    @Override
    public void onClick(View v) {
        if(v instanceof TextView && v.getTag() != null) {
            long spellId = (Long)v.getTag();
            if(spellId > 0) {
                Intent intent = new Intent(getContext(), ItemDetailActivity.class);
                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, spellId);
                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, SpellFactory.FACTORY_ID);
                intent.putExtra(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, character.getId());
                getContext().startActivity(intent);
            }
        } else if(v instanceof ImageView && v.getTag() != null) {
            final int colorDisabled = getContext().getResources().getColor(R.color.colorDisabled);
            final int colorEnabled = getContext().getResources().getColor(R.color.colorPrimaryDark);

            long spellId = (Long) v.getTag();
            if (spellId > 0) {
                boolean isSelected = character.hasSpell(spellId);
                DBHelper dbHelper = DBHelper.getInstance(getContext());
                DBEntity entity = (Spell) dbHelper.fetchEntity(spellId, SpellFactory.getInstance());
                if (isSelected && entity != null) {
                    character.removeSpell((Spell)entity);
                } else if (entity != null) {
                    character.addSpell((Spell)entity);
                }
                dbHelper.updateEntity(character, new HashSet<Integer>(Arrays.asList(CharacterFactory.FLAG_SPELLS)));
                ((ImageView) v).setColorFilter(!isSelected ? colorEnabled : colorDisabled, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }
}
