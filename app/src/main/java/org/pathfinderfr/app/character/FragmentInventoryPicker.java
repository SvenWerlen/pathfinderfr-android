package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.StringWithTag;

import java.util.ArrayList;
import java.util.List;

public class FragmentInventoryPicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_INVENTORY_IDX   = "arg_inventoryIdx";
    public static final String ARG_INVENTORY_NAME   = "arg_inventoryName";
    public static final String ARG_INVENTORY_WEIGHT = "arg_inventoryWeight";

    private FragmentInventoryPicker.OnFragmentInteractionListener mListener;

    private int invIdx;
    private Character.InventoryItem initial;

    public FragmentInventoryPicker() {
        // Required empty public constructor
        invIdx = -1;
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    public void setInitial(Character.InventoryItem item) { initial = item; }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentAbilityPicker.
     */
    public static FragmentInventoryPicker newInstance(OnFragmentInteractionListener listener) {
        FragmentInventoryPicker fragment = new FragmentInventoryPicker();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize from params
        if(getArguments().containsKey(ARG_INVENTORY_IDX)) {
            invIdx = getArguments().getInt(ARG_INVENTORY_IDX);
            String itemName = getArguments().getString(ARG_INVENTORY_NAME);
            Integer itemWeight = getArguments().getInt(ARG_INVENTORY_WEIGHT);
            initial = new Character.InventoryItem(itemName, itemWeight);
        }

        // restore values that were selected
        if(savedInstanceState != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_inventorypicker, container, false);
        final EditText itemWeight = rootView.findViewById(R.id.sheet_inventory_item_weight);
        itemWeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        rootView.findViewById(R.id.inventory_item_ok).setOnClickListener(this);
        rootView.findViewById(R.id.inventory_item_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.inventory_item_delete).setOnClickListener(this);

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (source.charAt(i) == '|' || source.charAt(i) == '#') {
                        return "";
                    }
                }
                return null;
            }
        };

        EditText itemName = rootView.findViewById(R.id.sheet_inventory_item_name);
        itemName.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(35) });

        // initialize form if required
        if(initial != null) {
            itemName.setText(initial.getName());
            itemWeight.setText(String.valueOf(initial.getWeight()));
        } else {
            rootView.findViewById(R.id.inventory_item_delete).setVisibility(View.GONE);
        }

        itemName.requestFocus();
        if(initial==null) {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);

        String itemName;
        Integer itemWeight;
        try {
            itemName = ((EditText) getView().findViewById(R.id.sheet_inventory_item_name)).getText().toString();
            itemWeight = Integer.valueOf(((EditText) getView().findViewById(R.id.sheet_inventory_item_weight)).getText().toString());
        } catch(NumberFormatException nfe) {
            return;
        }

        if(v.getId() == R.id.inventory_item_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.inventory_item_ok) {
            if(itemName != null && itemName.length() > 3 && itemWeight != null) {
                Character.InventoryItem item = new Character.InventoryItem(itemName, itemWeight);
                if(mListener != null) {
                    if(invIdx >= 0) {
                        mListener.onUpdateItem(invIdx, item);
                    } else {
                        mListener.onAddItem(item);
                    }
                }
                dismiss();
            }
            return;
        }
        else if(v.getId() == R.id.inventory_item_delete) {
            if(itemName != null && itemName.length() > 3 && itemWeight != null) {
                if(mListener != null) {
                    mListener.onDeleteItem(invIdx);
                }
                dismiss();
            }
            return;
        }
    }

    public interface OnFragmentInteractionListener {
        void onAddItem(Character.InventoryItem item);
        void onDeleteItem(int itemIdx);
        void onUpdateItem(int itemIdx, Character.InventoryItem item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

