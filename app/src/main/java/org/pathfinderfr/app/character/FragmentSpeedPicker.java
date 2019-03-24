package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.pathfinderfr.R;

public class FragmentSpeedPicker extends DialogFragment implements View.OnClickListener {

    public static int MAX_SPEED = 99;
    private int speed;
    private FragmentSpeedPicker.OnFragmentInteractionListener mListener;

    public FragmentSpeedPicker() {
        // Required empty public constructor
    }

    public static FragmentSpeedPicker newInstance(FragmentSpeedPicker.OnFragmentInteractionListener listener, int speed) {
        FragmentSpeedPicker fragment = new FragmentSpeedPicker();
        speed = Math.max(0, speed);             // min
        speed = Math.min(MAX_SPEED, speed); // max
        fragment.speed = speed;
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(FragmentSpeedPicker.OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_speedpicker, container, false);

        // set value and select all
        EditText edit = rootView.findViewById(R.id.sheet_speed_value);
        edit.setText(String.valueOf(speed));
        edit.setSelection(0, edit.getText().toString().length());

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        rootView.findViewById(R.id.speed_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.speed_ok).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (v.getId() == R.id.speed_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.speed_ok) {
            EditText edit = getView().findViewById(R.id.sheet_speed_value);
            int speed = 0;
            try {
                speed = Integer.parseInt(edit.getText().toString());
                speed = Math.max(0, speed);             // min
                speed = Math.min(MAX_SPEED, speed); // max
            } catch (NumberFormatException e) {}
            if(mListener != null && speed != this.speed) {
                mListener.onSaveSpeed(speed);
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onSaveSpeed(int value);
    }
}

