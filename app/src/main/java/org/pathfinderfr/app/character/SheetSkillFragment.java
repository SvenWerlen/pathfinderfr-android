package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.ItemDetailActivity;
import org.pathfinderfr.app.ItemDetailFragment;
import org.pathfinderfr.app.character.FragmentRacePicker.OnFragmentInteractionListener;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.util.CharacterUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Skill tab on character sheet
 */
public class SheetSkillFragment extends Fragment implements FragmentRankPicker.OnFragmentInteractionListener, FragmentSkillFilter.OnFragmentInteractionListener {

    private static final String ARG_CHARACTER_ID = "character_id";

    private Character character;
    private long characterId;

    private List<Pair<TableRow,Skill>> skills;

    public SheetSkillFragment() {
        // Required empty public constructor
    }

    /**
     * @param characterId character id to display or 0 if new character
     * @return A new instance of fragment SheetMainFragment.
     */
    public static SheetSkillFragment newInstance(long characterId) {
        SheetSkillFragment fragment = new SheetSkillFragment();
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


    private void applyFilters() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean filterOnlyClass = prefs.getBoolean(FragmentSkillFilter.KEY_SKILLFILTER_CLASS, false);
        boolean filterOnlyRank = prefs.getBoolean(FragmentSkillFilter.KEY_SKILLFILTER_RANK, false);
        //final int sort = prefs.getInt(FragmentSkillFilter.KEY_SKILL_SORT, 0);

        int rowId = 0;
        for(Pair<TableRow,Skill> entry : skills) {
            if(filterOnlyClass && !character.isClassSkill(entry.second.getName())) {
                entry.first.setVisibility(View.GONE);
                continue;
            } else if (filterOnlyRank && character.getSkillRank(entry.second.getId()) == 0) {
                entry.first.setVisibility(View.GONE);
                continue;
            }
            entry.first.setVisibility(View.VISIBLE);
            entry.first.setBackgroundColor(ContextCompat.getColor(getContext(),
                    rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
            rowId++;
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
        View view = inflater.inflate(R.layout.fragment_sheet_skills, container, false);

        // References
        TableLayout table = view.findViewById(R.id.sheet_skills_table);
        ImageView exampleIcon = view.findViewById(R.id.sheet_skills_example_icon);
        TextView exampleName = view.findViewById(R.id.sheet_skills_example_name);
        TextView exampleTotal = view.findViewById(R.id.sheet_skills_example_total);
        TextView exampleAbility = view.findViewById(R.id.sheet_skills_example_ability);
        TextView exampleAbilityBonus = view.findViewById(R.id.sheet_skills_example_ability_bonus);
        TextView exampleRank = view.findViewById(R.id.sheet_skills_example_rank);
        view.findViewById(R.id.sheet_skills_row).setVisibility(View.GONE);

        // determine size
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, view.getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 35, view.getResources().getDisplayMetrics());
        int widthAbility = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, view.getResources().getDisplayMetrics());

        // add all skills
        int rowId = 0;
        skills = new ArrayList<>();

        DBHelper helper = DBHelper.getInstance(view.getContext());
        for(DBEntity entity : helper.getAllEntitiesWithAllFields(SkillFactory.getInstance())) {
            final Skill skill = (Skill)entity;
            int abilityMod = character.getSkillAbilityMod(skill);
            int rank = character.getSkillRank(skill.getId());
            int classSkillBonus = (rank > 0 && character.isClassSkill(skill.getName())) ? 3 : 0;
            int total = abilityMod + rank + classSkillBonus;

            TableRow row = new TableRow(view.getContext());
            row.setTag("ROW" + skill.getId());
            row.setMinimumHeight(height);
            row.setGravity(Gravity.CENTER_VERTICAL);
            skills.add(new Pair(row,skill));

            // icon
            ImageView iconIv = FragmentUtil.copyExampleImageFragment(exampleIcon);
            if(character.isClassSkill(skill.getName())) {
                iconIv.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_checked));
            }
            iconIv.setColorFilter(exampleName.getCurrentTextColor());
            row.addView(iconIv);
            // name
            TextView nameTv = FragmentUtil.copyExampleTextFragment(exampleName);
            // TODO: fix name hack
            String name = skill.getName().replaceAll("Connaissances", "Conn.")
                    .replaceAll("exploration", "expl.");
            nameTv.setText(name);
            nameTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = SheetSkillFragment.this.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, skill.getId());
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, skill.getFactory().getFactoryId());
                    context.startActivity(intent);
                }
            });
            row.addView(nameTv);
            // total
            TextView totalTv = FragmentUtil.copyExampleTextFragment(exampleTotal);
            totalTv.setText(String.valueOf(total));
            totalTv.setWidth(width);
            totalTv.setTag("SKILL-TOTAL-" + skill.getId());
            totalTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(totalTv);
            // ability
            TextView abilityTv = FragmentUtil.copyExampleTextFragment(exampleAbility);
            abilityTv.setText(skill.getAbilityId());
            abilityTv.setWidth(widthAbility);
            abilityTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(abilityTv);
            // ability bonus
            TextView abilityBonusTv = FragmentUtil.copyExampleTextFragment(exampleAbilityBonus);
            abilityBonusTv.setText(String.valueOf(abilityMod));
            abilityBonusTv.setWidth(width);
            abilityBonusTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(abilityBonusTv);
            // rank selector
            TextView rankTv = FragmentUtil.copyExampleTextFragment(exampleRank);
            rankTv.setText(String.valueOf(rank));
            rankTv.setWidth(width);
            rankTv.setTag("SKILL-RANK-" + skill.getId());
            rankTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            rankTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("rank-picker");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = FragmentRankPicker.newInstance(SheetSkillFragment.this);
                    Bundle arguments = new Bundle();
                    arguments.putLong(FragmentRankPicker.ARG_RANK_SKILLID, skill.getId());
                    arguments.putString(FragmentRankPicker.ARG_RANK_SKILLNAME, skill.getName());
                    arguments.putInt(FragmentRankPicker.ARG_RANK, character.getSkillRank(skill.getId()));
                    arguments.putInt(FragmentRankPicker.ARG_RANK_MAX, character.getLevel());
                    newFragment.setArguments(arguments);
                    newFragment.show(ft, "rank-picker");
                }
            });
            row.addView(rankTv);

            // add to table
            table.addView(row);

            rowId++;
        }

        view.findViewById(R.id.skills_table_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = SheetSkillFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = SheetSkillFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag("skills-filter");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentSkillFilter.newInstance(SheetSkillFragment.this);
                newFragment.show(ft, "skills-filter");
            }
        });

        applyFilters();

        return view;
    }

    @Override
    public void onRanksSelected(long skillId, int rank) {
        // update character
        character.setSkillRank(skillId, rank);
        if(DBHelper.getInstance(getContext()).updateEntity(character)) {
            // update view
            TextView rankTv = getView().findViewWithTag("SKILL-RANK-" + skillId);
            TextView totalTv = getView().findViewWithTag("SKILL-TOTAL-" + skillId);
            Skill skill = (Skill)DBHelper.getInstance(getContext()).fetchEntity(skillId, SkillFactory.getInstance());
            if(skill != null && rankTv != null && totalTv != null) {
                int abilityMod = character.getSkillAbilityMod(skill);
                rank = character.getSkillRank(skill.getId());
                int classSkillBonus = (rank > 0 && character.isClassSkill(skill.getName())) ? 3 : 0;
                int total = abilityMod + rank + classSkillBonus;
                rankTv.setText(String.valueOf(rank));
                totalTv.setText(String.valueOf(total));
            }
        }
    }

    @Override
    public void onFilterApplied() {
        applyFilters();
    }
}
