package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import java.util.Arrays;
import java.util.List;

public class FragmentModifPicker extends DialogFragment implements View.OnClickListener {

    public static final float ZOOM_FACTOR = 1.5f;

    public static final String ARG_MODIF_IDX     = "arg_modifIdx";
    public static final String ARG_MODIF_SOURCE  = "arg_modifSource";
    public static final String ARG_MODIF_IDS     = "arg_modifIds";
    public static final String ARG_MODIF_VALS    = "arg_modifVals";
    public static final String ARG_MODIF_ICON    = "arg_modifIcon";
    public static final String ARG_MODIF_LINKTO  = "arg_modifLinkTo";
    public static final String ARG_MODIF_WEAPONS = "arg_modifWeapons";

    private FragmentModifPicker.OnFragmentInteractionListener mListener;
    private Integer selectedModif;
    private ImageView selectedIcon;
    private int selectedWeapon;
    private ArrayList<String> weapons;

    private List<LinearLayout> modifs;
    private Character.CharacterModif initial;
    private int modifIdx;

    private static final String[] icons = new String[] {
            // character initiatin
            "modif_rollingdices",
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
            "modif_orb_wand",
            "modif_lunar_wand",
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
            // jewellery
            "modif_ring",
            "modif_diamond_ring",
            "modif_emerald_necklace",
            "modif_necklace",
            "modif_jeweled_chalice",
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
            // spells
            "modif_spell_book",
            "modif_music_spell",
            "modif_magic_palm",
            "modif_magic_swirl",
            "modif_beams_aura",
            "modif_embrassed_energy",
            "modif_aura",
            "modif_magic_shield",
            "modif_invisible",
            "modif_healing",
            "modif_screaming",
            // potions
            "modif_standing_potion",
            "modif_square_bottle",
            "modif_potion_ball",
            "modif_brandy_bottle",
            "modif_heart_bottle",
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
            case Character.MODIF_SAVES_ALL: return getResources().getString(R.string.sheet_savingthrows_other_all);
            case Character.MODIF_SAVES_REF: return getResources().getString(R.string.sheet_savingthrows_other_reflex);
            case Character.MODIF_SAVES_FOR: return getResources().getString(R.string.sheet_savingthrows_other_fortitude);
            case Character.MODIF_SAVES_WIL: return getResources().getString(R.string.sheet_savingthrows_other_will);
            case Character.MODIF_SAVES_MAG_ALL: return getResources().getString(R.string.sheet_savingthrows_mag_all);
            case Character.MODIF_SAVES_MAG_REF: return getResources().getString(R.string.sheet_savingthrows_mag_reflex);
            case Character.MODIF_SAVES_MAG_FOR: return getResources().getString(R.string.sheet_savingthrows_mag_fortitude);
            case Character.MODIF_SAVES_MAG_WIL: return getResources().getString(R.string.sheet_savingthrows_mag_will);
            case Character.MODIF_COMBAT_INI: return getResources().getString(R.string.sheet_initiative);
            case Character.MODIF_COMBAT_AC: return getResources().getString(R.string.sheet_armorclass_other);
            case Character.MODIF_COMBAT_AC_ARMOR: return getResources().getString(R.string.sheet_armorclass_armor);
            case Character.MODIF_COMBAT_AC_SHIELD: return getResources().getString(R.string.sheet_armorclass_shield);
            case Character.MODIF_COMBAT_AC_NATURAL: return getResources().getString(R.string.sheet_armorclass_natural);
            case Character.MODIF_COMBAT_AC_PARADE: return getResources().getString(R.string.sheet_armorclass_parade);
            case Character.MODIF_COMBAT_MAG: return getResources().getString(R.string.sheet_magicresistance);
            case Character.MODIF_COMBAT_HP: return getResources().getString(R.string.sheet_hitpoints);
            case Character.MODIF_COMBAT_SPEED: return getResources().getString(R.string.sheet_speed);
            case Character.MODIF_COMBAT_ATT_MELEE: return getResources().getString(R.string.sheet_attack_melee);
            case Character.MODIF_COMBAT_DAM_MELEE: return getResources().getString(R.string.sheet_damage_melee);
            case Character.MODIF_COMBAT_ATT_RANGED: return getResources().getString(R.string.sheet_attack_distance);
            case Character.MODIF_COMBAT_DAM_RANGED: return getResources().getString(R.string.sheet_damage_distance);
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
        modifIdx = -1;
        modifs = new ArrayList<>();
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize from params
        if(getArguments() != null && getArguments().containsKey(ARG_MODIF_IDX)) {
            modifIdx = getArguments().getInt(ARG_MODIF_IDX, -1);
            String source = getArguments().getString(ARG_MODIF_SOURCE);
            List<Integer> modifIds = getArguments().getIntegerArrayList(ARG_MODIF_IDS);
            List<Integer> modifVals = getArguments().getIntegerArrayList(ARG_MODIF_VALS);
            List<Pair<Integer,Integer>> modifs = new ArrayList<>();
            String icon = getArguments().getString(ARG_MODIF_ICON);
            for(int i = 0; i<modifIds.size();i++) {
                modifs.add(new Pair<Integer, Integer>(modifIds.get(i), modifVals.get(i)));
            }
            int linkTo = getArguments().getInt(ARG_MODIF_LINKTO);
            if(modifIds != null && modifVals != null) {
                initial = new Character.CharacterModif(source, modifs, icon, linkTo);
            }
        }

        if(getArguments() != null) {
            weapons = getArguments().getStringArrayList(ARG_MODIF_WEAPONS);
        }

        // restore values that were selected
        if(savedInstanceState != null) {
            String source = savedInstanceState.getString(ARG_MODIF_SOURCE);
            List<Integer> modifIds = savedInstanceState.getIntegerArrayList(ARG_MODIF_IDS);
            List<Integer> modifVals = savedInstanceState.getIntegerArrayList(ARG_MODIF_VALS);
            List<Pair<Integer,Integer>> modifs = new ArrayList<>();
            String icon = savedInstanceState.getString(ARG_MODIF_ICON);
            for(int i = 0; i<modifIds.size();i++) {
                modifs.add(new Pair<Integer, Integer>(modifIds.get(i), modifVals.get(i)));
            }
            int linkTo = savedInstanceState.getInt(ARG_MODIF_LINKTO);
            if(modifIds != null && modifVals != null) {
                initial = new Character.CharacterModif(source, modifs, icon, linkTo);
            }
            weapons = savedInstanceState.getStringArrayList(ARG_MODIF_WEAPONS);
        }
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
        list.add(getStringWithTag(Character.MODIF_SAVES_MAG_REF));
        list.add(getStringWithTag(Character.MODIF_SAVES_REF));
        list.add(getStringWithTag(Character.MODIF_SAVES_MAG_FOR));
        list.add(getStringWithTag(Character.MODIF_SAVES_FOR));
        list.add(getStringWithTag(Character.MODIF_SAVES_MAG_WIL));
        list.add(getStringWithTag(Character.MODIF_SAVES_WIL));
        list.add(getStringWithTag(Character.MODIF_SAVES_MAG_ALL));
        list.add(getStringWithTag(Character.MODIF_SAVES_ALL));
        // Combat
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_combat), 0));
        list.add(getStringWithTag(Character.MODIF_COMBAT_INI));
        list.add(getStringWithTag(Character.MODIF_COMBAT_AC_ARMOR));
        list.add(getStringWithTag(Character.MODIF_COMBAT_AC_SHIELD));
        list.add(getStringWithTag(Character.MODIF_COMBAT_AC_NATURAL));
        list.add(getStringWithTag(Character.MODIF_COMBAT_AC_PARADE));
        list.add(getStringWithTag(Character.MODIF_COMBAT_AC));
        list.add(getStringWithTag(Character.MODIF_COMBAT_MAG));
        //list.add(getStringWithTag(Character.MODIF_COMBAT_HP));
        list.add(getStringWithTag(Character.MODIF_COMBAT_SPEED));
        list.add(getStringWithTag(Character.MODIF_COMBAT_ATT_MELEE));
        list.add(getStringWithTag(Character.MODIF_COMBAT_DAM_MELEE));
        list.add(getStringWithTag(Character.MODIF_COMBAT_ATT_RANGED));
        list.add(getStringWithTag(Character.MODIF_COMBAT_DAM_RANGED));
        list.add(getStringWithTag(Character.MODIF_COMBAT_CMB));
        list.add(getStringWithTag(Character.MODIF_COMBAT_CMD));
        // Skills
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_skills), 0));
        List<DBEntity> skills = DBHelper.getInstance(rootView.getContext()).getAllEntities(SkillFactory.getInstance(),
                PreferenceUtil.getSources(rootView.getContext()));
        for(DBEntity skill : skills) {
            list.add(new StringWithTag(skill.getName(), Character.MODIF_SKILL + (int)skill.getId()));
        }

        ArrayAdapter<StringWithTag> dataAdapter = new ArrayAdapter<>(this.getContext(),
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
                        // highlight selected icon
                        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(selectedIcon.getLayoutParams());
                        params.width = (int)(selectedIcon.getLayoutParams().width * ZOOM_FACTOR);
                        params.height = (int)(selectedIcon.getLayoutParams().height * ZOOM_FACTOR);
                        selectedIcon.setLayoutParams(params);
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

        // weapon list
        AppCompatSpinner wSpinner = rootView.findViewById(R.id.sheet_modifs_spinner_weapon);
        List<StringWithTag> listWeapons = new ArrayList<>();
        listWeapons.add(new StringWithTag(getResources().getString(R.string.sheet_modifs_linkto_nothing), 0));
        if(weapons != null) {
            for(String w : weapons) {
                listWeapons.add(new StringWithTag(w, 0));
            }
        }
        ArrayAdapter<StringWithTag> dataAdapterWeapons = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, listWeapons);
        dataAdapterWeapons.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wSpinner.setAdapter(dataAdapterWeapons);
        wSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWeapon = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedWeapon = 0;
            }
        });

        // initialize form if required
        rootView.findViewById(R.id.sheet_modifs_linkto_weapon).setVisibility(View.GONE);
        if(initial != null) {
            source.setText(initial.getSource());
            // icon has already been highlighted
            for(int i = 0; i<initial.getModifCount(); i++) {
                Pair<Integer,Integer> modif = initial.getModif(i);
                addBonusLine(rootView, modif.first, modif.second);
            }
            // link to weapon
            if(initial.getLinkToWeapon() > 0 && initial.getLinkToWeapon() <= weapons.size()) {
                wSpinner.setSelection(initial.getLinkToWeapon());
            }
        }

        rootView.findViewById(R.id.modifs_delete).setVisibility(modifIdx >= 0 ? View.VISIBLE : View.GONE);

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
        final View linktoView = view.findViewById(R.id.sheet_modifs_linkto_weapon);
        final TextView bonusTextExample = view.findViewById(R.id.sheet_modifs_bonus_example);
        final ImageView bonusRemoveExample = view.findViewById(R.id.sheet_modifs_remove);
        final String bonusTemplate = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.modif");

        TextView bonusText = FragmentUtil.copyExampleTextFragment(bonusTextExample);
        bonusText.setText(String.format(bonusTemplate, getModifText(modifId), bonus));
        ImageView bonusRemove = FragmentUtil.copyExampleImageFragment(bonusRemoveExample);
        final LinearLayout layout = new LinearLayout(getContext());
        layout.setTag(new Pair<>(modifId, bonus));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(bonusText);
        layout.addView(bonusRemove);

        if(modifId == Character.MODIF_COMBAT_ATT_MELEE || modifId == Character.MODIF_COMBAT_ATT_RANGED
            || modifId == Character.MODIF_COMBAT_DAM_MELEE || modifId == Character.MODIF_COMBAT_DAM_RANGED) {
            linktoView.setVisibility(View.VISIBLE);
        }

        bonusRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setVisibility(View.GONE);
                // hide "linkto" section if no bonus related to bonus attack anymore
                boolean hide = true;
                for(View m : modifs) {
                    if(m.getVisibility() == View.GONE) {
                        continue;
                    }
                    Pair<Integer,Integer> pair = (Pair<Integer,Integer>)m.getTag();
                    if(pair.first == Character.MODIF_COMBAT_ATT_MELEE || pair.first == Character.MODIF_COMBAT_ATT_RANGED
                        || pair.first == Character.MODIF_COMBAT_DAM_MELEE || pair.first == Character.MODIF_COMBAT_DAM_RANGED) {
                        hide = false;
                        break;
                    }
                }
                if(hide) {
                    linktoView.setVisibility(View.GONE);
                    selectedWeapon = 0;
                }
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
                if(modifIdx >= 0) {
                    mListener.onModifUpdated(modifIdx,
                            new Character.CharacterModif(text, bonusList, selectedIcon.getTag().toString(), selectedWeapon));
                } else {
                    mListener.onAddModif(new Character.CharacterModif(text, bonusList, selectedIcon.getTag().toString(), selectedWeapon));
                }
            }
            dismiss();
            return;
        }
        else if(v.getId() == R.id.modifs_delete) {
            if(modifIdx>=0) {
                mListener.onDeleteModif(modifIdx);
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
                ImageView exampleIcon = getView().findViewById(R.id.sheet_modifs_example_icon);
                selectedIcon.setLayoutParams(exampleIcon.getLayoutParams());
            }

            selectedIcon = (ImageView)v;
            if(selectedIcon.getDrawable() != null) {
                selectedIcon.setBackgroundColor(colorEnabled);
                // highlight selected icon
                FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(selectedIcon.getLayoutParams());
                params.width = (int)(selectedIcon.getLayoutParams().width * ZOOM_FACTOR);
                params.height = (int)(selectedIcon.getLayoutParams().height * ZOOM_FACTOR);
                selectedIcon.setLayoutParams(params);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onAddModif(Character.CharacterModif modif);
        void onDeleteModif(int modifIdx);
        void onModifUpdated(int modifIdx, Character.CharacterModif modif);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_MODIF_IDX, modifIdx);
        // store already typed source
        String text = ((EditText)getView().findViewById(R.id.sheet_modifs_source)).getText().toString();
        outState.putString(ARG_MODIF_SOURCE, text);
        // store already added modifs
        List<Pair<Integer,Integer>> bonusList = new ArrayList<>();
        ArrayList<Integer> modifsId = new ArrayList<>();
        ArrayList<Integer> modifsVal = new ArrayList<>();
        for(LinearLayout layout : modifs) {
            if(layout.getVisibility() == View.VISIBLE) {
                modifsId.add(((Pair<Integer, Integer>)layout.getTag()).first);
                modifsVal.add(((Pair<Integer, Integer>)layout.getTag()).second);
            }
        }
        // conversion to array
        outState.putIntegerArrayList(ARG_MODIF_IDS, modifsId);
        outState.putIntegerArrayList(ARG_MODIF_VALS, modifsVal);
        // store icon selection
        if(selectedIcon != null) {
            outState.putString(ARG_MODIF_ICON, selectedIcon.getTag().toString());
        }
        // store weapon list
        if(weapons != null && weapons.size() > 0) {
            outState.putStringArrayList(ARG_MODIF_WEAPONS, weapons);
        }
    }
}

