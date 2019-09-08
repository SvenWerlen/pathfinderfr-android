package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import org.pathfinderfr.R;

public class FragmentSkillFilter extends DialogFragment implements View.OnClickListener {

    public static final String KEY_SKILLFILTER_CLASS = "pref_skillfilter_class";
    public static final String KEY_SKILLFILTER_RANK  = "pref_skillfilter_rank";
    public static final String KEY_SKILLFILTER_FAV   = "pref_skillfilter_fav";
    //public static final String KEY_SKILL_SORT        = "pref_skill_sort";
    public static final int SKILL_SORT_BYNAME = 0;
    public static final int SKILL_SORT_BYBONUS = 1;

    private FragmentSkillFilter.OnFragmentInteractionListener mListener;

    public FragmentSkillFilter() {
        // Required empty public constructor
    }

    public static FragmentSkillFilter newInstance(FragmentSkillFilter.OnFragmentInteractionListener listener) {
        FragmentSkillFilter fragment = new FragmentSkillFilter();
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
        View rootView = inflater.inflate(R.layout.fragment_sheet_skillfilters, container, false);

        // Retrieve preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        boolean filterOnlyClass = prefs.getBoolean(KEY_SKILLFILTER_CLASS, false);
        boolean filterOnlyRank = prefs.getBoolean(KEY_SKILLFILTER_RANK, false);
        boolean filterOnlyFav = prefs.getBoolean(KEY_SKILLFILTER_FAV, false);
        //int sort = prefs.getInt(KEY_SKILL_SORT, SKILL_SORT_BYNAME);

        if(filterOnlyClass) {
            ((RadioButton)rootView.findViewById(R.id.skills_filter_class_only)).setChecked(true);
        }
        if(filterOnlyRank) {
            ((RadioButton)rootView.findViewById(R.id.skills_filter_rank_only)).setChecked(true);
        }
        if(filterOnlyFav) {
            ((RadioButton)rootView.findViewById(R.id.skills_filter_favorite_only)).setChecked(true);
        }
        //if(sort == SKILL_SORT_BYBONUS) {
        //    ((RadioButton)rootView.findViewById(R.id.skills_sort_bonus)).setChecked(true);
        //}

        rootView.findViewById(R.id.skills_filter_ok).setOnClickListener(this);
        rootView.findViewById(R.id.skills_filter_cancel).setOnClickListener(this);

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
        if(v.getId() == R.id.skills_filter_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.skills_filter_ok) {
            // store preferences
            boolean filterOnlyClass = ((RadioButton)getView().findViewById(R.id.skills_filter_class_only)).isChecked();
            boolean filterOnlyRank = ((RadioButton)getView().findViewById(R.id.skills_filter_rank_only)).isChecked();
            boolean filterOnlyFav = ((RadioButton)getView().findViewById(R.id.skills_filter_favorite_only)).isChecked();
            //int sort = ((RadioButton)getView().findViewById(R.id.skills_sort_bonus)).isChecked() ? 1 : 0;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getView().getContext()).edit();
            if(filterOnlyClass) {
                editor.putBoolean(KEY_SKILLFILTER_CLASS, filterOnlyClass);
            } else {
                editor.remove(KEY_SKILLFILTER_CLASS);
            }
            if(filterOnlyRank) {
                editor.putBoolean(KEY_SKILLFILTER_RANK, filterOnlyRank);
            } else {
                editor.remove(KEY_SKILLFILTER_RANK);
            }
            if(filterOnlyFav) {
                editor.putBoolean(KEY_SKILLFILTER_FAV, filterOnlyFav);
            } else {
                editor.remove(KEY_SKILLFILTER_FAV);
            }
            //if(sort != SKILL_SORT_BYNAME) {
            //    editor.putInt(KEY_SKILL_SORT, sort);
            //} else {
            //    editor.remove(KEY_SKILL_SORT);
            //}
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

