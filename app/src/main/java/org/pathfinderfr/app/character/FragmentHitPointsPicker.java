package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.core.text.HtmlCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.pathfinderfr.R;

public class FragmentHitPointsPicker extends DialogFragment implements View.OnClickListener {

    public static int MAX_HITPOINTS = 999;
    private int hitpoints;
    private int hitpointsTemp;
    private FragmentHitPointsPicker.OnFragmentInteractionListener mListener;

    public FragmentHitPointsPicker() {
        // Required empty public constructor
    }

    public static FragmentHitPointsPicker newInstance(FragmentHitPointsPicker.OnFragmentInteractionListener listener, int hitpoints, int hitpointsTemp) {

        FragmentHitPointsPicker fragment = new FragmentHitPointsPicker();
        hitpoints = Math.max(0, hitpoints);             // min
        hitpoints = Math.min(MAX_HITPOINTS, hitpoints); // max
        fragment.hitpoints = hitpoints;
        hitpointsTemp = Math.max(0, hitpointsTemp);             // min
        hitpointsTemp = Math.min(MAX_HITPOINTS, hitpointsTemp); // max
        fragment.hitpointsTemp = hitpointsTemp;
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(FragmentHitPointsPicker.OnFragmentInteractionListener listener) {
        mListener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_hitpointspicker, container, false);

        // set value and select all
        EditText edit = rootView.findViewById(R.id.sheet_hp_value);
        edit.setText(String.valueOf(hitpoints));
        edit.setSelection(0, edit.getText().toString().length());

        // set value and select all
        EditText editTemp = rootView.findViewById(R.id.sheet_hp_temp_value);
        editTemp.setText(String.valueOf(hitpointsTemp));

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        rootView.findViewById(R.id.hitpoints_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.hitpoints_ok).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (v.getId() == R.id.hitpoints_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.hitpoints_ok) {
            EditText edit = getView().findViewById(R.id.sheet_hp_value);
            int hp = 0;
            try {
                hp = Integer.parseInt(edit.getText().toString());
                hp = Math.max(0, hp);             // min
                hp = Math.min(MAX_HITPOINTS, hp); // max
            } catch (NumberFormatException e) {}
            EditText editTemp = getView().findViewById(R.id.sheet_hp_temp_value);
            int hpTemp = 0;
            try {
                hpTemp = Integer.parseInt(editTemp.getText().toString());
                hpTemp = Math.max(0, hpTemp);             // min
                hpTemp = Math.min(MAX_HITPOINTS, hpTemp); // max
            } catch (NumberFormatException e) {}
            if(mListener != null && (hp != hitpoints || hpTemp != hitpointsTemp)) {
                mListener.onSaveHP(hp, hpTemp);
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onSaveHP(int value, int valueTemp);
    }
}

