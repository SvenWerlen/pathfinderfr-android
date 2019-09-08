package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.util.FragmentUtil;

import java.util.ArrayList;
import java.util.List;

public class FragmentRankPicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_RANK_SKILLID   = "skill_id";
    public static final String ARG_RANK_SKILLNAME = "skill_name";
    public static final String ARG_RANK           = "rank";
    public static final String ARG_RANK_MAX       = "rank_max";


    private FragmentRankPicker.OnFragmentInteractionListener mListener;

    private Long skillId;     // selected skill (id)
    private String skillName; // selected skill (name)
    private int rank;        // current ranks
    private int maxRank;     // max ranks (corresponds to character level)

    private TextView selectedRank; // TextView (level) currently selected


    public FragmentRankPicker() {
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
    public static FragmentRankPicker newInstance(OnFragmentInteractionListener listener) {
        FragmentRankPicker fragment = new FragmentRankPicker();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rank = 0;

        if (getArguments() != null && getArguments().containsKey(ARG_RANK_SKILLID)) {
            skillId = getArguments().getLong(ARG_RANK_SKILLID);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_RANK_SKILLNAME)) {
            skillName = getArguments().getString(ARG_RANK_SKILLNAME);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_RANK)) {
            rank = getArguments().getInt(ARG_RANK);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_RANK_MAX)) {
            maxRank = getArguments().getInt(ARG_RANK_MAX);
        }
    }

    /**
     * Updates the fragment when rank is selected
     */
    private void updateChosenRank(int rank, View rootView) {

        this.rank = rank;

        if(selectedRank != null) {
            TextView example = rootView.findViewById(R.id.rank_predefined_example);
            selectedRank.setBackground(example.getBackground());
            selectedRank.setTextColor(example.getTextColors());
            selectedRank = null;
        }

        TextView predefined = rootView.findViewWithTag("rank" + this.rank);
        Log.d(FragmentRankPicker.class.getSimpleName(), "Predefined " + this.rank + " " + (predefined == null ? "not found" : "found"));
        if(predefined != null) {
            predefined.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            predefined.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
            selectedRank = predefined;
        }

        Button okButton = rootView.findViewById(R.id.rank_ok);
        okButton.setText(rootView.getResources().getString(R.string.sheet_skills_rank) + " " + this.rank);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_rankpicker, container, false);

        // Update skill name
        TextView skillNameTv = rootView.findViewById(R.id.choose_skill_name);
        skillNameTv.setText(skillName);

        List<TextView> predefedTv = new ArrayList<>();

        // Prepare ranks
        FlowLayout rankSelector = rootView.findViewById(R.id.skills_rank_layout);
        TextView example = rootView.findViewById(R.id.rank_predefined_example);
        example.setVisibility(View.GONE);
        for(int i = 0; i<=maxRank; i++) {
            TextView tv = FragmentUtil.copyExampleTextFragment(example);
            tv.setText(String.valueOf(i));
            tv.setTag("rank" + i);
            tv.setOnClickListener(this);
            rankSelector.addView(tv);
            predefedTv.add(tv);
        }

        rootView.findViewById(R.id.rank_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.rank_ok).setOnClickListener(this);

        updateChosenRank(rank, rootView);

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
        if(v.getId() == R.id.rank_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.rank_ok) {
            if(mListener != null) {
                mListener.onRanksSelected(skillId, rank);
            }
            dismiss();
            return;
        } else if(v instanceof TextView && v.getTag() != null && v.getTag().toString().startsWith("rank")) {
            int rank = Integer.valueOf(v.getTag().toString().substring("rank".length()));
            updateChosenRank(rank, getView());
        }
    }

    public interface OnFragmentInteractionListener {
        void onRanksSelected(long skillId, int rank);
    }
}

