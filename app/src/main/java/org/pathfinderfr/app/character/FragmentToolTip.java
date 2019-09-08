package org.pathfinderfr.app.character;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.core.text.HtmlCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import org.pathfinderfr.R;

public class FragmentToolTip extends DialogFragment {

    public static final String ARG_TOOLTIP_TITLE = "argToolTipTitle";
    public static final String ARG_TOOLTIP_TEXT = "argToolTipText";

    private String title;
    private String text;

    public FragmentToolTip() {
        // Required empty public constructor
    }

    public static FragmentToolTip newInstance() {
        FragmentToolTip fragment = new FragmentToolTip();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null && getArguments().containsKey(ARG_TOOLTIP_TITLE)) {
            title = getArguments().getString(ARG_TOOLTIP_TITLE);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_TOOLTIP_TEXT)) {
            text = getArguments().getString(ARG_TOOLTIP_TEXT);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_tooltip, container, false);
        TextView title = rootView.findViewById(R.id.sheet_tooltip_title);
        title.setText(HtmlCompat.fromHtml(this.title,HtmlCompat.FROM_HTML_MODE_COMPACT));
        WebView content = rootView.findViewById(R.id.sheet_tooltip_content);
        content.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
        content.setBackgroundColor(Color.TRANSPARENT);
        rootView.findViewById(R.id.tooltip_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentToolTip.this.dismiss();
                return;
            }
        });

        return rootView;
    }
}

