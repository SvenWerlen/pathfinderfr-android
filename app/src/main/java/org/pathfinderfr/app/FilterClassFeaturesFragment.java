package org.pathfinderfr.app;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassArchetypesFactory;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.util.ClassFeatureFilter;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.StringWithTag;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterClassFeaturesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterClassFeaturesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterClassFeaturesFragment extends DialogFragment implements View.OnClickListener {

    private ClassFeatureFilter filter;
    private long selectedClass;
    private long selectedArchetype;
    List<ClassArchetype> archetypes;

    private OnFragmentInteractionListener mListener;

    public FilterClassFeaturesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterSpellFragment.
     */
    public static FilterClassFeaturesFragment newInstance() {
        FilterClassFeaturesFragment fragment = new FilterClassFeaturesFragment();
        return fragment;
    }

    public void setFilter(ClassFeatureFilter filter) {
        this.filter = filter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_classfeatures_filter, container, false);

        rootView.findViewById(R.id.applyButton).setOnClickListener(this);
        rootView.findViewById(R.id.cancelButton).setOnClickListener(this);

        DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());
        String[] sources = PreferenceUtil.getSources(rootView.getContext());
        List<DBEntity> classes = dbHelper.getAllEntities(ClassFactory.getInstance(), sources);
        List<DBEntity> archs = dbHelper.getAllEntities(ClassArchetypesFactory.getInstance(), sources);
        archetypes = new ArrayList<>();
        for(DBEntity e : archs) {
            archetypes.add((ClassArchetype)e);
        }

        int selClassIdx = 0;

        final AppCompatSpinner classSpinner = rootView.findViewById(R.id.features_class_spinner);
        final AppCompatSpinner archSpinner = rootView.findViewById(R.id.features_archetype_spinner);
        List<StringWithTag> listClasses = new ArrayList<>();
        List<StringWithTag> listArchs = new ArrayList<>();

        // class list
        listClasses.add(new StringWithTag(getResources().getString(R.string.classfeatures_filter_all), ClassFeatureFilter.FILTER_CLASS_SHOW_ALL));
        int idx = 1;

        for(DBEntity cl : classes) {
            listClasses.add(new StringWithTag(cl.getName(), cl.getId()));
            if(filter != null && filter.getFilterClass() == cl.getId()) {
                selClassIdx = idx;
            }
            idx++;
        }

        ArrayAdapter<StringWithTag> dataAdapterClasses = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, listClasses);
        dataAdapterClasses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(dataAdapterClasses);
        classSpinner.setSelection(selClassIdx);
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                if(selectedClass == (Long)s.getTag()) {
                    return;
                }
                selectedClass = (Long)s.getTag();
                selectedArchetype = ClassFeatureFilter.FILTER_ARCH_BASE;
                archSpinner.setEnabled(position > 0);
                updateArchList(getView());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedClass = ClassFeatureFilter.FILTER_CLASS_SHOW_ALL;
                selectedArchetype = ClassFeatureFilter.FILTER_ARCH_BASE;
                archSpinner.setEnabled(false);
                updateArchList(getView());
            }
        });

        ArrayAdapter<StringWithTag> dataAdapterArchs = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, listArchs);
        dataAdapterArchs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        archSpinner.setAdapter(dataAdapterArchs);
        archSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                selectedArchetype = (Long)s.getTag();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedArchetype = ClassFeatureFilter.FILTER_ARCH_BASE;
            }
        });

        if(filter != null ) {
            selectedClass = filter.getFilterClass();
            selectedArchetype = filter.getFilterArchetype();
            if(filter.getFilterClass() == ClassFeatureFilter.FILTER_CLASS_SHOW_ALL) {
                archSpinner.setEnabled(false);
            } else {
                updateArchList(rootView);
            }
        }

        return rootView;
    }

    private void updateArchList(View view) {
        // archetypes list
        List<StringWithTag> listArchs = new ArrayList<>();
        listArchs.clear();
        listArchs.add(new StringWithTag(getResources().getString(R.string.classfeatures_filter_base), ClassFeatureFilter.FILTER_ARCH_BASE));

        final AppCompatSpinner archSpinner = view.findViewById(R.id.features_archetype_spinner);
        int idx = 1;
        int selected = 0;
        for(ClassArchetype a : archetypes) {
            if(selectedClass == ClassFeatureFilter.FILTER_CLASS_SHOW_ALL) {
                break;
            } else if(a.getClass_() == null || selectedClass != a.getClass_().getId()) {
                continue;
            }
            listArchs.add(new StringWithTag(a.getName(), a.getId()));
            if(selectedArchetype == a.getId()) {
                selected = idx;
            }
            idx++;
        }

        ArrayAdapter<StringWithTag> dataAdapterArchs = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, listArchs);
        dataAdapterArchs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        archSpinner.setAdapter(dataAdapterArchs);

        if(selected > 0) {
            archSpinner.setSelection(selected);
        }
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
                filter.setFilterClass(selectedClass);
                if(filter.getFilterClass() != ClassFeatureFilter.FILTER_CLASS_SHOW_ALL) {
                    filter.setFilterArchetype(selectedArchetype);
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
        void onApplyFilter(ClassFeatureFilter filter);
    }
}
