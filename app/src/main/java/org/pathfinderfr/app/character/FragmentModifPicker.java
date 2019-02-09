package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.StringWithTag;
import org.pathfinderfr.app.database.entity.Character;


import java.util.ArrayList;
import java.util.List;

public class FragmentModifPicker extends DialogFragment implements View.OnClickListener {

    private FragmentModifPicker.OnFragmentInteractionListener mListener;

    private Integer selectedModif;
    private ImageView selectedIcon;

    private List<LinearLayout> modifs;

    private Character.CharacterModif initial;

    private static final String[] icons = new String[] {
            // weapons (swords)
            "modif_plain_dagger",
            "modif_bowie_knife",
            "modif_stiletto",
            "modif_two_handed_sword",
            "modif_katana",
            "modif_sai",
            // weapons (axes)
            "modif_battered_axe",
            "modif_sharp_axe",
            // weapons (maces and similar)
            "modif_wood_club",
            "modif_flanged_mace",
            "modif_flail",
            // weapons (others)
            "modif_bo",
            "modif_wizard_staff",
            "modif_whip",
            "modif_scythe",
            // weapons (exotic)
            "modif_nunchaku",
            // weapons (range)
            "modif_daggers",
            "modif_hatchets",
            "modif_shuriken",
            "modif_hunting_bolas",
            "modif_slingshot",
            "modif_high_shot",
            "modif_crossbow",
            "modif_broadhead_arrow",
            "modif_stone_spear",
            // armors
            "modif_round_shield",
            "modif_shield",
            "modif_leg_armor",
            "modif_belt_armor",
            "modif_leather_armor",
            "modif_chest_armor",
            "modif_breastplate",
            "modif_gloves",
            "modif_visored_helm",
            // others
            "modif_terror",
            "modif_sleepy",
            "modif_archer",
            "modif_bowman",
            "modif_bow_arrow",
            "modif_slicing_arrow",
            "modif_awareness",
            "modif_sensuousness",
            "modif_angry_eyes",
            "modif_enrage",
            "modif_knockout",
            "modif_mighty_force",
            "modif_sword_brandish",
            "modif_lyre",
            // familiar
            "modif_white_cat",
            "modif_frog",
            "modif_bat",
            "modif_owl",
            "modif_raven",

    };
    
    private String getModifText(int modifId) {
        switch(modifId) {
            case Character.MODIF_ABILITY_ALL: return getResources().getString(R.string.sheet_ability_all);
            case Character.MODIF_ABILITY_STR: return getResources().getString(R.string.sheet_ability_strength);
            case Character.MODIF_ABILITY_DEX: return getResources().getString(R.string.sheet_ability_dexterity);
            case Character.MODIF_ABILITY_CON: return getResources().getString(R.string.sheet_ability_constitution);
            case Character.MODIF_ABILITY_INT: return getResources().getString(R.string.sheet_ability_intelligence);
            case Character.MODIF_ABILITY_WIS: return getResources().getString(R.string.sheet_ability_wisdom);
            case Character.MODIF_ABILITY_CHA: return getResources().getString(R.string.sheet_ability_charisma);
            case Character.MODIF_SAVES_ALL: return getResources().getString(R.string.sheet_savingthrows_all);
            case Character.MODIF_SAVES_REF: return getResources().getString(R.string.sheet_savingthrows_reflex);
            case Character.MODIF_SAVES_FOR: return getResources().getString(R.string.sheet_savingthrows_fortitude);
            case Character.MODIF_SAVES_WIL: return getResources().getString(R.string.sheet_savingthrows_will);
            case Character.MODIF_COMBAT_INI: return getResources().getString(R.string.sheet_initiative);
            case Character.MODIF_COMBAT_AC: return getResources().getString(R.string.sheet_armorclass);
            case Character.MODIF_COMBAT_MAG: return getResources().getString(R.string.sheet_magicresistance);
            case Character.MODIF_COMBAT_HP: return getResources().getString(R.string.sheet_hitpoints);
            case Character.MODIF_COMBAT_SPEED: return getResources().getString(R.string.sheet_speed);
            case Character.MODIF_COMBAT_ATT_MELEE: return getResources().getString(R.string.sheet_attack_melee);
            case Character.MODIF_COMBAT_ATT_RANGED: return getResources().getString(R.string.sheet_attack_distance);
            case Character.MODIF_COMBAT_CMB: return getResources().getString(R.string.sheet_combat_man_bonus);
            case Character.MODIF_COMBAT_CMD: return getResources().getString(R.string.sheet_combat_man_defense);
            default:
                if(modifId > Character.MODIF_SKILL) {
                    DBEntity entity = DBHelper.getInstance(getContext()).fetchEntity(modifId - Character.MODIF_SKILL, SkillFactory.getInstance());
                    if(entity != null) {
                        return entity.getName();
                    }
                }
                return "??";
        }
    }
    
