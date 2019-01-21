package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
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
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.PreferenceUtil;

import java.util.List;

public class FragmentRacePicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_RACE_ID = "race_id";


    private FragmentRacePicker.OnFragmentInteractionListener mListener;

    private Long raceId;
    private TextView selectedName;
    private TextView selectedDescr;

    public FragmentRacePicker() {
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
    public static FragmentRacePicker newInstance(OnFragmentInteractionListener listener) {
        FragmentRacePicker fragment = new FragmentRacePicker();
        fragment.setListener(listener);
        return fragment;
    }

    private void updateChosenRace(TextView name, TextView descr, View rootView) {
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
        raceId = (Long)selectedName.getTag();

        Button okButton = rootView.findViewById(R.id.race_ok);
        okButton.setText(name.getText());
        okButton.setVisibility(View.VISIBLE);

        final ScrollView scrollView = rootView.findViewById(R.id.choose_race_scrollview);
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

        if (getArguments() != null && getArguments().containsKey(ARG_RACE_ID)) {
            raceId = getArguments().getLong(ARG_RACE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_racepicker, container, false);
        LinearLayout layout = rootView.findViewById(R.id.choose_race_layout);
        TextView exampleName = rootView.findViewById(R.id.choose_race_name);
        TextView exampleDescr = rootView.findViewById(R.id.choose_race_details);
        exampleName.setVisibility(View.GONE);
        exampleDescr.setVisibility(View.GONE);

        List<DBEntity> entities =
                DBHelper.getInstance(rootView.getContext()).getAllEntities(RaceFactory.getInstance(), PreferenceUtil.getSources(rootView.getContext()));

        for(DBEntity e : entities) {
            final TextView raceName = FragmentUtil.copyExampleTextFragment(exampleName);
            raceName.setText(e.getName());
            long raceId = e.getId();
            raceName.setTag(raceId);
            layout.addView(raceName);
            final TextView raceDescr = FragmentUtil.copyExampleTextFragment(exampleDescr);
            String descrTemplate = ConfigurationUtil.getInstance(rootView.getContext()).getProperties().getProperty("template.sheet.racepicker");
            StringBuffer description = new StringBuffer();
            for(Race.Trait t : ((Race)e).getTraits()) {
                description.append(String.format(descrTemplate,t.getName(),t.getDescription()));
            }
            raceDescr.setText(Html.fromHtml(description.toString()));
            raceDescr.setVisibility(View.GONE);
            layout.addView(raceDescr);

            // selected if matching
            if(raceId == this.raceId) {
                updateChosenRace(raceName, raceDescr, rootView);
            }

            // race selected
            raceName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateChosenRace(raceName, raceDescr, getView());
                }
            });

            // race unselected
            raceDescr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    raceDescr.setVisibility(View.GONE);
                }
            });
        }
        rootView.findViewById(R.id.race_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.race_ok).setOnClickListener(this);

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
        if(v.getId() == R.id.race_cancel) {
            dismiss();
            return;
        } else if(v.getId() == R.id.race_ok) {
            if(selectedName == null) {
                dismiss();
                return;
            }
            if(mListener != null) {
                mListener.onRaceChosen(raceId);
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onRaceChosen(long raceId);
    }
}

