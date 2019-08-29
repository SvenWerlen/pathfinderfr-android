package org.pathfinderfr.app;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.util.FragmentUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class FragmentLinkedFeaturePicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_FEATURE_ID    = "arg_featureId";
    public static final String ARG_CHARACTER_ID  = "arg_characterId";

    private FragmentLinkedFeaturePicker.OnFragmentInteractionListener mListener;

    private Character character;
    private ClassFeature feature;

    private TextView selectedView;
    private ClassFeature selected;

    private TextView nolink;
    private List<TextView> choices;
    private EditText label;


    public FragmentLinkedFeaturePicker() {
        // Required empty public constructor
        choices = new ArrayList<>();
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
    public static FragmentLinkedFeaturePicker newInstance(OnFragmentInteractionListener listener) {
        FragmentLinkedFeaturePicker fragment = new FragmentLinkedFeaturePicker();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_CHARACTER_ID) && getArguments().containsKey(ARG_FEATURE_ID)) {
            long characterId = getArguments().getLong(ARG_CHARACTER_ID);
            long featureId = getArguments().getLong(ARG_FEATURE_ID);

            if(characterId > 0 && featureId > 0) {
                character = (Character)DBHelper.getInstance(null).fetchEntity(characterId, CharacterFactory.getInstance(),
                        new HashSet<>(Arrays.asList(CharacterFactory.FLAG_FEATURES)));

                if(character != null) {
                    for (ClassFeature cf : character.getClassFeatures()) {
                        if(cf.getId() == featureId) {
                            feature = cf;
                            selected = cf.getLinkedTo();
                            break;
                        }
                    }
                }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_features_linkedpicker, container, false);

        LinearLayout layout = rootView.findViewById(R.id.features_linkedlist);
        nolink = rootView.findViewById(R.id.choose_no_link);
        nolink.setOnClickListener(this);
        label = rootView.findViewById(R.id.features_label);

        rootView.findViewById(R.id.link_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.link_delete).setOnClickListener(this);
        rootView.findViewById(R.id.link_delete).setVisibility(selected != null ? View.VISIBLE : View.GONE);
        rootView.findViewById(R.id.link_ok).setOnClickListener(this);

        if(character != null && feature != null) {

            String text = String.format(getResources().getString(R.string.features_linkto), feature.getName());
            ((TextView)rootView.findViewById(R.id.features_linkto)).setText(text);

            if(feature.getLinkedName() != null) {
                label.setText(feature.getLinkedName());
            }

            for(ClassFeature cf : character.getClassFeatures()) {
                // ignore non-matching features
                if(feature.isAuto() == cf.isAuto()) {
                    continue;
                }
                // ignore already matched features
                if(cf.getLinkedTo() != null && cf.getLinkedTo().getId() != feature.getId()) {
                    continue;
                }
                TextView feature = FragmentUtil.copyExampleTextFragment(nolink);
                feature.setText(cf.getName());
                feature.setOnClickListener(this);
                feature.setTag(cf);
                choices.add(feature);
                layout.addView(feature);

                // highlight selected
                if(selected != null && selected.getId() == cf.getId()) {
                    updateChosenFeature(feature, rootView);
                }
            }
        }
        if(selected == null) {
            updateChosenFeature(nolink, rootView);
        }

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void updateChosenFeature(TextView feature, View view) {
        if(selectedView != null) {
            selectedView.setBackground(feature.getBackground());
            selectedView.setTextColor(feature.getTextColors());
        }
        selectedView = feature;
        selectedView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        selectedView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        selected = (ClassFeature) selectedView.getTag();
        label.setEnabled(selected == null);
        if(selected != null) {
            label.setText("");
        }
        boolean showDelete = selected != null || label.getText().length() > 0;
        view.findViewById(R.id.link_delete).setVisibility(showDelete ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.link_cancel) {
            dismiss();
        } else if(v.getId() == R.id.link_delete) {
            if(mListener != null) {
                mListener.onLink(null, null);
                dismiss();
            }
        } else if(v.getId() == R.id.link_ok) {
            if(mListener != null && selected != null) {
                mListener.onLink(selected, null);
                dismiss();
            } else if(mListener != null && label.getText().length() > 0) {
                mListener.onLink(null, label.getText().toString());
                dismiss();
            }

        } else if(v.getTag() instanceof ClassFeature) {
            updateChosenFeature((TextView)v, getView());
        } else if(v.getId() == R.id.choose_no_link){
            updateChosenFeature(nolink, getView());
        }
    }

    public interface OnFragmentInteractionListener {
        void onLink(ClassFeature cf, String text);
    }

}

