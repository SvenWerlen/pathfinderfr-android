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
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.ClassFeatureFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Equipment;
import org.pathfinderfr.app.database.entity.EquipmentFactory;
import org.pathfinderfr.app.util.ClassFeatureFilter;
import org.pathfinderfr.app.util.EquipmentFilter;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterEquipmentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterEquipmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterEquipmentFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_FILTER = "filter";

    private static final Integer TAG_CATEGORY = 1;

    private List<CheckBox> cbCategory;

    private EquipmentFilter filter;

    private OnFragmentInteractionListener mListener;

    public FilterEquipmentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterSpellFragment.
     */
    public static FilterEquipmentFragment newInstance() {
        FilterEquipmentFragment fragment = new FilterEquipmentFragment();
        return fragment;
    }

    public void setFilter(EquipmentFilter filter) {
        this.filter = filter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cbCategory = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_equipment_filter, container, false);
        FlowLayout layoutCategory = (FlowLayout) rootView.findViewById(R.id.flowCategory);
        CheckBox checkAllCategory = (CheckBox)rootView.findViewById(R.id.categoryAll);

        rootView.findViewById(R.id.applyButton).setOnClickListener(this);
        rootView.findViewById(R.id.cancelButton).setOnClickListener(this);

        checkAllCategory.setOnClickListener(this);

        DBHelper dbHelper = DBHelper.getInstance(rootView.getContext());
        String[] sources = PreferenceUtil.getSources(rootView.getContext());
        List<DBEntity> equipment = dbHelper.getAllEntities(EquipmentFactory.getInstance(), sources);

        Set<String> categ = new HashSet<>();
        for(DBEntity eq : equipment) {
            categ.add(((Equipment)eq).getCategory());
        }
        List<String> categories = new ArrayList<>();
        categories.addAll(categ);
        Collections.sort(categories);

        if(filter != null) {
            checkAllCategory.setChecked(!filter.hasFilterCategory());
            for (String cat : categories) {
                CheckBox cb = new CheckBox(getActivity());
                cb.setText(cat);
                cb.setLayoutParams(checkAllCategory.getLayoutParams());
                cb.setOnClickListener(this);
                cb.setEnabled(filter.hasFilterCategory());
                cb.setChecked(filter.isFilterCategoryEnabled(cat));
                layoutCategory.addView(cb);
                cbCategory.add(cb);
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
                for(CheckBox c : cbCategory) {
                    if(c.isChecked()) {
                        filter.addFilterCategory(c.getText().toString());
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

        if(cb.getId() == R.id.categoryAll) {
            for(CheckBox c : cbCategory) {
                c.setChecked(false);
                c.setEnabled(!status);
            }
        }
        else {
            ((CheckBox)getView().findViewById(R.id.categoryAll)).setChecked(false);
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
        void onApplyFilter(EquipmentFilter filter);
    }
}
