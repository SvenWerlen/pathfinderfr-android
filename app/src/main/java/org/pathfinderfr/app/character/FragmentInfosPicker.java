package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.entity.Character;

public class FragmentInfosPicker extends DialogFragment implements View.OnClickListener {

    public static int MAX_VALUE = 999;

    public static final String ARG_ALIGN = "arg_align";
    public static final String ARG_SEX   = "arg_sex";

    private int alignment;
    private TextView selectedAlignment;
    private String divinity;
    private String origin;
    private int sizeType;
    private int sex;
    private TextView selectedSex;
    private int age;
    private int height;
    private int weight;
    private String hair;
    private String eyes;
    private String lang;
    private int xp;

    private FragmentInfosPicker.OnFragmentInteractionListener mListener;


    public FragmentInfosPicker() {
        // Required empty public constructor
    }

    public static FragmentInfosPicker newInstance(FragmentInfosPicker.OnFragmentInteractionListener listener, int xp,
                                                  int alignment, String divinity, String origin, int sizeType, int sex, int age, int height, int weight, String hair, String eyes, String lang) {

        FragmentInfosPicker fragment = new FragmentInfosPicker();
        fragment.alignment = alignment;
        fragment.selectedAlignment = null;
        fragment.divinity = divinity;
        fragment.origin = origin;
        fragment.sizeType = sizeType;
        fragment.sex = sex;
        fragment.age = age;
        fragment.height = height;
        fragment.weight = weight;
        fragment.hair = hair;
        fragment.eyes = eyes;
        fragment.lang = lang;
        fragment.xp = xp;
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(FragmentInfosPicker.OnFragmentInteractionListener listener) {
        mListener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_infospicker, container, false);

        // restore value that was selected
        if(savedInstanceState != null) {
            alignment = savedInstanceState.getInt(ARG_ALIGN, alignment);
            sex = savedInstanceState.getInt(ARG_SEX, sex);
        }

        // alignment
        updateChosenAlignment(alignment, rootView);
        for(int i = 1; i<=9; i++) {
            TextView tv = (TextView)rootView.findViewWithTag(String.valueOf(i));
            if(tv != null) {
                tv.setOnClickListener(this);
            } else {
                Log.w(FragmentInfosPicker.class.getSimpleName(), "Alignment " + i + " couldn't be found!");
            }
        }

        // sex
        updateChosenSex(sex, rootView);
        rootView.findViewById(R.id.sheet_other_sex_m).setOnClickListener(this);
        rootView.findViewById(R.id.sheet_other_sex_f).setOnClickListener(this);

        // set value
        ((EditText)rootView.findViewById(R.id.sheet_other_xp)).setText(String.valueOf(xp));
        ((EditText)rootView.findViewById(R.id.sheet_other_divinity)).setText(divinity);
        ((EditText)rootView.findViewById(R.id.sheet_other_origin)).setText(origin);
        ((EditText)rootView.findViewById(R.id.sheet_other_age)).setText(String.valueOf(age));
        ((EditText)rootView.findViewById(R.id.sheet_other_height)).setText(String.valueOf(height));
        ((EditText)rootView.findViewById(R.id.sheet_other_weight)).setText(String.valueOf(weight));
        ((EditText)rootView.findViewById(R.id.sheet_other_hair)).setText(hair);
        ((EditText)rootView.findViewById(R.id.sheet_other_eyes)).setText(eyes);
        ((EditText)rootView.findViewById(R.id.sheet_other_lang)).setText(lang);

        // type de taille
        AppCompatSpinner spinner = rootView.findViewById(R.id.sheet_other_size_spinner);
        spinner.setSelection(sizeType < 0 || sizeType > Character.SIZE_COLO_LONG ? 0 : sizeType);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sizeType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sizeType = 0;
            }
        });

        rootView.findViewById(R.id.cancel).setOnClickListener(this);
        rootView.findViewById(R.id.ok).setOnClickListener(this);

        return rootView;
    }

    /**
     * Updates the fragment when alignment is selected
     */
    private void updateChosenAlignment(int alignment, View view) {

        if(selectedAlignment == view) {
            return;
        }
        this.alignment = alignment;

        if(selectedAlignment != null) {
            selectedAlignment.setBackground(view.getBackground());
            selectedAlignment.setTextColor(((TextView)view).getTextColors());
            selectedAlignment = null;
        }

        TextView predefined = view.findViewWithTag(String.valueOf(this.alignment));
        if(predefined != null) {
            predefined.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
            predefined.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorWhite));
            selectedAlignment = predefined;
        }
    }

    /**
     * Updates the fragment when alignment is selected
     */
    private void updateChosenSex(int sex, View view) {

        if(selectedSex == view) {
            return;
        }
        this.sex = sex;

        if(selectedSex != null) {
            selectedSex.setBackground(view.getBackground());
            selectedSex.setTextColor(((TextView)view).getTextColors());
            selectedSex = null;
        }

        TextView predefined = null;
        if(sex == Character.SEX_M) {
            predefined = view.findViewById(R.id.sheet_other_sex_m);
        } else if(sex == Character.SEX_F) {
            predefined = view.findViewById(R.id.sheet_other_sex_f);
        }
        if(predefined != null) {
            predefined.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
            predefined.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorWhite));
            selectedSex = predefined;
        }
    }

    private static int getValueFromEditText(View view, int id) {
        String text = ((EditText)view.findViewById(id)).getText().toString();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Log.w(FragmentInfosPicker.class.getSimpleName(), String.format("Invalid value '%s' from EditText!", text));
            return 0;
        }
    }

    @Override
    public void onClick(View v) {

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (v.getId() == R.id.cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.ok) {
            xp = Math.max(0, getValueFromEditText(getView(), R.id.sheet_other_xp));
            divinity =  ((TextView)getView().findViewById(R.id.sheet_other_divinity)).getText().toString();
            origin =  ((TextView)getView().findViewById(R.id.sheet_other_origin)).getText().toString();
            age = getValueFromEditText(getView(), R.id.sheet_other_age);
            height = getValueFromEditText(getView(), R.id.sheet_other_height);
            weight = getValueFromEditText(getView(), R.id.sheet_other_weight);
            hair =  ((TextView)getView().findViewById(R.id.sheet_other_hair)).getText().toString();
            eyes =  ((TextView)getView().findViewById(R.id.sheet_other_eyes)).getText().toString();
            lang =  ((TextView)getView().findViewById(R.id.sheet_other_lang)).getText().toString();
            mListener.onSaveInfos(xp, alignment, divinity, origin, sizeType, sex, age, height, weight, hair, eyes, lang);
            dismiss();
            return;
        } else if (v.getTag() != null) {
            if(v.getTag().toString().equals("M")) {
                updateChosenSex(1, v);
            } else if(v.getTag().toString().equals("F")) {
                updateChosenSex(2, v);
            } else {
                try {
                    int alignment = Integer.parseInt(v.getTag().toString());
                    updateChosenAlignment(alignment, v);
                } catch (NumberFormatException e) {
                    Log.w(FragmentInfosPicker.class.getSimpleName(), "Invalid tag " + v.getTag() + " (not a number!)");
                }
            }
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onSaveInfos(int xp, int alignment, String divinity, String origin, int sizeType, int sex,
                         int age, int height, int weight, String hair, String eyes, String lang);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // store currently selected value
        outState.putInt(ARG_ALIGN, alignment);
        outState.putInt(ARG_SEX, sex);
    }
}

