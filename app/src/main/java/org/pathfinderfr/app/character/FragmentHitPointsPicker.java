package org.pathfinderfr.app.character;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.text.HtmlCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.entity.Character;

public class FragmentHitPointsPicker extends DialogFragment implements View.OnClickListener {

    private static int MAX_HITPOINTS = 999;
    private int hitpoints;
    private FragmentHitPointsPicker.OnFragmentInteractionListener mListener;

    public FragmentHitPointsPicker() {
        // Required empty public constructor
    }

    public static FragmentHitPointsPicker newInstance(FragmentHitPointsPicker.OnFragmentInteractionListener listener, int hitpoints) {

        FragmentHitPointsPicker fragment = new FragmentHitPointsPicker();
        hitpoints = Math.max(0, hitpoints);             // min
        hitpoints = Math.min(MAX_HITPOINTS, hitpoints); // max
        fragment.hitpoints = hitpoints;
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
            if(mListener != null && hp != hitpoints) {
                mListener.onSaveHP(hp);
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onSaveHP(int value);
    }
}

