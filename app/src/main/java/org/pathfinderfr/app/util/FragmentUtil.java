package org.pathfinderfr.app.util;

import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.widget.TextView;

public class FragmentUtil {

    public static TextView copyExampleTextFragment(TextView fragment) {
        TextView tv = new TextView(fragment.getContext());
        tv.setText(fragment.getText());
        tv.setLayoutParams(fragment.getLayoutParams());
        tv.setBackground(fragment.getBackground());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, fragment.getTextSize());
        tv.setPadding(fragment.getPaddingLeft(),fragment.getPaddingTop(), fragment.getPaddingRight(), fragment.getPaddingBottom());
        tv.setTag(fragment.getTag());
        tv.setTypeface(fragment.getTypeface());
        tv.setTextColor(fragment.getTextColors());
        tv.setTextAlignment(fragment.getTextAlignment());
        return tv;
    }
}
