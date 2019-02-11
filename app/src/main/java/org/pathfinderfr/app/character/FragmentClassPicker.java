package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentClassPicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_CLASS_ID       = "class_id";
    public static final String ARG_CLASS_LVL      = "class_lvl";
    public static final String ARG_CLASS_MAX_LVL  = "class_max_lvl";
    public static final String ARG_CLASS_EXCL     = "class_excl";


    private FragmentClassPicker.OnFragmentInteractionListener mListener;

    private long classId;    // selected Class
    private int level;       // selected Level
    private long[] excluded; // excluded classes (already chosen!)
    private int maxLevel;    // max level (could be less < 20 if other classes selected)

    private TextView selectedName;  // TextView (name) currently selected
    private TextView selectedDescr; // TextView (description) currently selected
    private TextView selectedLevel; // TextView (level) currently selected

    private List<TextView> levels;

    public FragmentClassPicker() {
        // Required empty public constructor
        levels = new ArrayList<>();
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
    public static FragmentClassPicker newInstance(OnFragmentInteractionListener listener) {
        FragmentClassPicker fragment = new FragmentClassPicker();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        level = 1;

        if (getArguments() != null && getArguments().containsKey(ARG_CLASS_ID)) {
            classId = getArguments().getLong(ARG_CLASS_ID);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_CLASS_LVL)) {
            level = getArguments().getInt(ARG_CLASS_LVL);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_CLASS_EXCL)) {
            excluded = getArguments().getLongArray(ARG_CLASS_EXCL);
            Arrays.sort(excluded);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_CLASS_MAX_LVL)) {
            maxLevel = getArguments().getInt(ARG_CLASS_MAX_LVL);
        } else {
            maxLevel = 20;
        }

        // restore values that were selected
        if(savedInstanceState != null) {
            classId = savedInstanceState.getLong(ARG_CLASS_ID, classId);
            level = savedInstanceState.getInt(ARG_CLASS_LVL, level);
        }

        Log.i(FragmentClassPicker.class.getSimpleName(), String.format("ClassPicker with %d exclusions and %d max level.",
                excluded == null ? 0 : excluded.length, maxLevel));
    }

    /**
     * Updates the fragment when class is selected
     */
    private void updateChosenClass(TextView name, TextView descr, int level, View rootView) {
        // avoid no selection
        if(selectedName == null && name == null) {
            return;
        }
        // reset previously selected item
        if(selectedName != null && name != null) {
            selectedName.setBackground(name.getBackground());
            selectedName.setTextColor(name.getTextColors());
        }
        // reset previously selected item
        if(selectedDescr != null && descr != null) {
            selectedDescr.setVisibility(View.GONE);
        }

        // new selection
        selectedName = name == null ? selectedName : name;
        selectedName.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        selectedName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        selectedDescr = descr == null ? selectedDescr : descr;
        selectedDescr.setVisibility(View.VISIBLE);
        classId = (Long)selectedName.getTag();
        Class selectedClass = (Class)DBHelper.getInstance(getContext()).fetchEntity(classId, ClassFactory.getInstance());
        this.level = level;

        if(selectedLevel != null) {
            TextView example = rootView.findViewById(R.id.level_predefined_example);
            selectedLevel.setBackground(example.getBackground());
            selectedLevel.setTextColor(example.getTextColors());
            selectedLevel = null;
        }

        int lvl = 1;
        int maxClassLvl = (selectedClass == null ? maxLevel : selectedClass.getMaxLevel());
        System.out.println("MAXLEVEL = " + maxClassLvl);
        for(TextView tv : levels) {
            if(lvl <= maxClassLvl) {
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
            lvl++;
        }

        TextView predefined = rootView.findViewWithTag("level" + this.level);
        Log.d(FragmentClassPicker.class.getSimpleName(), "Predefined " + this.level + " " + (predefined == null ? "not found" : "found"));
        if(predefined != null) {
            predefined.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            predefined.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
            selectedLevel = predefined;
        }

        Button okButton = rootView.findViewById(R.id.class_ok);
        okButton.setText(selectedName.getText() + " " + this.level);
        okButton.setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.class_level_layout).setVisibility(View.VISIBLE);

        final ScrollView scrollView = rootView.findViewById(R.id.choose_class_scrollview);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, selectedName.getTop());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_classpicker, container, false);
        LinearLayout layout = rootView.findViewById(R.id.choose_class_layout);
        TextView exampleName = rootView.findViewById(R.id.choose_class_name);
        TextView exampleDescr = rootView.findViewById(R.id.choose_class_details);
        exampleName.setVisibility(View.GONE);
        exampleDescr.setVisibility(View.GONE);

        // Prepare levels
        FlowLayout levelSelector = rootView.findViewById(R.id.class_level_layout);
        TextView example = rootView.findViewById(R.id.level_predefined_example);
        example.setVisibility(View.GONE);
        for(int i = 1; i<=maxLevel; i++) {
            TextView tv = FragmentUtil.copyExampleTextFragment(example);
            tv.setText(String.valueOf(i));
            tv.setTag("level" + i);
            tv.setOnClickListener(this);
            levelSelector.addView(tv);
            levels.add(tv);
        }

        List<DBEntity> entities =
                DBHelper.getInstance(rootView.getContext()).getAllEntities(ClassFactory.getInstance(), PreferenceUtil.getSources(rootView.getContext()));

        for(DBEntity e : entities) {
            Class cl = (Class)e;
            // skip excluded classes
            if(excluded != null && Arrays.binarySearch(excluded, cl.getId()) >= 0) {
                continue;
            }
            // skip non-selected classes (if any)
            if(classId > 0 && cl.getId() != classId) {
                continue;
            }

            final TextView className = FragmentUtil.copyExampleTextFragment(exampleName);
            className.setText(cl.getName());
            long classId = cl.getId();
            className.setTag(classId);
            layout.addView(className);
            final TextView classDescr = FragmentUtil.copyExampleTextFragment(exampleDescr);
            String descrTemplate = ConfigurationUtil.getInstance(rootView.getContext()).getProperties().getProperty("template.sheet.classpicker");
            String description = String.format(descrTemplate, cl.getDescription(), cl.getAlignment(), cl.getHitDie(),
                    StringUtil.listToString(cl.getSkills().toArray(new String[0]), ", "));

            classDescr.setText(Html.fromHtml(description));
            classDescr.setVisibility(View.GONE);
            layout.addView(classDescr);

            // selected if matching
            if(this.classId > 0 && classId == this.classId) {
                updateChosenClass(className, classDescr, level, rootView);
            }

            // class selected
            className.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateChosenClass(className, classDescr, level, getView());
                }
            });

            // collapse class description
            classDescr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    classDescr.setVisibility(View.GONE);
                }
            });
        }

        rootView.findViewById(R.id.class_delete).setOnClickListener(this);
        rootView.findViewById(R.id.class_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.class_ok).setOnClickListener(this);

        if(selectedName == null) {
            rootView.findViewById(R.id.class_level_layout).setVisibility(View.GONE);
            rootView.findViewById(R.id.class_ok).setVisibility(View.GONE);
            rootView.findViewById(R.id.class_delete).setVisibility(View.GONE);
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
        // apply button was pressed!
        if(v.getId() == R.id.class_cancel) {
            dismiss();
            return;
        } else if(v.getId() == R.id.class_ok) {
            if(selectedName == null) {
                dismiss();
                return;
            }
            if(mListener != null) {
                mListener.onClassChosen(classId, level);
            }
            dismiss();
            return;
        } else if(v.getId() == R.id.class_delete) {
            if(selectedName == null) {
                dismiss();
                return;
            }
            if(mListener != null) {
                mListener.onClassDeleted(classId);
            }
            dismiss();
            return;
        } else if(v instanceof TextView && v.getTag() != null && v.getTag().toString().startsWith("level")) {
            int level = Integer.valueOf(v.getTag().toString().substring("level".length()));
            updateChosenClass(null, null, level, getView());
        }
    }

    public interface OnFragmentInteractionListener {
        void onClassDeleted(long classId);
        void onClassChosen(long classId, int level);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // store currently selected values
        outState.putLong(ARG_CLASS_ID, classId);
        outState.putInt(ARG_CLASS_LVL, level);
    }
}

