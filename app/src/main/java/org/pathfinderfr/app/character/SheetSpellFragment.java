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
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
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
public class SheetSpellFragment extends Fragment {

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

    private void applyFilters(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        
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


        SpellTable sTable = new SpellTable(classNames);
        DBHelper dbHelper = DBHelper.getInstance(view.getContext());
        List<DBEntity> spells = dbHelper.getAllEntities(SpellFactory.getInstance(), PreferenceUtil.getSources(view.getContext()));
        for(DBEntity entity : spells) {
            sTable.addSpell((Spell)entity);
        }

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

            for(SpellTable.SpellSchool school: level.getSchools()) {
                TableRow rowSchool = new TableRow(view.getContext());
                TextView schoolTv = FragmentUtil.copyExampleTextFragment(exampleSchool);
                schoolTv.setText(school.getSchoolName());
                rowSchool.addView(schoolTv);
                table.addView(rowSchool);

                for(final Spell spell: school.getSpells()) {
                    TableRow rowSpell = new TableRow(view.getContext());
                    rowSpell.setMinimumHeight(height);
                    rowSpell.setGravity(Gravity.CENTER_VERTICAL);
                    //rowSpell.setBackgroundColor(ContextCompat.getColor(getContext(),
                    //        rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
                    TextView spellTv = FragmentUtil.copyExampleTextFragment(exampleName);
                    if(classNames.size()>1) {
                        Pair<String, Integer> infos = SpellFilter.getLevel(classNames, spell);
                        spellTv.setText(String.format(templateSpell, spell.getName(), infos.first));
                    } else {
                        spellTv.setText(spell.getName());
                    }
                    rowSpell.addView(spellTv);
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
//                FragmentTransaction ft = SheetSpellFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
//                Fragment prev = SheetSpellFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag("spells-filter");
//                if (prev != null) {
//                    ft.remove(prev);
//                }
//                ft.addToBackStack(null);
//                DialogFragment newFragment = FragmentspellFilter.newInstance(SheetSpellFragment.this);
//                newFragment.show(ft, "spells-filter");
            }
        });

        if(rowId > 0) {
            view.findViewById(R.id.sheet_spells_empty_list).setVisibility(View.GONE);
        }

        applyFilters(view);

        return view;
    }

//    @Override
//    public void onFilterApplied() {
//        applyFilters(getView());
//    }



}
