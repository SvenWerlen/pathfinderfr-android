package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.pathfinderfr.R;

public class FragmentSkillMaxRanksPicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_DESC = "argDescr";

    public static int MAX_RANKS = 999;
    private int maxRank;
    private String maxDescr;
    private FragmentSkillMaxRanksPicker.OnFragmentInteractionListener mListener;

    public FragmentSkillMaxRanksPicker() {
        // Required empty public constructor
    }

    public static FragmentSkillMaxRanksPicker newInstance(FragmentSkillMaxRanksPicker.OnFragmentInteractionListener listener, int max, String htmlContent) {
        FragmentSkillMaxRanksPicker fragment = new FragmentSkillMaxRanksPicker();
        max = Math.max(0, max);
        max = Math.min(MAX_RANKS, max);
        fragment.maxRank = max;
        fragment.maxDescr = htmlContent;
        fragment.mListener = listener;
        return fragment;
    }

    public void setListener(FragmentSkillMaxRanksPicker.OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_skillmaxrankspicker, container, false);

        // restore value that was selected
        if(savedInstanceState != null) {
            maxDescr = savedInstanceState.getString(ARG_DESC, "");
        }

        // set value
        EditText edit = rootView.findViewById(R.id.sheet_ranksperlevel_max);
        if(maxRank > 0) {
            edit.setText(String.valueOf(maxRank));
            edit.setSelection(0, edit.getText().length());
        }

        if(maxDescr != null) {
            ((TextView) rootView.findViewById(R.id.sheet_ranksperlevel_descr)).setText(Html.fromHtml(maxDescr));
        }

        rootView.findViewById(R.id.max_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.max_ok).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (v.getId() == R.id.max_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.max_ok) {
            EditText edit = getView().findViewById(R.id.sheet_ranksperlevel_max);
            int max = 0;
            try {
                max = Integer.parseInt(edit.getText().toString());
                max = Math.max(0, max);         // min
                max = Math.min(MAX_RANKS, max); // max
            } catch (NumberFormatException e) {}

            if(mListener != null) {
                mListener.onSaveMaxRanksPerLevel(max);
            }
            dismiss();
            return;
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(maxDescr != null) {
            outState.putString(ARG_DESC, maxDescr);
        }
    }

    public interface OnFragmentInteractionListener {
        void onSaveMaxRanksPerLevel(int max);
    }
}

