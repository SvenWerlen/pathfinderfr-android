package org.pathfinderfr.app.character;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
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
import java.util.List;


/**
 * Skill tab on character sheet
 */
public class SheetSkillFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String ARG_CHARACTER_ID = "character_id";

    private Character character;
    private long characterId;

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

    /**
     * @param abilityId ability identifier
     * @return ability modifier
     */
    private int getAbilityMod(String abilityId) {
        if(character == null) {
            return 0;
        }
        // TODO: make it language-independant
        switch(abilityId) {
            case "FOR": return character.getStrengthModif();
            case "DEX": return character.getDexterityModif();
            case "CON": return character.getConstitutionModif();
            case "INT": return character.getIntelligenceModif();
            case "SAG": return character.getWisdomModif();
            case "CHA": return character.getCharismaModif();
            default: return 0;
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
        TextView exampleName = view.findViewById(R.id.sheet_skills_example_name);
        TextView exampleTotal = view.findViewById(R.id.sheet_skills_example_total);
        TextView exampleAbility = view.findViewById(R.id.sheet_skills_example_ability);
        TextView exampleAbilityBonus = view.findViewById(R.id.sheet_skills_example_ability_bonus);
        view.findViewById(R.id.sheet_skills_row).setVisibility(View.GONE);

        // add all skills
        DBHelper helper = DBHelper.getInstance(view.getContext());
        for(DBEntity entity : helper.getAllEntitiesWithAllFields(SkillFactory.getInstance())) {
            Skill skill = (Skill)entity;
            int abilityMod = getAbilityMod(skill.getAbilityId());
            int total = abilityMod;

            TableRow row = new TableRow(view.getContext());
            // name
            TextView nameTv = FragmentUtil.copyExampleTextFragment(exampleName);
            nameTv.setText(skill.getName());
            row.addView(nameTv);
            // total
            TextView totalTv = FragmentUtil.copyExampleTextFragment(exampleTotal);
            totalTv.setText(String.valueOf(total));
            row.addView(totalTv);
            // ability
            TextView abilityTv = FragmentUtil.copyExampleTextFragment(exampleAbility);
            abilityTv.setText(skill.getAbilityId());
            row.addView(abilityTv);
            // ability bonus
            TextView abilityBonusTv = FragmentUtil.copyExampleTextFragment(exampleAbilityBonus);
            abilityBonusTv.setText(String.valueOf(abilityMod));
            row.addView(abilityBonusTv);
            // rank selector
            int rank = character.getSkillRank(skill.getId());
            if(rank != 0) {
                System.out.println("FOUND SOMEHTING" + rank);
            }
            Spinner spinner = new Spinner(view.getContext());
            List<String> spinnerArray =  new ArrayList<String>();
            for(int i=0; i <= character.getLevel(); i++) {
                spinnerArray.add(String.valueOf(i));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    view.getContext(), android.R.layout.simple_spinner_item, spinnerArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(rank);
            spinner.setTag(String.valueOf(skill.getId()));
            row.addView(spinner);
            spinner.setOnItemSelectedListener(this);

            // add to table
            table.addView(row);
        }

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent != null && parent.getTag() != null) {
            long skillId = Long.parseLong(parent.getTag().toString());
            int ranks = Integer.parseInt(parent.getItemAtPosition(position).toString());
            Log.i(SheetSkillFragment.class.getSimpleName(), String.format("Changing ranks of %d to %d", skillId,ranks));
            if(character.setSkillRank(skillId,ranks)) {
                DBHelper.getInstance(getContext()).updateEntity(character);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        System.out.println(" NOTTTTTTTTTTTTTTTTTTIHG!!!");
    }
}
