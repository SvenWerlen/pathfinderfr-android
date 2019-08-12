package org.pathfinderfr.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.TraitFactory;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.StringWithTag;
import org.pathfinderfr.app.util.TraitFilter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterTraitFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterTraitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterTraitFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_FILTER = "filter";

    private TraitFilter filter;
    private long selectedRace;
    private String selectedType;

    private OnFragmentInteractionListener mListener;

    public FilterTraitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterSpellFragment.
     */
    public static FilterTraitFragment newInstance() {
        FilterTraitFragment fragment = new FilterTraitFragment();
        return fragment;
    }

    public void setFilter(TraitFilter filter) {
        this.filter = filter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedRace = -1L;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_trait_filter, container, false);
        CheckBox checkRace = (CheckBox)rootView.findViewById(R.id.traitRace);
        CheckBox checkType = (CheckBox)rootView.findViewById(R.id.traitCharacter);

        checkRace.setOnClickListener(this);
        checkType.setOnClickListener(this);

        rootView.findViewById(R.id.applyButton).setOnClickListener(this);
        rootView.findViewById(R.id.cancelButton).setOnClickListener(this);

        DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());
        String[] sources = PreferenceUtil.getSources(rootView.getContext());
        List<DBEntity> races = dbHelper.getAllEntities(RaceFactory.getInstance(), sources);

        int selRaceIdx = 0;
        int selTypeIdx = 0;

        // race list
        AppCompatSpinner raceSpinner = rootView.findViewById(R.id.trait_race_spinner);
        List<StringWithTag> listRaces = new ArrayList<>();
        listRaces.add(new StringWithTag(getResources().getString(R.string.trait_filter_character_all), TraitFilter.FILTER_RACE_SHOW_ALL));
        int idx = 1;

        for(DBEntity r : races) {
            listRaces.add(new StringWithTag(r.getName(), r.getId()));
            if(filter != null && filter.getRace() != null && filter.getRace() == r.getId()) {
                selRaceIdx = idx;
            }
            idx++;
        }

        ArrayAdapter<StringWithTag> dataAdapterWeapons = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, listRaces);
        dataAdapterWeapons.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceSpinner.setAdapter(dataAdapterWeapons);
        raceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                selectedRace = (Long)s.getTag();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRace = TraitFilter.FILTER_RACE_SHOW_ALL;
            }
        });

        // character type list
        AppCompatSpinner typeSpinner = rootView.findViewById(R.id.trait_character_spinner);
        List<StringWithTag> listTypes = new ArrayList<>();
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_all), 0));
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_combat), 0));
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_faith), 0));
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_magic), 0));
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_social), 0));
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_race), 0));
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_region), 0));
        listTypes.add(new StringWithTag(getResources().getString(R.string.trait_filter_type_religion), 0));

        idx = 0;
        for(StringWithTag swt : listTypes) {
            if(filter != null && swt.getString().equals(filter.getType())) {
                selTypeIdx = idx;
            }
            idx++;
        }

        ArrayAdapter<StringWithTag> dataAdapterTypes = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, listTypes);
        dataAdapterTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(dataAdapterTypes);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    selectedType = null; // i.e. all
                } else {
                    StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                    selectedType = s.getString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedType = null; // i.e. all
            }
        });

        if(filter != null ) {
            if(filter.getRace() == TraitFilter.FILTER_RACE_HIDE_ALL) {
                checkRace.setChecked(false);
            } else if(filter.getRace() != TraitFilter.FILTER_RACE_SHOW_ALL) {
                raceSpinner.setSelection(selRaceIdx);
            }
            if(TraitFilter.FILTER_TYPE_HIDE_ALL.equals(filter.getType())) {
                checkType.setChecked(false);
            } else if(filter.getType() != null) {
                typeSpinner.setSelection(selTypeIdx);
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
                CheckBox checkRace = (CheckBox)getView().findViewById(R.id.traitRace);
                CheckBox checkCharacter = (CheckBox)getView().findViewById(R.id.traitCharacter);

                if(!checkRace.isChecked()) {
                    filter.setRace(TraitFilter.FILTER_RACE_HIDE_ALL);
                } else {
                    filter.setRace(selectedRace);
                }
                if(!checkCharacter.isChecked()) {
                    filter.setType(TraitFilter.FILTER_TYPE_HIDE_ALL);
                } else {
                    filter.setType(selectedType);
                }

                dismiss();
                mListener.onApplyFilter(filter);
                System.out.println(filter.generatePreferences());
            }
            return;
        }

        // cancel button was pressed!
        if(v.getId() == R.id.cancelButton) {
            dismiss();
            return;
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
        void onApplyFilter(TraitFilter filter);
    }
}
