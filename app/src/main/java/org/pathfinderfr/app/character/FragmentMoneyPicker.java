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

public class FragmentMoneyPicker extends DialogFragment implements View.OnClickListener {

    public static int MAX_MONEY = 999999;
    private int cp, sp, gp, pp;
    private FragmentMoneyPicker.OnFragmentInteractionListener mListener;

    public FragmentMoneyPicker() {
        // Required empty public constructor
    }

    public static FragmentMoneyPicker newInstance(FragmentMoneyPicker.OnFragmentInteractionListener listener, int cp, int sp, int gp, int pp) {
        FragmentMoneyPicker fragment = new FragmentMoneyPicker();
        cp = Math.max(0, cp);
        cp = Math.min(MAX_MONEY, cp);
        fragment.cp = cp;
        sp = Math.max(0, sp);
        sp = Math.min(MAX_MONEY, sp);
        fragment.sp = sp;
        gp = Math.max(0, gp);
        gp = Math.min(MAX_MONEY, gp);
        fragment.gp = gp;
        pp = Math.max(0, pp);
        pp = Math.min(MAX_MONEY, pp);
        fragment.pp = pp;
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(FragmentMoneyPicker.OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_moneypicker, container, false);

        // set value
        EditText edit = rootView.findViewById(R.id.sheet_money_cp_value);
        edit.setText(String.valueOf(cp));

        edit = rootView.findViewById(R.id.sheet_money_sp_value);
        edit.setText(String.valueOf(sp));

        edit = rootView.findViewById(R.id.sheet_money_gp_value);
        edit.setText(String.valueOf(gp));

        edit = rootView.findViewById(R.id.sheet_money_pp_value);
        edit.setText(String.valueOf(pp));

        rootView.findViewById(R.id.money_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.money_ok).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (v.getId() == R.id.money_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.money_ok) {
            EditText edit = getView().findViewById(R.id.sheet_money_cp_value);
            int cp = 0;
            try {
                cp = Integer.parseInt(edit.getText().toString());
                cp = Math.max(0, cp);         // min
                cp = Math.min(MAX_MONEY, cp); // max
            } catch (NumberFormatException e) {}

            edit = getView().findViewById(R.id.sheet_money_sp_value);
            int sp = 0;
            try {
                sp = Integer.parseInt(edit.getText().toString());
                sp = Math.max(0, sp);         // min
                sp = Math.min(MAX_MONEY, sp); // max
            } catch (NumberFormatException e) {}

            edit = getView().findViewById(R.id.sheet_money_gp_value);
            int gp = 0;
            try {
                gp = Integer.parseInt(edit.getText().toString());
                gp = Math.max(0, gp);         // min
                gp = Math.min(MAX_MONEY, gp); // max
            } catch (NumberFormatException e) {}

            edit = getView().findViewById(R.id.sheet_money_pp_value);
            int pp = 0;
            try {
                pp = Integer.parseInt(edit.getText().toString());
                pp = Math.max(0, pp);         // min
                pp = Math.min(MAX_MONEY, pp); // max
            } catch (NumberFormatException e) {}


            if(mListener != null) {
                mListener.onSaveMoney(cp, sp, gp, pp);
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onSaveMoney(int cp, int sp, int gp, int pp);
    }
}

