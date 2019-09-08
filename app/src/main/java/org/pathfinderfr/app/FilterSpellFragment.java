package org.pathfinderfr.app;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.SpellFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterSpellFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterSpellFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterSpellFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_FILTER = "filter";

    private static final Integer TAG_SCHOOL = 1;
    private static final Integer TAG_CLASS = 2;
    private static final Integer TAG_LEVEL = 3;

    private SpellFilter filter;
    private List<CheckBox> cbSchool;
    private List<CheckBox> cbClass;
    private List<CheckBox> cbLevel;

    private OnFragmentInteractionListener mListener;

    public FilterSpellFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterSpellFragment.
     */
    public static FilterSpellFragment newInstance(SpellFilter filter) {
        FilterSpellFragment fragment = new FilterSpellFragment();
        fragment.setFilter(filter);
        return fragment;
    }

    public void setFilter(SpellFilter filter) {
        this.filter = filter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cbSchool = new ArrayList<>();
        cbClass = new ArrayList<>();
        cbLevel = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_spell_filter, container, false);
        FlowLayout layoutSchool = (FlowLayout) rootView.findViewById(R.id.flowSchool);
        FlowLayout layoutClass = (FlowLayout) rootView.findViewById(R.id.flowClass);
        FlowLayout layoutLevel = (FlowLayout) rootView.findViewById(R.id.flowLevel);
        ViewGroup.LayoutParams params = rootView.findViewById(R.id.schoolAll).getLayoutParams();

        CheckBox checkSchool = (CheckBox)rootView.findViewById(R.id.schoolAll);
        CheckBox checkClass = (CheckBox)rootView.findViewById(R.id.classAll);
        CheckBox checkLevel = (CheckBox)rootView.findViewById(R.id.levelAll);

        rootView.findViewById(R.id.applyButton).setOnClickListener(this);
        rootView.findViewById(R.id.cancelButton).setOnClickListener(this);

        checkSchool.setOnClickListener(this);
        checkClass.setOnClickListener(this);
        checkLevel.setOnClickListener(this);

        if(filter != null) {
            DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());
            String[] sources = PreferenceUtil.getSources(rootView.getContext());
            List<DBEntity> classes = dbHelper.getAllEntities(ClassFactory.getInstance(), sources);
            Set<Long> classIds = dbHelper.getClassesWithSpells();

            checkSchool.setChecked(!filter.hasFilterSchool());
            for( String s: dbHelper.getSpellSchools()) {
                CheckBox cb = new CheckBox(getActivity());
                cb.setText(s);
                cb.setTag(TAG_SCHOOL);
                cb.setLayoutParams(params);
                cb.setOnClickListener(this);
                cb.setEnabled(filter.hasFilterSchool());
                cb.setChecked(filter.isFilterSchoolEnabled(s));
                layoutSchool.addView(cb);
                cbSchool.add(cb);
            }

            checkClass.setChecked(!filter.hasFilterClass());
            for( DBEntity cl: classes) {
                if(classIds.contains(cl.getId())) {
                    CheckBox cb = new CheckBox(getActivity());
                    Long classId = ((Class) cl).getId();
                    cb.setText(((Class) cl).getNameShort());
                    cb.setTag(new Pair<Integer, Long>(TAG_CLASS,cl.getId()));
                    cb.setLayoutParams(params);
                    cb.setOnClickListener(this);
                    cb.setEnabled(filter.hasFilterClass());
                    cb.setChecked(filter.isFilterClassEnabled(classId));
                    layoutClass.addView(cb);
                    cbClass.add(cb);
                }
            }

            checkLevel.setChecked(!filter.hasFilterLevel());
            for(long l=0; l<10; l++) {
                CheckBox cb = new CheckBox(getActivity());
                cb.setText(String.valueOf(l));
                cb.setTag(new Pair<Integer, Long>(TAG_LEVEL,l));
                cb.setLayoutParams(params);
                cb.setOnClickListener(this);
                cb.setEnabled(filter.hasFilterLevel());
                cb.setChecked(filter.isFilterLevelEnabled(l));
                layoutLevel.addView(cb);
                cbLevel.add(cb);
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
                for(CheckBox c : cbSchool) {
                    if(c.isChecked()) {
                        filter.addFilterSchool(c.getText().toString());
                    }
                }
                for(CheckBox c : cbClass) {
                    if(c.isChecked()) {
                        filter.addFilterClass(((Pair<Integer, Long>)c.getTag()).second);
                    }
                }
                for(CheckBox c : cbLevel) {
                    if(c.isChecked()) {
                        filter.addFilterLevel(((Pair<Integer, Long>)c.getTag()).second);
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

        if(cb.getId() == R.id.schoolAll) {
            for(CheckBox c : cbSchool) {
                c.setChecked(false);
                c.setEnabled(!status);
            }
        }
        else if(cb.getId() == R.id.classAll) {
            for(CheckBox c : cbClass) {
                c.setChecked(false);
                c.setEnabled(!status);
            }
        }
        else if(cb.getId() == R.id.levelAll) {
            for(CheckBox c : cbLevel) {
                c.setChecked(false);
                c.setEnabled(!status);
            }
        }
        else if(cb.getTag() instanceof Integer && cb.getTag() == TAG_SCHOOL) {
            ((CheckBox)getView().findViewById(R.id.schoolAll)).setChecked(false);
        }
        else if(cb.getTag() instanceof Pair && ((Pair<Integer,Long>)cb.getTag()).first == TAG_CLASS) {
            ((CheckBox)getView().findViewById(R.id.classAll)).setChecked(false);
        }
        else if(cb.getTag() instanceof Pair && ((Pair<Integer,Long>)cb.getTag()).first == TAG_LEVEL) {
            ((CheckBox)getView().findViewById(R.id.levelAll)).setChecked(false);
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
        void onApplyFilter(SpellFilter filter);
    }
}