    private StringWithTag getStringWithTag(int modifId) {
        String text = getModifText(modifId);
        if(text != null) {
            return new StringWithTag(text, modifId);
        }
        return null;
    }

    public FragmentModifPicker() {
        // Required empty public constructor
        modifs = new ArrayList<>();
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }
    public void setInitial(Character.CharacterModif modif) { initial = modif; }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentAbilityPicker.
     */
    public static FragmentModifPicker newInstance(OnFragmentInteractionListener listener) {
        FragmentModifPicker fragment = new FragmentModifPicker();
        fragment.setListener(listener);
        return fragment;
    }

    public static FragmentModifPicker newInstance(OnFragmentInteractionListener listener, Character.CharacterModif modif) {
        FragmentModifPicker fragment = new FragmentModifPicker();
        fragment.setListener(listener);
        fragment.setInitial(modif);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_modifpicker, container, false);
        final EditText bonus = rootView.findViewById(R.id.sheet_modifs_value);
        bonus.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        final ImageView addButton = rootView.findViewById(R.id.sheet_modifs_add);
        addButton.setOnClickListener(this);

        AppCompatSpinner spinner = rootView.findViewById(R.id.sheet_modifs_spinner);
        List<StringWithTag> list = new ArrayList<>();
        // Abilities
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_choose), 0));
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_abilities), 0));
        list.add(getStringWithTag(Character.MODIF_ABILITY_STR));
        list.add(getStringWithTag(Character.MODIF_ABILITY_DEX));
        list.add(getStringWithTag(Character.MODIF_ABILITY_CON));
        list.add(getStringWithTag(Character.MODIF_ABILITY_INT));
        list.add(getStringWithTag(Character.MODIF_ABILITY_WIS));
        list.add(getStringWithTag(Character.MODIF_ABILITY_CHA));
        list.add(getStringWithTag(Character.MODIF_ABILITY_ALL));
        // Saves
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_saves), 0));
        list.add(getStringWithTag(Character.MODIF_SAVES_REF));
        list.add(getStringWithTag(Character.MODIF_SAVES_FOR));
        list.add(getStringWithTag(Character.MODIF_SAVES_WIL));
        list.add(getStringWithTag(Character.MODIF_SAVES_ALL));
        // Combat
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_combat), 0));
        list.add(getStringWithTag(Character.MODIF_COMBAT_INI));
        list.add(getStringWithTag(Character.MODIF_COMBAT_AC));
        list.add(getStringWithTag(Character.MODIF_COMBAT_MAG));
        //list.add(getStringWithTag(Character.MODIF_COMBAT_HP));
        list.add(getStringWithTag(Character.MODIF_COMBAT_SPEED));
        list.add(getStringWithTag(Character.MODIF_COMBAT_ATT_MELEE));
        list.add(getStringWithTag(Character.MODIF_COMBAT_ATT_RANGED));
        list.add(getStringWithTag(Character.MODIF_COMBAT_CMB));
        list.add(getStringWithTag(Character.MODIF_COMBAT_CMD));
        // Skills
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_skills), 0));
        List<DBEntity> skills = DBHelper.getInstance(rootView.getContext()).getAllEntities(SkillFactory.getInstance(),
                PreferenceUtil.getSources(rootView.getContext()));
        for(DBEntity skill : skills) {
            list.add(new StringWithTag(skill.getName(), Character.MODIF_SKILL + (int)skill.getId()));
        }

        ArrayAdapter<StringWithTag> dataAdapter = new ArrayAdapter<StringWithTag>(this.getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                selectedModif = (Integer)s.getTag();
                if(selectedModif!=0) {
                    bonus.requestFocus();
                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(bonus, 0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedModif = null;
            }
        });

        // Icons
        FlowLayout layout = rootView.findViewById(R.id.sheet_modifs_layout_icons);
        ImageView exampleIcon = rootView.findViewById(R.id.sheet_modifs_example_icon);
        for(String icon : icons) {
            final int resourceId = rootView.getResources().getIdentifier(icon, "drawable",
                    rootView.getContext().getPackageName());
            if(resourceId > 0) {
                ImageView iv = FragmentUtil.copyExampleImageFragment(exampleIcon);
                String iconName = icon.substring("modif_".length());
                iv.setTag(iconName);
                iv.setBackgroundColor(rootView.getResources().getColor(R.color.colorBlack));
                iv.setImageResource(resourceId);
                iv.setOnClickListener(this);

                if(initial != null && iconName.equals(initial.getIcon())) {
                    selectedIcon = iv;
                    if(selectedIcon.getDrawable() != null) {
                        selectedIcon.setBackgroundColor(rootView.getContext().getResources().getColor(R.color.colorPrimaryDark));
                    }
                }

                layout.addView(iv);
            }
        }
        exampleIcon.setVisibility(View.GONE);
        rootView.findViewById(R.id.sheet_modifs_bonus_layout_example).setVisibility(View.GONE);

        rootView.findViewById(R.id.modifs_ok).setOnClickListener(this);
        rootView.findViewById(R.id.modifs_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.modifs_delete).setOnClickListener(this);

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (source.charAt(i) == '|' || source.charAt(i) == '#' || source.charAt(i) == ':') {
                        return "";
                    }
                }
                return null;
            }
        };

        EditText source = rootView.findViewById(R.id.sheet_modifs_source);
        source.setFilters(new InputFilter[] { filter });

        // initialize form if required
        if(initial != null) {
            source.setText(initial.getSource());
            // icon has already been highlighted
            for(int i = 0; i<initial.getModifCount(); i++) {
                Pair<Integer,Integer> modif = initial.getModif(i);
                addBonusLine(rootView, modif.first, modif.second);
            }
        } else {
            rootView.findViewById(R.id.modifs_delete).setVisibility(View.GONE);
        }

        source.requestFocus();
        if(initial==null) {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addBonusLine(View view, int modifId, int bonus) {
        final TextView bonusTextExample = view.findViewById(R.id.sheet_modifs_bonus_example);
        final ImageView bonusRemoveExample = view.findViewById(R.id.sheet_modifs_remove);
        final String bonusTemplate = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.modif");

        TextView bonusText = FragmentUtil.copyExampleTextFragment(bonusTextExample);
        bonusText.setText(String.format(bonusTemplate, getModifText(modifId), bonus));
        ImageView bonusRemove = FragmentUtil.copyExampleImageFragment(bonusRemoveExample);
        final LinearLayout layout = new LinearLayout(getContext());
        layout.setTag(new Pair<Integer,Integer>(modifId, bonus));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(bonusText);
        layout.addView(bonusRemove);
        bonusRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setVisibility(View.GONE);
            }
        });
        ((LinearLayout)view.findViewById(R.id.sheet_modifs_bonuses)).addView(layout);
        modifs.add(layout);
    }

    @Override
    public void onClick(View v) {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);

        if(v.getId() == R.id.modifs_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.modifs_ok) {
            // check that all values have been properly filled
            String text = ((EditText)getView().findViewById(R.id.sheet_modifs_source)).getText().toString();
            String error = null;
            if(text.length() == 0) {
                error = getView().getResources().getString(R.string.sheet_modifs_error_nosource);
            }
            List<Pair<Integer,Integer>> bonusList = new ArrayList<>();
            for(LinearLayout layout : modifs) {
                if(layout.getVisibility() == View.VISIBLE) {
                    bonusList.add((Pair<Integer, Integer>)layout.getTag());
                }
            }
            if(error == null && bonusList.size() == 0) {
                error = getView().getResources().getString(R.string.sheet_modifs_error_nobonus);
            }
            if(error == null && selectedIcon == null) {
                error = getView().getResources().getString(R.string.sheet_modifs_error_noicon);
            }

            if(error != null) {
                Toast t = Toast.makeText(v.getContext(), error, Toast.LENGTH_SHORT);
                int[] xy = new int[2];
                v.getLocationOnScreen(xy);
                t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
                t.show();
                return;
            }
            if(mListener != null) {
                if(initial != null) {
                    initial.setSource(text);
                    initial.setModifs(bonusList);
                    initial.setIcon(selectedIcon.getTag().toString());
                    mListener.onModifUpdated();
                } else {
                    mListener.onAddModif(new Character.CharacterModif(text, bonusList, selectedIcon.getTag().toString()));
                }
            }
            dismiss();
            return;
        }
        else if(v.getId() == R.id.modifs_delete) {
            if(initial!=null) {
                mListener.onDeleteModif(initial);
            }
            dismiss();
            return;
        }
        else if(v.getId() == R.id.sheet_modifs_add) {
            if(selectedModif == null || selectedModif == 0) {
                Toast t = Toast.makeText(v.getContext(), getView().getResources().getString(R.string.sheet_modifs_error_invalidchoice), Toast.LENGTH_SHORT);
                int[] xy = new int[2];
                v.getLocationOnScreen(xy);
                t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
                t.show();
                return;
            }
            int bonus;
            try {
                bonus = Integer.parseInt(((EditText) getView().findViewById(R.id.sheet_modifs_value)).getText().toString());
            } catch(NumberFormatException e) {
                bonus = 0;
            }
            if(bonus == 0) {
                Toast t = Toast.makeText(v.getContext(), getView().getResources().getString(R.string.sheet_modifs_error_invalidmodif), Toast.LENGTH_SHORT);
                int[] xy = new int[2];
                v.getLocationOnScreen(xy);
                t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
                t.show();
                return;
            }

            // check if already in list and remove if it is the case
            for(LinearLayout tv : modifs) {
                Pair<Integer, Integer> mod = (Pair<Integer, Integer>)tv.getTag();
                if(mod.first == selectedModif) {
                    tv.setVisibility(View.GONE);
                }
            }

            addBonusLine(getView(), selectedModif, bonus);

            // clear selection
            ((AppCompatSpinner)getView().findViewById(R.id.sheet_modifs_spinner)).setSelection(0);
            ((EditText)getView().findViewById(R.id.sheet_modifs_value)).setText("");
        }
        else if(v instanceof ImageView) {
            final int colorDisabled = getContext().getResources().getColor(R.color.colorBlack);
            final int colorEnabled = getContext().getResources().getColor(R.color.colorPrimaryDark);

            if(selectedIcon != null && selectedIcon.getDrawable() != null) {
                selectedIcon.setBackgroundColor(colorDisabled);
            }

            selectedIcon = (ImageView)v;
            if(selectedIcon.getDrawable() != null) {
                selectedIcon.setBackgroundColor(colorEnabled);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onAddModif(Character.CharacterModif modif);
        void onDeleteModif(Character.CharacterModif modif);
        void onModifUpdated();
    }
}

