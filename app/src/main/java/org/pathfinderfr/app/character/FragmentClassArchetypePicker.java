package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassArchetypesFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.PreferenceUtil;

import java.util.List;

public class FragmentClassArchetypePicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_CLASS_ID = "argClassId";
    public static final String ARG_ARCHETYPE_ID = "argArchetypeId";


    private FragmentClassArchetypePicker.OnFragmentInteractionListener mListener;

    private Long classId;
    private Long archetypeId;
    private TextView selectedName;
    private TextView selectedDescr;

    public FragmentClassArchetypePicker() {
        // Required empty public constructor
        archetypeId = 0L;
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
    public static FragmentClassArchetypePicker newInstance(OnFragmentInteractionListener listener) {
        FragmentClassArchetypePicker fragment = new FragmentClassArchetypePicker();
        fragment.setListener(listener);
        return fragment;
    }

    private void updateChosenArchetype(TextView name, TextView descr, View rootView) {
        if(selectedName != null) {
            selectedName.setBackground(name.getBackground());
            selectedName.setTextColor(name.getTextColors());
        }
        if(selectedDescr != null) {
            selectedDescr.setVisibility(View.GONE);
        }
        selectedName = name;
        selectedName.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        selectedName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        selectedDescr = descr;
        selectedDescr.setVisibility(View.VISIBLE);
        archetypeId = (Long)selectedName.getTag();

        Button okButton = rootView.findViewById(R.id.archetype_ok);
        okButton.setText(name.getText());
        okButton.setVisibility(View.VISIBLE);

        final ScrollView scrollView = rootView.findViewById(R.id.choose_archetype_scrollview);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, selectedName.getTop());
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_CLASS_ID)) {
            classId = getArguments().getLong(ARG_CLASS_ID);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_ARCHETYPE_ID)) {
            archetypeId = getArguments().getLong(ARG_ARCHETYPE_ID);
        }

        // restore value that was selected
        if(savedInstanceState != null) {
            classId = savedInstanceState.getLong(ARG_CLASS_ID, classId);
            archetypeId = savedInstanceState.getLong(ARG_ARCHETYPE_ID, archetypeId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_archetypepicker, container, false);
        LinearLayout layout = rootView.findViewById(R.id.choose_archetype_layout);
        TextView exampleName = rootView.findViewById(R.id.choose_archetype_name);
        TextView exampleDescr = rootView.findViewById(R.id.choose_archetype_details);
        exampleName.setVisibility(View.GONE);
        exampleDescr.setVisibility(View.GONE);

        List<DBEntity> entities =
                DBHelper.getInstance(rootView.getContext()).getAllEntitiesWithAllFields(ClassArchetypesFactory.getInstance(), PreferenceUtil.getSources(rootView.getContext()));

        for(DBEntity e : entities) {
            ClassArchetype ca = (ClassArchetype)e;
            if(ca.getClass_().getId() != classId) {
                continue;
            }

            final TextView archetypeName = FragmentUtil.copyExampleTextFragment(exampleName);
            archetypeName.setText(e.getName());
            long archetypeId = e.getId();
            archetypeName.setTag(archetypeId);
            layout.addView(archetypeName);
            final TextView archetypeDescr = FragmentUtil.copyExampleTextFragment(exampleDescr);
            StringBuffer description = new StringBuffer();
            archetypeDescr.setText(Html.fromHtml(e.getDescription().toString()));
            archetypeDescr.setVisibility(View.GONE);
            layout.addView(archetypeDescr);

            // selected if matching
            if(archetypeId == this.archetypeId) {
                updateChosenArchetype(archetypeName, archetypeDescr, rootView);
            }

            // archetype selected
            archetypeName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateChosenArchetype(archetypeName, archetypeDescr, getView());
                }
            });

            // archetype unselected
            archetypeDescr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    archetypeDescr.setVisibility(View.GONE);
                }
            });
        }
        rootView.findViewById(R.id.archetype_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.archetype_ok).setOnClickListener(this);

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
        // apply button was pressed!
        if(v.getId() == R.id.archetype_cancel) {
            if(mListener != null) {
                mListener.onArchetypeDeleted();
            }
            dismiss();
            return;
        } else if(v.getId() == R.id.archetype_ok) {
            if(selectedName == null) {
                dismiss();
                return;
            }
            if(mListener != null) {
                mListener.onArchetypeChosen(archetypeId);
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onArchetypeDeleted();
        void onArchetypeChosen(long archetypeId);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // store currently selected value
        outState.putLong(ARG_CLASS_ID, classId);
        outState.putLong(ARG_ARCHETYPE_ID, archetypeId);
    }
}

