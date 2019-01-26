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

public class FragmentFeatFilter extends DialogFragment implements View.OnClickListener {

    public static final String KEY_FEATFILTER_TYPE   = "pref_featfilter_type";
    public static final String KEY_FEATFILTER_FAV    = "pref_featfilter_fav";

    public static final int FEATFILTER_TYPE_ALL      = 0;
    public static final int FEATFILTER_TYPE_COMBAT   = 1;
    public static final int FEATFILTER_TYPE_NOCOMBAT = 2;


    private FragmentFeatFilter.OnFragmentInteractionListener mListener;

    public FragmentFeatFilter() {
        // Required empty public constructor
    }

    public static FragmentFeatFilter newInstance(FragmentFeatFilter.OnFragmentInteractionListener listener) {
        FragmentFeatFilter fragment = new FragmentFeatFilter();
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
        View rootView = inflater.inflate(R.layout.fragment_sheet_featfilters, container, false);

        // Retrieve preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        int filterOnlyType = prefs.getInt(KEY_FEATFILTER_TYPE, FEATFILTER_TYPE_ALL);
        boolean filterOnlyFav = prefs.getBoolean(KEY_FEATFILTER_FAV, false);

        if(filterOnlyType == FEATFILTER_TYPE_COMBAT) {
            ((RadioButton)rootView.findViewById(R.id.feats_filter_type_combat)).setChecked(true);
        } else if(filterOnlyType == FEATFILTER_TYPE_NOCOMBAT) {
            ((RadioButton)rootView.findViewById(R.id.feats_filter_type_nocombat)).setChecked(true);
        }
        if(filterOnlyFav) {
            ((RadioButton)rootView.findViewById(R.id.feats_filter_favorite_only)).setChecked(true);
        }

        rootView.findViewById(R.id.feats_filter_ok).setOnClickListener(this);
        rootView.findViewById(R.id.feats_filter_cancel).setOnClickListener(this);

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
        if(v.getId() == R.id.feats_filter_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.feats_filter_ok) {
            // store preferences
            int filterOnlyType = FEATFILTER_TYPE_ALL;
            if(((RadioButton)getView().findViewById(R.id.feats_filter_type_combat)).isChecked()) {
                filterOnlyType = FEATFILTER_TYPE_COMBAT;
            } else if(((RadioButton)getView().findViewById(R.id.feats_filter_type_nocombat)).isChecked()) {
                filterOnlyType = FEATFILTER_TYPE_NOCOMBAT;
            }
            boolean filterOnlyFav = ((RadioButton)getView().findViewById(R.id.feats_filter_favorite_only)).isChecked();

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getView().getContext()).edit();
            if(filterOnlyType > 0) {
                editor.putInt(KEY_FEATFILTER_TYPE, filterOnlyType);
            } else {
                editor.remove(KEY_FEATFILTER_TYPE);
            }
            if(filterOnlyFav) {
                editor.putBoolean(KEY_FEATFILTER_FAV, filterOnlyFav);
            } else {
                editor.remove(KEY_FEATFILTER_FAV);
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

