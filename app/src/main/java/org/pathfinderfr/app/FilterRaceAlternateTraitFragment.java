package org.pathfinderfr.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceAlternateTrait;
import org.pathfinderfr.app.database.entity.RaceAlternateTraitFactory;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.RaceAlternateTraitFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterRaceAlternateTraitFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterRaceAlternateTraitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterRaceAlternateTraitFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_FILTER = "filter";

    private List<CheckBox> cbRace;

    private RaceAlternateTraitFilter filter;

    private OnFragmentInteractionListener mListener;

    public FilterRaceAlternateTraitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterSpellFragment.
     */
    public static FilterRaceAlternateTraitFragment newInstance() {
        FilterRaceAlternateTraitFragment fragment = new FilterRaceAlternateTraitFragment();
        return fragment;
    }

    public void setFilter(RaceAlternateTraitFilter filter) {
        this.filter = filter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cbRace = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_racealternatetrait_filter, container, false);
        FlowLayout layoutRace = (FlowLayout) rootView.findViewById(R.id.flowRace);
        CheckBox checkRace = (CheckBox)rootView.findViewById(R.id.raceAll);

        rootView.findViewById(R.id.applyButton).setOnClickListener(this);
        rootView.findViewById(R.id.cancelButton).setOnClickListener(this);

        checkRace.setOnClickListener(this);

        DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());
        String[] sources = PreferenceUtil.getSources(rootView.getContext());
        List<DBEntity> traits = dbHelper.getAllEntities(RaceAlternateTraitFactory.getInstance(), sources);
        List<DBEntity> races = dbHelper.getAllEntities(RaceFactory.getInstance(), sources);

        Set<Long> traitIds = new HashSet<>();
        for(DBEntity t : traits) {
            traitIds.add(((RaceAlternateTrait)t).getRace().getId());
        }

        if(filter != null) {
            checkRace.setChecked(!filter.hasFilterRace());
            for (DBEntity r : races) {
                if (traitIds.contains(r.getId())) {
                    CheckBox cb = new CheckBox(getActivity());
                    Long raceId = ((Race) r).getId();
                    cb.setText(((Race) r).getName());
                    cb.setTag(r.getId());
                    cb.setLayoutParams(checkRace.getLayoutParams());
                    cb.setOnClickListener(this);
                    cb.setEnabled(filter.hasFilterRace());
                    cb.setChecked(filter.isFilterRaceEnabled(raceId));
                    layoutRace.addView(cb);
                    cbRace.add(cb);
                }
            }
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        // apply button was pressed!
        if(v.getId() == R.id.applyButton) {
            if (mListener != null && filter != null) {
                // fill filters
                filter.clearFilters();
                for(CheckBox c : cbRace) {
                    if(c.isChecked()) {
                        filter.addFilterRace((long)c.getTag());
                    }
                }
                dismiss();
                mListener.onApplyFilter(filter);
            }
            return;
        }

        // cancel button was pressed!
        if(v.getId() == R.id.cancelButton) {
            dismiss();
            return;
        }

        // checkboxes only
        if(!(v instanceof CheckBox)) {
            return;
        }

        CheckBox cb = (CheckBox) v;
        boolean status = cb.isChecked();

        if(cb.getId() == R.id.raceAll) {
            for(CheckBox c : cbRace) {
                c.setChecked(false);
                c.setEnabled(!status);
            }
        }
        else {
            ((CheckBox)getView().findViewById(R.id.raceAll)).setChecked(false);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onApplyFilter(RaceAlternateTraitFilter filter);
    }
}
