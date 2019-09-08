package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.util.FragmentUtil;

import java.util.ArrayList;
import java.util.List;


public class FragmentAbilityPicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_ABILITY_ID = "ability_id";
    public static final String ARG_ABILITY_VALUE = "ability_value";

    private static final int VALUE_PREDEFINED_MIN = 7;
    private static final int VALUE_PREDEFINED_MAX = 18;

    private FragmentAbilityPicker.OnFragmentInteractionListener mListener;

    private int abilityId;
    private int abilityValue;
    private TextView selected;

    public FragmentAbilityPicker() {
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
    public static FragmentAbilityPicker newInstance(OnFragmentInteractionListener listener) {
        FragmentAbilityPicker fragment = new FragmentAbilityPicker();
        fragment.setListener(listener);
        return fragment;
    }

    private void updateChosenValue(View view, int value) {
        Button button = (Button)view.findViewById(R.id.ability_ok);
        if(button != null) {
            button.setText(String.valueOf(value));
        }

        if(selected != null) {
            TextView example = view.findViewById(R.id.ability_predefined_example);
            selected.setBackground(example.getBackground());
            selected.setTextColor(example.getTextColors());
            selected = null;
        }

        if(value >= VALUE_PREDEFINED_MIN && value <= VALUE_PREDEFINED_MAX) {
            TextView predefined = view.findViewWithTag("predefined" + value);
            if(predefined != null) {
                predefined.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                predefined.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
                selected = predefined;
            }
        }

        ((SeekBar)view.findViewById(R.id.ability_seekbar)).setProgress(value);

        abilityValue = value;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ABILITY_ID) && getArguments().containsKey(ARG_ABILITY_VALUE)) {
            abilityId = getArguments().getInt(ARG_ABILITY_ID);
            abilityValue = getArguments().getInt(ARG_ABILITY_VALUE);;
        } else {
            throw new IllegalArgumentException("Fragment not properly initialized!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<TextView> predefedTv = new ArrayList<>();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_abilitypicker, container, false);
        FlowLayout layout = rootView.findViewById(R.id.ability_predefined);
        TextView example = rootView.findViewById(R.id.ability_predefined_example);
        example.setVisibility(View.GONE);
        for(int i = VALUE_PREDEFINED_MIN; i<=VALUE_PREDEFINED_MAX; i++) {
            TextView tv = FragmentUtil.copyExampleTextFragment(example);
            tv.setText(String.valueOf(i));
            tv.setTag("predefined" + i);
            tv.setOnClickListener(this);
            layout.addView(tv);
            predefedTv.add(tv);
        }
        ((SeekBar)rootView.findViewById(R.id.ability_seekbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(fromUser) {
                    updateChosenValue(FragmentAbilityPicker.this.getView(), progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        rootView.findViewById(R.id.ability_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.ability_ok).setOnClickListener(this);
        rootView.findViewById(R.id.ability_calc).setOnClickListener(this);

        // restore value that was selected
        if(savedInstanceState != null) {
            abilityValue = savedInstanceState.getInt(ARG_ABILITY_VALUE, abilityValue);
        }

        // initialize
        updateChosenValue(rootView, abilityValue);

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

            for(TextView tv : predefedTv) {
                FragmentUtil.adaptForFatFingers(tv, minHeight, scale);
            }
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

    @Override
    public void onClick(View v) {
        // apply button was pressed!
        if(v.getId() == R.id.ability_cancel) {
            dismiss();
            return;
        } else if(v.getId() == R.id.ability_ok) {
            if(mListener != null) {
                mListener.onAbilityValueChosen(abilityId, abilityValue);
            }
            dismiss();
            return;
        } else if(v.getId() == R.id.ability_calc) {
            dismiss();
            if(mListener != null) {
                mListener.onAbilityCalcChosen();
            }
            return;
        } else if(v instanceof TextView && v.getTag() != null && v.getTag().toString().startsWith("predefined")) {
            updateChosenValue(getView(), Integer.valueOf(v.getTag().toString().substring("predefined".length())));
        }
    }

    public interface OnFragmentInteractionListener {
        void onAbilityValueChosen(int abilityId, int abilityValue);
        void onAbilityCalcChosen();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // store currently selected value
        outState.putInt(ARG_ABILITY_VALUE, abilityValue);
    }
}

