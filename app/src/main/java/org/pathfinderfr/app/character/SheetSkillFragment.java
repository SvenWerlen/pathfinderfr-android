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
import org.pathfinderfr.app.ItemListRecyclerViewAdapter;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.StringUtil;
import org.pathfinderfr.app.util.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Skill tab on character sheet
 */
public class SheetSkillFragment extends Fragment implements FragmentRankPicker.OnFragmentInteractionListener,
        FragmentSkillFilter.OnFragmentInteractionListener, FragmentSkillMaxRanksPicker.OnFragmentInteractionListener {

    private static final String ARG_CHARACTER_ID = "character_id";
    private static final String DIALOG_SKILL_FILTER = "skill-filter";
    private static final String DIALOG_SKILL_MAXRANKS = "skill-maxranks";

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


    private void applyFilters(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        boolean filterOnlyClass = prefs.getBoolean(FragmentSkillFilter.KEY_SKILLFILTER_CLASS, false);
        boolean filterOnlyRank = prefs.getBoolean(FragmentSkillFilter.KEY_SKILLFILTER_RANK, false);
        boolean filterOnlyFav = prefs.getBoolean(FragmentSkillFilter.KEY_SKILLFILTER_FAV, false);
        //final int sort = prefs.getInt(FragmentSkillFilter.KEY_SKILL_SORT, 0);

        boolean filtersApplied = filterOnlyClass || filterOnlyRank || filterOnlyFav;
        ImageView iv = view.findViewById(R.id.sheet_skills_filters);
        iv.setImageDrawable(ContextCompat.getDrawable(view.getContext(),
                (filtersApplied ? R.drawable.ic_filtered : R.drawable.ic_filter)));

        Set<Long> favorites = new HashSet<>();
        for(DBEntity e : DBHelper.getInstance(view.getContext()).getAllEntities(FavoriteFactory.getInstance())) {
            if(e instanceof Skill) {
                favorites.add(e.getId());
            }
        }

        int rowId = 0;
        for(Pair<TableRow,Skill> entry : skills) {
            if(filterOnlyClass && !character.isClassSkill(entry.second)) {
                entry.first.setVisibility(View.GONE);
                continue;
            } else if (filterOnlyRank && character.getSkillRank(entry.second.getId()) == 0) {
                entry.first.setVisibility(View.GONE);
                continue;
            } else if (filterOnlyFav && !favorites.contains(entry.second.getId())) {
                entry.first.setVisibility(View.GONE);
                continue;
            }
            entry.first.setVisibility(View.VISIBLE);
            entry.first.setBackgroundColor(ContextCompat.getColor(getContext(),
                    rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
            rowId++;
        }

        view.findViewById(R.id.sheet_skills_filter_empty).setVisibility(skills.size() > 0 && rowId == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sheet_skills, container, false);

        // fetch character
        if(characterId > 0) {
            character = (Character)DBHelper.getInstance(getContext()).fetchEntity(characterId, CharacterFactory.getInstance());
        }
        if(character == null) {
            throw new IllegalStateException("No character selected!");
        }
        SheetMainFragment.initializeCharacterModifsStates(view.getContext(), character);

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int lineHeight = Integer.parseInt(prefs.getString(MainActivity.PREF_LINEHEIGHT, "0"));
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, lineHeight, view.getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 35, view.getResources().getDisplayMetrics());
        int widthAbility = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, view.getResources().getDisplayMetrics());

        // add all skills
        skills = new ArrayList<>();

        final String skillTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.skill.title");
        final String skillTooltipClassSkill = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.skill.content.classskill");
        final String savTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.skill.content");
        final String tooltipModif = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.modif.entry");

        int classSkillCount = 0;
        DBHelper helper = DBHelper.getInstance(view.getContext());
        for(DBEntity entity : helper.getAllEntitiesWithAllFields(SkillFactory.getInstance())) {
            final Skill skill = (Skill)entity;
            int abilityMod = character.getSkillAbilityMod(skill);
            int rank = character.getSkillRank(skill.getId());
            int classSkillBonus = (rank > 0 && character.isClassSkill(skill)) ? 3 : 0;
            int addBonus = character.getAdditionalBonus(Character.MODIF_SKILL + (int)skill.getId());
            int total = abilityMod + rank + classSkillBonus + addBonus;

            TableRow row = new TableRow(view.getContext());
            row.setMinimumHeight(height);
            row.setGravity(Gravity.CENTER_VERTICAL);
            skills.add(new Pair(row,skill));

            // icon
            ImageView iconIv = FragmentUtil.copyExampleImageFragment(exampleIcon);
            if(character.isClassSkill(skill)) {
                iconIv.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_checked));
                classSkillCount += 1;
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
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_SEL_CHARACTER, character.getId());
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
            totalTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int abilityMod = character.getSkillAbilityMod(skill);
                    int rank = character.getSkillRank(skill.getId());
                    int total = character.getSkillTotalBonus(skill);
                    String classSkillText = "";
                    if(character.isClassSkill(skill) && rank > 0) {
                        classSkillText = skillTooltipClassSkill;
                    }
                    ((CharacterSheetActivity)getActivity()).showTooltip(
                            String.format(skillTooltipTitle,skill.getName()),
                            String.format(savTooltipContent,
                                    rank,
                                    classSkillText,
                                    skill.getAbility().toLowerCase(), abilityMod,
                                    SheetMainFragment.generateOtherBonusText(character, Character.MODIF_SKILL + (int)skill.getId(), tooltipModif), // other
                                    total ));
                }
            });
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
        }

        view.findViewById(R.id.skills_table_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = SheetSkillFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = SheetSkillFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_SKILL_FILTER);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentSkillFilter.newInstance(SheetSkillFragment.this);
                newFragment.show(ft, DIALOG_SKILL_FILTER);
            }
        });

        updateTotalRanks(view);

        // ranks per level
        String rplTemplate = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.skill.ranksperlevel");
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i<character.getClassesCount(); i++) {
            Class cl = character.getClass(i).first;
            buf.append(String.format(rplTemplate, cl.getNameShort(), cl.getRanksPerLevel()));
            buf.append(", ");
        }
        if(buf.length()>=2) {
            buf.delete(buf.length()-2, buf.length());
        }
        ((TextView)view.findViewById(R.id.ranksperlevel)).setText(buf.toString());

        view.findViewById(R.id.skills_table_footer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction ft = SheetSkillFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = SheetSkillFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_SKILL_MAXRANKS);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // compute total #ranks
                StringBuffer descrClasses = new StringBuffer();
                int level = character.getLevel();
                int totalRanks = 0;
                String template = ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("template.skill.maxranks.class");
                //template.skill.maxranks.class=%d niveau%s de <b>%s</b> (%d+Int) = %d rangs<br/>
                for(int i = 0; i<character.getClassesCount(); i++) {
                    Triplet<Class, ClassArchetype, Integer> val = character.getClass(i);
                    int ranks = val.third * (val.first.getRanksPerLevel()+character.getIntelligenceModif());
                    descrClasses.append(String.format(template,
                            val.third, val.third > 1 ? "x" : "",
                            val.first.getName(),
                            val.first.getRanksPerLevel(),
                            ranks));
                    totalRanks += ranks;
                }
                if(character.getRace() != null && "Humain".equals(character.getRace().getName())) {
                    template = ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("template.skill.maxranks.race");
                    descrClasses.append(String.format(template, level));
                    totalRanks += level;
                }

                String descrTemplate = ConfigurationUtil.getInstance(getContext()).getProperties().getProperty("template.skill.maxranks");
                String htmlContent = String.format(descrTemplate,
                        character.getIntelligenceModif(),
                        descrClasses,
                        level,
                        totalRanks, totalRanks+level);

                DialogFragment newFragment = FragmentSkillMaxRanksPicker.newInstance(SheetSkillFragment.this, character.getMaxSkillRanks(), htmlContent);
                newFragment.show(ft, DIALOG_SKILL_MAXRANKS);
            }
        });

        applyFilters(view);

        // reset listeners for opened dialogs
        if (savedInstanceState != null) {
            FragmentSkillFilter fragSkillFilter = (FragmentSkillFilter) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_SKILL_FILTER);
            if (fragSkillFilter != null) {
                fragSkillFilter.setListener(this);
            }
            FragmentSkillMaxRanksPicker fragSkillMaxRanks = (FragmentSkillMaxRanksPicker) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_SKILL_MAXRANKS);
            if (fragSkillMaxRanks != null) {
                fragSkillMaxRanks.setListener(this);
            }
        }

        return view;
    }

    private void updateTotalRanks(View view) {
        String total = "";
        if(character.getMaxSkillRanks() <= 0) {
            total = String.valueOf(character.getSkillRanksTotal());
        } else {
            total = character.getSkillRanksTotal() + "/" + character.getMaxSkillRanks();
        }
        ((TextView)view.findViewById(R.id.ranks_total)).setText(total);
    }

    @Override
    public void onRanksSelected(long skillId, int rank) {
        // update character
        character.setSkillRank(skillId, rank);
        if(DBHelper.getInstance(getContext()).updateEntity(character, new HashSet<Integer>(Arrays.asList(CharacterFactory.FLAG_SKILLS)))) {
            // update view
            TextView rankTv = getView().findViewWithTag("SKILL-RANK-" + skillId);
            TextView totalTv = getView().findViewWithTag("SKILL-TOTAL-" + skillId);
            Skill skill = (Skill)DBHelper.getInstance(getContext()).fetchEntity(skillId, SkillFactory.getInstance());
            if(skill != null && rankTv != null && totalTv != null) {
                rankTv.setText(String.valueOf(character.getSkillRank(skill.getId())));
                totalTv.setText(String.valueOf(character.getSkillTotalBonus(skill)));
            }
            updateTotalRanks(getView());
        }
    }

    @Override
    public void onSaveMaxRanksPerLevel(int max) {
        max = max <= 0 ? -1 : max;
        // update character
        character.setMaxSkillRanks(max);
        if(DBHelper.getInstance(getContext()).updateEntity(character, new HashSet<Integer>(Arrays.asList(CharacterFactory.FLAG_SKILLS)))) {
            updateTotalRanks(getView());
        }
    }

    @Override
    public void onFilterApplied() {
        applyFilters(getView());
    }

}
