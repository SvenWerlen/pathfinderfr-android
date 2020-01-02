package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;
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
import org.pathfinderfr.app.database.entity.Modification;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.StringWithTag;


import java.util.ArrayList;
import java.util.List;

public class FragmentModifPicker extends DialogFragment implements View.OnClickListener {

    public static final float ZOOM_FACTOR = 1.5f;

    public static final String ARG_MODIF_ID      = "arg_modifId";
    public static final String ARG_MODIF_NAME    = "arg_modifName";
    public static final String ARG_MODIF_IDS     = "arg_modifIds";
    public static final String ARG_MODIF_VALS    = "arg_modifVals";
    public static final String ARG_MODIF_ITEMID  = "arg_itemId";
    public static final String ARG_MODIF_ITEMS   = "arg_items";
    public static final String ARG_MODIF_ICON    = "arg_modifIcon";

    private FragmentModifPicker.OnFragmentInteractionListener mListener;
    private Integer selectedModif;
    private ImageView selectedIcon;
    private int selectedWeapon;
    private ArrayList<String> weapons;

    private List<LinearLayout> modifs;
    private Modification initial;
    private long modifId;
    private String items;

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
            case Modification.MODIF_ABILITY_ALL: return getResources().getString(R.string.sheet_ability_all);
            case Modification.MODIF_ABILITY_STR: return getResources().getString(R.string.sheet_ability_strength);
            case Modification.MODIF_ABILITY_DEX: return getResources().getString(R.string.sheet_ability_dexterity);
            case Modification.MODIF_ABILITY_CON: return getResources().getString(R.string.sheet_ability_constitution);
            case Modification.MODIF_ABILITY_INT: return getResources().getString(R.string.sheet_ability_intelligence);
            case Modification.MODIF_ABILITY_WIS: return getResources().getString(R.string.sheet_ability_wisdom);
            case Modification.MODIF_ABILITY_CHA: return getResources().getString(R.string.sheet_ability_charisma);
            case Modification.MODIF_SAVES_ALL: return getResources().getString(R.string.sheet_savingthrows_other_all);
            case Modification.MODIF_SAVES_REF: return getResources().getString(R.string.sheet_savingthrows_other_reflex);
            case Modification.MODIF_SAVES_FOR: return getResources().getString(R.string.sheet_savingthrows_other_fortitude);
            case Modification.MODIF_SAVES_WIL: return getResources().getString(R.string.sheet_savingthrows_other_will);
            case Modification.MODIF_SAVES_MAG_ALL: return getResources().getString(R.string.sheet_savingthrows_mag_all);
            case Modification.MODIF_SAVES_MAG_REF: return getResources().getString(R.string.sheet_savingthrows_mag_reflex);
            case Modification.MODIF_SAVES_MAG_FOR: return getResources().getString(R.string.sheet_savingthrows_mag_fortitude);
            case Modification.MODIF_SAVES_MAG_WIL: return getResources().getString(R.string.sheet_savingthrows_mag_will);
            case Modification.MODIF_COMBAT_INI: return getResources().getString(R.string.sheet_initiative);
            case Modification.MODIF_COMBAT_AC: return getResources().getString(R.string.sheet_armorclass_other);
            case Modification.MODIF_COMBAT_AC_ARMOR: return getResources().getString(R.string.sheet_armorclass_armor);
            case Modification.MODIF_COMBAT_AC_SHIELD: return getResources().getString(R.string.sheet_armorclass_shield);
            case Modification.MODIF_COMBAT_AC_NATURAL: return getResources().getString(R.string.sheet_armorclass_natural);
            case Modification.MODIF_COMBAT_AC_PARADE: return getResources().getString(R.string.sheet_armorclass_parade);
            case Modification.MODIF_COMBAT_MAG: return getResources().getString(R.string.sheet_magicresistance);
            case Modification.MODIF_COMBAT_MAG_LVL: return getResources().getString(R.string.sheet_magiclevel);
            case Modification.MODIF_COMBAT_HP: return getResources().getString(R.string.sheet_hitpoints);
            case Modification.MODIF_COMBAT_SPEED: return getResources().getString(R.string.sheet_speed);
            case Modification.MODIF_COMBAT_ATT_MELEE: return getResources().getString(R.string.sheet_attack_melee);
            case Modification.MODIF_COMBAT_DAM_MELEE: return getResources().getString(R.string.sheet_damage_melee);
            case Modification.MODIF_COMBAT_ATT_RANGED: return getResources().getString(R.string.sheet_attack_distance);
            case Modification.MODIF_COMBAT_DAM_RANGED: return getResources().getString(R.string.sheet_damage_distance);
            case Modification.MODIF_COMBAT_CMB: return getResources().getString(R.string.sheet_combat_man_bonus);
            case Modification.MODIF_COMBAT_CMD: return getResources().getString(R.string.sheet_combat_man_defense);
            case Modification.MODIF_SKILL_ALL: return getResources().getString(R.string.sheet_skill_all);
            case Modification.MODIF_SKILL_FOR: return getResources().getString(R.string.sheet_skill_for);
            case Modification.MODIF_SKILL_DEX: return getResources().getString(R.string.sheet_skill_dex);
            case Modification.MODIF_SKILL_CON: return getResources().getString(R.string.sheet_skill_con);
            case Modification.MODIF_SKILL_INT: return getResources().getString(R.string.sheet_skill_int);
            case Modification.MODIF_SKILL_WIS: return getResources().getString(R.string.sheet_skill_wis);
            case Modification.MODIF_SKILL_CHA: return getResources().getString(R.string.sheet_skill_cha);
            default:
                if(modifId > Modification.MODIF_SKILL) {
                    DBEntity entity = DBHelper.getInstance(getContext()).fetchEntity(modifId - Modification.MODIF_SKILL, SkillFactory.getInstance());
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
        modifId = -1;
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

        // item list must always be provided
        if(getArguments() != null && getArguments().containsKey(ARG_MODIF_ITEMS)) {
            items = getArguments().getString(ARG_MODIF_ITEMS);
        } else {
            throw new IllegalStateException("Missing items list");
        }

        // initialize from params
        if(getArguments() != null && getArguments().containsKey(ARG_MODIF_ID)) {
            modifId = getArguments().getLong(ARG_MODIF_ID, -1);
            String name = getArguments().getString(ARG_MODIF_NAME);
            List<Integer> modifIds = getArguments().getIntegerArrayList(ARG_MODIF_IDS);
            List<Integer> modifVals = getArguments().getIntegerArrayList(ARG_MODIF_VALS);
            List<Pair<Integer,Integer>> modifs = new ArrayList<>();
            long itemId = getArguments().getLong(ARG_MODIF_ITEMID);
            String icon = getArguments().getString(ARG_MODIF_ICON);
            for(int i = 0; i<modifIds.size();i++) {
                modifs.add(new Pair<>(modifIds.get(i), modifVals.get(i)));
            }
            if(modifVals != null) {
                initial = new Modification(name, modifs, icon);
                initial.setItemId(itemId);
            }
        }

        // restore values that were selected
        if(savedInstanceState != null) {
            String name = savedInstanceState.getString(ARG_MODIF_NAME);
            List<Integer> modifIds = savedInstanceState.getIntegerArrayList(ARG_MODIF_IDS);
            List<Integer> modifVals = savedInstanceState.getIntegerArrayList(ARG_MODIF_VALS);
            List<Pair<Integer,Integer>> modifs = new ArrayList<>();
            long itemId = savedInstanceState.getLong(ARG_MODIF_ITEMID);
            String icon = savedInstanceState.getString(ARG_MODIF_ICON);
            for(int i = 0; i<modifIds.size();i++) {
                modifs.add(new Pair<>(modifIds.get(i), modifVals.get(i)));
            }
            if(modifVals != null) {
                initial = new Modification(name, modifs, icon);
                initial.setItemId(itemId);
            }
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
        list.add(getStringWithTag(Modification.MODIF_ABILITY_STR));
        list.add(getStringWithTag(Modification.MODIF_ABILITY_DEX));
        list.add(getStringWithTag(Modification.MODIF_ABILITY_CON));
        list.add(getStringWithTag(Modification.MODIF_ABILITY_INT));
        list.add(getStringWithTag(Modification.MODIF_ABILITY_WIS));
        list.add(getStringWithTag(Modification.MODIF_ABILITY_CHA));
        list.add(getStringWithTag(Modification.MODIF_ABILITY_ALL));
        // Saves
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_saves), 0));
        list.add(getStringWithTag(Modification.MODIF_SAVES_MAG_REF));
        list.add(getStringWithTag(Modification.MODIF_SAVES_REF));
        list.add(getStringWithTag(Modification.MODIF_SAVES_MAG_FOR));
        list.add(getStringWithTag(Modification.MODIF_SAVES_FOR));
        list.add(getStringWithTag(Modification.MODIF_SAVES_MAG_WIL));
        list.add(getStringWithTag(Modification.MODIF_SAVES_WIL));
        list.add(getStringWithTag(Modification.MODIF_SAVES_MAG_ALL));
        list.add(getStringWithTag(Modification.MODIF_SAVES_ALL));
        // Combat
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_combat), 0));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_INI));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_AC_ARMOR));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_AC_SHIELD));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_AC_NATURAL));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_AC_PARADE));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_AC));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_MAG));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_MAG_LVL));
        //list.add(getStringWithTag(Character.MODIF_COMBAT_HP));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_SPEED));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_ATT_MELEE));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_DAM_MELEE));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_ATT_RANGED));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_DAM_RANGED));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_CMB));
        list.add(getStringWithTag(Modification.MODIF_COMBAT_CMD));
        // Skills
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_skills), 0));
        list.add(getStringWithTag(Modification.MODIF_SKILL_ALL));
        list.add(getStringWithTag(Modification.MODIF_SKILL_FOR));
        list.add(getStringWithTag(Modification.MODIF_SKILL_DEX));
        list.add(getStringWithTag(Modification.MODIF_SKILL_CON));
        list.add(getStringWithTag(Modification.MODIF_SKILL_INT));
        list.add(getStringWithTag(Modification.MODIF_SKILL_WIS));
        list.add(getStringWithTag(Modification.MODIF_SKILL_CHA));
        List<DBEntity> skills = DBHelper.getInstance(rootView.getContext()).getAllEntities(SkillFactory.getInstance(),
                PreferenceUtil.getSources(rootView.getContext()));
        for(DBEntity skill : skills) {
            list.add(new StringWithTag(skill.getName(), Modification.MODIF_SKILL + (int)skill.getId()));
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

        // LinkedTo
        AppCompatSpinner spinnerLinkedTo = rootView.findViewById(R.id.sheet_modifs_linkedto);
        List<StringWithTag> listLinkedTo = new ArrayList<>();
        int selected = 0;
        // Items
        listLinkedTo.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_linkto_nothing), 0L));
        int idx = 1;
        for(String item : items.split("#")) {
            String[] val = item.split("\\|");
            if(val.length == 2) {
                Long itemId = Long.parseLong(val[0]);
                String itemName = val[1];
                listLinkedTo.add(new StringWithTag(itemName, itemId));
                if(initial != null && initial.getItemId() == itemId) {
                    selected = idx;
                }
                idx++;
            }
        }
        ArrayAdapter<StringWithTag> dataAdapterLinkedTo = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, listLinkedTo);
        dataAdapterLinkedTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLinkedTo.setAdapter(dataAdapterLinkedTo);

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

        EditText name = rootView.findViewById(R.id.sheet_modifs_name);
        name.setFilters(new InputFilter[] { filter });

        if(initial != null) {
            name.setText(initial.getName());
            // icon has already been highlighted
            for(int i = 0; i<initial.getModifCount(); i++) {
                Pair<Integer,Integer> modif = initial.getModif(i);
                addBonusLine(rootView, modif.first, modif.second);
            }
            spinnerLinkedTo.setSelection(selected);
        }

        rootView.findViewById(R.id.modifs_delete).setVisibility(modifId >= 0 ? View.VISIBLE : View.GONE);

        name.requestFocus();
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
        final ImageView bonusMinusExample = view.findViewById(R.id.sheet_modifs_minus1);
        final ImageView bonusPlusExample = view.findViewById(R.id.sheet_modifs_plus1);
        final ImageView bonusRemoveExample = view.findViewById(R.id.sheet_modifs_remove);
        final String bonusTemplate = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.modif");

        final TextView bonusText = FragmentUtil.copyExampleTextFragment(bonusTextExample);
        bonusText.setText(String.format(bonusTemplate, getModifText(modifId), bonus));
        ImageView bonusMinus1 = FragmentUtil.copyExampleImageFragment(bonusMinusExample);
        ImageView bonusPlus1 = FragmentUtil.copyExampleImageFragment(bonusPlusExample);
        ImageView bonusRemove = FragmentUtil.copyExampleImageFragment(bonusRemoveExample);
        final LinearLayout layout = new LinearLayout(getContext());
        layout.setTag(new Pair<>(modifId, bonus));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(bonusText);
        layout.addView(bonusMinus1);
        layout.addView(bonusPlus1);
        layout.addView(bonusRemove);

        bonusMinus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Integer, Integer> p = (Pair<Integer, Integer>)layout.getTag();
                layout.setTag(new Pair<>(p.first, p.second-1));
                bonusText.setText(String.format(bonusTemplate, getModifText(p.first), p.second-1));
            }
        });

        bonusPlus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Integer, Integer> p = (Pair<Integer, Integer>)layout.getTag();
                layout.setTag(new Pair<>(p.first, p.second+1));
                bonusText.setText(String.format(bonusTemplate, getModifText(p.first), p.second+1));
            }
        });

        bonusRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setVisibility(View.GONE);
                for(View m : modifs) {
                    if(m.getVisibility() == View.GONE) {
                        continue;
                    }
                    Pair<Integer,Integer> pair = (Pair<Integer,Integer>)m.getTag();
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
            String text = ((EditText)getView().findViewById(R.id.sheet_modifs_name)).getText().toString();
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

            StringWithTag selected = (StringWithTag)((AppCompatSpinner)getView().findViewById(R.id.sheet_modifs_linkedto)).getSelectedItem();

            if(mListener != null) {
                Modification modif = new Modification(text, bonusList, selectedIcon.getTag().toString());
                modif.setItemId((Long)selected.getTag());
                if(modifId >= 0) {
                    mListener.onModifUpdated(modifId, modif);
                } else {
                    mListener.onAddModif(modif);
                }
            }
            dismiss();
            return;
        }
        else if(v.getId() == R.id.modifs_delete) {
            if(modifId >=0) {
                mListener.onDeleteModif(modifId);
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
                if(mod.first.equals(selectedModif)) {
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
        void onAddModif(Modification modif);
        void onDeleteModif(long modifId);
        void onModifUpdated(long modifId, Modification modif);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_MODIF_ID, modifId);
        // store already typed source
        String text = ((EditText)getView().findViewById(R.id.sheet_modifs_name)).getText().toString();
        outState.putString(ARG_MODIF_NAME, text);
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
    }
}

