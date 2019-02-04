package org.pathfinderfr.app.character;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.StringWithTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FragmentModifPicker extends DialogFragment implements View.OnClickListener {

    private FragmentModifPicker.OnFragmentInteractionListener mListener;

    private static final int MODIF_ABILITY_ALL = 1;
    private static final int MODIF_ABILITY_STR = 2;
    private static final int MODIF_ABILITY_DEX = 3;
    private static final int MODIF_ABILITY_CON = 4;
    private static final int MODIF_ABILITY_INT = 5;
    private static final int MODIF_ABILITY_WIS = 6;
    private static final int MODIF_ABILITY_CHA = 7;

    private static final int MODIF_SAVES_ALL = 11;
    private static final int MODIF_SAVES_REF = 12;
    private static final int MODIF_SAVES_FOR = 13;
    private static final int MODIF_SAVES_WIL = 14;

    private static final int MODIF_COMBAT_INI = 21;
    private static final int MODIF_COMBAT_AC = 22;
    private static final int MODIF_COMBAT_MAG = 23;

    private Integer selectedModif;
    private ImageView selectedIcon;

    private Map<Integer, String> modifsTexts;


    private static final String[] icons = new String[] {
            "modif_terror",
            "modif_sleepy",
            "modif_archer",
            "modif_bow_arrow",
            "modif_awareness",
            "modif_sensuousness",
            "modif_bowman",
            "modif_angry_eyes",
            "modif_enrage",
            "modif_battered_axe",
            "modif_broadhead_arrow",
            "modif_slicing_arrow",
            "modif_slingshot",
            "modif_knockout",
            "modif_mighty_force",
            "modif_sword_brandish",
            "modif_crossbow",
    };
    
    private String getModifText(int modifId) {
        switch(modifId) {
            case MODIF_ABILITY_ALL: return getResources().getString(R.string.sheet_modifs_abilities);
            case MODIF_ABILITY_STR: return getResources().getString(R.string.sheet_ability_strength);
            case MODIF_ABILITY_DEX: return getResources().getString(R.string.sheet_ability_dexterity);
            case MODIF_ABILITY_CON: return getResources().getString(R.string.sheet_ability_constitution);
            case MODIF_ABILITY_INT: return getResources().getString(R.string.sheet_ability_intelligence);
            case MODIF_ABILITY_WIS: return getResources().getString(R.string.sheet_ability_wisdom);
            case MODIF_ABILITY_CHA: return getResources().getString(R.string.sheet_ability_charisma);
            case MODIF_SAVES_ALL: return getResources().getString(R.string.sheet_modifs_saves);
            case MODIF_SAVES_REF: return getResources().getString(R.string.sheet_savingthrows_reflex);
            case MODIF_SAVES_FOR: return getResources().getString(R.string.sheet_savingthrows_fortitude);
            case MODIF_SAVES_WIL: return getResources().getString(R.string.sheet_savingthrows_will);
            case MODIF_COMBAT_INI: return getResources().getString(R.string.sheet_initiative);
            case MODIF_COMBAT_AC: return getResources().getString(R.string.sheet_armorclass);
            case MODIF_COMBAT_MAG: return getResources().getString(R.string.sheet_magicresistance);
            default: return null;
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
        list.add(getStringWithTag(MODIF_ABILITY_STR));
        list.add(getStringWithTag(MODIF_ABILITY_DEX));
        list.add(getStringWithTag(MODIF_ABILITY_CON));
        list.add(getStringWithTag(MODIF_ABILITY_INT));
        list.add(getStringWithTag(MODIF_ABILITY_WIS));
        list.add(getStringWithTag(MODIF_ABILITY_CHA));
        list.add(getStringWithTag(MODIF_ABILITY_ALL));
        // Saves
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_saves), 0));
        list.add(getStringWithTag(MODIF_SAVES_REF));
        list.add(getStringWithTag(MODIF_SAVES_FOR));
        list.add(getStringWithTag(MODIF_SAVES_WIL));
        list.add(getStringWithTag(MODIF_SAVES_ALL));
        // Combat
        list.add(new StringWithTag(rootView.getResources().getString(R.string.sheet_modifs_combat), 0));
        list.add(getStringWithTag(MODIF_COMBAT_INI));
        list.add(getStringWithTag(MODIF_COMBAT_AC));
        list.add(getStringWithTag(MODIF_COMBAT_MAG));

        ArrayAdapter<StringWithTag> dataAdapter = new ArrayAdapter<StringWithTag>(this.getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                selectedModif = (Integer)s.getTag();
                bonus.setText("");
                bonus.requestFocus();
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
                iv.setBackgroundColor(rootView.getResources().getColor(R.color.colorBlack));
                iv.setImageResource(resourceId);
                iv.setOnClickListener(this);
                layout.addView(iv);
            }
        }
        exampleIcon.setVisibility(View.GONE);
        rootView.findViewById(R.id.sheet_modifs_bonus_layout_example).setVisibility(View.GONE);

        rootView.findViewById(R.id.modifs_ok).setOnClickListener(this);
        rootView.findViewById(R.id.modifs_cancel).setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.modifs_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.modifs_ok) {
//            if(mListener != null) {
//                mListener.onRanksSelected(skillId, rank);
//            }
            dismiss();
            return;
        }
        else if(v.getId() == R.id.sheet_modifs_add) {
            if(selectedModif == null || selectedModif == 0) {
                Toast t = Toast.makeText(v.getContext(), getView().getResources().getString(R.string.sheet_modifs_invalidchoice), Toast.LENGTH_SHORT);
                int[] xy = new int[2];
                v.getLocationOnScreen(xy);
                t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
                t.show();
                return;
            }
            final TextView bonusTextExample = getView().findViewById(R.id.sheet_modifs_bonus_example);
            final ImageView bonusRemoveExample = getView().findViewById(R.id.sheet_modifs_remove);
            final String bonusTemplate = ConfigurationUtil.getInstance(getView().getContext()).getProperties().getProperty("template.modif");

            int bonus = Integer.parseInt(((EditText)getView().findViewById(R.id.sheet_modifs_value)).getText().toString());
            TextView bonusText = FragmentUtil.copyExampleTextFragment(bonusTextExample);
            bonusText.setText(String.format(bonusTemplate, getModifText(selectedModif), bonus));
            ImageView bonusRemove = FragmentUtil.copyExampleImageFragment(bonusRemoveExample);
            final LinearLayout layout = new LinearLayout(getContext());
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
            ((LinearLayout)getView().findViewById(R.id.sheet_modifs_bonuses)).addView(layout);
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
        void onModif();
    }
}

