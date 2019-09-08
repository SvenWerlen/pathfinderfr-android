package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.util.CharacterUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.InputFilterMinMax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FragmentAbilityCalc extends DialogFragment implements View.OnClickListener, TextWatcher {

    private FragmentAbilityCalc.OnFragmentInteractionListener mListener;

    public static final String ARG_RACE_ID = "race_id";
    public static final String ARG_ABILITIES = "abilities";
    public static final String ARG_RACES = "races";

    private static final int ABILITY_MIN = 7;
    private static final int ABILITY_MAX = 18;

    private long raceId;

    private int ability_str = 10;
    private int ability_dex = 10;
    private int ability_con = 10;
    private int ability_int = 10;
    private int ability_wis = 10;
    private int ability_cha = 10;

    private int race_str = 0;
    private int race_dex = 0;
    private int race_con = 0;
    private int race_int = 0;
    private int race_wis = 0;
    private int race_cha = 0;

    public FragmentAbilityCalc() {
        // Required empty public constructor
    }

    private static int getCost(int value) {
        switch(value) {
            case 7: return -4;
            case 8: return -2;
            case 9: return -1;
            case 10: return 0;
            case 11: return 1;
            case 12: return 2;
            case 13: return 3;
            case 14: return 5;
            case 15: return 7;
            case 16: return 10;
            case 17: return 13;
            case 18: return 17;
            default: new IllegalArgumentException("Abilities < 7 or > 18 not supported");
        }
        return 0;
    }

    private static String getCostDelta(int value, int newValue) {
        return String.format("%+d", (getCost(newValue) - getCost(value)));
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
    public static FragmentAbilityCalc newInstance(OnFragmentInteractionListener listener) {
        FragmentAbilityCalc fragment = new FragmentAbilityCalc();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_RACE_ID)) {
            raceId = getArguments().getLong(ARG_RACE_ID);
        } else {
            raceId = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_abilities_calc, container, false);

        rootView.findViewById(R.id.sheet_calc_ok).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_cancel).setOnClickListener(this);

        rootView.findViewById(R.id.ability_str_minus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_str_plus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_dex_minus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_dex_plus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_con_minus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_con_plus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_int_minus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_int_plus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_wis_minus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_wis_plus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_cha_minus).setOnClickListener(this);
        rootView.findViewById(R.id.ability_cha_plus).setOnClickListener(this);

        EditText et = (EditText) rootView.findViewById(R.id.ability_str_modif);
        et.setFilters(new InputFilter[]{ new InputFilterMinMax(-4, 4)});
        et.setSelectAllOnFocus(true);
        et.addTextChangedListener(this);
        et = (EditText) rootView.findViewById(R.id.ability_dex_modif);
        et.setFilters(new InputFilter[]{ new InputFilterMinMax(-4, 4)});
        et.setSelectAllOnFocus(true);
        et.addTextChangedListener(this);
        et = (EditText) rootView.findViewById(R.id.ability_con_modif);
        et.setFilters(new InputFilter[]{ new InputFilterMinMax(-4, 4)});
        et.setSelectAllOnFocus(true);
        et.addTextChangedListener(this);
        et = (EditText) rootView.findViewById(R.id.ability_int_modif);
        et.setFilters(new InputFilter[]{ new InputFilterMinMax(-4, 4)});
        et.setSelectAllOnFocus(true);
        et.addTextChangedListener(this);
        et = (EditText) rootView.findViewById(R.id.ability_wis_modif);
        et.setFilters(new InputFilter[]{ new InputFilterMinMax(-4, 4)});
        et.setSelectAllOnFocus(true);
        et.addTextChangedListener(this);
        et = (EditText) rootView.findViewById(R.id.ability_cha_modif);
        et.setFilters(new InputFilter[]{ new InputFilterMinMax(-4, 4)});
        et.setSelectAllOnFocus(true);
        et.addTextChangedListener(this);

        if(raceId > 0) {
            Race race = (Race) DBHelper.getInstance(rootView.getContext()).fetchEntity(raceId, RaceFactory.getInstance());
            if(race != null && race.getAbilitiesTrait() != null) {
                TextView title = (TextView) rootView.findViewById(R.id.ability_calc_race_label);
                title.setText(String.format("%s: %s", title.getText().toString(), race.getName()));
                ((TextView) rootView.findViewById(R.id.ability_calc_race_descr)).setText(race.getAbilitiesTrait().getDescription());
            }
        } else {
            rootView.findViewById(R.id.ability_calc_race_label).setVisibility(View.GONE);
            rootView.findViewById(R.id.ability_calc_race_descr).setVisibility(View.GONE);
        }

        rootView.findViewById(R.id.sheet_calc_quick_contact_simple).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_contact_heroic).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_range_simple).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_range_heroic).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_magic1_simple).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_magic1_heroic).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_magic2_simple).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_magic2_heroic).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_magic3_simple).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_magic3_heroic).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_skills_simple).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_calc_quick_skills_heroic).setOnClickListener(this);

        // restore values that were selected
        if(savedInstanceState != null) {
            List<Integer> abilities = savedInstanceState.getIntegerArrayList(ARG_ABILITIES);
            if(abilities != null && abilities.size() == 6) {
                ability_str = abilities.get(0);
                ability_dex = abilities.get(1);
                ability_con = abilities.get(2);
                ability_int = abilities.get(3);
                ability_wis = abilities.get(4);
                ability_cha = abilities.get(5);
            }
            List<Integer> races = savedInstanceState.getIntegerArrayList(ARG_RACES);
            if(abilities != null && abilities.size() == 6) {
                ability_str = abilities.get(0);
                ability_dex = abilities.get(1);
                ability_con = abilities.get(2);
                ability_int = abilities.get(3);
                ability_wis = abilities.get(4);
                ability_cha = abilities.get(5);
            }
            updateValueAndCost(rootView);
        }

        // fat fingers
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        int base = 32;
        float scale = 1f;
        try {
            scale = (Integer.parseInt(preferences.getString(MainActivity.PREF_FATFINGERS, "0"))/100f);
        } catch(NumberFormatException nfe) {}
        if(scale > 1) {
            int minHeight = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, base * scale, rootView.getResources().getDisplayMetrics());

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_str), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_str_minus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_str_base), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_str_plus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_str_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_str_modif), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_dex), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_dex_minus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_dex_base), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_dex_plus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_dex_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_dex_modif), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_con), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_con_minus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_con_base), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_con_plus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_con_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_con_modif), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_int), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_int_minus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_int_base), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_int_plus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_int_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_int_modif), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_wis), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_wis_minus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_wis_base), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_wis_plus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_wis_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_wis_modif), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_cha), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_cha_minus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_cha_base), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_cha_plus), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_cha_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.ability_cha_modif), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_contact_simple), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_contact_heroic), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_range_simple), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_range_heroic), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_magic1_simple), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_magic1_heroic), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_magic2_simple), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_magic2_heroic), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_magic3_simple), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_magic3_heroic), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_skills_simple), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.sheet_calc_quick_skills_heroic), 0, scale);

            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_cp), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_cp_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_sp), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_sp_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_gp), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_gp_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_pp), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) rootView.findViewById(R.id.money_pp_value), minHeight, scale);

        }

        return rootView;
    }

    private static void updateMinButton(int ability_val, TextView tv) {
        if(ability_val == ABILITY_MIN) {
            tv.setVisibility(View.INVISIBLE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(getCostDelta(ability_val,ability_val-1));
        }
    }

    private static void updateMaxButton(int ability_val, TextView tv) {
        if(ability_val == ABILITY_MAX) {
            tv.setVisibility(View.INVISIBLE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(getCostDelta(ability_val,ability_val+1));
        }
    }

    private void updateValueAndCost(View view) {
        // update base value
        ((TextView)view.findViewById(R.id.ability_str_base)).setText(String.valueOf(ability_str));
        ((TextView)view.findViewById(R.id.ability_dex_base)).setText(String.valueOf(ability_dex));
        ((TextView)view.findViewById(R.id.ability_con_base)).setText(String.valueOf(ability_con));
        ((TextView)view.findViewById(R.id.ability_int_base)).setText(String.valueOf(ability_int));
        ((TextView)view.findViewById(R.id.ability_wis_base)).setText(String.valueOf(ability_wis));
        ((TextView)view.findViewById(R.id.ability_cha_base)).setText(String.valueOf(ability_cha));
        // update minus/plus buttons
        updateMinButton(ability_str, (TextView)view.findViewById(R.id.ability_str_minus));
        updateMaxButton(ability_str, (TextView)view.findViewById(R.id.ability_str_plus));
        updateMinButton(ability_dex, (TextView)view.findViewById(R.id.ability_dex_minus));
        updateMaxButton(ability_dex, (TextView)view.findViewById(R.id.ability_dex_plus));
        updateMinButton(ability_con, (TextView)view.findViewById(R.id.ability_con_minus));
        updateMaxButton(ability_con, (TextView)view.findViewById(R.id.ability_con_plus));
        updateMinButton(ability_int, (TextView)view.findViewById(R.id.ability_int_minus));
        updateMaxButton(ability_int, (TextView)view.findViewById(R.id.ability_int_plus));
        updateMinButton(ability_wis, (TextView)view.findViewById(R.id.ability_wis_minus));
        updateMaxButton(ability_wis, (TextView)view.findViewById(R.id.ability_wis_plus));
        updateMinButton(ability_cha, (TextView)view.findViewById(R.id.ability_cha_minus));
        updateMaxButton(ability_cha, (TextView)view.findViewById(R.id.ability_cha_plus));
        // update value
        ((TextView)view.findViewById(R.id.ability_str_value)).setText(String.format("%d (%+d)", ability_str + race_str, CharacterUtil.getAbilityBonus(ability_str + race_str)));
        ((TextView)view.findViewById(R.id.ability_dex_value)).setText(String.format("%d (%+d)", ability_dex + race_dex, CharacterUtil.getAbilityBonus(ability_dex + race_dex)));
        ((TextView)view.findViewById(R.id.ability_con_value)).setText(String.format("%d (%+d)", ability_con + race_con, CharacterUtil.getAbilityBonus(ability_con + race_con)));
        ((TextView)view.findViewById(R.id.ability_int_value)).setText(String.format("%d (%+d)", ability_int + race_int, CharacterUtil.getAbilityBonus(ability_int + race_int)));
        ((TextView)view.findViewById(R.id.ability_wis_value)).setText(String.format("%d (%+d)", ability_wis + race_wis, CharacterUtil.getAbilityBonus(ability_wis + race_wis)));
        ((TextView)view.findViewById(R.id.ability_cha_value)).setText(String.format("%d (%+d)", ability_cha + race_cha, CharacterUtil.getAbilityBonus(ability_cha + race_cha)));

        // update total cost
        int totalCost = getCost(ability_str) + getCost(ability_dex) + getCost(ability_con) + getCost(ability_int) + getCost(ability_wis) + getCost(ability_cha);
        ((TextView)view.findViewById(R.id.ability_calc_cost)).setText(String.valueOf(totalCost));
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

    private void initiate(int str, int dex, int con, int intel, int wis, int cha) {
        ability_str = str;
        ability_dex = dex;
        ability_con = con;
        ability_int = intel;
        ability_wis = wis;
        ability_cha = cha;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.sheet_calc_cancel) {
            dismiss();
            return;
        } else if(v.getId() == R.id.sheet_calc_ok) {
            if(mListener != null) {
                mListener.onAbilityValueChosen(
                        ability_str, ability_dex, ability_con, ability_int, ability_wis, ability_cha,
                        race_str, race_dex, race_con, race_int, race_wis, race_cha);
            }
            dismiss();
            return;
        }

        if(v.getId() == R.id.ability_str_minus) {
            ability_str = Math.max(ABILITY_MIN, ability_str-1);
        } else if(v.getId() == R.id.ability_str_plus) {
            ability_str = Math.min(ABILITY_MAX, ability_str+1);
        } else if(v.getId() == R.id.ability_dex_minus) {
            ability_dex = Math.max(ABILITY_MIN, ability_dex-1);
        } else if(v.getId() == R.id.ability_dex_plus) {
            ability_dex = Math.min(ABILITY_MAX, ability_dex+1);
        } else if(v.getId() == R.id.ability_con_minus) {
            ability_con = Math.max(ABILITY_MIN, ability_con-1);
        } else if(v.getId() == R.id.ability_con_plus) {
            ability_con = Math.min(ABILITY_MAX, ability_con+1);
        } else if(v.getId() == R.id.ability_int_minus) {
            ability_int = Math.max(ABILITY_MIN, ability_int-1);
        } else if(v.getId() == R.id.ability_int_plus) {
            ability_int = Math.min(ABILITY_MAX, ability_int+1);
        } else if(v.getId() == R.id.ability_wis_minus) {
            ability_wis = Math.max(ABILITY_MIN, ability_wis-1);
        } else if(v.getId() == R.id.ability_wis_plus) {
            ability_wis = Math.min(ABILITY_MAX, ability_wis+1);
        } else if(v.getId() == R.id.ability_cha_minus) {
            ability_cha = Math.max(ABILITY_MIN, ability_cha-1);
        } else if(v.getId() == R.id.ability_cha_plus) {
            ability_cha = Math.min(ABILITY_MAX, ability_cha+1);
        }

        else if(v.getId() == R.id.sheet_calc_quick_contact_simple) {
            initiate(13,11,12,9,10,8);
        } else if(v.getId() == R.id.sheet_calc_quick_contact_heroic) {
            initiate(15,13,14,10,12,8);
        } else if(v.getId() == R.id.sheet_calc_quick_range_simple) {
            initiate(11,13,12,10,9,8);
        } else if(v.getId() == R.id.sheet_calc_quick_range_heroic) {
            initiate(13,15,14,12,10,8);
        } else if(v.getId() == R.id.sheet_calc_quick_magic1_simple) {
            initiate(10,8,12,9,13,11);
        } else if(v.getId() == R.id.sheet_calc_quick_magic1_heroic) {
            initiate(12,8,14,10,15,13);
        } else if(v.getId() == R.id.sheet_calc_quick_magic2_simple) {
            initiate(8,12,10,13,9,11);
        } else if(v.getId() == R.id.sheet_calc_quick_magic2_heroic) {
            initiate(8,14,12,15,10,13);
        } else if(v.getId() == R.id.sheet_calc_quick_magic3_simple) {
            initiate(8,12,10,11,9,13);
        } else if(v.getId() == R.id.sheet_calc_quick_magic3_heroic) {
            initiate(8,14,12,13,10,15);
        } else if(v.getId() == R.id.sheet_calc_quick_skills_simple) {
            initiate(10,12,11,13,8,9);
        } else if(v.getId() == R.id.sheet_calc_quick_skills_heroic) {
            initiate(12,14,13,15,8,10);
        }
        updateValueAndCost(getView());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            race_str = Integer.parseInt(((EditText) getView().findViewById(R.id.ability_str_modif)).getText().toString());
            race_dex = Integer.parseInt(((EditText) getView().findViewById(R.id.ability_dex_modif)).getText().toString());
            race_con = Integer.parseInt(((EditText) getView().findViewById(R.id.ability_con_modif)).getText().toString());
            race_int = Integer.parseInt(((EditText) getView().findViewById(R.id.ability_int_modif)).getText().toString());
            race_wis = Integer.parseInt(((EditText) getView().findViewById(R.id.ability_wis_modif)).getText().toString());
            race_cha = Integer.parseInt(((EditText) getView().findViewById(R.id.ability_cha_modif)).getText().toString());
            updateValueAndCost(getView());
        } catch (NumberFormatException e) {
            Log.e(FragmentAbilityCalc.class.getSimpleName(), "Error parsing value. Should never happen.", e);
        }
    }

    public interface OnFragmentInteractionListener {
        void onAbilityValueChosen(
                int str, int dex, int con, int intel, int wis, int cha,
                int strRace, int dexRace, int conRace, int intRace, int wisRace, int chaRace);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_RACE_ID, raceId);
        outState.putIntegerArrayList(ARG_ABILITIES, new ArrayList<Integer>(Arrays.asList( ability_str, ability_dex, ability_con, ability_int, ability_wis, ability_cha)));
        outState.putIntegerArrayList(ARG_RACES, new ArrayList<Integer>(Arrays.asList( race_str, race_dex, race_con, race_int, race_wis, race_cha)));
    }
}

