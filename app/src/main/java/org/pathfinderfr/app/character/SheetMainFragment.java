package org.pathfinderfr.app.character;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.FilterSpellFragment;
import org.pathfinderfr.app.ItemDetailFragment;
import org.pathfinderfr.app.util.CharacterUtil;
import org.pathfinderfr.app.util.SpellFilter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SheetMainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SheetMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SheetMainFragment extends Fragment implements AbilityPickerFragment.OnFragmentInteractionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SheetMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SheetMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SheetMainFragment newInstance(String param1, String param2) {
        SheetMainFragment fragment = new SheetMainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sheet_main, container, false);
        View.OnClickListener listener = new AbilityListener(this);
        view.findViewById(R.id.ability_str_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_dex_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_con_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_int_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_wis_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_cha_value).setOnClickListener(listener);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private static class AbilityListener implements View.OnClickListener {

        SheetMainFragment parent;

        public AbilityListener(SheetMainFragment fragment) {
            parent = fragment;
        }

        @Override
        public void onClick(View v) {

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                int value = ( tv.getText() != null ? Integer.valueOf(tv.getText().toString()) : 10);

                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag("picker");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = AbilityPickerFragment.newInstance(parent);

                Bundle arguments = new Bundle();
                arguments.putInt(AbilityPickerFragment.ARG_ABILITY_ID, tv.getId());
                arguments.putInt(AbilityPickerFragment.ARG_ABILITY_VALUE, value);
                newFragment.setArguments(arguments);
                newFragment.show(ft, "picker");
            }
        }
    }

    @Override
    public void onAbilityValueChosen(int abilityId, int abilityValue) {
        View v = getView().findViewById(abilityId);
        if(v != null && v instanceof TextView) {
            TextView tv = (TextView)v;
            tv.setText(String.valueOf(abilityValue));

            switch(abilityId) {
                case R.id.ability_str_value:
                    tv = getView().findViewById(R.id.ability_str_modif); break;
                case R.id.ability_dex_value:
                    tv = getView().findViewById(R.id.ability_dex_modif); break;
                case R.id.ability_con_value:
                    tv = getView().findViewById(R.id.ability_con_modif); break;
                case R.id.ability_int_value:
                    tv = getView().findViewById(R.id.ability_int_modif); break;
                case R.id.ability_wis_value:
                    tv = getView().findViewById(R.id.ability_wis_modif); break;
                case R.id.ability_cha_value:
                    tv = getView().findViewById(R.id.ability_cha_modif); break;
            }

            if(tv != null) {
                tv.setText(String.valueOf(CharacterUtil.getAbilityBonus(abilityValue)));
            }

        }
    }
}
