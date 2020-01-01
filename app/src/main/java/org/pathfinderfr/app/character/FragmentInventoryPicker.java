package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.pathfinderfr.R;
import org.pathfinderfr.app.ItemDetailActivity;
import org.pathfinderfr.app.ItemDetailFragment;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Weapon;

public class FragmentInventoryPicker extends DialogFragment implements View.OnClickListener {

    public static final String ARG_INVENTORY_ID       = "arg_inventoryId";
    public static final String ARG_INVENTORY_NAME     = "arg_inventoryName";
    public static final String ARG_INVENTORY_WEIGHT   = "arg_inventoryWeight";
    public static final String ARG_INVENTORY_PRICE    = "arg_inventoryPrice";
    public static final String ARG_INVENTORY_OBJID    = "arg_inventoryObjectId";
    public static final String ARG_INVENTORY_LOCATION = "arg_inventoryLocation";
    public static final String ARG_INVENTORY_INFOS    = "arg_inventoryInfos";
    public static final String ARG_INVENTORY_EQUIPED  = "arg_inventoryActive";

    private FragmentInventoryPicker.OnFragmentInteractionListener mListener;

    private long invId;
    private CharacterItem initial;
    private TextView tooltip;

    public FragmentInventoryPicker() {
        // Required empty public constructor
        invId = -1;
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
    public static FragmentInventoryPicker newInstance(OnFragmentInteractionListener listener) {
        FragmentInventoryPicker fragment = new FragmentInventoryPicker();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize from params
        if(getArguments().containsKey(ARG_INVENTORY_ID)) {
            invId = getArguments().getLong(ARG_INVENTORY_ID);
            String itemName = getArguments().getString(ARG_INVENTORY_NAME);
            int itemWeight = getArguments().getInt(ARG_INVENTORY_WEIGHT);
            long itemPrice = getArguments().getLong(ARG_INVENTORY_PRICE);
            long itemObjectId = getArguments().getLong(ARG_INVENTORY_OBJID);
            int itemLocation = getArguments().getInt(ARG_INVENTORY_LOCATION);
            String itemInfos = getArguments().getString(ARG_INVENTORY_INFOS);
            boolean active = getArguments().getBoolean(ARG_INVENTORY_EQUIPED);
            initial = new CharacterItem(0L, itemName, itemWeight, itemPrice, itemObjectId, itemInfos, itemLocation);
            initial.setEquiped(active);
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
        itemWeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        final EditText itemPrice = rootView.findViewById(R.id.sheet_inventory_item_price);
        itemPrice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        rootView.findViewById(R.id.sheet_inventory_reference_section).setVisibility(View.GONE);
        rootView.findViewById(R.id.sheet_inventory_reference_section).setOnClickListener(this);
        rootView.findViewById(R.id.inventory_item_ok).setOnClickListener(this);
        rootView.findViewById(R.id.inventory_item_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.inventory_item_delete).setOnClickListener(this);

        rootView.findViewById(R.id.sheet_inventory_item_equiped_descr).setVisibility(View.GONE);
        tooltip = rootView.findViewById(R.id.sheet_inventory_item_equiped_descr);
        rootView.findViewById(R.id.sheet_inventory_item_equiped_tooltip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tooltip.setVisibility(tooltip.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });

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

        EditText itemInfos = rootView.findViewById(R.id.sheet_inventory_item_infos);
        itemInfos.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(20) });

        rootView.findViewById(R.id.sheet_inventory_item_infos_section).setVisibility(View.GONE);

        // initialize form if required
        if(initial != null) {
            itemName.setText(initial.getName());
            itemWeight.setText(String.valueOf(initial.getWeight()));
            long price = initial.getPrice();
            if(price % 100 == 0) {
                ((AppCompatSpinner)rootView.findViewById(R.id.sheet_inventory_item_price_unit)).setSelection(2);
                itemPrice.setText(String.valueOf(initial.getPrice()/100));
            } else if(price % 10 == 0) {
                ((AppCompatSpinner)rootView.findViewById(R.id.sheet_inventory_item_price_unit)).setSelection(1);
                itemPrice.setText(String.valueOf(initial.getPrice()/10));
            } else {
                itemPrice.setText(String.valueOf(initial.getPrice()));
            }
            ((AppCompatSpinner)rootView.findViewById(R.id.sheet_body_location_spinner)).setSelection(initial.getLocation());

            if(initial.getItemRef() > 0) {
                DBEntity e = DBHelper.getInstance(rootView.getContext()).fetchObjectEntity(initial);
                if(e != null) {
                    ((TextView)rootView.findViewById(R.id.sheet_inventory_reference)).setText(e.getName());
                    rootView.findViewById(R.id.sheet_inventory_reference_section).setVisibility(View.VISIBLE);
                }
                if(e instanceof Weapon) {
                    Weapon w = (Weapon)e;
                    if(w.isRanged()) {
                        rootView.findViewById(R.id.sheet_inventory_item_infos_section).setVisibility(View.VISIBLE);
                    }
                    itemInfos.setText(initial.getAmmo());
                }
            }

            ((Switch)rootView.findViewById(R.id.sheet_inventory_item_equiped)).setChecked(initial.isEquiped());
        } else {
            ((AppCompatSpinner)rootView.findViewById(R.id.sheet_inventory_item_price_unit)).setSelection(2);
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

        if(v.getId() == R.id.inventory_item_cancel) {
            dismiss();
            return;
        }
        else if(v.getId() == R.id.inventory_item_ok) {
            String itemName = null;
            Integer itemWeight = null;
            Integer itemPrice = null;
            Integer itemLocation = null;
            String itemInfos = null;
            boolean isActive = false;
            itemName = ((EditText) getView().findViewById(R.id.sheet_inventory_item_name)).getText().toString();
            try {
                itemWeight = Integer.valueOf(((EditText) getView().findViewById(R.id.sheet_inventory_item_weight)).getText().toString());
            } catch(NumberFormatException nfe) {
                itemWeight = 0;
            }
            try {
                itemPrice = Integer.valueOf(((EditText) getView().findViewById(R.id.sheet_inventory_item_price)).getText().toString());
            } catch(NumberFormatException nfe) {
                itemPrice = 0;
            }
            itemInfos = ((EditText) getView().findViewById(R.id.sheet_inventory_item_infos)).getText().toString();
            int idx = ((AppCompatSpinner)getView().findViewById(R.id.sheet_inventory_item_price_unit)).getSelectedItemPosition();
            if(idx == 1) { // silver
                itemPrice *= 10;
            } else if(idx == 2) { // gold
                itemPrice *= 100;
            }
            itemLocation = ((AppCompatSpinner)getView().findViewById(R.id.sheet_body_location_spinner)).getSelectedItemPosition();

            if(itemName.length() < 3) {
                Toast t = Toast.makeText(v.getContext(), getView().getResources().getString(R.string.sheet_inventory_error_name), Toast.LENGTH_SHORT);
                int[] xy = new int[2];
                v.getLocationOnScreen(xy);
                t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
                t.show();
            } else if(itemWeight < 0) {
                Toast t = Toast.makeText(v.getContext(), getView().getResources().getString(R.string.sheet_inventory_error_weight), Toast.LENGTH_SHORT);
                int[] xy = new int[2];
                v.getLocationOnScreen(xy);
                t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
                t.show();
            } else if(itemPrice < 0) {
                Toast t = Toast.makeText(v.getContext(), getView().getResources().getString(R.string.sheet_inventory_error_price), Toast.LENGTH_SHORT);
                int[] xy = new int[2];
                v.getLocationOnScreen(xy);
                t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
                t.show();
            }
            else {
                CharacterItem item = new CharacterItem(0L, itemName, itemWeight, itemPrice,
                        initial == null ? 0L : initial.getItemRef(),
                        itemInfos, initial == null ? CharacterItem.LOCATION_NOLOC : initial.getLocation());
                item.setLocation(itemLocation);
                item.setEquiped(((Switch)getView().findViewById(R.id.sheet_inventory_item_equiped)).isChecked());
                if(mListener != null) {
                    if(invId >= 0) {
                        mListener.onUpdateItem(invId, item);
                    } else {
                        mListener.onAddItem(item);
                    }
                }
                dismiss();
            }
            return;
        }
        else if(v.getId() == R.id.inventory_item_delete) {

            if(mListener != null) {
                mListener.onDeleteItem(invId);
            }
            dismiss();
            return;
        }
        else if(v.getId() == R.id.sheet_inventory_reference_section) {
            DBEntity object = DBHelper.getInstance(v.getContext()).fetchObjectEntity(initial);
            if(object != null) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, object.getId());
                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, object.getFactory().getFactoryId());
                context.startActivity(intent);
                return;
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onAddItem(CharacterItem item);
        void onDeleteItem(long itemId);
        void onUpdateItem(long itemId, CharacterItem item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // store already typed source
        String text = ((EditText)getView().findViewById(R.id.sheet_inventory_item_name)).getText().toString();
        outState.putString(ARG_INVENTORY_NAME, text);
        // store already typed weight
        String weight = ((EditText)getView().findViewById(R.id.sheet_inventory_item_weight)).getText().toString();
        outState.putString(ARG_INVENTORY_WEIGHT, weight);
    }
}

