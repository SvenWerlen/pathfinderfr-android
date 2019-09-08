package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;

import org.pathfinderfr.R;

public class FragmentClassFeatureFilter extends DialogFragment implements View.OnClickListener {

    public static final String KEY_CLASSFEATUREFILTER_FAV    = "pref_classfeatfilter_fav";
    public static final String KEY_CLASSFEATUREFILTER_TRAITS = "pref_classfeatfilter_traits";
    public static final String KEY_CLASSFEATUREFILTER_AUTO   = "pref_classfeatfilter_auto";

    private FragmentClassFeatureFilter.OnFragmentInteractionListener mListener;

    public FragmentClassFeatureFilter() {
        // Required empty public constructor
    }

    public static FragmentClassFeatureFilter newInstance(FragmentClassFeatureFilter.OnFragmentInteractionListener listener) {
        FragmentClassFeatureFilter fragment = new FragmentClassFeatureFilter();
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
        View rootView = inflater.inflate(R.layout.fragment_sheet_classfeaturefilters, container, false);

        // Retrieve preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        boolean filterTraits = prefs.getBoolean(KEY_CLASSFEATUREFILTER_TRAITS, true);
        boolean filterAuto = prefs.getBoolean(KEY_CLASSFEATUREFILTER_AUTO, true);
        boolean filterOnlyFav = prefs.getBoolean(KEY_CLASSFEATUREFILTER_FAV, false);

        if(!filterTraits) {
            ((CheckBox)rootView.findViewById(R.id.classfeatures_traits)).setChecked(false);
        }
        if(!filterAuto) {
            ((CheckBox)rootView.findViewById(R.id.classfeatures_auto)).setChecked(false);
        }
        if(filterOnlyFav) {
            ((RadioButton)rootView.findViewById(R.id.classfeatures_filter_favorite_only)).setChecked(true);
        }

        rootView.findViewById(R.id.classfeatures_filter_ok).setOnClickListener(this);
        rootView.findViewById(R.id.classfeatures_filter_cancel).setOnClickListener(this);

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
        if(v.getId() == R.id.classfeatures_filter_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.classfeatures_filter_ok) {
            // store preferences
            boolean filterTraits = ((CheckBox)getView().findViewById(R.id.classfeatures_traits)).isChecked();
            boolean filterAuto = ((CheckBox)getView().findViewById(R.id.classfeatures_auto)).isChecked();
            boolean filterOnlyFav = ((RadioButton)getView().findViewById(R.id.classfeatures_filter_favorite_only)).isChecked();


            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getView().getContext()).edit();
            if(filterTraits) {
                editor.remove(KEY_CLASSFEATUREFILTER_TRAITS);
            } else {
                editor.putBoolean(KEY_CLASSFEATUREFILTER_TRAITS, filterTraits);
            }
            if(filterAuto) {
                editor.remove(KEY_CLASSFEATUREFILTER_AUTO);
            } else {
                editor.putBoolean(KEY_CLASSFEATUREFILTER_AUTO, filterAuto);
            }
            if(filterOnlyFav) {
                editor.putBoolean(KEY_CLASSFEATUREFILTER_FAV, filterOnlyFav);
            } else {
                editor.remove(KEY_CLASSFEATUREFILTER_FAV);
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

