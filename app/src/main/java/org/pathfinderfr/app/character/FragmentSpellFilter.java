package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import org.pathfinderfr.R;

public class FragmentSpellFilter extends DialogFragment implements View.OnClickListener {

    public static final String KEY_SPELLFILTER_FAV    = "pref_spellfilter_fav";
    public static final String KEY_SPELLFILTER_MODE   = "pref_spellfilter_mode";
    public static int SPELLFILTER_MODE_SCHOOL = 0;
    public static int SPELLFILTER_MODE_LEVEL = 1;


    private FragmentSpellFilter.OnFragmentInteractionListener mListener;

    public FragmentSpellFilter() {
        // Required empty public constructor
    }

    public static FragmentSpellFilter newInstance(FragmentSpellFilter.OnFragmentInteractionListener listener) {
        FragmentSpellFilter fragment = new FragmentSpellFilter();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_spellfilters, container, false);

        // Retrieve preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        boolean filterOnlyFav = prefs.getBoolean(KEY_SPELLFILTER_FAV, false);
        int filterMode = prefs.getInt(KEY_SPELLFILTER_MODE, SPELLFILTER_MODE_SCHOOL);

        if(filterOnlyFav) {
            ((RadioButton)rootView.findViewById(R.id.spells_filter_favorite_only)).setChecked(true);
        }
        if(filterMode != SPELLFILTER_MODE_SCHOOL) {
            ((RadioButton)rootView.findViewById(R.id.spells_filter_mode_level)).setChecked(true);
        }

        rootView.findViewById(R.id.spells_filter_ok).setOnClickListener(this);
        rootView.findViewById(R.id.spells_filter_cancel).setOnClickListener(this);

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
        if(v.getId() == R.id.spells_filter_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.spells_filter_ok) {
            // store preferences
            boolean filterOnlyFav = ((RadioButton)getView().findViewById(R.id.spells_filter_favorite_only)).isChecked();
            int filterMode = SPELLFILTER_MODE_SCHOOL;
            if(((RadioButton)getView().findViewById(R.id.spells_filter_mode_level)).isChecked()) {
                filterMode = SPELLFILTER_MODE_LEVEL;
            }

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getView().getContext()).edit();
            if(filterOnlyFav) {
                editor.putBoolean(KEY_SPELLFILTER_FAV, filterOnlyFav);
            } else {
                editor.remove(KEY_SPELLFILTER_FAV);
            }
            if(filterMode != SPELLFILTER_MODE_SCHOOL) {
                editor.putInt(KEY_SPELLFILTER_MODE, filterMode);
            } else {
                editor.remove(KEY_SPELLFILTER_MODE);
            }
            editor.apply();

            if (mListener != null) {
                mListener.onFilterApplied();
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFilterApplied();
    }
}

