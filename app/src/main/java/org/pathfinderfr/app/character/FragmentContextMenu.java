package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.pathfinderfr.R;
import org.pathfinderfr.app.util.FragmentUtil;

import java.util.List;

public class FragmentContextMenu extends DialogFragment {


    public static final String ARG_CONTEXT_ITEMID     = "argItemId";
    public static final String ARG_CONTEXT_TITLE      = "argTitle";
    public static final String ARG_CONTEXT_ICONS      = "argIcons";
    public static final String ARG_CONTEXT_ICONSCOLOR = "argIcColor";
    public static final String ARG_CONTEXT_NAMES      = "argNames";
    public static final String ARG_CONTEXT_MENUIDS    = "argMenus";

    private FragmentContextMenu.OnFragmentInteractionListener mListener;

    private String title;
    private long itemId;
    private List<String> names;
    private List<Integer> icons;
    private List<Integer> colors;
    private List<Integer> menus;

    public FragmentContextMenu() {
        // Required empty public constructor
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    public static FragmentContextMenu newInstance(OnFragmentInteractionListener listener) {
        FragmentContextMenu fragment = new FragmentContextMenu();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            itemId = getArguments().getLong(ARG_CONTEXT_ITEMID);
            title = getArguments().getString(ARG_CONTEXT_TITLE);
            menus = getArguments().getIntegerArrayList(ARG_CONTEXT_MENUIDS);
            icons = getArguments().getIntegerArrayList(ARG_CONTEXT_ICONS);
            colors = getArguments().getIntegerArrayList(ARG_CONTEXT_ICONSCOLOR);
            names = getArguments().getStringArrayList(ARG_CONTEXT_NAMES);
        }

        if(itemId <= 0 || icons == null || names == null || colors == null || menus == null || icons.size() == 0
                || icons.size() != names.size() || icons.size() != colors.size() || icons.size() != menus.size()) {
            throw new IllegalStateException("Invalid state for context menu");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.contextmenu, container, false);
        if(title != null && title.length() > 0) {
            ((TextView) rootView.findViewById(R.id.contextmenu_title)).setText(title);
        } else {
            ((TextView) rootView.findViewById(R.id.contextmenu_title)).setVisibility(View.GONE);
        }

        LinearLayout layout = rootView.findViewById(R.id.contextmenu_list);
        ImageView exampleImage = rootView.findViewById(R.id.contextmenu_icon_example);
        TextView exampleText = rootView.findViewById(R.id.contextmenu_text_example);
        layout.removeAllViews();

        for(int i = 0; i< icons.size(); i++) {
            LinearLayout itemLayout = new LinearLayout(rootView.getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.START | Gravity.CENTER);
            ImageView icon = FragmentUtil.copyExampleImageFragment(exampleImage);
            if(colors.get(i) != 0) {
                icon.setBackgroundColor(colors.get(i));
            }
            icon.setImageResource(icons.get(i));
            itemLayout.addView(icon);
            TextView text = FragmentUtil.copyExampleTextFragment(exampleText);
            text.setText(names.get(i));
            itemLayout.addView(text);
            layout.addView(itemLayout);

            final int menuId = menus.get(i);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if(mListener != null) {
                        mListener.onContextMenu(itemId, menuId);
                    }
                }
            });
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

    public interface OnFragmentInteractionListener {
        void onContextMenu(long itemId, int menuId);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

