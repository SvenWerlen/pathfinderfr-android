package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.util.StringWithTag;

public class FragmentSpeedPicker extends DialogFragment implements View.OnClickListener {

    public static int MAX_SPEED = 99;
    private int speed;
    private int speedWithArmor;
    private int speedDig;
    private int speedFly;
    private int speedMan;
    private FragmentSpeedPicker.OnFragmentInteractionListener mListener;

    public FragmentSpeedPicker() {
        // Required empty public constructor
    }

    public static FragmentSpeedPicker newInstance(FragmentSpeedPicker.OnFragmentInteractionListener listener, int speed, int speedWithArmor, int speedDig, int speedFly, int speedMan) {
        FragmentSpeedPicker fragment = new FragmentSpeedPicker();
        speed = Math.max(0, speed);
        speed = Math.min(MAX_SPEED, speed);
        fragment.speed = speed;
        speedWithArmor = Math.max(0, speedWithArmor);
        speedWithArmor = Math.min(MAX_SPEED, speedWithArmor);
        fragment.speedWithArmor = speedWithArmor;
        speedDig = Math.max(0, speedDig);
        speedDig = Math.min(MAX_SPEED, speedDig);
        fragment.speedDig = speedDig;
        speedFly = Math.max(0, speedFly);
        speedFly = Math.min(MAX_SPEED, speedFly);
        fragment.speedFly = speedFly;
        fragment.speedMan = speedMan;
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

        edit = rootView.findViewById(R.id.sheet_speed_armor_value);
        edit.setText(String.valueOf(speedWithArmor));

        edit = rootView.findViewById(R.id.sheet_speed_dig_value);
        edit.setText(String.valueOf(speedDig));

        edit = rootView.findViewById(R.id.sheet_speed_fly_value);
        edit.setText(String.valueOf(speedFly));

        AppCompatSpinner spinner = rootView.findViewById(R.id.sheet_speed_fly_maneuver_spinner);
        spinner.setSelection(speedMan < 0 || speedMan > Character.SPEED_MANEUV_PERFECT ? 0 : speedMan);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speedMan = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                speedMan = 0;
            }
        });

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

            int speedWithArmor = 0;
            edit = getView().findViewById(R.id.sheet_speed_armor_value);
            try {
                speedWithArmor = Integer.parseInt(edit.getText().toString());
                speedWithArmor = Math.max(0, speedWithArmor);             // min
                speedWithArmor = Math.min(MAX_SPEED, speedWithArmor); // max
            } catch (NumberFormatException e) {}

            int speedDig = 0;
            edit = getView().findViewById(R.id.sheet_speed_dig_value);
            try {
                speedDig = Integer.parseInt(edit.getText().toString());
                speedDig = Math.max(0, speedDig);             // min
                speedDig = Math.min(MAX_SPEED, speedDig); // max
            } catch (NumberFormatException e) {}

            int speedFly = 0;
            edit = getView().findViewById(R.id.sheet_speed_fly_value);
            try {
                speedFly = Integer.parseInt(edit.getText().toString());
                speedFly = Math.max(0, speedFly);             // min
                speedFly = Math.min(MAX_SPEED, speedFly); // max
            } catch (NumberFormatException e) {}

            if(mListener != null) {
                mListener.onSaveSpeed(speed, speedWithArmor, speedDig, speedFly, speedMan);
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onSaveSpeed(int speed, int speedArmor, int speedDig, int speedFly, int speedMan);
    }
}

