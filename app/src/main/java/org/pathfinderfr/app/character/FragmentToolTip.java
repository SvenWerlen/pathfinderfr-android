package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.text.HtmlCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.pathfinderfr.R;

public class FragmentToolTip extends DialogFragment {

    private String title;
    private String text;

    public FragmentToolTip() {
        // Required empty public constructor
    }

    public static FragmentToolTip newInstance(String title, String text) {
        FragmentToolTip fragment = new FragmentToolTip();
        fragment.title = title;
        fragment.text = text;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_tooltip, container, false);
        TextView title = rootView.findViewById(R.id.sheet_tooltip_title);
        title.setText(HtmlCompat.fromHtml(this.title,HtmlCompat.FROM_HTML_MODE_COMPACT));
        WebView content = rootView.findViewById(R.id.sheet_tooltip_content);
        content.loadDataWithBaseURL(null, this.text, "text/html", "utf-8", null);
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

