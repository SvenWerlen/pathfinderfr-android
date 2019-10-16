package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import org.pathfinderfr.R;


public class FragmentSync extends DialogFragment implements View.OnClickListener {

    public static final String ARG_NAME = "argName";
    public static final String ARG_UUID = "argUUID";

    private FragmentSync.OnFragmentInteractionListener mListener;
    private String name;
    private String uuid;

    public FragmentSync() {
        // Required empty public constructor
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    public static FragmentSync newInstance(OnFragmentInteractionListener listener) {
        FragmentSync fragment = new FragmentSync();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_NAME)) {
            name = getArguments().getString(ARG_NAME);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_UUID)) {
            uuid = getArguments().getString(ARG_UUID);
        }

        // restore value that was selected
        if(savedInstanceState != null) {
            name = savedInstanceState.getString(ARG_NAME, name);
            uuid = savedInstanceState.getString(ARG_UUID, uuid);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_sync, container, false);

        TextView charUUID = rootView.findViewById(R.id.sheet_main_id);
        if(charUUID != null) {
            charUUID.setText(uuid);
        }

        TextView charName = rootView.findViewById(R.id.sheet_name);
        if(charName != null) {
            charName.setText(name);
        }

        rootView.findViewById(R.id.sync_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.sync_ok).setOnClickListener(this);

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
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);

        if(v.getId() == R.id.sync_cancel) {
            dismiss();
            return;
        } else if(v.getId() == R.id.sync_ok) {
            if(mListener != null) {
                mListener.onSync();
            }
            dismiss();
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onSync();
    }
}

