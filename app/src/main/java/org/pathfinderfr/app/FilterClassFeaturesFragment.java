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
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.util.ClassFeatureFilter;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterClassFeaturesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterClassFeaturesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterClassFeaturesFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_FILTER = "filter";

    private static final Integer TAG_CLASS = 1;
    private static final Integer TAG_MAXLEVEL = 2;

    private List<CheckBox> cbClass;
    private List<CheckBox> cbMaxLevel;

    private ClassFeatureFilter filter;

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

        cbClass = new ArrayList<>();
        cbMaxLevel = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_classfeatures_filter, container, false);
        FlowLayout layoutClass = (FlowLayout) rootView.findViewById(R.id.flowClass);
        FlowLayout layoutMaxLevel = (FlowLayout) rootView.findViewById(R.id.flowMaxLevel);
        CheckBox checkClass = (CheckBox)rootView.findViewById(R.id.classAll);

        rootView.findViewById(R.id.applyButton).setOnClickListener(this);
        rootView.findViewById(R.id.cancelButton).setOnClickListener(this);

        checkClass.setOnClickListener(this);

        DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());
        String[] sources = PreferenceUtil.getSources(rootView.getContext());
        List<DBEntity> classFeatures = dbHelper.getAllEntities(ClassFeatureFactory.getInstance(), sources);
        List<DBEntity> classes = dbHelper.getAllEntities(ClassFactory.getInstance(), sources);

        Set<Long> clIds = new HashSet<>();
        for(DBEntity clf : classFeatures) {
            clIds.add(((ClassFeature)clf).getClass_().getId());
        }

        if(filter != null) {
            checkClass.setChecked(!filter.hasFilterClass());
            for (DBEntity cl : classes) {
                if (clIds.contains(cl.getId())) {
                    CheckBox cb = new CheckBox(getActivity());
                    Long classId = ((Class) cl).getId();
                    cb.setText(((Class) cl).getShortName());
                    cb.setTag(new Pair<Integer, Long>(TAG_CLASS, cl.getId()));
                    cb.setLayoutParams(checkClass.getLayoutParams());
                    cb.setOnClickListener(this);
                    cb.setEnabled(filter.hasFilterClass());
                    cb.setChecked(filter.isFilterClassEnabled(classId));
                    layoutClass.addView(cb);
                    cbClass.add(cb);
                }
            }
            for (int i=1; i<=20; i++) {
                CheckBox cb = new CheckBox(getActivity());
                cb.setText(String.valueOf(i));
                cb.setTag(TAG_MAXLEVEL);
                cb.setLayoutParams(checkClass.getLayoutParams());
                cb.setOnClickListener(this);
                cb.setChecked(filter.getFilterMaxLevel() == i);
                layoutMaxLevel.addView(cb);
                cbMaxLevel.add(cb);
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
                for(CheckBox c : cbClass) {
                    if(c.isChecked()) {
                        filter.addFilterClass(((Pair<Integer, Long>)c.getTag()).second);
                    }
                }
                for(CheckBox c : cbMaxLevel) {
                    if(c.isChecked()) {
                        filter.setFilterMaxLevel(Integer.valueOf(c.getText().toString()));
                        break;
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

        if(cb.getId() == R.id.classAll) {
            for(CheckBox c : cbClass) {
                c.setChecked(false);
                c.setEnabled(!status);
            }
        }
        else if(cb.getTag() instanceof Pair && ((Pair<Integer,Long>)cb.getTag()).first == TAG_CLASS) {
            ((CheckBox)getView().findViewById(R.id.classAll)).setChecked(false);
        }
        else if(cb.getTag() == TAG_MAXLEVEL) {
            for(CheckBox c : cbMaxLevel) {
                c.setChecked(v == c ? true : false);
            }
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
