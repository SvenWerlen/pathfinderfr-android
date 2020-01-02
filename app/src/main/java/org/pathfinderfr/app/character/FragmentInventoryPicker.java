package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.pathfinderfr.R;
import org.pathfinderfr.app.ItemDetailActivity;
import org.pathfinderfr.app.ItemDetailFragment;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Modification;
import org.pathfinderfr.app.database.entity.ModificationFactory;
import org.pathfinderfr.app.database.entity.Weapon;
import org.pathfinderfr.app.util.FragmentUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class FragmentInventoryPicker extends DialogFragment implements View.OnClickListener, View.OnLongClickListener, FragmentModifPicker.OnFragmentInteractionListener {

    public static final String ARG_INVENTORY_ID       = "arg_inventoryId";
    public static final String ARG_INVENTORY_CHARACID = "arg_inventoryCharacterId";
    public static final String ARG_INVENTORY_NAME     = "arg_inventoryName";
    public static final String ARG_INVENTORY_WEIGHT   = "arg_inventoryWeight";
    public static final String ARG_INVENTORY_PRICE    = "arg_inventoryPrice";
    public static final String ARG_INVENTORY_OBJID    = "arg_inventoryObjectId";
    public static final String ARG_INVENTORY_CATEGORY = "arg_inventoryCategory";
    public static final String ARG_INVENTORY_LOCATION = "arg_inventoryLocation";
    public static final String ARG_INVENTORY_INFOS    = "arg_inventoryInfos";
    public static final String ARG_INVENTORY_EQUIPED  = "arg_inventoryActive";

    private FragmentInventoryPicker.OnFragmentInteractionListener mListener;

    private long itemId;
    private long characterId;
    private CharacterItem initial;
    private TextView tooltip;

    private ImageView modifIconExample;
    private TextView modifNameExample;

    public FragmentInventoryPicker() {
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
            itemId = getArguments().getLong(ARG_INVENTORY_ID);
            characterId = getArguments().getLong(ARG_INVENTORY_CHARACID);
            String itemName = getArguments().getString(ARG_INVENTORY_NAME);
            int itemWeight = getArguments().getInt(ARG_INVENTORY_WEIGHT);
            long itemPrice = getArguments().getLong(ARG_INVENTORY_PRICE);
            long itemObjectId = getArguments().getLong(ARG_INVENTORY_OBJID);
            int itemCategory = getArguments().getInt(ARG_INVENTORY_CATEGORY);
            int itemLocation = getArguments().getInt(ARG_INVENTORY_LOCATION);
            String itemInfos = getArguments().getString(ARG_INVENTORY_INFOS);
            boolean active = getArguments().getBoolean(ARG_INVENTORY_EQUIPED);
            initial = new CharacterItem(0L, itemName, itemWeight, itemPrice, itemObjectId, itemInfos, itemCategory, itemLocation);
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

        modifIconExample = rootView.findViewById(R.id.sheet_main_modifs_example_icon);
        modifNameExample = rootView.findViewById(R.id.sheet_main_modifs_example_text);

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
            ((AppCompatSpinner)rootView.findViewById(R.id.sheet_category_spinner)).setSelection(initial.getCategory());
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

        updateModifs(rootView);

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

    private void updateModifs(View v) {
        // item not yet created
        if(characterId <= 0 || itemId <= 0) {
            v.findViewById(R.id.sheet_inventory_item_modifs).setVisibility(View.GONE);
            return;
        }
        // retrieve all modifs matching character and item
        List<DBEntity> modifs = DBHelper.getInstance(v.getContext()).fetchAllEntitiesByForeignIds(new long[] {characterId}, ModificationFactory.getInstance());
        LinearLayout layout = v.findViewById(R.id.sheet_inventory_item_modifs);
        layout.removeAllViews();
        final int colorDisabled = v.getContext().getResources().getColor(R.color.colorBlack);
        final int colorEnabled = v.getContext().getResources().getColor(R.color.colorPrimaryDark);
        for(DBEntity m : modifs) {
            Modification modif = (Modification)m;
            if(modif.getItemId() != itemId) {
                continue;
            }

            LinearLayout modifLayout = new LinearLayout(v.getContext());
            modifLayout.setOrientation(LinearLayout.HORIZONTAL);
            modifLayout.setGravity(Gravity.START | Gravity.CENTER);
            ImageView icon = FragmentUtil.copyExampleImageFragment(modifIconExample);
            final int resourceId = v.getResources().getIdentifier("modif_" + modif.getIcon(), "drawable", v.getContext().getPackageName());
            if(resourceId > 0) {
                icon.setBackgroundColor(modif.isEnabled() ? colorEnabled : colorDisabled);
                icon.setImageResource(resourceId);
            }
            TextView name = FragmentUtil.copyExampleTextFragment(modifNameExample);
            name.setText(modif.getName());
            modifLayout.addView(icon);
            modifLayout.addView(name);
            modifLayout.setTag(modif);
            modifLayout.setOnClickListener(this);
            modifLayout.setOnLongClickListener(this);
            layout.addView(modifLayout);
        }
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
            Integer itemCategory = null;
            Integer itemLocation = null;
            String itemInfos = null;
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
            itemCategory = ((AppCompatSpinner)getView().findViewById(R.id.sheet_category_spinner)).getSelectedItemPosition();
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
                        itemInfos, itemCategory, itemLocation);
                item.setEquiped(((Switch)getView().findViewById(R.id.sheet_inventory_item_equiped)).isChecked());
                if(mListener != null) {
                    if(itemId > 0) {
                        mListener.onUpdateItem(itemId, item);
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
                mListener.onDeleteItem(itemId);
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
        } else if(v instanceof LinearLayout) {
            // toggle modif status
            Modification modif = (Modification)v.getTag();
            modif.setEnabled(!modif.isEnabled());
            DBHelper.getInstance(v.getContext()).updateEntity(modif,  new HashSet<>(Arrays.asList(ModificationFactory.FLAG_ACTIVE)));
            updateModifs(getView());
            if(mListener != null) {
                SheetMainFragment parent = (SheetMainFragment)mListener;
                parent.getCharacter().resyncModifs();
                parent.updateSheet(parent.getView());
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Modification modif = (Modification)v.getTag();
        if(modif != null && mListener instanceof SheetMainFragment) {
            SheetMainFragment parent = (SheetMainFragment)mListener;
            FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(SheetMainFragment.DIALOG_PICK_MODIFS);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            DialogFragment newFragment = FragmentModifPicker.newInstance(this);

            Bundle arguments = new Bundle();
            arguments.putLong(FragmentModifPicker.ARG_MODIF_ID, modif.getId());
            arguments.putString(FragmentModifPicker.ARG_MODIF_NAME, modif.getName());
            ArrayList<Integer> modifIds = new ArrayList<>();
            ArrayList<Integer> modifVals = new ArrayList<>();
            for(int i = 0; i<modif.getModifCount(); i++) {
                modifIds.add(modif.getModif(i).first);
                modifVals.add(modif.getModif(i).second);
            }
            arguments.putIntegerArrayList(FragmentModifPicker.ARG_MODIF_IDS, modifIds);
            arguments.putIntegerArrayList(FragmentModifPicker.ARG_MODIF_VALS, modifVals);
            arguments.putLong(FragmentModifPicker.ARG_MODIF_ITEMID, modif.getItemId());
            arguments.putString(FragmentModifPicker.ARG_MODIF_ITEMS, parent.getCharacter().getInventoryItemsAsString());
            arguments.putString(FragmentModifPicker.ARG_MODIF_ICON, modif.getIcon());

            newFragment.setArguments(arguments);
            newFragment.show(ft, SheetMainFragment.DIALOG_PICK_MODIFS);
            return true;
        }
        return false;
    }

    @Override
    public void onAddModif(Modification modif) {
        if(mListener != null) {
            ((SheetMainFragment) mListener).onAddModif(modif);
            updateModifs(getView());
        }
    }

    @Override
    public void onDeleteModif(long modifId) {
        if(mListener != null) {
            ((SheetMainFragment) mListener).onDeleteModif(modifId);
            updateModifs(getView());
        }
    }

    @Override
    public void onModifUpdated(long modifId, Modification modif) {
        if(mListener != null) {
            ((SheetMainFragment) mListener).onModifUpdated(modifId, modif);
            updateModifs(getView());
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

